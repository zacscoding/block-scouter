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

package com.github.zacscoding.blockscouter.chain;

import java.util.List;
import java.util.Optional;

/**
 * Factory of block chains
 */
public interface ChainFactory<T> {

    /**
     * Returns all chain id
     */
    List<String> getChainIds();

    /**
     * Returns whether contains or not given chain id
     */
    boolean contains(String chainId);

    /**
     * Returns a {@link T} chain manager given chain id or empty
     */
    Optional<T> getChainManager(String chainId);
}
