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

package com.github.zacscoding.blockscouter.chain.eth;

import java.util.List;

import org.web3j.protocol.core.methods.response.Transaction;

/**
 * Ethereum chain event listener
 */
public interface EthChainListener {

    /**
     * Listen to subscribe new blocks.
     */
    void onNewBlocks(EthChainConfig chainConfig, EthBestBlockResult result);

    /**
     * Listen to subscribe new pending transactions
     */
    void onPendingTransactions(EthChainConfig chainConfig, List<Transaction> pendingTransactions);
}
