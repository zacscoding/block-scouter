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

package blockscouter.core.chain.eth.event;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ethereum abstract sync event
 */
public abstract class EthSyncEvent {

    protected final EthSyncEventType eventType;

    EthSyncEvent(EthSyncEventType eventType) {
        this.eventType = checkNotNull(eventType, "eventType");
    }

    public EthSyncEventType getEventType() {
        return eventType;
    }
}
