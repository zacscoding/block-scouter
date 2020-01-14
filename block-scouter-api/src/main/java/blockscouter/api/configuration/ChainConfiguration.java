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

package blockscouter.api.configuration;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.core.methods.response.Transaction;

import blockscouter.core.chain.eth.EthChainConfig;
import blockscouter.core.chain.eth.EthChainListener;
import blockscouter.core.chain.eth.event.EthBestBlockResult;
import lombok.extern.slf4j.Slf4j;

/**
 */
@Configuration
@Slf4j
public class ChainConfiguration {

    @Bean
    public EthChainListener ethChainListener() {
        return new EthChainListener() {
            @Override
            public void onNewBlocks(EthChainConfig chainConfig, EthBestBlockResult result) {
                logger.info("onNewBlocks. {} ==> {}", chainConfig, result.getHighestBlock().getNumber());
            }

            @Override
            public void onPendingTransactions(EthChainConfig chainConfig,
                                              List<Transaction> pendingTransactions) {
                logger.info("onPendingTxns. {} ==> {}", chainConfig, pendingTransactions.size());
            }

            @Override
            public void prepareNewChain(EthChainConfig chainConfig) {
                logger.info("prepareNewChain ==> {}", chainConfig);
            }
        };
    }
}
