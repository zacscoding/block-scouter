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
package com.github.zacscoding.blockscouter.node;

import com.codahale.metrics.health.HealthCheck.Result;
import com.github.zacscoding.blockscouter.health.HealthIndicator;
import com.github.zacscoding.blockscouter.node.enums.BlockchainType;

/**
 * Interface of nodes
 */
public interface Node<H extends HealthIndicator> {

    /**
     * Returns a {@link BlockchainType} of this node
     */
    BlockchainType getBlockchainType();

    /**
     * Returns average block time in milliseconds
     */
    long getBlockTime();

    /**
     * Returns name of a node
     */
    String getNodeName();

    /**
     * Returns rpc url of a node
     */
    String getRpcUrl();

    /**
     * Setup a node after added into chain manager
     */
    void initialize();

    /**
     * Returns a {@link HealthIndicator}
     */
    H getHealthIndicator();

    /**
     * Listen to change of health status
     */
    void onHealthStateChange(Result current);

    /**
     * Destory a node after removed from chain manager
     */
    void destroy();
}
