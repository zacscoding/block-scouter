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

package blockscouter.core.dev;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import org.web3j.protocol.core.methods.response.Transaction;

import blockscouter.core.chain.eth.EthChainConfig;
import blockscouter.core.chain.eth.EthChainConfigBuilder;
import blockscouter.core.chain.eth.EthChainListener;
import blockscouter.core.chain.eth.EthChainManager;
import blockscouter.core.chain.eth.event.EthBestBlockResult;
import blockscouter.core.health.eth.EthHealthIndicatorType.EthConnectedOnly;
import blockscouter.core.health.eth.EthHealthIndicatorType.EthSynchronized;
import blockscouter.core.node.eth.EthNode;
import blockscouter.core.node.eth.EthNodeConfig;
import blockscouter.core.node.eth.EthNodeConfigBuilder;
import blockscouter.core.node.eth.EthNodeManager;
import blockscouter.core.sdk.eth.DefaultEthRpcServiceFactory;
import blockscouter.core.sdk.eth.EthRpcServiceFactory;

/**
 * Chain manager IT
 */
@Disabled
public class ChainManagerConsoleTest {

    EthChainConfig chainConfig;
    EthNodeManager nodeManager;
    EthChainListener chainListener;
    EthRpcServiceFactory rpcServiceFactory;

    EthNodeConfig node1;
    EthNodeConfig node2;

    @BeforeEach
    public void setUp() {
        chainConfig = EthChainConfigBuilder.builder()
                                           .chainId("36435")
                                           .blockTime(5000L)
                                           .pendingTransactionBatchMaxSize(3)
                                           .pendingTransactionBatchMaxSeconds(5)
                                           .build();

        nodeManager = new EthNodeManager();
        chainListener = new EthChainListener() {
            @Override
            public void onNewBlocks(EthChainConfig chainConfig, EthBestBlockResult result) {
                final StringBuilder builder = new StringBuilder("## Receive new blocks \n")
                        .append("chain config : ").append(chainConfig).append('\n');
                builder.append("## New blocks\n");
                for (Block block : result.getBlocks()) {
                    builder.append("#").append(block.getNumber().longValue())
                           .append(" ").append(block.getHash()).append('\n');
                }

                builder.append("## Eth nodes\n");
                for (EthNode node : result.getNodes()) {
                    builder.append("> ").append(node.getNodeName()).append('\n');
                }

                System.out.println(builder);
            }

            @Override
            public void onPendingTransactions(EthChainConfig chainConfig,
                                              List<Transaction> pendingTransactions) {

                final StringBuilder builder = new StringBuilder("## Receive new pending transactions")
                        .append("(#").append(pendingTransactions.size()).append(")\n")
                        .append("chain config : ").append(chainConfig).append('\n');

                for (Transaction pendingTransaction : pendingTransactions) {
                    builder.append("> ").append(pendingTransaction.getHash()).append('\n');
                }

                System.out.println(builder);
            }

            @Override
            public void prepareNewChain(EthChainConfig chainConfig) {
            }
        };

        rpcServiceFactory = new DefaultEthRpcServiceFactory();

        node1 = EthNodeConfigBuilder.builder("node1")
                                    .blockTime(5000L)
                                    .chainId(chainConfig.getChainId())
                                    .healthIndicatorType(EthConnectedOnly.INSTANCE)
                                    .pendingTransactionPollingInterval(1000L)
                                    .rpcUrl("http://localhost:8545")
                                    .subscribeNewBlock(false)
                                    .subscribePendingTransaction(true)
                                    .build();

        node2 = EthNodeConfigBuilder.builder("node2")
                                    .blockTime(5000L)
                                    .chainId(chainConfig.getChainId())
                                    .healthIndicatorType(EthSynchronized.INSTANCE)
                                    .pendingTransactionPollingInterval(1000L)
                                    .rpcUrl("ws://localhost:9546")
                                    .subscribeNewBlock(false)
                                    .subscribePendingTransaction(true)
                                    .build();
    }

    @Test
    public void getLoadBalancer() throws Exception {
        final EthChainManager chainManager = new EthChainManager(chainConfig,
                                                                 nodeManager,
                                                                 chainListener,
                                                                 rpcServiceFactory);

        chainManager.addNode(node1, false);
        chainManager.addNode(node2, false);

        Web3jService web3jService = chainManager.getLoadBalancedWeb3jService();
        Web3j web3j = Web3j.build(web3jService);

        for (int i = 0; i < 50; i++) {
            System.out.println("## Check syncing >> " + web3j.ethSyncing().send().isSyncing());
            TimeUnit.SECONDS.sleep(3L);
        }
    }

    @Test
    @DisplayName("listen events")
    public void start() throws Exception {
        final EthChainManager chainManager = new EthChainManager(chainConfig,
                                                                 nodeManager,
                                                                 chainListener,
                                                                 rpcServiceFactory);

        chainManager.addNode(node1, false);
        chainManager.addNode(node2, false);

        final Thread worker = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    final List<EthNode> activeNodes = chainManager.getActiveNodes();
                    final StringBuilder builder = new StringBuilder("Active node : ")
                            .append(activeNodes.size())
                            .append(" >> ")
                            .append(activeNodes.stream()
                                               .map(EthNode::getNodeName)
                                               .collect(Collectors.joining(" ")));
                    System.out.println(builder);
                    TimeUnit.SECONDS.sleep(5L);
                }
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        });
        worker.setDaemon(true);
        worker.start();

        TimeUnit.MINUTES.sleep(5L);
    }
}
