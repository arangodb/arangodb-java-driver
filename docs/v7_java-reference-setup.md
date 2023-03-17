# Driver setup

The driver can be configured and instantiated using `com.arangodb.ArangoDB.Builder`:

```java
ArangoDB arangoDB = new ArangoDB.Builder()
        // ...
        .build();
```

To customize the configuration properties can be set programmatically in the builder:

```java
ArangoDB arangoDB = new ArangoDB.Builder()
        .host("127.0.0.1",8529)
        // ...
        .build();
```

or providing an implementation of `com.arangodb.config.ArangoConfigProperties` to the builder:

```java
ArangoConfigProperties props = ...
ArangoDB arangoDB = new ArangoDB.Builder()
        .loadProperties(props)
        // ...
        .build();
```

Implementations of `com.arangodb.config.ArangoConfigProperties` could supply configuration properties coming from
different sources, eg. system properties, remote stores, frameworks integrations, etc. 
An implementation for loading properties from local files is provided by 
`ArangoConfigProperties.fromFile(String fileName, String prefix)` and its overloaded variants.

For example, to read config properties from `arangodb.properties` file (same as in version `6`):

```java
ArangoConfigProperties props = ArangoConfigProperties.fromFile();  // reads "arangodb.properties" by default
```

Overloaded variants can be used to specify the configuration file name and the prefix of the config keys:

```java
// ## arangodb-with-prefix.properties
// adb.hosts=172.28.0.1:8529
// adb.password=test
// ...

ArangoConfigProperties props = ArangoConfigProperties.fromFile("arangodb-with-prefix.properties", "adb");
```

