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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import blockscouter.core.health.eth.EthHealthChecker;
import blockscouter.core.health.eth.EthHealthIndicator;
import blockscouter.core.node.eth.EthNode;

/**
 * Dynamic ethereum load balancer based on {@link EthHealthChecker}
 */
public class DynamicEthLoadBalancer extends AbstractEthLoadBalancer {

    private final EthHealthChecker healthChecker;
    private final AtomicInteger nextServerCounter;

    public DynamicEthLoadBalancer(EthHealthChecker healthChecker) {
        this.healthChecker = checkNotNull(healthChecker, "healthChecker");
        nextServerCounter = new AtomicInteger(0);
    }

    @Override
    protected List<EthNode> getAvailableEthNodes(final Object key) {
        return Collections.unmodifiableList(healthChecker.getHealthyIndicators()
                                                         .stream()
                                                         .map(EthHealthIndicator::getNode)
                                                         .sorted(Comparator.comparing(EthNode::getNodeName))
                                                         .collect(Collectors.toList()));
    }

    @Override
    protected List<EthNode> getAllEthNodes(final Object key) {
        return Collections.unmodifiableList(healthChecker.getIndicators()
                                                         .stream()
                                                         .map(EthHealthIndicator::getNode)
                                                         .sorted(Comparator.comparing(EthNode::getNodeName))
                                                         .collect(Collectors.toList()));
    }

    @Override
    protected AtomicInteger getNextServerCounter(final Object key) {
        return nextServerCounter;
    }
}
