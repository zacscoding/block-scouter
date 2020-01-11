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
package blockscouter.core.node.eth;

import blockscouter.core.health.eth.EthHealthIndicatorType;
import blockscouter.core.node.NodeConfig;
import blockscouter.core.node.enums.BlockchainType;

import com.google.common.base.MoreObjects;

/**
 * Configuration of a ethereum node
 */
public class EthNodeConfig implements NodeConfig {

    /***
     * Ethereum chain id (network id)
     */
    private final String chainId;

    /**
     * Name of a node
     */
    private final String name;

    /**
     * Rpc url of a node (IPC, HTTP, WEBSOCKET)
     */
    private final String rpcUrl;

    /**
     * Average time of a block creation in milliseconds
     */
    private final long blockTime;

    /**
     * subscribe a new block or not
     */
    private final boolean subscribeNewBlock;

    /**
     * subscribe a new pending transaction or not
     */
    private final boolean subscribePendingTransaction;

    /**
     * polling interval
     */
    private final long pendingTransactionPollingInterval;

    /**
     * indicator for health check
     */
    private EthHealthIndicatorType healthIndicatorType;

    EthNodeConfig(String chainId, String name, String rpcUrl, long blockTime, boolean subscribeNewBlock,
                  boolean subscribePendingTransaction, long pendingTransactionPollingInterval,
                  EthHealthIndicatorType healthIndicatorType) {

        this.chainId = chainId;
        this.name = name;
        this.rpcUrl = rpcUrl;
        this.blockTime = blockTime;
        this.subscribeNewBlock = subscribeNewBlock;
        this.subscribePendingTransaction = subscribePendingTransaction;
        this.pendingTransactionPollingInterval = pendingTransactionPollingInterval;
        this.healthIndicatorType = healthIndicatorType;
    }

    public String getChainId() {
        return chainId;
    }

    public String getName() {
        return name;
    }

    public String getRpcUrl() {
        return rpcUrl;
    }

    public long getBlockTime() {
        return blockTime;
    }

    public boolean isSubscribeNewBlock() {
        return subscribeNewBlock;
    }

    public boolean isSubscribePendingTransaction() {
        return subscribePendingTransaction;
    }

    public long getPendingTransactionPollingInterval() {
        return pendingTransactionPollingInterval;
    }

    public EthHealthIndicatorType getHealthIndicatorType() {
        return healthIndicatorType;
    }

    @Override
    public String toString() {
        return MoreObjects
                .toStringHelper(this)
                .add("chainId", chainId)
                .add("rpcUrl", rpcUrl)
                .add("blockTime", blockTime)
                .add("subscribeNewBlock", subscribeNewBlock)
                .add("subscribePendingTransaction", subscribePendingTransaction)
                .add("pendingTransactionPollingInterval", pendingTransactionPollingInterval)
                .add("healthIndicatorType", healthIndicatorType)
                .toString();
    }

    @Override
    public BlockchainType getBlockchainType() {
        return BlockchainType.ETHEREUM;
    }
}
