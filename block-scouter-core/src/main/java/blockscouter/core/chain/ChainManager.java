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

package blockscouter.core.chain;

import java.util.List;

import blockscouter.core.node.Node;
import blockscouter.core.node.NodeConfig;

/**
 * Chain manager
 */
public interface ChainManager<N extends Node<?>, NC extends NodeConfig> {

    /**
     * Returns a chain id
     */
    String getChainId();

    /**
     * Adds a node
     */
    void addNode(NC nodeConfig);

    /**
     * Remove a node given node name
     */
    void removeNode(String name);

    /**
     * Return active nodes i.e healthy in chain
     */
    List<N> getActiveNodes();

    /**
     * Shutdown this chain manager
     */
    void shutdown();
}
