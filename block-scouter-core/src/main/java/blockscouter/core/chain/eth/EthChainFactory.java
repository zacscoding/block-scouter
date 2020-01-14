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

package blockscouter.core.chain.eth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import blockscouter.core.chain.ChainFactory;

/**
 * Handle ethereum chain managers
 */
public class EthChainFactory implements ChainFactory<EthChainManager> {

    private final Map<String, EthChainManager> chainsMap = new ConcurrentHashMap<>();

    /**
     * Adds a chain
     *
     * @return null if chainId is not exist, otherwise prev chain manager
     */
    public EthChainManager addChain(String chainId, EthChainManager chainManager) {
        return chainsMap.put(chainId, chainManager);
    }

    @Override
    public List<String> getChainIds() {
        return chainsMap.isEmpty() ? Collections.emptyList() : new ArrayList<>(chainsMap.keySet());
    }

    @Override
    public boolean contains(String chainId) {
        return chainsMap.containsKey(chainId);
    }

    @Override
    public Optional<EthChainManager> getChainManager(String chainId) {
        return Optional.ofNullable(chainsMap.get(chainId));
    }
}
