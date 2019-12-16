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

package com.github.zacscoding.blockscouter.chain.eth.download;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.web3j.protocol.core.methods.response.EthBlock.Block;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

/**
 * Downloaded block with transactions and receipts
 */
public class EthDownloadBlock {

    private Block block;
    private Map<String, TransactionReceipt> receiptMap;

    public EthDownloadBlock(Block block) {
        this.block = checkNotNull(block, "block");
        if (block.getTransactions().isEmpty()) {
            receiptMap = Collections.emptyMap();
        } else {
            receiptMap = new HashMap<>();
        }
    }

    /**
     * Adds a transaction receipts
     */
    public void addTransactionReceipt(TransactionReceipt receipt) {
        checkNotNull(receipt, "receipt");
        checkState(receipt.getBlockNumber().equals(block.getNumber()));
        receiptMap.put(receipt.getTransactionHash(), receipt);
    }

    /**
     * Returns a {@link Block}
     */
    public Block getBlock() {
        return block;
    }

    /**
     * Returns a transaction receipts map with transaction hash key
     */
    public Map<String, TransactionReceipt> getReceiptMap() {
        return receiptMap;
    }
}
