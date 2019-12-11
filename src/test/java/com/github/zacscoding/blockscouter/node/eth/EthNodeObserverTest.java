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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.EthUninstallFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;

/**
 * Tests for eth node observer
 */
public class EthNodeObserverTest {

    // mocking web3j
    private Web3jService web3jService;
    private Web3j web3j;
    private final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    private final ScheduledExecutorService scheduledExecutorService
            = Executors.newSingleThreadScheduledExecutor();

    private EthFilter ethFilter;
    private EthUninstallFilter ethUninstallFilter;

    private EthNode node;

    @BeforeEach
    public void setUp() throws Exception {
        web3jService = mock(Web3jService.class);
        web3j = Web3j.build(web3jService, 1000L, scheduledExecutorService);
        node = mock(EthNode.class);

        when(node.getBlockTime()).thenReturn(50L);
        when(node.getPendingTxPollingInterval()).thenReturn(50L);
        when(node.getWeb3j()).thenReturn(web3j);

        ethFilter = objectMapper.readValue(
                "{\n"
                + "  \"id\":1,\n"
                + "  \"jsonrpc\": \"2.0\",\n"
                + "  \"result\": \"0x1\"\n"
                + "}", EthFilter.class);

        ethUninstallFilter = objectMapper.readValue(
                "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":true}", EthUninstallFilter.class);
    }

    @Test
    @DisplayName("start/stop to pending transaction filter")
    public void testStartStopPendingTransactionFilter() throws Exception {
        // given
        final EthLog ethLog = objectMapper.readValue(
                "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":["
                + "\"0xb546ba07b4d32d68120d0bf724ded2704ab7c0696039a86d54885c8b89990f41\","
                + "\"0x174a9eb6d5a30a1726dc1a88db5c89ebb7298a25194a7b51b532bbadee483e9d\""
                + "]}", EthLog.class);

        final List<String> pendingTxHashes = Arrays.asList(
                "0xb546ba07b4d32d68120d0bf724ded2704ab7c0696039a86d54885c8b89990f41",
                "0x174a9eb6d5a30a1726dc1a88db5c89ebb7298a25194a7b51b532bbadee483e9d"
        );

        when(web3jService.send(any(Request.class), eq(EthFilter.class))).thenReturn(ethFilter);
        when(web3jService.send(any(Request.class), eq(EthLog.class))).thenReturn(ethLog);
        when(web3jService.send(any(Request.class), eq(EthUninstallFilter.class)))
                .thenReturn(ethUninstallFilter);

        final EthNodeObserver nodeObserver = new EthNodeObserver(node);

        // when
        boolean result = nodeObserver.startPendingTxFilter();

        // then
        assertThat(result).isTrue();
        final CountDownLatch countDownLatch = new CountDownLatch(2);
        Flux.from(nodeObserver.getPendingTransactionStream())
            .subscribe(txHash -> {
                assertThat(pendingTxHashes.contains(txHash)).isTrue();
                countDownLatch.countDown();
            });
        assertThat(countDownLatch.await(500L, TimeUnit.MILLISECONDS)).isTrue();

        // when
        result = nodeObserver.stopPendingTxFilter();

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("start/stop to block filter")
    public void testStartStopBlockFilter() throws Exception {
        // given
        final EthLog ethLog = objectMapper.readValue(
                "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":["
                + "\"0x31c2342b1e0b8ffda1507fbffddf213c4b3c1e819ff6a84b943faabb0ebf2403\","
                + "\"0xccc0d2e07c1febcaca0c3341c4e1268204b06fefa4bb0c8c0d693d8e581c82da\""
                + "]}", EthLog.class);

        final List<EthBlock> mockBlocks = Arrays.asList(
                mock(EthBlock.class),
                mock(EthBlock.class)
        );

        when(web3jService.send(any(Request.class), eq(EthFilter.class))).thenReturn(ethFilter);
        when(web3jService.send(any(Request.class), eq(EthLog.class))).thenReturn(ethLog);
        when(web3jService.send(any(Request.class), eq(EthBlock.class)))
                .thenReturn(mockBlocks.get(0))
                .thenReturn(mockBlocks.get(1));
        when(web3jService.send(any(Request.class), eq(EthUninstallFilter.class)))
                .thenReturn(ethUninstallFilter);

        final EthNodeObserver nodeObserver = new EthNodeObserver(node);

        // when
        boolean result = nodeObserver.startBlockFilter();

        // then
        assertThat(result).isTrue();
        final CountDownLatch countDownLatch = new CountDownLatch(2);
        Flux.from(nodeObserver.getBlockStream())
            .subscribe(block -> {
                assertThat(mockBlocks.contains(block)).isTrue();
                countDownLatch.countDown();
            });

        assertThat(countDownLatch.await(500L, TimeUnit.MILLISECONDS)).isTrue();

        // when
        result = nodeObserver.stopBlockFilter();

        // then
        assertThat(result).isTrue();
    }
}
