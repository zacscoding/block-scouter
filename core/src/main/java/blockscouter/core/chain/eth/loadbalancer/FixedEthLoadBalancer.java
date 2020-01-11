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

package blockscouter.core.chain.eth.loadbalancer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import blockscouter.core.node.eth.EthNode;

/**
 * Ethereum load balanced client with fixed {@link EthNode}
 */
public class FixedEthLoadBalancer extends AbstractEthLoadBalancer {

    private final AtomicInteger nextServerCounter;
    private final List<EthNode> nodes;

    public FixedEthLoadBalancer(List<EthNode> nodes) {
        this.nodes = nodes;
        nextServerCounter = new AtomicInteger(0);
    }

    @Override
    protected List<EthNode> getAvailableEthNodes() {
        return nodes;
    }

    @Override
    protected List<EthNode> getAllEthNodes() {
        return nodes;
    }

    @Override
    protected AtomicInteger getNextServerCounter() {
        return nextServerCounter;
    }
}
