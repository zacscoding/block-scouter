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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.codahale.metrics.health.HealthCheck.Result;

public class AbstractNodesHealthCheckerTest {

    StubNodeHealthChecker nodeHealthChecker;

    @BeforeEach
    public void setUp() {
        nodeHealthChecker = new StubNodeHealthChecker();
    }

    @AfterEach
    public void tearDown() {
        if (nodeHealthChecker != null) {
            nodeHealthChecker.stop();
        }
    }

    @Test
    @DisplayName("start/stop/isRunning tests")
    public void testStartStopIsRunning() {
        // when then
        assertThat(nodeHealthChecker.isRunning()).isFalse();

        nodeHealthChecker.start(1000L, 1000L);
        assertThat(nodeHealthChecker.isRunning()).isTrue();

        nodeHealthChecker.stop();
        assertThat(nodeHealthChecker.isRunning()).isFalse();
    }

    @Test
    @DisplayName("addIndicator test")
    public void testAddIndicator() {
        // given
        final String nodeName = "node1";
        final StubHealthIndicator healthIndicator = mock(StubHealthIndicator.class);

        // when then
        assertThatThrownBy(() -> nodeHealthChecker.addIndicator(healthIndicator))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Must start health checker before adding a indicator");

        // given
        nodeHealthChecker.start(5000L, 5000L);
        when(healthIndicator.getName()).thenReturn(nodeName);
        when(healthIndicator.execute()).thenReturn(Result.healthy());

        // execute() is called after added a indicator
        // when
        assertThat(nodeHealthChecker.addIndicator(healthIndicator)).isTrue();

        // then
        assertThat(nodeHealthChecker.getIndicators().size()).isEqualTo(1);
        assertThat(nodeHealthChecker.getIndicators().get(0).getName()).isEqualTo(nodeName);

        assertThat(nodeHealthChecker.getHealthyIndicators().size()).isEqualTo(1);
        assertThat(nodeHealthChecker.getHealthyIndicators().get(0).getName()).isEqualTo(nodeName);

        assertThat(nodeHealthChecker.getIndicator(nodeName)).isNotNull();

        // add a indicator with same name
        // when then
        assertThat(nodeHealthChecker.addIndicator(healthIndicator)).isFalse();
    }

    @Test
    @DisplayName("removeIndicator test")
    public void testRemoveIndicator() {
        // given
        final String nodeName = "node1";
        final StubHealthIndicator healthIndicator = mock(StubHealthIndicator.class);
        when(healthIndicator.getName()).thenReturn(nodeName);
        when(healthIndicator.execute()).thenReturn(Result.healthy());

        // when then
        assertThatThrownBy(() -> nodeHealthChecker.removeIndicator(nodeName))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Must start health checker before remove a indicator");

        // given
        nodeHealthChecker.start(5L, 1000L);
        assertThat(nodeHealthChecker.addIndicator(healthIndicator)).isTrue();
        assertThat(nodeHealthChecker.getHealthyIndicators().size()).isEqualTo(1);

        // when
        nodeHealthChecker.removeIndicator(nodeName);

        // then
        assertThat(nodeHealthChecker.getIndicators()).isEmpty();
        assertThat(nodeHealthChecker.getHealthyIndicators()).isEmpty();

        assertThat(nodeHealthChecker.getIndicator(nodeName).isPresent()).isFalse();
    }

    @Test
    @DisplayName("listen to change of health status test")
    public void testListenStateChanged() throws Exception {
        // given
        final AtomicInteger supplier = new AtomicInteger(1);

        final StubHealthIndicator healthIndicator = new StubHealthIndicator("node1@", i -> {
            if (supplier.getAndIncrement() == 1) {
                return Result.healthy();
            }
            return Result.unhealthy("mock unhealthy");
        });
        final CountDownLatch latch = new CountDownLatch(1);
        nodeHealthChecker.setHealthCheckListener((checker, prev, current) -> {
            // then
            assertThat(checker.getName()).isEqualTo(healthIndicator.getName());
            assertThat(prev.isHealthy()).isTrue();
            assertThat(current.isHealthy()).isFalse();
            latch.countDown();
        });

        nodeHealthChecker.start(0L, 50);
        nodeHealthChecker.addIndicator(healthIndicator);

        assertThat(latch.await(1000L, TimeUnit.MILLISECONDS)).isTrue();
    }

    private static class StubNodeHealthChecker extends AbstractHealthChecker<StubHealthIndicator> {
    }

    public static class StubHealthIndicator extends HealthIndicator {

        private final String name;
        private final Function<Integer, Result> resultFunction;
        private final AtomicInteger checkCount = new AtomicInteger(0);

        public StubHealthIndicator(String name, Function<Integer, Result> resultFunction) {
            super("1", name);
            this.name = name;
            this.resultFunction = resultFunction;
        }

        @Override
        protected Result check() throws Exception {
            return resultFunction.apply(
                    checkCount.incrementAndGet()
            );
        }

        public int countOfCheck() {
            return checkCount.get();
        }
    }
}
