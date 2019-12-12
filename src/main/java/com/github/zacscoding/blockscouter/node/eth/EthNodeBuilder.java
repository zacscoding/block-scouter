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

import com.github.zacscoding.blockscouter.health.eth.EthHealthIndicatorType;
import com.github.zacscoding.blockscouter.health.eth.EthHealthIndicatorType.EthConnectedOnly;

/**
 * Ethereum node config builder
 */
public final class EthNodeBuilder {

    private String chainId = Defaults.CHAIN_ID;

    private String name;

    private String rpcUrl = Defaults.RPC_URL;

    private long blockTime = Defaults.BLOCK_TIME;

    private boolean subscribeNewBlock = Defaults.SUBSCRIBE_NEW_BLOCK;

    private boolean subscribePendingTransaction = Defaults.SUBSCRIBE_PENDING_TRANSACTION;

    private long pendingTransactionPollingInterval = Defaults.PENDING_TRANSACTION_POLLING_INTERVAL;

    private EthHealthIndicatorType healthIndicatorType = Defaults.HEALTH_INDICATOR_TYPE;

    public static EthNodeBuilder builder(String name) {
        return new EthNodeBuilder(name);
    }

    private EthNodeBuilder(String name) {
        this.name = checkNotNull(name, name);
    }

    public EthNodeBuilder chainId(String chainId) {
        this.chainId = checkNotNull(chainId, "chainId");
        return this;
    }

    public EthNodeBuilder rpcUrl(String rpcUrl) {
        this.rpcUrl = checkNotNull(rpcUrl, "rpcUrl");
        return this;
    }

    public EthNodeBuilder blockTime(long blockTime) {
        if (blockTime <= 1000L) {
            throw new IllegalStateException("Block time must be greater than or equals to 1000L");
        }

        this.blockTime = checkNotNull(blockTime, "blockTime");
        return this;
    }

    public EthNodeBuilder subscribeNewBlock(boolean subscribeNewBlock) {
        this.subscribeNewBlock = checkNotNull(subscribeNewBlock, "subscribeNewBlock");
        return this;
    }

    public EthNodeBuilder subscribePendingTransaction(boolean subscribePendingTransaction) {
        this.subscribePendingTransaction = checkNotNull(subscribePendingTransaction,
                                                        "subscribePendingTransaction");
        return this;
    }

    public EthNodeBuilder pendingTransactionPollingInterval(long pendingTransactionPollingInterval) {
        if (blockTime <= 0) {
            throw new IllegalStateException("Block time must be greater than or equals to 0L");
        }

        this.pendingTransactionPollingInterval = checkNotNull(pendingTransactionPollingInterval,
                                                              "pendingTransactionPollingInterval");
        return this;
    }

    public EthNodeBuilder healthIndicatorType(EthHealthIndicatorType healthIndicatorType) {
        this.healthIndicatorType = checkNotNull(healthIndicatorType, "healthIndicatorType");

        return this;
    }

    /**
     * Build a {@link EthNodeConfig}
     */
    public EthNodeConfig build() {
        return new EthNodeConfig(chainId, name, rpcUrl, blockTime, subscribeNewBlock,
                                 subscribePendingTransaction, pendingTransactionPollingInterval,
                                 healthIndicatorType);
    }

    private static final class Defaults {

        private static final String CHAIN_ID = "0";
        private static final String RPC_URL = "http://localhost:8545";
        private static final long BLOCK_TIME = 15000L;
        private static final boolean SUBSCRIBE_NEW_BLOCK = false;
        private static final boolean SUBSCRIBE_PENDING_TRANSACTION = false;
        private static final long PENDING_TRANSACTION_POLLING_INTERVAL = 0L;
        private static final EthHealthIndicatorType HEALTH_INDICATOR_TYPE = EthConnectedOnly.INSTANCE;
    }
}
