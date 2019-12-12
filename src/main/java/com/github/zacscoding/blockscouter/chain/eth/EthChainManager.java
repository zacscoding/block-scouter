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

package com.github.zacscoding.blockscouter.chain.eth;

import static com.google.common.base.Preconditions.checkNotNull;

import java.time.Duration;
import java.util.Optional;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.health.HealthCheck.Result;
import com.github.zacscoding.blockscouter.chain.ChainManager;
import com.github.zacscoding.blockscouter.chain.eth.event.EthPendingTransactionBatch;
import com.github.zacscoding.blockscouter.health.eth.EthHealthChecker;
import com.github.zacscoding.blockscouter.health.eth.EthHealthIndicator;
import com.github.zacscoding.blockscouter.node.eth.EthNode;
import com.github.zacscoding.blockscouter.node.eth.EthNodeConfig;
import com.github.zacscoding.blockscouter.node.eth.EthNodeManager;
import com.github.zacscoding.blockscouter.sdk.eth.EthRpcServiceFactory;

/**
 * Manage given ethereum nodes in the same chain
 */
public class EthChainManager implements ChainManager<EthNodeConfig> {

    private static final Logger logger = LoggerFactory.getLogger(EthChainManager.class);

    // required
    private final EthChainConfig chainConfig;
    private final EthNodeManager nodeManager;
    private final EthChainReader chainReader;
    private final Optional<EthChainListener> chainListenerOptional;
    private final EthRpcServiceFactory rpcServiceFactory;

    // post constructed
    private String genesisHash;
    private EthHealthChecker healthChecker;
    private EthPendingTransactionBatch pendingTransactionBatch;

    public EthChainManager(EthChainConfig chainConfig,
                           EthNodeManager nodeManager,
                           EthChainReader chainReader,
                           EthChainListener chainListener,
                           EthRpcServiceFactory rpcServiceFactory) {

        this.chainConfig = checkNotNull(chainConfig, "chainConfig");
        this.nodeManager = checkNotNull(nodeManager, "nodeManager");
        this.chainReader = checkNotNull(chainReader, "chainReader");
        chainListenerOptional = Optional.ofNullable(chainListener);
        this.rpcServiceFactory = checkNotNull(rpcServiceFactory, "rpcServiceFactory");

        initialize();
    }

    @Override
    public String getGenesisHash() {
        if (genesisHash == null) {
            try {
                genesisHash = chainReader.getBlockHashByNumber(chainConfig.getChainId(), 0L);
            } catch (Exception e) {
                // ignore
            }
        }

        return genesisHash;
    }

    @Override
    public String getChainId() {
        return chainConfig.getChainId();
    }

    @Override
    public void addNode(EthNodeConfig nodeConfig) {
        addNode(nodeConfig, true);
    }

    /**
     * Adds a ethereum node.
     *
     * If publishNewNodeEvent is true, then try to synchronize chain after added
     */
    public void addNode(EthNodeConfig nodeConfig, boolean publishNewNodeEvent) {
        try {
            checkNotNull(nodeConfig, "nodeConfig");
            logger.debug("try to add a eth node. {}", nodeConfig);

            removeNode(nodeConfig.getName());

            final EthNode node = EthNode.newInstance(nodeConfig, rpcServiceFactory);
            nodeManager.addNode(chainConfig.getChainId(), node);

            // adds a health check
            healthChecker.addIndicator(node.getHealthIndicator());

            // pending transaction subscribe
            final Optional<Publisher<String>> pendingTxStream = node.getPendingTransactionStream();
            if (pendingTxStream.isPresent()) {
                // TODO : adds a batch
            }

            // publish new node event
            if (publishNewNodeEvent) {

            }
            logger.debug("success to add a eth node");
        } catch (Exception e) {
            logger.warn("Exception occur while adding a {}}", nodeConfig, e);
            removeNode(nodeConfig.getName());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeNode(String name) {
        final EthNode ethNode = nodeManager.removeNode(chainConfig.getChainId(), name);

        if (ethNode == null) {
            return;
        }

        try {
            healthChecker.removeIndicator(name);
            pendingTransactionBatch.unsubscribePendingTx(name);
        } catch (Exception e) {
            logger.warn("Exception occur while removing a ethereum node {}", name, e);
        }
    }

    /**
     * Initialize a ethereum chain
     */
    private void initialize() {
        // genesis hash
        getGenesisHash();

        // start to health checker
        healthChecker = new EthHealthChecker();
        healthChecker.setHealthCheckListener(this::handleHealthStateChange);
        healthChecker.start(100L, chainConfig.getBlockTime());

        // pending transaction batch
        pendingTransactionBatch = new EthPendingTransactionBatch(chainConfig, chainListenerOptional);
        pendingTransactionBatch.start(chainConfig.getPendingTransactionBatchMaxSize(),
                                      Duration.ofSeconds(chainConfig.getPendingTransactionBatchMaxSeconds()));

        final StringBuilder logging = new StringBuilder(
                "\n// ====================================================\n")
                .append("Initialize ethereum chain.\n")
                .append("> chain config : ").append(chainConfig).append('\n')
                .append("> genesis hash : ").append(genesisHash).append('\n')
                .append("==================================================== //");
        logger.debug(logging.toString());
    }

    private void handleHealthStateChange(EthHealthIndicator indicator, Result prev, Result current) {
        logger.debug(
                "Ethereum node health state is changed. name : {} / prev healthy : {} / current healthy: {}"
                , indicator.getName(), prev.isHealthy(), current.isHealthy());

        // 1) 노드에게 직접 알려줌
        Optional<EthNode> node = nodeManager.getNode(chainConfig.getChainId(), indicator.getName());

        if (node.isPresent()) {
            node.get().onHealthStateChange(current);
        }

        // 2) publish new peer event
        // eventQueue.offer();
    }
}
