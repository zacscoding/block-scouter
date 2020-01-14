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

package blockscouter.core.health.eth;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigInteger;

/**
 * Types of ethereum node health indicator
 */
public interface EthHealthIndicatorType {

    /**
     * Healthy state if connected
     */
    boolean isConnectedOnly();

    /**
     * Healthy state if syncing or synchronized
     */
    boolean isSyncing();

    /**
     * Healthy state if synchronized
     */
    boolean isSynchronized();

    /**
     *
     */
    BigInteger getMaxSyncingDiff();

    /**
     * Determine to healthy if connected only
     */
    final class EthConnectedOnly implements EthHealthIndicatorType {

        public static final EthConnectedOnly INSTANCE = new EthConnectedOnly();

        private EthConnectedOnly() {
        }

        @Override
        public boolean isConnectedOnly() {
            return true;
        }

        @Override
        public boolean isSyncing() {
            return false;
        }

        @Override
        public boolean isSynchronized() {
            return false;
        }

        @Override
        public BigInteger getMaxSyncingDiff() {
            return BigInteger.ZERO;
        }
    }

    /**
     * Determine to healthy if synchronized or syncing in max difference
     * For example, suppose maxDiff is 50 blocks and currentBlock is 30, highest block is 70.
     * then difference is 40(70-30). so this node will have healthy state.
     */
    final class EthSyncingMaxDiff implements EthHealthIndicatorType {

        private final BigInteger maxDiff;

        public EthSyncingMaxDiff(BigInteger maxDiff) {
            checkArgument(maxDiff != null && maxDiff.compareTo(BigInteger.ZERO) >= 0,
                          "maxDiff must greater than or equals to 0");
            this.maxDiff = maxDiff;
        }

        @Override
        public boolean isConnectedOnly() {
            return false;
        }

        @Override
        public boolean isSyncing() {
            return true;
        }

        @Override
        public boolean isSynchronized() {
            return false;
        }

        @Override
        public BigInteger getMaxSyncingDiff() {
            return maxDiff;
        }
    }

    /**
     * Determine to healthy if synchronized
     */
    final class EthSynchronized implements EthHealthIndicatorType {

        public static final EthSynchronized INSTANCE = new EthSynchronized();

        private EthSynchronized() {
        }

        @Override
        public boolean isConnectedOnly() {
            return false;
        }

        @Override
        public boolean isSyncing() {
            return false;
        }

        @Override
        public boolean isSynchronized() {
            return true;
        }

        @Override
        public BigInteger getMaxSyncingDiff() {
            return BigInteger.ZERO;
        }
    }
}
