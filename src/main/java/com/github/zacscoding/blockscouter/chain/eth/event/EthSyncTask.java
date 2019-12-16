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

package com.github.zacscoding.blockscouter.chain.eth.event;

import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock.Block;

import com.github.zacscoding.blockscouter.chain.eth.EthChainConfig;
import com.github.zacscoding.blockscouter.chain.eth.EthChainListener;
import com.github.zacscoding.blockscouter.chain.eth.EthChainReader;
import com.github.zacscoding.blockscouter.health.eth.EthHealthChecker;
import com.github.zacscoding.blockscouter.health.eth.EthHealthIndicator;
import com.github.zacscoding.blockscouter.node.eth.EthNode;
import com.github.zacscoding.blockscouter.node.eth.EthNodeManager;

/**
 * Ethereum sync task
 */
public class EthSyncTask extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(EthSyncTask.class);

    private static final long MINIMUM_FORCE_SYNC = 1000L;

    // required
    private final EthChainConfig chainConfig;
    private final EthChainReader chainReader;
    private final EthHealthChecker healthChecker;
    private final EthNodeManager nodeManager;
    private final Optional<EthChainListener> chainListenerOptional;
    private final BlockingQueue<EthSyncEvent> eventQueue;
    private final long forceSyncTimeout;

    // fields
    private final Object syncLock = new Object();
    private boolean syncing = false;

    public EthSyncTask(EthChainConfig chainConfig,
                       EthChainReader chainReader,
                       EthHealthChecker healthChecker,
                       EthNodeManager nodeManager,
                       Optional<EthChainListener> chainListenerOptional,
                       BlockingQueue<EthSyncEvent> eventQueue,
                       long forceSyncTimeout) {

        this.chainConfig = checkNotNull(chainConfig, "chainConfig");
        this.chainReader = checkNotNull(chainReader, "chainReader");
        this.healthChecker = checkNotNull(healthChecker, "healthChecker");
        this.nodeManager = checkNotNull(nodeManager, "nodeManager");
        this.chainListenerOptional = checkNotNull(chainListenerOptional, "chainListenerOptional");
        this.eventQueue = checkNotNull(eventQueue, "eventQueue");
        this.forceSyncTimeout = Math.max(MINIMUM_FORCE_SYNC, forceSyncTimeout);

        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                EthSyncEvent syncEvent = eventQueue.poll(forceSyncTimeout, TimeUnit.MILLISECONDS);

                if (syncEvent == null) {
                    syncEvent = EthForceSyncEvent.INSTANCE;
                }

                synchronized (syncLock) {
                    if (syncing) {
                        logger.debug("Skip to sync event : {}. already working", syncEvent);
                        continue;
                    }
                    syncing = true;
                }
                handleSyncEvent(syncEvent);
            }
        } catch (InterruptedException e) {
            logger.warn("InterruptedException occur while getting sync event.", e);
            Thread.currentThread().interrupt();
        }
    }

    // handle sync event
    private void handleSyncEvent(final EthSyncEvent event) {
        final Thread syncTask = new Thread(() -> {
            try {
                logger.debug("handle sync event : {}", event);
                final EthSyncEventType eventType = event.getEventType();
                Map<String, EthBestBlockResult> blockResultMap = null;

                switch (eventType) {
                    case NEW_PEER:
                    case FORCE_SYNC:
                        blockResultMap = sortBlockHashInActiveNodes();
                        break;
                    default:
                        logger.warn("Received unknown sync event. {}", event);
                }
                synchronize(blockResultMap);
            } catch (Exception e) {
                logger.warn("Exception occur while handle sync event in sync task thread.", e);
            } finally {
                synchronized (syncLock) {
                    syncing = false;
                }
            }
        });
        syncTask.setDaemon(true);
        syncTask.start();
    }

    /**
     * classify block hash from active nodes
     *
     * @return key:blockHash, value:{@link EthBestBlockResult} or empty map
     */
    private Map<String, EthBestBlockResult> sortBlockHashInActiveNodes() {
        final List<EthHealthIndicator> indicators = healthChecker.getHealthyIndicators();

        if (indicators.isEmpty()) {
            logger.debug("skip to sort block hash because of empty active nodes.");
            return Collections.emptyMap();
        }

        final Map<String, EthBestBlockResult> blockResultMap = new HashMap<>();

        for (EthHealthIndicator indicator : indicators) {
            try {
                Optional<EthNode> nodeOptional =
                        nodeManager.getNode(chainConfig.getChainId(), indicator.getName());

                if (!nodeOptional.isPresent()) {
                    continue;
                }

                final EthNode node = nodeOptional.get();
                final BigInteger bestBlockNumber = node.getWeb3j()
                                                       .ethBlockNumber()
                                                       .send()
                                                       .getBlockNumber();

                final Block block = node.getWeb3j()
                                        .ethGetBlockByNumber(DefaultBlockParameter.valueOf(bestBlockNumber),
                                                             true)
                                        .send()
                                        .getBlock();

                EthBestBlockResult result = blockResultMap.get(block.getHash());

                if (result == null) {
                    result = new EthBestBlockResult(chainConfig.getChainId(), block);
                    blockResultMap.put(block.getHash(), result);
                }

                result.addEthNode(node);
            } catch (Exception e) {
                logger.warn("failed to get best block.", e);
            }
        }

        return blockResultMap;
    }

    private void synchronize(Map<String, EthBestBlockResult> bestBlocks) {
        if (bestBlocks == null || bestBlocks.isEmpty()) {
            logger.debug("Skip to sync blocks because empty best blocks.");
            return;
        }

        logger.debug("Start to synchronize best blocks[size : {}]", bestBlocks.size());

        // choice canonical chain from best block results
        final Iterator<EthBestBlockResult> iterator = bestBlocks.values().iterator();
        EthBestBlockResult bestBlockResult = iterator.next();

        while (iterator.hasNext()) {
            EthBestBlockResult currentResult = iterator.next();

            if (currentResult.getTotalDifficulty().compareTo(bestBlockResult.getTotalDifficulty()) > 0) {
                bestBlockResult = currentResult;
            }
        }

        // compare chain with provided chain reader
        if (chainReader.getTotalDifficulty(chainConfig.getChainId()).compareTo(
                bestBlockResult.getTotalDifficulty()) >= 0) {
            logger.debug("skip to synchronize because before total difficulty is greater");
            return;
        }

        if (chainListenerOptional.isPresent()) {
            chainListenerOptional.get().onNewBlocks(chainConfig, bestBlockResult);
        } else {
            logger.warn("No chain lister although sorted chain");
        }
    }
}
