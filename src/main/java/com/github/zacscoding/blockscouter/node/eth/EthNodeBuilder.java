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

import static java.util.Objects.requireNonNull;

/**
 * Ethereum node config builder
 */
public class EthNodeBuilder {

    private String chainId = Defaults.CHAIN_ID;

    private String name = Defaults.NAME;

    private String rpcUrl = Defaults.RPC_URL;

    private long blockTime = Defaults.BLOCK_TIME;

    private boolean subscribeNewBlock = Defaults.SUBSCRIBE_NEW_BLOCK;

    private boolean subscribePendingTransaction = Defaults.SUBSCRIBE_PENDING_TRANSACTION;

    private long pendingTransactionPollingInterval = Defaults.PENDING_TRANSACTION_POLLING_INTERVAL;

    public static EthNodeBuilder builder(String name) {
        return new EthNodeBuilder(name);
    }

    public EthNodeBuilder(String name) {
        this.name = requireNonNull(name, name);
    }

    public EthNodeBuilder chainId(String chainId) {
        this.chainId = requireNonNull(chainId, "chainId");
        return this;
    }

    public EthNodeBuilder rpcUrl(String rpcUrl) {
        this.rpcUrl = requireNonNull(rpcUrl, "rpcUrl");
        return this;
    }

    public EthNodeBuilder blockTime(long blockTime) {
        if (blockTime <= 1000L) {
            throw new IllegalStateException("Block time must be greater than or equals to 1000L");
        }

        this.blockTime = requireNonNull(blockTime, "blockTime");
        return this;
    }

    public EthNodeBuilder subscribeNewBlock(boolean subscribeNewBlock) {
        this.subscribeNewBlock = requireNonNull(subscribeNewBlock, "subscribeNewBlock");
        return this;
    }

    public EthNodeBuilder subscribePendingTransaction(boolean subscribePendingTransaction) {
        this.subscribePendingTransaction = requireNonNull(subscribePendingTransaction,
                                                          "subscribePendingTransaction");
        return this;
    }

    public EthNodeBuilder pendingTransactionPollingInterval(long pendingTransactionPollingInterval) {
        if (blockTime <= 0) {
            throw new IllegalStateException("Block time must be greater than or equals to 0L");
        }

        this.pendingTransactionPollingInterval = requireNonNull(pendingTransactionPollingInterval,
                                                                "pendingTransactionPollingInterval");
        return this;
    }

    /**
     * Build a {@link EthNodeConfig}
     */
    public EthNodeConfig build() {
        return new EthNodeConfig(chainId, name, rpcUrl, blockTime, subscribeNewBlock,
                                 subscribePendingTransaction, pendingTransactionPollingInterval);
    }

    private static final class Defaults {

        private static final String CHAIN_ID = "0";
        private static final String NAME = "NODE0";
        private static final String RPC_URL = "http://localhost:8545";
        private static final long BLOCK_TIME = 15000L;
        private static final boolean SUBSCRIBE_NEW_BLOCK = false;
        private static final boolean SUBSCRIBE_PENDING_TRANSACTION = false;
        private static final long PENDING_TRANSACTION_POLLING_INTERVAL = 0L;
    }
}
