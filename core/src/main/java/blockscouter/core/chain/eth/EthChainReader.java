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

import java.math.BigInteger;

/**
 * Ethereum chain reader
 */
public interface EthChainReader {

    /**
     * Returns a total difficulty given chain id
     */
    BigInteger getTotalDifficulty(String chainId);

    /**
     * Returns a block hash given chain id and block number
     */
    String getBlockHashByNumber(String chainId, Long blockNumber);
}
