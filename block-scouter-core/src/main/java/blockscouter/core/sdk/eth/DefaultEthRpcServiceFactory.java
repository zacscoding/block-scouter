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

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.commons.lang3.StringUtils;
import org.web3j.protocol.Web3jService;

import blockscouter.core.exception.SDKCreateException;
import blockscouter.core.node.eth.EthNode;

/**
 * Default ethereum rpc service factory
 */
public class DefaultEthRpcServiceFactory implements EthRpcServiceFactory {

    @Override
    public Web3jService createWeb3jService(EthNode ethNode) {
        try {
            final String rpcUrl = checkNotNull(ethNode.getRpcUrl(), "ethNode.rpcUrl");

            if (StringUtils.isEmpty(rpcUrl)) {
                throw new SDKCreateException("Invalid empty rpc url");
            }

            // 1) websocket
            if (rpcUrl.startsWith("ws://")) {
                return EthRpcServiceFactory.createWebSocketService(ethNode);
            }

            // 2) http
            if (rpcUrl.startsWith("http://") || rpcUrl.startsWith("https://")) {
                return EthRpcServiceFactory.createHttpService(ethNode);
            }

            // 3) ipc
            return EthRpcServiceFactory.createIpcService(ethNode);
        } catch (Exception e) {
            throw new SDKCreateException(e);
        }
    }
}
