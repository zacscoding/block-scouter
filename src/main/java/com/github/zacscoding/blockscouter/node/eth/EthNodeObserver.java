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

import java.io.IOException;
import java.util.concurrent.Executors;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.filters.BlockFilter;
import org.web3j.protocol.core.filters.PendingTransactionFilter;
import org.web3j.protocol.core.methods.response.EthBlock;

import com.github.zacscoding.blockscouter.node.NodeObserver;

import reactor.core.publisher.FluxSink;
import reactor.core.publisher.ReplayProcessor;

/**
 * Ethereum node observer
 *
 * - subscribe (new blocks | pending transactions)
 * - provide stream above subscription
 */
public class EthNodeObserver implements NodeObserver<EthNode> {

    private static final Logger logger = LoggerFactory.getLogger(EthNodeObserver.class);

    // required
    private final EthNode node;

    // build
    private ReplayProcessor<EthBlock> blockProcessor;
    private FluxSink<EthBlock> blockSink;
    private ReplayProcessor<String> txProcessor;
    private FluxSink<String> txSink;

    // filters
    private BlockFilter blockFilter;
    private PendingTransactionFilter pendingTxFilter;

    EthNodeObserver(EthNode node) {
        this.node = node;

        // FIXME : best way to create, manage flux (after started or stopped)
        blockProcessor = ReplayProcessor.cacheLast();
        blockSink = blockProcessor.sink();
        txProcessor = ReplayProcessor.create();
        txSink = txProcessor.sink();
    }

    /**
     * Returns a new block {@link Publisher}
     */
    public Publisher<EthBlock> getBlockStream() {
        return blockProcessor;
    }

    /**
     * Returns a pending transaction {@link Publisher}
     */
    public Publisher<String> getPendingTransactionStream() {
        return txProcessor;
    }

    @Override
    public void start() {
        startBlockFilter();
        startPendingTxFilter();
    }

    @Override
    public void stop() {
        stopBlockFilter();
        stopPendingTxFilter();
    }

    @Override
    public EthNode getNode() {
        return node;
    }

    /**
     * Start to subscribe new blocks
     *
     * @return true if success to start, otherwise false
     */
    public boolean startBlockFilter() {
        if (blockFilter != null) {
            logger.warn("Already started to subscribe new blocks");
            return false;
        }

        final Web3j web3j = node.getWeb3j();
        blockFilter = new BlockFilter(web3j, blockHash -> {
            try {
                final EthBlock block = web3j.ethGetBlockByHash(blockHash, true)
                                            .send();
                blockSink.next(block);
            } catch (IOException e) {
                blockSink.error(e);
            }
        });
        blockFilter.run(Executors.newSingleThreadScheduledExecutor(), node.getBlockTime());
        return true;
    }

    /**
     * Stop to subscribe new blocks
     *
     * @return true if success to stop, otherwise false
     */
    public boolean stopBlockFilter() {
        if (blockFilter == null) {
            logger.warn("Already block filter is stopped");
            return false;
        }

        try {
            blockFilter.cancel();
            return true;
        } catch (Exception e) {
            logger.warn("Exception occur while stopping block filter", e);
            return false;
        } finally {
            blockFilter = null;
        }
    }

    /**
     * Start to subscribe new pending transactions
     *
     * @return true if success to start, otherwise false
     */
    public boolean startPendingTxFilter() {
        if (pendingTxFilter != null) {
            logger.warn("Already started to pending tx filter");
            return false;
        }

        final Web3j web3j = node.getWeb3j();
        pendingTxFilter = new PendingTransactionFilter(web3j, txHash -> txSink.next(txHash));
        pendingTxFilter.run(Executors.newSingleThreadScheduledExecutor(),
                            node.getPendingTxPollingInterval());

        return true;
    }

    /**
     * Stop to subscribe new pending transactions
     *
     * @return true if success to stop, otherwise false
     */
    public boolean stopPendingTxFilter() {
        if (pendingTxFilter == null) {
            logger.warn("Already pending tx filter is stopped");
            return false;
        }

        pendingTxFilter.cancel();
        pendingTxFilter = null;

        return true;
    }
}
