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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.codahale.metrics.health.HealthCheck.Result;
import com.github.zacscoding.blockscouter.health.HealthIndicator;
import com.github.zacscoding.blockscouter.node.enums.BlockchainType;

public class NodeManagerTest {

    private NodeManager<MockNode> nodeManager;

    @BeforeEach
    public void setUp() {
        nodeManager = new DefaultNodeManager<>();
    }

    @Test
    @DisplayName("add a node")
    public void testAddNode() throws Exception {
        // given
        final String chainId = "123";
        final MockNode node = new MockNode("node1");
        node.setInitLatch(new CountDownLatch(1));

        // when
        final MockNode prevNode = nodeManager.addNode(chainId, node);

        // then
        assertThat(prevNode).isNull();
        assertThat(node.getInitLatch().await(100L, TimeUnit.MILLISECONDS))
                .isTrue();
    }

    @Test
    @DisplayName("remove a node")
    public void testRemoveNode() throws Exception {
        // given
        final String chainId = "123";
        final MockNode node = new MockNode("node1");
        node.setDestroyLatch(new CountDownLatch(1));
        assertThat(nodeManager.addNode(chainId, node)).isNull();

        // when
        final MockNode prevNode = nodeManager.removeNode(chainId, node.getNodeName());

        // then
        assertThat(prevNode).isEqualTo(node);
        assertThat(node.getDestroyLatch().await(100L, TimeUnit.MILLISECONDS))
                .isTrue();
    }

    @Test
    @DisplayName("get nodes")
    public void testGetNodes() throws Exception {
        // given
        final String chainId = "123";
        final MockNode node1 = new MockNode("node1");
        final MockNode node2 = new MockNode("node2");
        assertThat(nodeManager.addNode(chainId, node1)).isNull();
        assertThat(nodeManager.addNode(chainId, node2)).isNull();

        // when
        List<MockNode> nodes = nodeManager.getNodes(chainId);

        // then
        assertThat(nodes.size()).isEqualTo(2);
        assertThat(nodes.contains(node1)).isTrue();
        assertThat(nodes.contains(node2)).isTrue();

        // when then
        assertThat(nodeManager.getNodes(chainId + "_$$")).isEmpty();
    }

    @Test
    @DisplayName("get a node")
    public void testGetNode() {
        // given
        final String chainId = "123";
        final MockNode node1 = new MockNode("node1");
        final MockNode node2 = new MockNode("node2");
        assertThat(nodeManager.addNode(chainId, node1)).isNull();
        assertThat(nodeManager.addNode(chainId, node2)).isNull();
        assertThat(nodeManager.getNodes(chainId).size()).isEqualTo(2);

        // when
        Optional<MockNode> optional = nodeManager.getNode(chainId, node1.getNodeName());

        // then
        assertThat(optional).isPresent();
        assertThat(optional.get()).isEqualTo(node1);

        // when
        optional = nodeManager.getNode(chainId, node2.getNodeName());

        // then
        assertThat(optional).isPresent();
        assertThat(optional.get()).isEqualTo(node2);

        // when then
        assertThat(nodeManager.getNode(chainId, node2.getNodeName() + "aaa"))
                .isNotPresent();
    }

    public static class MockNode implements Node<HealthIndicator> {
        final String nodeName;
        CountDownLatch initLatch;
        CountDownLatch destroyLatch;

        public MockNode(String nodeName) {
            this.nodeName = nodeName;
        }

        public CountDownLatch getInitLatch() {
            return initLatch;
        }

        public void setInitLatch(CountDownLatch initLatch) {
            this.initLatch = initLatch;
        }

        public CountDownLatch getDestroyLatch() {
            return destroyLatch;
        }

        public void setDestroyLatch(CountDownLatch destroyLatch) {
            this.destroyLatch = destroyLatch;
        }

        @Override
        public BlockchainType getBlockchainType() {
            return null;
        }

        @Override
        public long getBlockTime() {
            return 0;
        }

        @Override
        public String getNodeName() {
            return nodeName;
        }

        @Override
        public String getRpcUrl() {
            return "";
        }

        @Override
        public void initialize() {
            if (initLatch != null) {
                initLatch.countDown();
            }
        }

        @Override
        public HealthIndicator getHealthIndicator() {
            return null;
        }

        @Override
        public void onHealthStateChange(Result current) {

        }

        @Override
        public void destroy() {
            if (destroyLatch != null) {
                destroyLatch.countDown();
            }
        }
    }
}
