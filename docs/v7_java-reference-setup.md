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
- `useProtocol(Protocol)`:       communication protocol, possible values are: `VST`, `HTTP_JSON`, `HTTP_VPACK`, `HTTP2_JSON`, `HTTP2_VPACK`, (default: `HTTP2_JSON`)
- `timeout(Integer)`:            connection and request timeout (ms), (default `0`, no timeout)
- `user(String)`:                username for authentication, (default: `root`)
- `password(String)`:            password for authentication
- `jwt(String)`:                 JWT for authentication
- `useSsl(Boolean)`:             use SSL connection, (default: `false`)
- `sslContext(SSLContext)`:      SSL context
- `verifyHost(Boolean)`:         enable hostname verification, (HTTP only, default: `true`)
- `chunkSize(Integer)`: `VST`    chunk size in bytes, (default: `30000`)
- `maxConnections(Integer)`:     max number of connections per host, (default: 1 VST, 1 HTTP/2, 20 HTTP/1.1)
- `connectionTtl(Long)`:         max lifetime of a connection (ms)
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



## Network protocol

The drivers default used network protocol is the binary protocol VelocyStream
which offers the best performance within the driver. To use HTTP, you have to
set the configuration `useProtocol` to `Protocol.HTTP_JSON` for HTTP with JSON
content or `Protocol.HTTP_VPACK` for HTTP with
[VelocyPack](https://github.com/arangodb/velocypack/blob/master/VelocyPack.md){:target="_blank"} content.

```java
ArangoDB arangoDB = new ArangoDB.Builder()
  .useProtocol(Protocol.VST)
  .build();
```

In addition to set the configuration for HTTP you have to add the
apache httpclient to your classpath.

```xml
<dependency>
  <groupId>org.apache.httpcomponents</groupId>
  <artifactId>httpclient</artifactId>
  <version>4.5.1</version>
</dependency>
```

**Note**: If you are using ArangoDB 3.0.x you have to set the protocol to
`Protocol.HTTP_JSON` because it is the only one supported.

## SSL

To use SSL, you have to set the configuration `useSsl` to `true` and set a `SSLContext`
(see [example code](https://github.com/arangodb/arangodb-java-driver/blob/master/src/test/java/com/arangodb/example/ssl/SslExample.java){:target="_blank"}).

```java
ArangoDB arangoDB = new ArangoDB.Builder()
  .useSsl(true)
  .sslContext(sc)
  .build();
```

No additional configuration is required to use TLSv1.3 (if available on the
server side), but a JVM that supports it is required (OpenJDK 11 or later, or
distributions of Java 8 with TLSv1.3 support).

## Connection Pooling

The driver supports connection pooling for VelocyStream with a default of 1 and
HTTP with a default of 20 maximum connections per host. To change this value
use the method `maxConnections(Integer)` in `ArangoDB.Builder`.

```java
ArangoDB arangoDB = new ArangoDB.Builder()
  .maxConnections(8)
  .build();
```

The driver does not explicitly release connections. To avoid exhaustion of
resources when no connection is needed, you can clear the connection pool
(close all connections to the server) or use [connection TTL](#connection-time-to-live).

```java
arangoDB.shutdown();
```

{% hint 'info' %}
Opening and closing connections very frequently can exhaust the amount of
connections allowed by the operating system. TCP connections enter a special
state `WAIT_TIME` after close, and typically remain in this state for two
minutes (maximum segment life * 2). These connections count towards the global
limit, which depends on the operating system but is usually around 28,000.
Connections should thus be reused as much as possible.

You may run into this problem if you bypass the driver's safe guards by
setting a very high connection limit or by using multiple ArangoDB objects
and thus pools.
{% endhint %}


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

Since version 4.3 the driver support acquiring a list of known hosts in a
cluster setup or a single server setup with followers. For this the driver has
to be able to successfully open a connection to at least one host to get the
list of hosts. Then it can use this list when fallback is needed. To use this
feature just pass `true` to the method `acquireHostList(boolean)`.

```java
ArangoDB arangoDB = new ArangoDB.Builder()
  .acquireHostList(true)
  .build();
```

## Load Balancing

Since version 4.3 the driver supports load balancing for cluster setups in
two different ways.

The first one is a round robin load balancing where the driver iterates
through a list of known hosts and performs every request on a different
host than the request before.

```java
ArangoDB arangoDB = new ArangoDB.Builder()
  .loadBalancingStrategy(LoadBalancingStrategy.ROUND_ROBIN)
  .build();
```

Just like the Fallback hosts feature the round robin load balancing strategy
can use the `acquireHostList` configuration to acquire a list of all known hosts
in the cluster. Do so only requires the manually configuration of only one host.
Because this list is updated frequently it makes load balancing over the whole
cluster very comfortable.

```java
ArangoDB arangoDB = new ArangoDB.Builder()
  .loadBalancingStrategy(LoadBalancingStrategy.ROUND_ROBIN)
  .acquireHostList(true)
  .build();
```

The second load balancing strategy allows to pick a random host from the
configured or acquired list of hosts and sticks to that host as long as the
connection is open. This strategy is useful for an application - using the driver -
which provides a session management where each session has its own instance of
`ArangoDB` build from a global configured list of hosts. In this case it could
be wanted that every sessions sticks with all its requests to the same host but
not all sessions should use the same host. This load balancing strategy also
works together with `acquireHostList`.

```java
ArangoDB arangoDB = new ArangoDB.Builder()
  .loadBalancingStrategy(LoadBalancingStrategy.ONE_RANDOM)
  .acquireHostList(true)
  .build();
```

## Active Failover

In case of an _Active Failover_ deployment the driver should be configured in
the following way:
- the load balancing strategy must be either set to `LoadBalancingStrategy.NONE`
  or not set at all, since that would be the default
- `acquireHostList` should be set to `true`

```java
ArangoDB arangoDB = new ArangoDB.Builder()
  .loadBalancingStrategy(LoadBalancingStrategy.NONE)
  .acquireHostList(true)
  .build();
```

## Connection time to live

Since version 4.4 the driver supports setting a TTL (time to life) in milliseconds
for connections managed by the internal connection pool.

```java
ArangoDB arango = new ArangoDB.Builder()
  .connectionTtl(5 * 60 * 1000)
  .build();
```

In this example all connections will be closed/reopened after 5 minutes.

Connection TTL can be disabled setting it to `null`:

```java
.connectionTtl(null)
```

The default TTL is `null` (no automatic connection closure).


## VST Keep-Alive

Since version 6.8 the driver supports setting keep-alive interval (in seconds)
for VST connections. If set, every VST connection will perform a no-op request
at the specified intervals, to avoid to be closed due to inactivity by the
server (or by the external environment, e.g. firewall, intermediate routers,
operating system, ... ).

This option can be set using the key `arangodb.connections.keepAlive.interval`
in the properties file or programmatically from the driver builder:

```java
ArangoDB arangoDB = new ArangoDB.Builder()
  .keepAliveInterval(1800) // 30 minutes
  .build();
```

If not set or set to `null` (default), no keep-alive probes will be sent.
