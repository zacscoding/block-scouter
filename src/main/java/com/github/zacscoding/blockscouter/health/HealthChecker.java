/*
 * Copyright 2019 Block scouter Project.
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

package com.github.zacscoding.blockscouter.health;

import java.util.List;
import java.util.Optional;

import com.codahale.metrics.health.HealthCheck;

public interface HealthChecker<H extends HealthIndicator> {

    /**
     * Settings a health check listener
     */
    void setHealthCheckListener(HealthCheckListener<H> listener);

    /**
     * Start to check node's health status given initial delay and interval
     *
     * @param initDelay : initial delay in milliseconds before starting
     * @param period  : interval of execution in milliseconds
     */
    void start(long initDelay, long period);

    /**
     * Stop to check
     */
    void stop();

    /**
     * Returns health checker is running or not
     *
     * @return true if running, otherwise false
     */
    boolean isRunning();

    /**
     * Adds a given health indicator given
     *
     * @param healthIndicator : indicator to add
     *
     * @return true if success to add, otherwise false
     */
    boolean addIndicator(H healthIndicator);

    /**
     * Remove a health indicator with given name
     */
    void removeIndicator(String name);

    /**
     * Return list of {@link HealthIndicator} indicators
     *
     * @return registered health indicators or empty list if not exist
     */
    List<H> getIndicators();

    /**
     * Return list of {@link HealthIndicator} indicators with healthy status
     *
     * @return healthy node's indicators or empty list
     */
    List<H> getHealthyIndicators();

    /**
     * Returns a {@link HealthIndicator} given name
     */
    Optional<H> getIndicator(String name);

    /**
     * Returns a {@link HealthCheck.Result} of last executed from health checker
     */
    Optional<HealthCheck.Result> getHealthCheckResult(String name);
}
