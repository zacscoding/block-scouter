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

import com.google.common.base.MoreObjects;

/**
 * Configuration of ethereum chain
 */
public class EthChainConfig implements Cloneable {

    /**
     * chain id
     */
    private final String chainId;

    /**
     * average time of block creation
     */
    private final long blockTime;

    /**
     * subscribe new blocks or not
     */
    private final boolean subscribeNewBlocks;

    /**
     * subscribe pending transactions or not
     */
    private final boolean subscribePendingTransactions;

    /**
     * pending transaction batch max size
     */
    private final int pendingTransactionBatchMaxSize;

    /**
     * pending transaction batch max timeout seconds
     */
    private final int pendingTransactionBatchMaxSeconds;

    public EthChainConfig(String chainId, long blockTime, boolean subscribeNewBlocks,
                          boolean subscribePendingTransactions, int pendingTransactionBatchMaxSize,
                          int pendingTransactionBatchMaxSeconds) {

        this.chainId = chainId;
        this.blockTime = blockTime;
        this.subscribeNewBlocks = subscribeNewBlocks;
        this.subscribePendingTransactions = subscribePendingTransactions;
        this.pendingTransactionBatchMaxSize = pendingTransactionBatchMaxSize;
        this.pendingTransactionBatchMaxSeconds = pendingTransactionBatchMaxSeconds;
    }

    public String getChainId() {
        return chainId;
    }

    public long getBlockTime() {
        return blockTime;
    }

    public boolean isSubscribeNewBlocks() {
        return subscribeNewBlocks;
    }

    public boolean isSubscribePendingTransactions() {
        return subscribePendingTransactions;
    }

    public int getPendingTransactionBatchMaxSize() {
        return pendingTransactionBatchMaxSize;
    }

    public int getPendingTransactionBatchMaxSeconds() {
        return pendingTransactionBatchMaxSeconds;
    }

    @Override
    public String toString() {
        return MoreObjects
                .toStringHelper(this)
                .add("chainId", chainId)
                .add("blockTime", blockTime)
                .add("subscribeNewBlocks", subscribeNewBlocks)
                .add("subscribePendingTransactions", subscribePendingTransactions)
                .add("pendingTransactionBatchMaxSize", pendingTransactionBatchMaxSize)
                .add("pendingTransactionBatchMaxSeconds", pendingTransactionBatchMaxSeconds)
                .toString();
    }

    @Override
    public EthChainConfig clone() {
        return new EthChainConfig(chainId, blockTime, subscribeNewBlocks, subscribePendingTransactions,
                                  pendingTransactionBatchMaxSize, pendingTransactionBatchMaxSeconds);
    }
}
