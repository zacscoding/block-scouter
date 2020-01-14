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

package blockscouter.core.dev;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.websocket.WebSocketService;

/**
 */
public class Temp {

    @Test
    public void t() throws Exception {
        WebSocketService webSocketService = new WebSocketService("ws://localhost:9546", false);
        webSocketService.connect();
        Web3j web3j = Web3j.build(webSocketService);

        for (int i = 0; i < 10; i++) {
            try {
                System.out.println(web3j.web3ClientVersion().send().getWeb3ClientVersion());
                TimeUnit.SECONDS.sleep(1L);
            } catch (Exception e) {
                System.out.println("Exception :: " + e.getClass());
                System.out.println("Cause :: " + e.getCause().getClass());
            }
        }
    }
}
