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

package blockscouter.core.sdk.eth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.ipc.IpcService;
import org.web3j.protocol.websocket.WebSocketService;

import blockscouter.core.node.eth.EthNode;

public class EthRpcServiceFactoryTest {

    EthNode node = mock(EthNode.class);
    EthRpcServiceFactory rpcServiceFactory;

    @BeforeEach
    public void setUp() {
        rpcServiceFactory = new DefaultEthRpcServiceFactory();
    }

    @Test
    @DisplayName("create ipc service if file path is provided")
    public void createWeb3jFromIpcService() throws Exception {
        // given
        when(node.getRpcUrl()).thenReturn("/home/app/node.ipc");

        // when
        final Web3jService web3jService = rpcServiceFactory.createWeb3jService(node);

        // then
        assertThat(web3jService).isNotNull();
        assertThat(web3jService instanceof IpcService).isTrue();
    }

    @Test
    @DisplayName("create http service if http url is provided")
    public void createWeb3jFromHttpService() throws Exception {
        // given
        when(node.getRpcUrl()).thenReturn("http://localhost:8545");

        // when
        final Web3jService web3jService = rpcServiceFactory.createWeb3jService(node);

        // then
        assertThat(web3jService).isNotNull();
        assertThat(web3jService instanceof HttpService).isTrue();
    }

    @Test
    @DisplayName("create ws service if ws url is provided and then throw connection exception")
    public void createWeb3jFromWebsocketService() throws Exception {
        // given
        when(node.getRpcUrl()).thenReturn("ws://localhost:1");

        // when
        final Web3jService web3jService = rpcServiceFactory.createWeb3jService(node);

        // then
        assertThat(web3jService).isNotNull();
        assertThat(web3jService instanceof WebSocketService).isTrue();
    }
}
