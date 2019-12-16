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

package com.github.zacscoding.blockscouter.chain.eth.download;

import org.web3j.protocol.Web3jService;

import io.reactivex.Flowable;

/**
 * Ethereum block downloader
 */
public interface EthBlockDownloader {

    static EthBlockDownloader buildDownloader(Web3jService web3jService) {
        return new EthBlockDownloaderReactiveStream(web3jService);
    }

    /**
     * Download blocks and transaction receipts with [startBlockNumber, endBlockNumber]
     */
    Flowable<EthDownloadBlock> downloadBlocks(long startBlockNumber, long endBlockNumber);
}
