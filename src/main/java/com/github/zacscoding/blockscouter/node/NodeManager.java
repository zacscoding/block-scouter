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

package com.github.zacscoding.blockscouter.node;

import java.util.List;
import java.util.Optional;

/**
 * Node manager
 */
public interface NodeManager<N extends Node> {

    /**
     * Adds a node
     *
     * After added, {@link Node#initialize()} is called.
     *
     * @return prev node if already saved, otherwise null
     */
    N addNode(String chainId, N node);

    /**
     * Remove a node given node name.
     *
     * After removed, {@link Node#destroy()} is called.
     *
     * @return node if saved, otherwise null
     */
    N removeNode(String chainId, String name);

    /**
     * Returns list of {@link Node} given chain id
     *
     * @return nodes given chain id or empty list
     */
    List<N> getNodes(String chainId);

    /**
     * Returns a {@link Node} given chain id and node name
     */
    Optional<N> getNode(String chainId, String name);
}
