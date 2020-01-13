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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3jService;

import com.google.common.reflect.Reflection;

import blockscouter.core.node.eth.EthNode;

/**
 * Abstract load balanced ethereum client
 */
public abstract class AbstractEthLoadBalancer {

    private static final Logger logger = LoggerFactory.getLogger(AbstractEthLoadBalancer.class);

    private static final int RETRY_COUNT = 3;

    public Web3jService getLoadBalancedWeb3jService() {
        return getLoadBalancedWeb3jService(null);
    }

    public Web3jService getLoadBalancedWeb3jService(final Object key) {
        return Reflection.newProxy(Web3jService.class, (proxy, method, args) -> {
            Throwable exception = null;

            for (int i = 0; i < RETRY_COUNT; i++) {
                try {
                    final EthNode node = chooseNode(key);

                    if (node == null) {
                        if (exception == null) {
                            exception = new IOException("No available web3j service");
                        }

                        continue;
                    }

                    return method.invoke(node.getWeb3jService(), args);
                } catch (InvocationTargetException e) {
                    exception = e.getTargetException() != null ?
                                e.getTargetException() : new IOException("unknown exception");
                } catch (Exception e) {
                    exception = new IOException("unknown exception");
                }
            }

            throw exception;
        });
    }

    protected abstract List<EthNode> getAvailableEthNodes(Object key);

    protected abstract List<EthNode> getAllEthNodes(Object key);

    protected abstract AtomicInteger getNextServerCounter(Object key);

    private EthNode chooseNode(final Object key) {
        EthNode node;
        final AtomicInteger counter = getNextServerCounter(key);

        int count = 0;
        while (count++ < 10) {
            final List<EthNode> nodes = getAllEthNodes(key);

            if (nodes.isEmpty()) {
                logger.warn("No nodes from load balancer");
                return null;
            }

            if (getAvailableEthNodes(key).isEmpty()) {
                final String nodeNames = nodes
                        .stream()
                        .map(EthNode::getNodeName)
                        .collect(Collectors.joining(","));

                logger.warn("No available healthy nodes from load balancer. nodes : {}", nodeNames);

                return null;
            }

            int nextServerIndex = incrementAndGetModulo(counter, nodes.size());
            node = nodes.get(nextServerIndex);

            if (node.getHealthIndicator().isAlive()) {
                System.out.println(">> Choose  ==> " + node);
                logger.debug("Choose {}", node);
                return node;
            }
        }

        logger.warn("No available alive nodes after 10 tries from load balancer");
        return null;
    }

    private static int incrementAndGetModulo(AtomicInteger nextServerCyclicCounter, int modulo) {
        while (true) {
            int current = nextServerCyclicCounter.get();
            int next = (current + 1) % modulo;
            if (nextServerCyclicCounter.compareAndSet(current, next)) { return next; }
        }
    }
}
