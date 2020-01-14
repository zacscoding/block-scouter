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

package blockscouter.core.node;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DefaultNodeManager<N extends Node> implements NodeManager<N> {

    private final Map<String, Map<String, N>> nodesByChainId;
    private final ReentrantReadWriteLock lock;

    public DefaultNodeManager() {
        nodesByChainId = new HashMap<>();
        lock = new ReentrantReadWriteLock();
    }

    @Override
    public N addNode(String chainId, N node) {
        checkNotNull(chainId, "chainId");
        checkNotNull(node, "node");

        try {
            lock.writeLock().lock();
            Map<String, N> nodes = nodesByChainId.get(chainId);

            if (nodes == null) {
                nodes = new HashMap<>();
                nodesByChainId.put(chainId, nodes);
            }

            node.initialize();
            return nodes.put(node.getNodeName(), node);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public N removeNode(String chainId, String name) {
        checkNotNull(chainId, "chainId");
        checkNotNull(name, "name");

        try {
            lock.writeLock().lock();
            Map<String, N> nodes = nodesByChainId.get(chainId);

            if (nodes == null) {
                return null;
            }

            final N removed = nodes.remove(name);

            if (removed != null) {
                removed.destroy();
            }

            return removed;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<N> getNodes(String chainId) {
        try {
            lock.readLock().lock();
            Map<String, N> nodes = nodesByChainId.get(chainId);

            if (nodes == null) {
                return Collections.emptyList();
            }

            return new ArrayList<>(nodes.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<N> getNode(String chainId, String name) {
        try {
            lock.readLock().lock();
            Map<String, N> nodes = nodesByChainId.get(chainId);

            if (nodes == null) {
                return Optional.empty();
            }

            return Optional.ofNullable(nodes.get(name));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void shutdown() {
        for (Map<String, N> entry : nodesByChainId.values()) {
            for (N value : entry.values()) {
                try {
                    value.destroy();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
