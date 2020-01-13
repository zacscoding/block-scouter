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

import org.junit.jupiter.api.Test;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionResult;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import blockscouter.core.chain.eth.EthChainConfig;
import blockscouter.core.chain.eth.EthChainConfigBuilder;
import blockscouter.core.chain.eth.EthChainListener;
import blockscouter.core.chain.eth.EthChainManager;
import blockscouter.core.chain.eth.download.EthBlockDownloader;
import blockscouter.core.chain.eth.download.EthDownloadBlock;
import blockscouter.core.chain.eth.event.EthBestBlockResult;
import blockscouter.core.health.eth.EthHealthIndicatorType.EthConnectedOnly;
import blockscouter.core.health.eth.EthHealthIndicatorType.EthSynchronized;
import blockscouter.core.node.eth.EthNodeConfig;
import blockscouter.core.node.eth.EthNodeConfigBuilder;
import blockscouter.core.node.eth.EthNodeManager;
import blockscouter.core.sdk.eth.DefaultEthRpcServiceFactory;
import blockscouter.core.sdk.eth.EthRpcServiceFactory;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;

/**
 *
 */
public class EthChainUsageTest {

    @Test
    public void runTests() throws Exception {
        /**
         * Build a EthChainManager
         */
        // build ethereum chain config
        EthChainConfig chainConfig = EthChainConfigBuilder.builder()
                                                          // ethereum's chain id
                                                          .chainId("36435")
                                                          // average block time
                                                          .blockTime(5000L)
                                                          // pending tx buffer max size
                                                          .pendingTransactionBatchMaxSize(3)
                                                          // pending tx buffer max seconds
                                                          .pendingTransactionBatchMaxSeconds(5)
                                                          .build();

        // in-memory node manage (add,delete nodes)
        EthNodeManager nodeManager = new EthNodeManager();

        // ethereum rpc service factory (i.e create a web3jservice given protocol)
        EthRpcServiceFactory rpcServiceFactory = new DefaultEthRpcServiceFactory();

        // build ethereum node1, node2
        EthNodeConfig node1Config = EthNodeConfigBuilder.builder("node1") // node name
                                                        // average block time
                                                        .blockTime(chainConfig.getBlockTime())
                                                        // chain id
                                                        .chainId(chainConfig.getChainId())
                                                        // health check type
                                                        .healthIndicatorType(EthConnectedOnly.INSTANCE)
                                                        // pending transaction polling interval(ms)
                                                        .pendingTransactionPollingInterval(1000L)
                                                        // rpc url (use http protocol)
                                                        .rpcUrl("http://localhost:8545")
                                                        // subscribe a new block or not
                                                        // if true, create a block stream
                                                        .subscribeNewBlock(false)
                                                        // subscribe pending transaction
                                                        // if true, create a transaction stream and
                                                        // collect distinct pending tx hash from EthChainManager
                                                        .subscribePendingTransaction(true)
                                                        .build();

        EthNodeConfig node2Config = EthNodeConfigBuilder.builder("node2")
                                                        .blockTime(chainConfig.getBlockTime())
                                                        .chainId(chainConfig.getChainId())
                                                        .healthIndicatorType(EthSynchronized.INSTANCE)
                                                        .pendingTransactionPollingInterval(1000L)
                                                        // rpc url (use websocket protocol)
                                                        .rpcUrl("ws://localhost:9546")
                                                        .subscribeNewBlock(false)
                                                        .subscribePendingTransaction(true)
                                                        .build();

        EthChainListener chainListener = new EthChainListener() {
            @Override
            public void onNewBlocks(EthChainConfig chainConfig, EthBestBlockResult result) {
                // new blocks event occur if satisfy conditions.
                // 1) after for sync timeout(blockTime * 1.5)
                // 2) adds a new node or changed to healthy state(unhealthy -> healthy)
            }

            @Override
            public void onPendingTransactions(EthChainConfig chainConfig,
                                              List<Transaction> pendingTransactions) {

                // pending transaction occur if satisfy conditions.
                // 1) after 5s == max time seconds of a chain config's pending tx buffer
                // 2) collected 3 pending transactions == max size of a chain config's max size
            }

            @Override
            public void prepareNewChain(EthChainConfig chainConfig) {
                // if create a new EthChainManager, this method is called only one time
            }
        };

        // Create a new chain manager
        EthChainManager chainManager = new EthChainManager(chainConfig, nodeManager,
                                                           chainListener, rpcServiceFactory);

        // Adds node1, node2
        chainManager.addNode(node1Config, false);
        chainManager.addNode(node2Config, false);

        /**
         * Use load balanced Web3jService
         */
        // Returns a load balanced web3jservice proxy
        Web3jService web3jService = chainManager.getLoadBalancedWeb3jService();
        Web3j web3j = Web3j.build(web3jService);

        // choose node1
        String node1ClientVersion = web3j.web3ClientVersion().send().getWeb3ClientVersion();
        // choose node2
        String node2ClientVersion = web3j.web3ClientVersion().send().getWeb3ClientVersion();

        /**
         * Download block [0, 10]
         */
        // Try to request getBlockByNumber(X) by load balanced Web3jService
        EthBlockDownloader downloader = EthBlockDownloader.buildDownloader(web3jService);
        Flowable<EthDownloadBlock> ethDownloadBlockFlowable = downloader.downloadBlocks(0L, 10L);
        Disposable subscription =
                ethDownloadBlockFlowable.subscribe(
                        result -> {
                            Block block = result.getBlock();
                            for (TransactionResult transaction : block.getTransactions()) {
                                Transaction tx = (Transaction) transaction;
                                TransactionReceipt tr = result.getReceiptMap().get(tx.getHash());
                            }
                        },
                        throwable -> throwable.printStackTrace(System.err),
                        () -> System.out.println("onComplete to download"));
    }
}
