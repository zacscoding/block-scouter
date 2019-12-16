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

package com.github.zacscoding.blockscouter.chain.eth.event;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.web3j.protocol.core.methods.response.EthBlock.Block;

import com.github.zacscoding.blockscouter.node.eth.EthNode;

/**
 *
 */
public class EthBestBlockResult {

    /**
     * chain id
     */
    private String chainId;
    /**
     * blocks sorted with block number DESC
     */
    private List<Block> blocks;
    /**
     * nodes having same chain
     */
    private List<EthNode> nodes;

    private BigInteger totalDifficulty;

    public EthBestBlockResult(String chainId, Block block) {
        this(chainId, Collections.singletonList(block));
    }

    public EthBestBlockResult(String chainId, List<Block> blocks) {
        this(chainId, blocks, null);
    }

    public EthBestBlockResult(String chainId, List<Block> blocks, List<EthNode> nodes) {
        this.chainId = checkNotNull(chainId, "chainId");
        checkArgument(blocks != null && !blocks.isEmpty());
        blocks.sort(Comparator.comparing(Block::getNumber).reversed());
        this.blocks = blocks;
        this.nodes = nodes == null ? new ArrayList<>() : new ArrayList<>(nodes);
    }

    public void addEthNode(EthNode node) {
        nodes.add(checkNotNull(node, "node"));
    }

    public void addNodes(List<EthNode> nodes) {
        nodes.addAll(nodes);
    }

    /**
     * Returns a highest {@link Block}
     */
    public Block getHighestBlock() {
        return blocks.get(blocks.size() - 1);
    }

    /**
     * Returns a total difficulty or zero if empty blocks
     */
    public BigInteger getTotalDifficulty() {
        if (totalDifficulty != null) {
            return totalDifficulty;
        }

        final Block highestBlock = getHighestBlock();

        if (highestBlock == null) {
            return totalDifficulty = BigInteger.ZERO;
        }

        return totalDifficulty = highestBlock.getTotalDifficulty().add(highestBlock.getDifficulty());
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public List<EthNode> getNodes() {
        return nodes;
    }
}
