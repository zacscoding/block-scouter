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

import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.methods.response.Transaction;

import com.github.zacscoding.blockscouter.chain.eth.EthChainConfig;
import com.github.zacscoding.blockscouter.chain.eth.EthChainListener;
import com.github.zacscoding.blockscouter.node.eth.EthNode;

import reactor.core.Disposable;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.FluxSink.OverflowStrategy;
import reactor.core.scheduler.Schedulers;

/**
 * Pending transaction batch
 *
 * Collect new pending transactions with max size and timeout, and then flush all
 */
public class EthPendingTransactionBatch {

    private static final Logger logger = LoggerFactory.getLogger(EthPendingTransactionBatch.class);

    // required
    private final EthChainConfig chainConfig;
    private final Optional<EthChainListener> listenerOptional;

    // builded
    private final FluxSink<Transaction> pendingTxFluxSink;
    private final Flux<Transaction> pendingTxFlux;
    private final Set<String> pendingTxHashSet;
    private final ReentrantReadWriteLock lock;
    private final ConcurrentHashMap<String, Disposable> subscriptionMap;

    // after started
    private Disposable subscription;

    public EthPendingTransactionBatch(EthChainConfig chainConfig, Optional<EthChainListener> listenerOptional) {
        this.chainConfig = checkNotNull(chainConfig, "chainConfig");
        this.listenerOptional = checkNotNull(listenerOptional, "listenerOptional");

        // builded
        final EmitterProcessor<Transaction> emitterProcessor = EmitterProcessor.create();
        pendingTxFluxSink = emitterProcessor.sink(OverflowStrategy.BUFFER);
        pendingTxFlux = emitterProcessor.publishOn(Schedulers.elastic());
        pendingTxHashSet = new HashSet<>();
        lock = new ReentrantReadWriteLock();
        subscriptionMap = new ConcurrentHashMap<>();
    }

    /**
     * Start a pending transaction batch with max size and duration to flush
     */
    public void start(int maxSize, Duration maxTime) {
        if (subscription != null) {
            logger.warn("Already started pending tx batch");
            return;
        }

        if (!listenerOptional.isPresent()) {
            logger.warn("Pending tx batch will start but there is no chain listener i.e it will be useless.");
        }

        checkArgument(maxSize > 0, "maxSize must greater than or equals to 0");
        maxSize = maxSize;
        maxTime = checkNotNull(maxTime, "maxTime");

        subscription = pendingTxFlux.bufferTimeout(maxSize, maxTime, Schedulers.elastic())
                                    .subscribe(values -> {
                                        // pending tx hash set clear
                                        try {
                                            lock.writeLock().lock();
                                            pendingTxHashSet.clear();
                                        } finally {
                                            lock.writeLock().unlock();
                                        }

                                        if (listenerOptional.isPresent()) {
                                            listenerOptional.get().onPendingTransactions(chainConfig.clone(),
                                                                                         values);
                                        }
                                    });
    }

    /**
     * Stop to pending transactions batch
     */
    public void stop() {
        if (subscription != null) {
            subscription.dispose();
            subscription = null;
            logger.debug("Success to stop pending tx batch");
        }
    }

    /**
     * Subscribe pending tx stream
     */
    public void subscribePendingTx(final EthNode node, final Publisher<String> publisher) {
        checkNotNull(node, "node");
        checkNotNull(publisher, "publisher");
        subscriptionMap.put(node.getNodeName(),
                            Flux.from(publisher).subscribe(txHash -> publishPendingTx(txHash, node)));
    }

    /**
     * Unsubscribe pending tx stream
     */
    public void unsubscribePendingTx(String nodeName) {
        final Disposable disposable = subscriptionMap.remove(nodeName);

        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    /**
     * Publish pending transaction if not exist in local map
     */
    public void publishPendingTx(String txHash, EthNode node) {
        try {
            lock.writeLock().lock();

            if (pendingTxHashSet.contains(txHash)) {
                return;
            }

            final Optional<Transaction> txOptional = node.getWeb3j()
                                                         .ethGetTransactionByHash(txHash)
                                                         .send()
                                                         .getTransaction();

            if (txOptional.isPresent()) {
                pendingTxHashSet.add(txHash);
                pendingTxFluxSink.next(txOptional.get());
            }
        } catch (Exception e) {
            logger.warn("failed to getting pending tx", e);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
