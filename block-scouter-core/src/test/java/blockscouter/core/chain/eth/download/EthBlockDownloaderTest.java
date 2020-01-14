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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import org.web3j.protocol.core.methods.response.Transaction;

/**
 * TODO
 */
public class EthBlockDownloaderTest {
    Block[] blocks;
    Transaction tx10;
    Transaction tx11;

    @BeforeEach
    public void setUp() {
        blocks = new Block[] {
                mock(Block.class), mock(Block.class), mock(Block.class)
        };

        for (int i = 0; i < blocks.length; i++) {
            when(blocks[i].getNumber()).thenReturn(BigInteger.valueOf(i));
        }

        tx10 = mock(Transaction.class);
        tx11 = mock(Transaction.class);

        when(blocks[0].getTransactions()).thenReturn(Collections.emptyList());
        when(blocks[2].getTransactions()).thenReturn(Collections.emptyList());
    }

    @DisplayName("download with ascending")
    @Test
    public void testDownloadBlockWithAscending() {

    }
}
