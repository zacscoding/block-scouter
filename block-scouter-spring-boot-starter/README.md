# Block Scouter with spring boot  

> ## Getting started  

- Adds `block-scouter-spring-boot-starter` dependency  
- Configure `Block scouter properties`  
- Configure required bean `ChainListener`  

#### Adds dependency  

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
  <artifactId>block-scouter-spring-boot-starter</artifactId>
  <version>0.2.1</version>
</dependency>  

// gradle
implementation 'com.github.zacscoding:block-scouter-spring-boot-starter:0.2.1'
```    

#### Configure application.yaml  

**application.yaml**  

```yaml
spring:
  blockscouter:
    # Ethereum chains
    eth:
      chains:
        - chainId: 1
          blockTime: 5000
          subscribeNewBlocks: true # Subscribe new blocks or not
          subscribePendingTransactions: true # Subscribe pending txns or not
          pendingTxBatchMaxSize: 100 # pending tx batch max size to flush
          pendingTxBatchMaxSeconds: 5  # pending tx batch max seconds to flush

          ######################
          # health check
          health:
            type: connected # health check type ["connected", "syncing", "synchronized"]
            allowance: 0 # max allowance between current_block and highest block if "sycing" type is provided

          ######################
          # nodes
          nodes:
            - name: eth-node1
              rpcUrl: http://192.168.79.121:8540
              subscribeBlock: true
              subscribePendingTx: true
              pendingTxPollingInterval: 1000 # polling interval to request filter changes in milli seconds
            - name: eth-node2
              rpcUrl: http://192.168.79.123:8540
              subscribeBlock: false
              subscribePendingTx: false
              pendingTxPollingInterval: 1000
```   

#### Configure required bean  

- `blockscouter.core.chain.eth.EthChainListener`

```java  
@Bean
public EthChainListener ethChainListener() {
  return new EthChainListener() {
    @Override
    public void onNewBlocks(EthChainConfig chainConfig, EthBestBlockResult result) {
        // if spring.blockscouter.chains[].subscribeNewBlocks is true,
        // will produce new block event every (spring.blockscouter.chains[].blockTime * 1.5) milliseconds.
    }

    @Override
    public void onPendingTransactions(EthChainConfig chainConfig, List<Transaction> pendingTransactions) {
        // if spring.blockscouter.chains[].subscribePendingTransactions is true,
        // will produce pending tx batch events for every pendingTxBatchMaxSeconds seconds
        // or pending tx size is equals to pendingTxBatchMaxSize
    }

    @Override
    public void prepareNewChain(EthChainConfig chainConfig) {
        // this methods is called only one time at first after EthChainManager is created
    }
};
```  