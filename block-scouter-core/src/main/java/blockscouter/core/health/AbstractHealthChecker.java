/*
 * Copyright 2020 Block scouter Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package blockscouter.core.health;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheck.Result;
import com.codahale.metrics.health.HealthCheckRegistry;

import blockscouter.core.util.BlockScouterThreadFactory;

/**
 * Abstract health checker
 */
public class AbstractHealthChecker<H extends HealthIndicator> implements HealthChecker<H> {

    private static final Logger logger = LoggerFactory.getLogger(HealthChecker.class);

    private final AtomicBoolean isActive = new AtomicBoolean(false);

    private ScheduledExecutorService scheduledExecutor;
    private final HealthCheckRegistry healthCheckRegistry;
    private final ConcurrentHashMap<String, Result> resultMap;
    private Optional<HealthCheckListener<H>> listenerOptional;

    protected AbstractHealthChecker() {
        healthCheckRegistry = new HealthCheckRegistry();
        resultMap = new ConcurrentHashMap<>();
    }

    @Override
    public void setHealthCheckListener(HealthCheckListener<H> listener) {
        listenerOptional = Optional.ofNullable(listener);
    }

    @Override
    public void start(long initDelay, long period) {
        checkArgument(initDelay >= 0L, "initDelay must be greater than or equal to 0");
        checkArgument(period > 0L, "period must be greater than 0");

        if (!isActive.compareAndSet(false, true)) {
            logger.warn("Already health checker is running");
            return;
        }

        if (scheduledExecutor == null) {
            scheduledExecutor = Executors.newSingleThreadScheduledExecutor(
                    new BlockScouterThreadFactory("HealthChecker", true)
            );
        }

        logger.debug("Start to node's health checker. init delay {}[ms], period : {}[ms]",
                     initDelay, period);

        scheduledExecutor.scheduleAtFixedRate(
                this::doHealthCheck, initDelay, period, TimeUnit.MILLISECONDS
        );

        logger.debug("Success to start {} health checker", getClass().getSimpleName());
    }

    @Override
    public void stop() {
        logger.debug("Try to stop health checker.");

        if (!isActive.compareAndSet(true, false)) {
            logger.debug("Already stopped health checker");
            return;
        }

        // thread pool shutdown
        scheduledExecutor.shutdown();

        try {
            scheduledExecutor.awaitTermination(3000, TimeUnit.MILLISECONDS);
            logger.debug("Stopped health checker");
        } catch (InterruptedException e) {
        }
    }

    @Override
    public boolean isRunning() {
        return isActive.get();
    }

    @Override
    public boolean addIndicator(H healthIndicator) {
        ensureRunningState("Must start health checker before adding a indicator");

        checkNotNull(healthIndicator, "healthIndicator");

        try {
            healthCheckRegistry.register(healthIndicator.getName(), healthIndicator);
            resultMap.put(healthIndicator.getName(), healthIndicator.execute());
            return true;
        } catch (Exception e) {
            logger.warn("Exception occur while adding a health indicator", e);
            return false;
        }
    }

    @Override
    public void removeIndicator(String name) {
        ensureRunningState("Must start health checker before remove a indicator");
        healthCheckRegistry.unregister(checkNotNull(name, "name"));
        resultMap.remove(name);
    }

    @Override
    public List<H> getIndicators() {
        ensureRunningState("Must start health checker before access health result");

        return healthCheckRegistry.getNames()
                                  .stream()
                                  .filter(resultMap::containsKey)
                                  .map(name -> (H) healthCheckRegistry.getHealthCheck(name))
                                  .collect(Collectors.toList());
    }

    @Override
    public List<H> getHealthyIndicators() {
        ensureRunningState("Must start health checker before access health result");

        return healthCheckRegistry.getNames()
                                  .stream()
                                  .filter(name -> {
                                      Result result = resultMap.get(name);
                                      return result != null && result.isHealthy();
                                  })
                                  .map(name -> (H) healthCheckRegistry.getHealthCheck(name))
                                  .collect(Collectors.toList());
    }

    @Override
    public Optional<H> getIndicator(String name) {
        return Optional.ofNullable((H) healthCheckRegistry.getHealthCheck(checkNotNull(name, "name")));
    }

    @Override
    public Optional<Result> getHealthCheckResult(String name) {
        return Optional.ofNullable(resultMap.get(checkNotNull(name, "name")));
    }

    private void ensureRunningState(String errorMessage) {
        checkState(isRunning(), errorMessage);
    }

    private void doHealthCheck() {
        // run health checks
        final SortedMap<String, Result> results = healthCheckRegistry.runHealthChecks();

        for (Entry<String, Result> entry : results.entrySet()) {
            final String name = entry.getKey();
            final HealthCheck.Result result = entry.getValue();
            final HealthCheck.Result prevResult = resultMap.put(name, result);

            if (!listenerOptional.isPresent()) {
                continue;
            }

            if (prevResult == null) {
                continue;
            }

            if (prevResult != null && result.isHealthy() ^ prevResult.isHealthy()) {
                listenerOptional.get().onStateChanged((H) healthCheckRegistry.getHealthCheck(name),
                                                      prevResult, result);
            }
        }
    }
}
