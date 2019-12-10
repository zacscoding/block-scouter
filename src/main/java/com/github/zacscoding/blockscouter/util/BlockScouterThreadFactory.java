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

package com.github.zacscoding.blockscouter.util;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockScouterThreadFactory implements ThreadFactory {

    private final static AtomicInteger THREAD_FACTORY_NUMBER = new AtomicInteger(0);
    private final AtomicInteger threadNumber = new AtomicInteger(0);
    private boolean daemon;
    private String threadName;

    public BlockScouterThreadFactory() {
        this("Block-Scouter");
    }

    public BlockScouterThreadFactory(String threadName) {
        this(threadName, false);
    }

    public BlockScouterThreadFactory(String threadName, boolean daemon) {
        this.threadName = requireNonNull(threadName, "threadName")
                          + "-"
                          + THREAD_FACTORY_NUMBER.incrementAndGet();
        this.daemon = daemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        final String threadName = generateThreadName();
        final Thread t = Executors.defaultThreadFactory().newThread(r);
        t.setName(threadName);
        t.setDaemon(daemon);
        return t;
    }

    private String generateThreadName() {
        final StringBuilder sb = new StringBuilder(threadName.length() + 8);
        return sb.append(threadName).append('-').append(threadNumber.incrementAndGet()).toString();
    }
}
