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
package com.github.zacscoding.blockscouter.node.eth;

import com.codahale.metrics.health.HealthCheck.Result;
import com.github.zacscoding.blockscouter.health.HealthIndicator;
import com.github.zacscoding.blockscouter.node.Node;
import com.github.zacscoding.blockscouter.node.enums.BlockchainType;

/**
 *
 */
public class EthNode implements Node {

    public BlockchainType getBlockchainType() {
        return null;
    }

    public long getBlockTime() {
        return 0;
    }

    public String getNodeName() {
        return null;
    }

    public String getRpcUrl() {
        return null;
    }

    public void initialize() {

    }

    @Override
    public HealthIndicator getHealthIndicator() {
        return null;
    }

    @Override
    public void onHealthStateChange(Result current) {

    }

    public void destroy() {

    }
}
