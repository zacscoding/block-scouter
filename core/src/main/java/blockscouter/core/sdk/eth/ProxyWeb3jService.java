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

package blockscouter.core.sdk.eth;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;

import com.google.common.reflect.Reflection;

/**
 * Proxy web3j service
 */
public final class ProxyWeb3jService {

    private static final Logger logger = LoggerFactory.getLogger(ProxyWeb3jService.class);

    private final List<Web3jService> web3jServices;
    private final Web3jService proxyWeb3jService;
    private final Web3j web3j;
    private final AtomicLong indexGenerator;

    public static ProxyWeb3jService build(List<Web3jService> web3jServices) {
        return new ProxyWeb3jService(web3jServices);
    }

    private ProxyWeb3jService(List<Web3jService> web3jServices) {
        checkArgument(web3jServices != null && !web3jServices.isEmpty(), "web3jServices");
        this.web3jServices = new ArrayList<>(web3jServices);

        indexGenerator = new AtomicLong(0L);
        proxyWeb3jService = createProxyWeb3jService();
        web3j = Web3j.build(proxyWeb3jService);
    }

    public Web3j getWeb3j() {
        return web3j;
    }

    public Web3jService getProxyWeb3jService() {
        return proxyWeb3jService;
    }

    private Web3jService createProxyWeb3jService() {
        return Reflection.newProxy(Web3jService.class, (proxy, method, args) -> {

            final int size = web3jServices.size();
            final int startIndex = (int) indexGenerator.getAndIncrement();
            Throwable exception = null;

            for (int i = 0; i < size; i++) {
                // IllegalAccessException, IllegalArgumentException, InvocationTargetException
                try {
                    final int idx = (startIndex + i) % web3jServices.size();
                    return method.invoke(web3jServices.get(idx), args);
                } catch (InvocationTargetException e) {
                    exception = e.getTargetException() != null ?
                                e.getTargetException() : new IOException("unknown exception");
                } catch (Exception e) {
                    logger.warn("Unexpected exception occur after invoke Web3jService. {}", e);
                    exception = new IOException("unknown exception");
                }
            }

            throw exception;
        });
    }
}
