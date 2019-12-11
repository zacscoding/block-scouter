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

import org.apache.commons.lang3.SystemUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.ipc.IpcService;
import org.web3j.protocol.ipc.UnixIpcService;
import org.web3j.protocol.ipc.WindowsIpcService;
import org.web3j.protocol.websocket.WebSocketService;

import com.github.zacscoding.blockscouter.exception.SDKCreateException;
import com.github.zacscoding.blockscouter.node.eth.EthNode;

/**
 * Web3j factory
 */
@FunctionalInterface
public interface EthRpcServiceFactory {

    /**
     * Create a {@link Web3j} given ethereum node
     *
     * @throws SDKCreateException throw exception if invalid rpc url in given node
     */
    Web3j createWeb3j(EthNode ethNode);

    /**
     * build web3j from ipc service
     */
    static Web3j buildWeb3jFromIpcService(EthNode ethNode) throws Exception {
        IpcService ipcService = SystemUtils.IS_OS_WINDOWS ? new WindowsIpcService(ethNode.getRpcUrl())
                                                          : new UnixIpcService(ethNode.getRpcUrl());

        return Web3j.build(ipcService);
    }

    /**
     * build web3j from http service
     */
    static Web3j buildWeb3jFromHttpService(EthNode ethNode) throws Exception {
        return Web3j.build(new HttpService(ethNode.getRpcUrl()));
    }

    /**
     * build web3j from websocket service
     */
    static Web3j buildWeb3jFromWebsocketService(EthNode ethNode) throws Exception {
        return Web3j.build(new WebSocketService(ethNode.getRpcUrl(), false));
    }
}
