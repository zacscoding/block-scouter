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

package blockscouter.core.chain.eth.download;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Optional;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionResult;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Async;
import org.web3j.utils.Flowables;

import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Ethereum block downloader based on reactive stream
 */
public class EthBlockDownloaderReactiveStream implements EthBlockDownloader {

    private final Web3j web3j;
    private final Scheduler scheduler;

    EthBlockDownloaderReactiveStream(Web3jService web3jService) {
        scheduler = Schedulers.from(Async.defaultExecutorService());
        web3j = Web3j.build(checkNotNull(web3jService, "web3jService"));
    }

    @Override
    public Flowable<EthDownloadBlock> downloadBlocks(long startBlockNumber, long endBlockNumber) {
        checkArgument(startBlockNumber >= 0L, "startBlockNumber must greater than or equals to 0");
        checkArgument(endBlockNumber >= 0L, "endBlockNumber must greater than or equals to 0");

        final boolean ascending = startBlockNumber < endBlockNumber;

        return downloadBlocksFlowableSync(startBlockNumber, endBlockNumber, ascending)
                .subscribeOn(scheduler);
    }

    private Flowable<EthDownloadBlock> downloadBlocksFlowableSync(long start, long end, boolean ascending) {

        if (!ascending) {
            long t = start;
            start = end;
            end = t;
        }

        final BigInteger startBlockNumber = BigInteger.valueOf(start);
        final BigInteger endBlockNumber = BigInteger.valueOf(end);

        final Function<BigInteger, Flowable<EthDownloadBlock>> downloadFlowable =
                number -> Flowable.fromCallable(
                        () -> {
                            final Block block = web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(number),
                                                                          true)
                                                     .send()
                                                     .getBlock();

                            final EthDownloadBlock downloadBlock = new EthDownloadBlock(block);

                            for (TransactionResult tx : block.getTransactions()) {
                                final String transactionHash = ((Transaction) tx).getHash();
                                final Optional<TransactionReceipt> receiptOptional =
                                        web3j.ethGetTransactionReceipt(transactionHash)
                                             .send().getTransactionReceipt();

                                if (!receiptOptional.isPresent()) {
                                    throw new IOException(
                                            "Cannot download a transaction receipt :" + transactionHash);
                                }

                                downloadBlock.addTransactionReceipt(receiptOptional.get());
                            }
                            return downloadBlock;
                        }
                );

        return Flowables.range(startBlockNumber, endBlockNumber, ascending).flatMap(downloadFlowable);
    }
}
