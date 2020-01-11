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

package blockscouter.core.health.eth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthSyncing;

import com.codahale.metrics.health.HealthCheck.Result;
import com.fasterxml.jackson.databind.ObjectMapper;

import blockscouter.core.health.eth.EthHealthIndicatorType.EthSynchronized;
import blockscouter.core.health.eth.EthHealthIndicatorType.EthSyncingMaxDiff;
import blockscouter.core.node.eth.EthNode;

/**
 */
public class EthHealthIndicatorTest {

    final String chainId = "1";
    final EthNode node = mock(EthNode.class);
    final Web3jService web3jService = mock(Web3jService.class);
    private final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    Web3j web3j;
    EthSyncing syncing;
    EthSyncing notSyncing;

    @BeforeEach
    public void setUp() throws Exception {
        web3j = Web3j.build(web3jService);
        when(node.getWeb3j()).thenReturn(web3j);
        when(node.getNodeName()).thenReturn("node1");

        syncing = objectMapper.readValue(
                "{\n"
                + "  \"id\":1,\n"
                + "  \"jsonrpc\": \"2.0\",\n"
                + "  \"result\": {\n"
                + "  \"startingBlock\": \"0x380\",\n"
                + "  \"currentBlock\": \"0x383\",\n"
                + "  \"highestBlock\": \"0x389\"\n"
                + "  }\n"
                + "}", EthSyncing.class);

        notSyncing = objectMapper.readValue(
                "{\n"
                + "  \"id\":1,\n"
                + "  \"jsonrpc\": \"2.0\",\n"
                + "  \"result\": false\n"
                + "}", EthSyncing.class);
    }

    @Test
    @DisplayName("Connected only health check")
    public void testCheckWithConnectedOnly() throws Exception {
        // given
        final EthHealthIndicator indicator = new EthHealthIndicator(
                chainId, node, EthHealthIndicatorType.EthConnectedOnly.INSTANCE
        );
        when(web3jService.send(any(Request.class), eq(EthSyncing.class))).thenReturn(notSyncing);

        // when
        Result result = indicator.check();

        // then
        assertThat(result.isHealthy()).isTrue();

        // given
        when(web3jService.send(any(Request.class), eq(EthSyncing.class))).thenThrow(new IOException());

        // when
        result = indicator.check();

        // then
        assertThat(result.isHealthy()).isFalse();
    }

    @Test
    @DisplayName("Syncing health check")
    public void testCheckWithSyncingType() throws Exception {
        // given
        final long[] failuresDiff = { 5L, 4L, 3L };
        final long[] successesDiff = { 6L, 7L, 8L, 9L };
        when(web3jService.send(any(Request.class), eq(EthSyncing.class))).thenReturn(syncing);

        // when then
        for (long failureDiff : failuresDiff) {
            final Result result = new EthHealthIndicator(
                    chainId, node, new EthSyncingMaxDiff(BigInteger.valueOf(failureDiff))
            ).check();

            assertThat(result.isHealthy()).isFalse();
        }

        for (long successDiff : successesDiff) {
            final Result result = new EthHealthIndicator(
                    chainId, node, new EthSyncingMaxDiff(BigInteger.valueOf(successDiff))
            ).check();

            assertThat(result.isHealthy()).isTrue();
        }
    }

    @Test
    @DisplayName("Complete sync health check")
    public void testCheckWithCompleteSync() throws Exception {
        // given
        final EthHealthIndicator indicator = new EthHealthIndicator(
                chainId, node, EthSynchronized.INSTANCE
        );

        when(web3jService.send(any(Request.class), eq(EthSyncing.class))).thenReturn(syncing);

        // when then
        assertThat(indicator.check().isHealthy()).isFalse();

        // given
        when(web3jService.send(any(Request.class), eq(EthSyncing.class))).thenReturn(notSyncing);

        // when then
        assertThat(indicator.check().isHealthy()).isTrue();
    }
}
