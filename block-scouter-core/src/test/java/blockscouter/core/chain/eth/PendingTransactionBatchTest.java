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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.core.methods.response.Transaction;

import blockscouter.core.chain.eth.EthChainConfig;
import blockscouter.core.chain.eth.EthChainListener;
import blockscouter.core.chain.eth.event.EthBestBlockResult;
import blockscouter.core.chain.eth.EthPendingTransactionBatch;
import blockscouter.core.node.eth.EthNode;

public class PendingTransactionBatchTest {

    Web3jService web3jService;
    Web3j web3j;
    EthNode node;
    AtomicInteger callCount;

    @BeforeEach
    public void setUp() throws Exception {
        callCount = new AtomicInteger(0);
        web3jService = mock(Web3jService.class);
        web3j = Web3j.build(web3jService);
        node = mock(EthNode.class);

        when(node.getWeb3j()).thenReturn(web3j);

        doAnswer((Answer<EthTransaction>) invocationOnMock -> {
            Request<String, ?> request = invocationOnMock.getArgument(0);
            String hash = request.getParams().get(0);
            EthTransaction ethTransaction = mock(EthTransaction.class);
            Transaction transaction = mock(Transaction.class);
            when(transaction.getHash()).thenReturn(hash);
            when(ethTransaction.getTransaction()).thenReturn(Optional.of(transaction));

            callCount.incrementAndGet();
            return ethTransaction;
        }).when(web3jService).send(any(Request.class), eq(EthTransaction.class));
    }

    @DisplayName("publish-with-maxsize")
    @Test
    public void testPublishAndGetOnlyOneWithMaxSize() throws Exception {
        // given
        final List<String> hashes = Arrays.asList(
                "hash1", "hash2", "hash3"
        );
        final EthChainConfig chainConfig = mock(EthChainConfig.class);
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final EthChainListener listener = new EthChainListener() {
            @Override
            public void onNewBlocks(EthChainConfig chainConfig, EthBestBlockResult result) {
                fail("Unexpected new blocks event");
                throw new RuntimeException();
            }

            @Override
            public void onPendingTransactions(EthChainConfig chainConfig,
                                              List<Transaction> pendingTransactions) {
                // then
                for (Transaction pendingTransaction : pendingTransactions) {
                    assertThat(hashes.contains(pendingTransaction.getHash())).isTrue();
                }
                assertThat(pendingTransactions.size()).isEqualTo(hashes.size());
                countDownLatch.countDown();
            }

            @Override
            public void prepareNewChain(EthChainConfig chainConfig) {

            }
        };

        final EthPendingTransactionBatch batch =
                new EthPendingTransactionBatch(chainConfig, Optional.of(listener));

        final int maxSize = hashes.size();
        final int maxSeconds = 10;

        // when
        batch.start(maxSize, Duration.ofSeconds(maxSeconds));

        for (String hash : hashes) {
            batch.publishPendingTx(hash, node);
        }

        // then
        assertThat(countDownLatch.await(100L, TimeUnit.MILLISECONDS)).isTrue();
    }

    @DisplayName("publish-with-timeout")
    @Test
    public void testPublishAndGetOnlyOneWithMaxTimeout() throws Exception {
        // given
        final List<String> hashes = Arrays.asList(
                "hash1"
        );
        final EthChainConfig chainConfig = mock(EthChainConfig.class);
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final EthChainListener listener = new EthChainListener() {
            @Override
            public void onNewBlocks(EthChainConfig chainConfig, EthBestBlockResult result) {
                fail("Unexpected new blocks event");
                throw new RuntimeException();
            }

            @Override
            public void onPendingTransactions(EthChainConfig chainConfig,
                                              List<Transaction> pendingTransactions) {
                // then
                for (Transaction pendingTransaction : pendingTransactions) {
                    assertThat(hashes.contains(pendingTransaction.getHash())).isTrue();
                }
                assertThat(pendingTransactions.size()).isEqualTo(hashes.size());
                countDownLatch.countDown();
            }

            @Override
            public void prepareNewChain(EthChainConfig chainConfig) {

            }
        };

        final EthPendingTransactionBatch batch =
                new EthPendingTransactionBatch(chainConfig, Optional.of(listener));

        final int maxSize = hashes.size() * 10;
        final int maxSeconds = 1;

        // when
        batch.start(maxSize, Duration.ofSeconds(maxSeconds));

        for (String hash : hashes) {
            batch.publishPendingTx(hash, node);
        }

        // then
        assertThat(countDownLatch.await(1500L, TimeUnit.MILLISECONDS)).isTrue();
    }
}
