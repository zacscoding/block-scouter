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

import org.apache.commons.lang3.SystemUtils;
import org.web3j.protocol.Service;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.ipc.IpcService;
import org.web3j.protocol.ipc.UnixIpcService;
import org.web3j.protocol.ipc.WindowsIpcService;
import org.web3j.protocol.websocket.WebSocketService;

import blockscouter.core.exception.SDKCreateException;
import blockscouter.core.node.eth.EthNode;
import okhttp3.OkHttpClient;

/**
 * Web3j factory
 */
@FunctionalInterface
public interface EthRpcServiceFactory {

    /**
     * Create a {@link Service} given ethereum node
     *
     * @throws SDKCreateException throw exception if invalid rpc url in given node
     */
    Web3jService createWeb3jService(EthNode ethNode);

    /**
     * Create a {@link IpcService} given {@link EthNode}
     */
    static Web3jService createIpcService(EthNode ethNode) throws Exception {
        return SystemUtils.IS_OS_WINDOWS ? new WindowsIpcService(ethNode.getRpcUrl())
                                         : new UnixIpcService(ethNode.getRpcUrl());
    }

    /**
     * Create a {@link HttpService} given {@link EthNode}
     */
    static Web3jService createHttpService(EthNode ethNode) throws Exception {
        return new HttpService(ethNode.getRpcUrl());
    }

    /**
     * Create a {@link HttpService} given {@link EthNode} and {@link OkHttpClient}
     */
    static Web3jService createHttpService(EthNode ethNode, OkHttpClient httpClient) throws Exception {
        return new HttpService(ethNode.getRpcUrl(), httpClient);
    }

    /**
     * Create a {@link WebSocketService} given {@link EthNode} and {@link OkHttpClient}
     */
    static Web3jService createWebSocketService(EthNode ethNode) throws Exception {
        return new WebSocketService(ethNode.getRpcUrl(), false);
    }
}
