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

import static com.google.common.base.Preconditions.checkNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;

import com.codahale.metrics.health.HealthCheck.Result;
import com.github.zacscoding.blockscouter.health.eth.EthHealthIndicator;
import com.github.zacscoding.blockscouter.node.Node;
import com.github.zacscoding.blockscouter.node.enums.BlockchainType;
import com.github.zacscoding.blockscouter.sdk.eth.EthRpcServiceFactory;

/**
 * Ethereum node
 */
public class EthNode implements Node<EthHealthIndicator> {

    private static final Logger logger = LoggerFactory.getLogger(EthNode.class);

    private boolean initialized;

    // required
    private final EthNodeConfig nodeConfig;
    private final EthRpcServiceFactory rpcServiceFactory;

    // build
    private Web3j web3j;
    private EthNodeObserver nodeObserver;
    private EthHealthIndicator healthIndicator;

    public static EthNode newInstance(EthNodeConfig nodeConfig, EthRpcServiceFactory rpcServiceFactory) {
        return new EthNode(nodeConfig, rpcServiceFactory);
    }

    private EthNode(EthNodeConfig nodeConfig, EthRpcServiceFactory rpcServiceFactory) {
        this.nodeConfig = checkNotNull(nodeConfig, "nodeConfig");
        this.rpcServiceFactory = checkNotNull(rpcServiceFactory, "rpcServiceFactory");
    }

    @Override
    public BlockchainType getBlockchainType() {
        return BlockchainType.ETHEREUM;
    }

    @Override
    public long getBlockTime() {
        return nodeConfig.getBlockTime();
    }

    @Override
    public String getNodeName() {
        return nodeConfig.getName();
    }

    @Override
    public String getRpcUrl() {
        return nodeConfig.getRpcUrl();
    }

    @Override
    public EthHealthIndicator getHealthIndicator() {
        return healthIndicator;
    }

    @Override
    public void onHealthStateChange(Result current) {
        // handle node observer
        if (nodeObserver != null) {
            // 1) unhealthy -> healthy : start to subscribe
            if (current.isHealthy()) {
                startSubscribe();
            } else { // 2) healthy -> unhealthy : stop to subscribe
                stopSubscribe();
            }
        }
    }

    @Override
    public void initialize() {
        if (initialized) {
            return;
        }

        // web3j
        try {
            web3j = rpcServiceFactory.createWeb3j(this);
        } catch (Exception e) {
            logger.warn("Exception occur while building web3j. {}", nodeConfig, e);
        }

        // subscribe
        try {
            startSubscribe();
        } catch (Exception e) {
            logger.warn("Exception occur while initialize subscription", e);
        }

        // health check
        healthIndicator = new EthHealthIndicator(nodeConfig.getChainId(), this,
                                                 nodeConfig.getHealthIndicatorType());

        initialized = true;
    }

    @Override
    public void destroy() {
        if (nodeObserver != null) {
            nodeObserver.stop();
        }

        initialized = false;
    }

    /**
     * Returns a {@link Web3j}
     */
    public Web3j getWeb3j() {
        return web3j;
    }

    /**
     * Returns a pending transaction polling interval in milliseconds
     */
    public long getPendingTxPollingInterval() {
        return nodeConfig.getPendingTransactionPollingInterval();
    }

    // start to subscribe new blocks, pending transactions
    private void startSubscribe() {
        if (nodeConfig.isSubscribeNewBlock()) {
            createObserverIfNull();
            nodeObserver.startBlockFilter();
        }

        if (nodeConfig.isSubscribePendingTransaction()) {
            createObserverIfNull();
            nodeObserver.startPendingTxFilter();
        }
    }

    // stop to subscribe new blocks, pending transactions
    private void stopSubscribe() {
        if (nodeConfig.isSubscribeNewBlock()) {
            nodeObserver.stopBlockFilter();
        }

        if (nodeConfig.isSubscribePendingTransaction()) {
            nodeObserver.stopPendingTxFilter();
        }
    }

    private void createObserverIfNull() {
        if (nodeObserver == null) {
            nodeObserver = new EthNodeObserver(this);
        }
    }
}
