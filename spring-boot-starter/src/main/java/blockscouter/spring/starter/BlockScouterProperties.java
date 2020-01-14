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

package blockscouter.spring.starter;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 */
@ConfigurationProperties(prefix = "spring.blockscouter")
public class BlockScouterProperties {

    private Eth eth;

    public Eth getEth() {
        return eth;
    }

    public void setEth(Eth eth) {
        this.eth = eth;
    }

    public static class Eth {

        private List<EthChain> chains;

        public List<EthChain> getChains() {
            return chains;
        }

        public void setChains(List<EthChain> chains) {
            this.chains = chains;
        }
    }

    public static class EthChain {

        private String chainId;

        private Long blockTime;

        private boolean subscribeNewBlocks = true;

        private boolean subscribePendingTransactions = true;

        private int pendingTxBatchMaxSize;

        private int pendingTxBatchMaxSeconds;

        private Health health;

        private List<EthNode> nodes;

        public String getChainId() {
            return chainId;
        }

        public void setChainId(String chainId) {
            this.chainId = chainId;
        }

        public Long getBlockTime() {
            return blockTime;
        }

        public void setBlockTime(Long blockTime) {
            this.blockTime = blockTime;
        }

        public boolean isSubscribeNewBlocks() {
            return subscribeNewBlocks;
        }

        public void setSubscribeNewBlocks(boolean subscribeNewBlocks) {
            this.subscribeNewBlocks = subscribeNewBlocks;
        }

        public boolean isSubscribePendingTransactions() {
            return subscribePendingTransactions;
        }

        public void setSubscribePendingTransactions(boolean subscribePendingTransactions) {
            this.subscribePendingTransactions = subscribePendingTransactions;
        }

        public int getPendingTxBatchMaxSize() {
            return pendingTxBatchMaxSize;
        }

        public void setPendingTxBatchMaxSize(int pendingTxBatchMaxSize) {
            this.pendingTxBatchMaxSize = pendingTxBatchMaxSize;
        }

        public int getPendingTxBatchMaxSeconds() {
            return pendingTxBatchMaxSeconds;
        }

        public void setPendingTxBatchMaxSeconds(int pendingTxBatchMaxSeconds) {
            this.pendingTxBatchMaxSeconds = pendingTxBatchMaxSeconds;
        }

        public Health getHealth() {
            return health;
        }

        public void setHealth(Health health) {
            this.health = health;
        }

        public List<EthNode> getNodes() {
            return nodes;
        }

        public void setNodes(List<EthNode> nodes) {
            this.nodes = nodes;
        }
    }

    public static class Health {

        private String type;

        private int allowance;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getAllowance() {
            return allowance;
        }

        public void setAllowance(int allowance) {
            this.allowance = allowance;
        }
    }

    public static class EthNode {
        private String name;

        private String rpcUrl;

        private boolean subscribeBlock;

        private boolean subscribePendingTx;

        private long pendingTxPollingInterval;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRpcUrl() {
            return rpcUrl;
        }

        public void setRpcUrl(String rpcUrl) {
            this.rpcUrl = rpcUrl;
        }

        public boolean isSubscribeBlock() {
            return subscribeBlock;
        }

        public void setSubscribeBlock(boolean subscribeBlock) {
            this.subscribeBlock = subscribeBlock;
        }

        public boolean isSubscribePendingTx() {
            return subscribePendingTx;
        }

        public void setSubscribePendingTx(boolean subscribePendingTx) {
            this.subscribePendingTx = subscribePendingTx;
        }

        public long getPendingTxPollingInterval() {
            return pendingTxPollingInterval;
        }

        public void setPendingTxPollingInterval(long pendingTxPollingInterval) {
            this.pendingTxPollingInterval = pendingTxPollingInterval;
        }
    }
}
