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

package blockscouter.core.chain.eth;

import static com.google.common.base.Preconditions.checkNotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3jService;

import com.codahale.metrics.health.HealthCheck.Result;

import blockscouter.core.chain.ChainManager;
import blockscouter.core.chain.eth.event.EthNewPeerEvent;
import blockscouter.core.chain.eth.event.EthSyncEvent;
import blockscouter.core.chain.eth.loadbalancer.EthLoadBalancer;
import blockscouter.core.health.eth.EthHealthChecker;
import blockscouter.core.health.eth.EthHealthIndicator;
import blockscouter.core.node.eth.EthNode;
import blockscouter.core.node.eth.EthNodeConfig;
import blockscouter.core.node.eth.EthNodeManager;
import blockscouter.core.sdk.eth.EthRpcServiceFactory;

/**
 * Manage given ethereum nodes in the same chain
 */
public class EthChainManager implements ChainManager<EthNode, EthNodeConfig> {

    private static final Logger logger = LoggerFactory.getLogger(EthChainManager.class);

    // required
    private final EthChainConfig chainConfig;
    private final EthNodeManager nodeManager;
    private final Optional<EthChainListener> chainListenerOptional;
    private final EthRpcServiceFactory rpcServiceFactory;

    // post constructed
    private EthLoadBalancer loadBalancer;
    private EthHealthChecker healthChecker;
    private LinkedBlockingQueue<EthSyncEvent> syncEventQueue;
    private EthSyncTask syncTask;
    private EthPendingTransactionBatch pendingTransactionBatch;

    public EthChainManager(EthChainConfig chainConfig,
                           EthNodeManager nodeManager,
                           EthChainListener chainListener,
                           EthRpcServiceFactory rpcServiceFactory) {

        this.chainConfig = checkNotNull(chainConfig, "chainConfig");
        this.nodeManager = checkNotNull(nodeManager, "nodeManager");
        chainListenerOptional = Optional.ofNullable(chainListener);
        this.rpcServiceFactory = checkNotNull(rpcServiceFactory, "rpcServiceFactory");

        initialize();
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

    @Override
    public List<EthNode> getActiveNodes() {
        final List<EthHealthIndicator> healthyIndicators = healthChecker.getHealthyIndicators();

        if (healthyIndicators.isEmpty()) {
            return Collections.emptyList();
        }

        final List<EthNode> activeNodes = new ArrayList<>(healthyIndicators.size());

        for (EthHealthIndicator healthyIndicator : healthyIndicators) {
            Optional<EthNode> nodeOptional =
                    nodeManager.getNode(chainConfig.getChainId(), healthyIndicator.getName());
            if (!nodeOptional.isPresent()) {
                continue;
            }

            activeNodes.add(nodeOptional.get());
        }

        return activeNodes;
    }

    /**
     * Returns a load balanced {@link Web3jService} proxy
     */
    public Web3jService getLoadBalancedWeb3jService() {
        return loadBalancer.getLoadBalancedWeb3jService();
    }

    @Override
    public void shutdown() {
        try {
            if (nodeManager != null) {
                nodeManager.shutdown();
            }
        } catch (Exception ignored) {
            // ignored
        }
    }

    /**
     * Initialize a ethereum chain
     */
    private void initialize() {
        // start to health checker
        healthChecker = new EthHealthChecker();
        healthChecker.setHealthCheckListener(this::handleHealthStateChange);
        healthChecker.start(100L, chainConfig.getBlockTime());

        // loadbalancer
        loadBalancer = new EthLoadBalancer(healthChecker);

        // chain sync task
        syncEventQueue = new LinkedBlockingQueue<>();
        syncTask = new EthSyncTask(
                chainConfig, healthChecker, nodeManager,
                chainListenerOptional, syncEventQueue, (long) (chainConfig.getBlockTime() * 1.5D)
        );
        syncTask.start();

        // pending transaction batch
        pendingTransactionBatch = new EthPendingTransactionBatch(chainConfig, chainListenerOptional);
        pendingTransactionBatch.start(chainConfig.getPendingTransactionBatchMaxSize(),
                                      Duration.ofSeconds(chainConfig.getPendingTransactionBatchMaxSeconds()));

        final String logging = "\n// ====================================================\n"
                               + "Initialize ethereum chain.\n"
                               + "> chain config : " + chainConfig + '\n'
                               + "==================================================== //";
        logger.debug(logging);
    }

    private void handleHealthStateChange(EthHealthIndicator indicator, Result prev, Result current) {
        logger.debug(
                "Ethereum node health state is changed. name : {} / prev healthy : {} / current healthy: {}"
                , indicator.getName(), prev.isHealthy(), current.isHealthy());

        // 1) notify health state changed to a node
        final Optional<EthNode> node = nodeManager.getNode(chainConfig.getChainId(), indicator.getName());

        if (node.isPresent()) {
            node.get().onHealthStateChange(current);
        }

        // 2) publish new peer event
        syncEventQueue.offer(EthNewPeerEvent.INSTANCE);
    }
}
