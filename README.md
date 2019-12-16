# Block scouter
; Subscribe block chain events such as ethereum.

> ## Getting started
; will be added 

> ## Building a ethereum chain  
[See](src/test/java/com/github/zacscoding/blockscouter/dev/ChainManagerConsoleTest.java)

- manage ethereum's node 
- manage healthy/unhealthy node
- provide Web3jService proxy from active nodes
- listen new block event 
- listen pending transaction buffer

> chain config

```aidl
final EthChainConfig chainConfig = EthChainConfigBuilder.builder()
                                           .chainId("36435") // ethereum chain id
                                           .blockTime(5000L) // average block time
                                           .pendingTransactionBatchMaxSize(3) // pending tx batch max size
                                           .pendingTransactionBatchMaxSeconds(5) // pending tx batch max timeout
                                           .build();
```  

> node manager

```aidl
final EthNodeManager nodeManager = new EthNodeManager();
```

> rpc service factory

```aidl
final EthRpcServiceFactory rpcServiceFactory = new DefaultEthRpcServiceFactory();
``` 

> build a node

```aidl
node1 = EthNodeConfigBuilder.builder("node1")
                                    .blockTime(5000L)
                                    .chainId(chainConfig.getChainId())
                                    .healthIndicatorType(EthConnectedOnly.INSTANCE)
                                    .pendingTransactionPollingInterval(1000L)
                                    .rpcUrl("http://localhost:8545")
                                    .subscribeNewBlock(false)
                                    .subscribePendingTransaction(true)
                                    .build();
```

> build a ethereum chain

```aidl
final EthChainManager chainManager = new EthChainManager(chainConfig,
                                                         nodeManager,
                                                         chainReader,
                                                         chainListener,
                                                         rpcServiceFactory);

chainManager.addNode(node1, false);
```

> Provide chain reader & chain listener

> EthChainReader

```aidl
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
```

> EthChainListener

```aidl
public interface EthChainListener {

    /**
     * Listen to subscribe new blocks.
     */
    void onNewBlocks(EthChainConfig chainConfig, EthBestBlockResult result);

    /**
     * Listen to subscribe new pending transactions
     */
    void onPendingTransactions(EthChainConfig chainConfig, List<Transaction> pendingTransactions);
}
```

