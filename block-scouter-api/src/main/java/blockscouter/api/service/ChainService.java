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

package blockscouter.api.service;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import blockscouter.core.chain.eth.EthChainFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ChainService {

    private final EthChainFactory ethChainFactory;

    @PostConstruct
    private void setUp() {
        logger.info("## Check eth chain ids : {}", ethChainFactory.getChainIds());
    }
}
