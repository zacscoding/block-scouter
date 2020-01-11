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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ethereum chain config builder
 */
public final class EthChainConfigBuilder {
    /**
     * chain id
     */
    private String chainId = Defaults.CHAIN_ID;

    /**
     * average time of block creation
     */
    private long blockTime = Defaults.BLOCK_TIME;

    /**
     * pending transaction batch max size
     */
    private int pendingTransactionBatchMaxSize = Defaults.PENDING_TRANSACTION_BATCH_MAX_SIZE;

    /**
     * pending transaction batch max timeout seconds
     */
    private int pendingTransactionBatchMaxSeconds = Defaults.PENDING_TRANSACTION_BATCH_MAX_SECONDS;

    public static EthChainConfigBuilder builder() {
        return new EthChainConfigBuilder();
    }

    private EthChainConfigBuilder() {
    }

    public EthChainConfigBuilder chainId(String chainId) {
        this.chainId = checkNotNull(chainId, "chainId");
        return this;
    }

    public EthChainConfigBuilder blockTime(long blockTime) {
        checkArgument(blockTime > 0L, "blockTime must greater than 0");
        this.blockTime = blockTime;
        return this;
    }

    public EthChainConfigBuilder pendingTransactionBatchMaxSize(int pendingTransactionBatchMaxSize) {
        checkArgument(pendingTransactionBatchMaxSize > 0,
                      "pendingTransactionBatchMaxSize must greater than 0");
        this.pendingTransactionBatchMaxSize = pendingTransactionBatchMaxSize;

        return this;
    }

    public EthChainConfigBuilder pendingTransactionBatchMaxSeconds(int pendingTransactionBatchMaxSeconds) {
        checkArgument(pendingTransactionBatchMaxSeconds > 0,
                      "pendingTransactionBatchMaxSeconds must greater than 0");
        this.pendingTransactionBatchMaxSeconds = pendingTransactionBatchMaxSeconds;

        return this;
    }

    public EthChainConfig build() {
        return new EthChainConfig(chainId, blockTime, pendingTransactionBatchMaxSize,
                                  pendingTransactionBatchMaxSeconds);
    }

    private static final class Defaults {
        private static final String CHAIN_ID = "0";
        private static final long BLOCK_TIME = 1500L;
        private static final int PENDING_TRANSACTION_BATCH_MAX_SIZE = 100;
        private static final int PENDING_TRANSACTION_BATCH_MAX_SECONDS = 5;
    }

}