Here are examples to integrate configuration properties from different sources:
- [Eclipse MicroProfile Config](https://github.com/arangodb-helper/arango-quarkus-native-example/blob/master/src/main/java/org/acme/quickstart/ArangoConfig.java)
- [Micronaut Configuration](https://github.com/arangodb-helper/arango-micronaut-native-example/blob/main/src/main/kotlin/com/example/ArangoConfig.kt)



## Configuration

`ArangoDB.Builder` has the following configuration methods:

- `host(String, int)`:           adds a host (hostname and port) to connect to, multiple hosts can be added
- `protocol(Protocol)`:       communication protocol, possible values are: `VST`, `HTTP_JSON`, `HTTP_VPACK`, `HTTP2_JSON`, `HTTP2_VPACK`, (default: `HTTP2_JSON`)
- `timeout(Integer)`:            connection and request timeout (ms), (default `0`, no timeout)
- `user(String)`:                username for authentication, (default: `root`)
- `password(String)`:            password for authentication
- `jwt(String)`:                 JWT for authentication
- `useSsl(Boolean)`:             use SSL connection, (default: `false`)
- `sslContext(SSLContext)`:      SSL context
- `verifyHost(Boolean)`:         enable hostname verification, (HTTP only, default: `true`)
- `chunkSize(Integer)`: `VST`    chunk size in bytes, (default: `30000`)
- `maxConnections(Integer)`:     max number of connections per host, (default: 1 VST, 1 HTTP/2, 20 HTTP/1.1)
- `connectionTtl(Long)`:         max lifetime of a connection (ms), (default: no ttl)
- `keepAliveInterval(Integer)`:  VST keep-alive interval (s), (default: no keep-alive probes will be sent)
- `acquireHostList(Boolean)`:    acquire the list of available hosts, (default: `false`)
- `acquireHostListInterval(Integer)`:             acquireHostList interval (ms), (default: `3_600_000`, 1 hour)
- `loadBalancingStrategy(LoadBalancingStrategy)`: load balancing strategy, possible values are: `NONE`, `ROUND_ROBIN`, `ONE_RANDOM`, (default: `NONE`)
- `responseQueueTimeSamples(Integer)`:            amount of samples kept for queue time metrics, (default: `10`)
- `serde(ArangoSerde)`:                           serde to serialize and deserialize user data


`ArangoConfigProperties.fromFile()` reads the following properties:
- `hosts`: comma-separated list of `<hostname>:<port>` entries
- `protocol`: `VST`, `HTTP_JSON`, `HTTP_VPACK`, `HTTP2_JSON` or `HTTP2_VPACK`
- `timeout`
- `user` 
- `password` 
- `jwt` 
- `useSsl` 
- `verifyHost` 
- `chunkSize` 
- `maxConnections` 
- `connectionTtl` 
- `keepAliveInterval` 
- `acquireHostList` 
- `acquireHostListInterval` 
- `loadBalancingStrategy`: `NONE`, `ROUND_ROBIN` or `ONE_RANDOM`
- `responseQueueTimeSamples` 


## SSL

To use SSL, you have to set the configuration `useSsl` to `true` and set a `SSLContext`
(see [example code](../driver/src/test/java/com/arangodb/example/ssl/SslExampleTest.java){:target="_blank"}).

```java
ArangoDB arangoDB = new ArangoDB.Builder()
  .useSsl(true)
  .sslContext(sc)
  .build();
```


## Connection Pooling

The driver keeps a pool of connections for each host, the max amount of connections is configurable.

Connections are released after the configured connection ttl or when the driver is shut down:

```java
arangoDB.shutdown();
```


## Thread Safety

The driver can be used concurrently by multiple threads. All the following classes are thread safe:
- `com.arangodb.ArangoDB`
- `com.arangodb.ArangoDatabase`
- `com.arangodb.ArangoCollection`
- `com.arangodb.ArangoGraph`
- `com.arangodb.ArangoVertexCollection`
- `com.arangodb.ArangoEdgeCollection`
- `com.arangodb.ArangoView`
- `com.arangodb.ArangoSearch`

Any other class should not be considered thread safe. In particular classes representing request options (package 
`com.arangodb.model`) and response entities (package `com.arangodb.entity`) are not thread safe.


## Fallback hosts

The driver supports configuring multiple hosts. The first host is used to open a
connection to. When this host is not reachable the next host from the list is used.
To use this feature just call the method `host(String, int)` multiple times.

```java
ArangoDB arangoDB = new ArangoDB.Builder()
  .host("host1", 8529)
  .host("host2", 8529)
  .build();
```

The driver is also able to acquire a list of known hosts in a cluster. For this the driver has
to be able to successfully open a connection to at least one host to get the
list of hosts. Then it can use this list when fallback is needed. To enable this
feature:

```java
ArangoDB arangoDB = new ArangoDB.Builder()
  .acquireHostList(true)
  .build();
```


## Load Balancing

The driver supports load balancing for cluster setups in
two different ways.

The first one is a round robin load balancing where the driver iterates
through a list of known hosts and performs every request on a different
host than the request before.

```java
ArangoDB arangoDB = new ArangoDB.Builder()
  .loadBalancingStrategy(LoadBalancingStrategy.ROUND_ROBIN)
  .build();
```

The second load balancing strategy picks a random host from host list
(configured or acquired) and sticks to it as long as the
connection is open.

```java
ArangoDB arangoDB = new ArangoDB.Builder()
  .loadBalancingStrategy(LoadBalancingStrategy.ONE_RANDOM)
  .build();
```


## Active Failover

In case of an _Active Failover_ deployment the driver should be configured in
the following way:
- the load balancing strategy must be either set to `LoadBalancingStrategy.NONE` (default)
- `acquireHostList` should be set to `true`

```java
ArangoDB arangoDB = new ArangoDB.Builder()
  .loadBalancingStrategy(LoadBalancingStrategy.NONE)
  .acquireHostList(true)
  .build();
```


## Connection time to live

The driver supports setting a TTL (time to life) for connections:

```java
ArangoDB arango = new ArangoDB.Builder()
  .connectionTtl(5 * 60 * 1000) // ms
  .build();
```

In this example all connections will be closed/reopened after 5 minutes.

If not set or set to `null` (default), no automatic connection closure will be performed.



## VST Keep-Alive

The driver supports setting keep-alive interval (in seconds)
for VST connections. If set, every VST connection will perform a no-op request
at the specified intervals, to avoid to be closed due to inactivity by the
server (or by the external environment, e.g. firewall, intermediate routers,
operating system, ... ).

```java
ArangoDB arangoDB = new ArangoDB.Builder()
  .keepAliveInterval(1_800) // 30 minutes
  .build();
```

If not set or set to `null` (default), no keep-alive probes will be sent.
