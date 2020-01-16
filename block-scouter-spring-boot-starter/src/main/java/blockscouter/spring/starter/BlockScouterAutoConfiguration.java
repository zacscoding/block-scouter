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

import java.math.BigInteger;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.core.methods.response.Transaction;

import blockscouter.core.chain.ChainFactory;
import blockscouter.core.chain.eth.EthChainConfig;
import blockscouter.core.chain.eth.EthChainConfigBuilder;
import blockscouter.core.chain.eth.EthChainFactory;
import blockscouter.core.chain.eth.EthChainListener;
import blockscouter.core.chain.eth.EthChainManager;
import blockscouter.core.chain.eth.event.EthBestBlockResult;
import blockscouter.core.health.eth.EthHealthIndicatorType;
import blockscouter.core.health.eth.EthHealthIndicatorType.EthConnectedOnly;
import blockscouter.core.health.eth.EthHealthIndicatorType.EthSynchronized;
import blockscouter.core.health.eth.EthHealthIndicatorType.EthSyncingMaxDiff;
import blockscouter.core.node.eth.EthNodeConfig;
import blockscouter.core.node.eth.EthNodeConfigBuilder;
import blockscouter.core.node.eth.EthNodeManager;
import blockscouter.core.sdk.eth.DefaultEthRpcServiceFactory;
import blockscouter.core.sdk.eth.EthRpcServiceFactory;
import blockscouter.spring.starter.BlockScouterProperties.EthChain;
import blockscouter.spring.starter.BlockScouterProperties.EthNode;
import blockscouter.spring.starter.BlockScouterProperties.Health;

/**
 */
@Configuration
@ConditionalOnClass(ChainFactory.class)
@EnableConfigurationProperties({ BlockScouterProperties.class })
public class BlockScouterAutoConfiguration {

    private final BlockScouterProperties blockScouterProperties;
    private final EthChainListener ethChainListener;

    @Autowired
    public BlockScouterAutoConfiguration(BlockScouterProperties blockScouterProperties,
                                         EthChainListener ethChainListener) {

        this.blockScouterProperties = blockScouterProperties;
        this.ethChainListener = ethChainListener;
    }

    @Bean
    public EthChainListener ethChainListener() {
        return new EthChainListener() {
            @Override
            public void onNewBlocks(EthChainConfig chainConfig, EthBestBlockResult result) {

            }

            @Override
            public void onPendingTransactions(EthChainConfig chainConfig,
                                              List<Transaction> pendingTransactions) {

            }

            @Override
            public void prepareNewChain(EthChainConfig chainConfig) {

            }
        };
    }

    // ==== ethereum

    @Bean
    @ConditionalOnMissingBean
    public EthRpcServiceFactory ethRpcServiceFactory() {
        return new DefaultEthRpcServiceFactory();
    }

    @Bean
    @ConditionalOnMissingBean(EthNodeManager.class)
    public EthNodeManager ethNodeManager() {
        return new EthNodeManager();
    }

    @Bean
    @ConditionalOnMissingBean(EthChainFactory.class)
    public EthChainFactory ethChainFactory(EthNodeManager ethNodeManager,
                                           EthRpcServiceFactory ethRpcServiceFactory) {

        final EthChainFactory factory = new EthChainFactory();

        for (EthChain chain : blockScouterProperties.getEth().getChains()) {
            final EthChainConfig chainConfig
                    = EthChainConfigBuilder.builder()
                                           .chainId(chain.getChainId())
                                           .blockTime(chain.getBlockTime())
                                           .subscribeNewBlocks(chain.isSubscribeNewBlocks())
                                           .subscribePendingTransactions(chain.isSubscribePendingTransactions())
                                           .pendingTransactionBatchMaxSize(chain.getPendingTxBatchMaxSize())
                                           .pendingTransactionBatchMaxSeconds(
                                                   chain.getPendingTxBatchMaxSeconds())
                                           .build();

            final EthChainManager chainManager = new EthChainManager(chainConfig,
                                                                     ethNodeManager,
                                                                     ethChainListener,
                                                                     ethRpcServiceFactory);

            factory.addChain(chainConfig.getChainId(), chainManager);

            // onPrepare new chain
            ethChainListener.prepareNewChain(chainConfig);

            // add nodes
            for (EthNode node : chain.getNodes()) {
                final EthNodeConfig nodeConfig =
                        EthNodeConfigBuilder.builder(node.getName())
                                            .blockTime(chain.getBlockTime())
                                            .chainId(chainConfig.getChainId())
                                            .healthIndicatorType(getHealthCheckIndicator(chain))
                                            .pendingTransactionPollingInterval(
                                                    node.getPendingTxPollingInterval())
                                            .rpcUrl(node.getRpcUrl())
                                            .subscribeNewBlock(node.isSubscribeBlock())
                                            .subscribePendingTransaction(node.isSubscribePendingTx())
                                            .build();

                chainManager.addNode(nodeConfig, false);
            }
        }

        return factory;
    }

    private static EthHealthIndicatorType getHealthCheckIndicator(EthChain chain) {
        final Health health = chain.getHealth();
        final EthHealthIndicatorType healthIndicatorType;

        switch (health.getType()) {
            case "connected":
                healthIndicatorType = EthConnectedOnly.INSTANCE;
                break;
            case "syncing":
                healthIndicatorType = new EthSyncingMaxDiff(BigInteger.valueOf(health.getAllowance()));
                break;
            case "synchronized":
                healthIndicatorType = EthSynchronized.INSTANCE;
                break;
            default:
                throw new RuntimeException("Invalid chain's health check type " + health.getType());
        }

        return healthIndicatorType;
    }
}
