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

package com.github.zacscoding.blockscouter.health.eth;

import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.methods.response.EthSyncing;
import org.web3j.protocol.websocket.WebSocketService;
import org.web3j.utils.Numeric;

import com.github.zacscoding.blockscouter.health.HealthIndicator;
import com.github.zacscoding.blockscouter.node.eth.EthNode;

/**
 * Indicator of a ethereum node's health check
 */
public class EthHealthIndicator extends HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(EthHealthIndicator.class);

    private final EthNode node;
    private final EthHealthIndicatorType type;

    public EthHealthIndicator(String chainId, EthNode node, EthHealthIndicatorType type) {
        super(checkNotNull(chainId, "chainId"), checkNotNull(node, "node").getNodeName());
        this.node = node;
        this.type = checkNotNull(type, "type");
    }

    @Override
    protected Result check() throws Exception {
        try {
            final EthSyncing.Result result = getSyncingResult();

            // 1) connected only
            if (type.isConnectedOnly()) {
                return Result.healthy();
            }

            // 2) completed synchronize
            if (!result.isSyncing()) {
                return Result.healthy();
            }

            // 3) syncing now

            // 3-1) type : synchronized
            if (type.isSynchronized()) {
                return Result.unhealthy("Syncing");
            }

            // 3-2) type : syncing -> check max diff
            if (!(result instanceof EthSyncing.Syncing)) {
                logger.warn("Received unknown syncing result type {}.", result.getClass().getName());
                throw new IllegalStateException("Unknown syncing result type " + result.getClass().getName());
            }

            final EthSyncing.Syncing syncing = (EthSyncing.Syncing) result;
            final BigInteger highestBlock = new BigInteger(
                    Numeric.hexStringToByteArray(syncing.getHighestBlock()));
            final BigInteger currentBlock = new BigInteger(
                    Numeric.hexStringToByteArray(syncing.getCurrentBlock()));

            final BigInteger diff = highestBlock.subtract(currentBlock);

            if (diff.compareTo(type.getMaxSyncingDiff()) <= 0) {
                return Result.healthy();
            }

            return Result.unhealthy("Syncing(Greater than maximum diff)");

        } catch (Exception e) {
            return Result.unhealthy(e.getMessage());
        }
    }

    private EthSyncing.Result getSyncingResult() throws Exception {
        try {
            return node.getWeb3j().ethSyncing().send().getResult();
        } catch (Exception e) {
            final Web3jService web3jService = node.getWeb3jService();

            // throw exception if not web socket service
            if (!(web3jService instanceof WebSocketService)) {
                throw e;
            }

            // if use websocket service, then try to reconnect
            final WebSocketService webSocketService = (WebSocketService) web3jService;

            if (!webSocketService.isConnected()) {
                webSocketService.connect();
            }

            return node.getWeb3j().ethSyncing().send().getResult();
        }
    }
}
