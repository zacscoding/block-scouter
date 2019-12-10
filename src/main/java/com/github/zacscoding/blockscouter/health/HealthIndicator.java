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

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import com.codahale.metrics.health.HealthCheck;

/**
 * Indicator of health
 */
public abstract class HealthIndicator extends HealthCheck {

    private String chainId;
    private String name;

    public HealthIndicator(String chainId, String name) {
        this.chainId = requireNonNull(chainId, "chainId");
        this.name = requireNonNull(name, "name");
    }

    public String getChainId() {
        return chainId;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof HealthIndicator)) { return false; }
        HealthIndicator that = (HealthIndicator) o;
        return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
