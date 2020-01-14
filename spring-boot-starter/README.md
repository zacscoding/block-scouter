# Block Scouter with spring boot  

> ## Getting started  

> ## Getting started

**maven**  

```aidl

<repositories>
  <repository>
    <id>jcenter</id>
    <url>https://jcenter.bintray.com/</url>
  </repository>
</repositories>

...


<dependency>
	<groupId>block-scouter</groupId>
	<artifactId>spring-boot-starter</artifactId>
	<version>0.2.1</version>
</dependency>
```

**gradle**  

```aidl

repositories {
    jecenter()
}

...

implementation 'block-scoute:spring-boot-starter:0.2.1'

```  

**application.yaml**  

```yaml
spring:
  blockscouter:
    # Ethereum chains
    eth:
      chains:
        - chainId: 1
          blockTime: 5000
          # Subscribe new blocks or not
          subscribeNewBlocks: true
          # Subscribe pending txns or not
          subscribePendingTransactions: true
          # pending tx batch max size to flush
          pendingTxBatchMaxSize: 100
          # pending tx batch max seconds to flush
          pendingTxBatchMaxSeconds: 5

          ######################
          # health check
          health:
            # health check type ["connected", "syncing", "synchronized"]
            type: connected
            # max allowance between current_block and highest block if "sycing" type is provided
            allowance: 0

          ######################
          # nodes
          nodes:
            - name: eth-node1
              rpcUrl: http://192.168.79.121:8540
              subscribeBlock: true
              subscribePendingTx: true
              # polling interval to request filter changes in milli seconds
              pendingTxPollingInterval: 1000
            - name: eth-node2
              rpcUrl: http://192.168.79.123:8540
              subscribeBlock: false
              subscribePendingTx: false
              # polling interval to request filter changes in milli seconds
              pendingTxPollingInterval: 1000
```   