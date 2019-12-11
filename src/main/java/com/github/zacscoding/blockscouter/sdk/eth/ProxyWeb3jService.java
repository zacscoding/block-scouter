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

package com.github.zacscoding.blockscouter.sdk.eth;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Proxy;
import java.util.function.Function;

import org.web3j.protocol.Web3jService;

import com.google.common.reflect.Reflection;

/**
 * Proxy web3j service
 */
public class ProxyWeb3jService {

    private final Function<Integer, Web3jService> delegate;

    public ProxyWeb3jService(Function<Integer, Web3jService> delegate) {
        this.delegate = checkNotNull(delegate);
    }

    public Web3jService getProxyWeb3jService() {
        return Reflection.newProxy(Web3jService.class, (proxy, method, args) -> {
            Exception lastException = null;
            final int request = 0;
//                    while (true) {
//
//                    }
//
//                    for (int i = 0; i < services.size(); i++) {
//                        final int idx = (start + i) % services.size();
//
//                        try {
//                            return method.invoke(services.get(idx), args);
//                        } catch (Exception e) {
//                            lastException = e;
//                        }
//                    }
//
//                    if (lastException == null) {
//                        lastException = new IOException("possible error ?? after calling all web3j in proxy?");
//                    }
            throw lastException;
        });
    }
}
