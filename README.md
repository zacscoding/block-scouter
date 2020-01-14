# Block scouter  
`Block scouter` is a library for managing blockchain clients.  

## Features

- Ethereum  
  - [x] manage nodes  
    - (un)subscribe new block, pending transaction  
    - check healthy or not depends on health check type  
  - [x] manage a chain      
    - listen to block event  
      - new block event  
      - pending transaction buffer
    - provide load balanced web3j proxy  

---  

## Modules  

- **block-scouter-core** : block scouter core module  
- **block-scouter-spring-boot-starter** : spring boot auto configure for block-scouter
- **block-scouter-api** : block scouter api server example(support searching blockchain data)  

---  

> ## Getting started

**Adds repository**  

```aidl
// maven
<repositories>
  <repository>
    <id>jcenter</id>
    <url>https://jcenter.bintray.com/</url>
  </repository>
</repositories>

// gradle
repositories {
  jecenter()
}
```  

**Adds dependency**  

```aidl
// maven
<dependency>
  <groupId>com.github.zacscoding</groupId>
  <artifactId>block-scouter-core</artifactId>
  <version>0.2.1</version>
</dependency>  

// gradle
implementation 'com.github.zacscoding:block-scouter-core:0.2.1'
```  

---  

## Usage of ethereum chain  

<a href="core/src/test/java/blockscouter/block-scouter-core/dev/EthChainUsageTest.java">See example code</a>  

> #### Build a EthChainManager  

```java
@Test
public void runTests() throws Exception {
    // build a ethereum chain config
    EthChainConfig chainConfig = EthChainConfigBuilder.builder()
                                                      // ethereum's chain id
                                                      .chainId("36435")
                                                      // average block time
                                                      .blockTime(5000L)
                                                      // pending tx buffer max size
                                                      .pendingTransactionBatchMaxSize(3)
                                                      // pending tx buffer max seconds
                                                      .pendingTransactionBatchMaxSeconds(5)
                                                      .build();

    // in-memory node manage (add,delete nodes)
    EthNodeManager nodeManager = new EthNodeManager();

    // ethereum rpc service factory (i.e create a web3jservice given protocol)
    EthRpcServiceFactory rpcServiceFactory = new DefaultEthRpcServiceFactory();

    // build ethereum node1, node2
    EthNodeConfig node1Config = EthNodeConfigBuilder.builder("node1") // node name
                                                    // average block time
                                                    .blockTime(chainConfig.getBlockTime())
                                                    // chain id
                                                    .chainId(chainConfig.getChainId())
                                                    // health check type
                                                    .healthIndicatorType(EthConnectedOnly.INSTANCE)
                                                    // pending transaction polling interval(ms)
                                                    .pendingTransactionPollingInterval(1000L)
                                                    // rpc url (use http protocol)
                                                    .rpcUrl("http://localhost:8545")
                                                    // subscribe a new block or not
                                                    // if true, create a block stream
                                                    .subscribeNewBlock(false)
                                                    // subscribe pending transaction
                                                    // if true, create a transaction stream and
                                                    // collect distinct pending tx hash from EthChainManager
                                                    .subscribePendingTransaction(true)
                                                    .build();

    EthNodeConfig node2Config = EthNodeConfigBuilder.builder("node2")
                                                    .blockTime(chainConfig.getBlockTime())
                                                    .chainId(chainConfig.getChainId())
                                                    .healthIndicatorType(EthSynchronized.INSTANCE)
                                                    .pendingTransactionPollingInterval(1000L)
                                                    // rpc url (use websocket protocol)
                                                    .rpcUrl("ws://localhost:9546")
                                                    .subscribeNewBlock(false)
                                                    .subscribePendingTransaction(true)
                                                    .build();

    EthChainListener chainListener = new EthChainListener() {
        @Override
        public void onNewBlocks(EthChainConfig chainConfig, EthBestBlockResult result) {
            // new blocks event occur if below conditions satisfied
            // => determine a best block from nodes by comparing total difficulty
            // 1) after force sync timeout(blockTime * 1.5)            
            // 2) adds a new node or changed to healthy state(unhealthy -> healthy)
        }

        @Override
        public void onPendingTransactions(EthChainConfig chainConfig,
                                          List<Transaction> pendingTransactions) {
            // pending transaction batch occur if below conditions satisfied.
            // 1) after 5s(== max time seconds of a chain config's pending tx batch)
            // 2) collected 3 pending transactions(== max size of a chain config's pending tx max size)
        }

        @Override
        public void prepareNewChain(EthChainConfig chainConfig) {
            // if create a new EthChainManager, this method is called only one time at first.
        }
    };

    // Create a new chain manager
    EthChainManager chainManager = new EthChainManager(chainConfig, nodeManager,
                                                       chainListener, rpcServiceFactory);

    // Adds node1, node2
    chainManager.addNode(node1Config, false);
    chainManager.addNode(node2Config, false);    
}
```  

> #### Use load balanced ethereum client i.e Web3jService  

```java
@Test
public void runTests() throws Exception {
    ...
    // Returns a load balanced web3jservice proxy
    Web3jService web3jService = chainManager.getLoadBalancedWeb3jService();
    Web3j web3j = Web3j.build(web3jService);

    // choose node1
    String node1ClientVersion = web3j.web3ClientVersion().send().getWeb3ClientVersion();
    // choose node2
    String node2ClientVersion = web3j.web3ClientVersion().send().getWeb3ClientVersion();
}
```  

> #### Use block download  

```java
@Test
public void runTests() throws Exception {    
    ...
    // Try to request getBlockByNumber(X) by load balanced Web3jService
    // EthDownloadBlock contains a block with transactions, transaction receipts in this block
    Flowable<EthDownloadBlock> ethDownloadBlockFlowable = downloader.downloadBlocks(0L, 10L);
    Disposable subscription =
        ethDownloadBlockFlowable.subscribe(
            result -> {
                Block block = result.getBlock();
                for (TransactionResult transaction : block.getTransactions()) {
                    Transaction tx = (Transaction) transaction;
                    TransactionReceipt tr = result.getReceiptMap().get(tx.getHash());
                }
            },
            throwable -> throwable.printStackTrace(System.err),
            () -> System.out.println("onComplete to download"));
    ...
}
```  
