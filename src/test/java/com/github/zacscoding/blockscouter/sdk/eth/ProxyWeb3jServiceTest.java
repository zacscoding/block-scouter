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

package com.github.zacscoding.blockscouter.sdk.eth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthSyncing;

public class ProxyWeb3jServiceTest {

    @DisplayName("calling all")
    @Test
    public void testProxyWeb3jServices() throws Exception {
        // given
        final Web3jService service1 = mock(Web3jService.class);
        final Web3jService service2 = mock(Web3jService.class);

        final EthSyncing ethSyncing = ObjectMapperFactory.getObjectMapper()
                                                         .readValue("{\n"
                                                                    + "  \"id\":1,\n"
                                                                    + "  \"jsonrpc\": \"2.0\",\n"
                                                                    + "  \"result\": false\n"
                                                                    + "}", EthSyncing.class);

        when(service1.send(any(Request.class), any())).thenThrow(new IOException());
        when(service2.send(any(Request.class), any())).thenReturn(ethSyncing);

        final ProxyWeb3jService proxy = ProxyWeb3jService.build(Arrays.asList(service1, service2));

        // when
        EthSyncing.Result result = proxy.getWeb3j()
                                        .ethSyncing()
                                        .send()
                                        .getResult();

        // then
        assertThat(result.isSyncing()).isFalse();
        verify(service1, only()).send(any(Request.class), any());
        verify(service2, only()).send(any(Request.class), any());
    }

    @DisplayName("throw last exception")
    @Test
    public void testProxyWeb3jServicesThrowLastException() throws Exception {
        // given
        final Web3jService service1 = mock(Web3jService.class);
        final Web3jService service2 = mock(Web3jService.class);

        final IOException exception1 = new IOException("exception1");
        final IOException exception2 = new IOException("exception2");

        when(service1.send(any(Request.class), any())).thenThrow(exception1);
        when(service2.send(any(Request.class), any())).thenThrow(exception2);

        final ProxyWeb3jService proxy = ProxyWeb3jService.build(Arrays.asList(service1, service2));

        // when then
        assertThatThrownBy(() -> proxy.getWeb3j().ethSyncing().send())
                .isInstanceOf(IOException.class)
                .hasMessage(exception2.getMessage());

        verify(service1, only()).send(any(Request.class), any());
        verify(service2, only()).send(any(Request.class), any());
    }
}
