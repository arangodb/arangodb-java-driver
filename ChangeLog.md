# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [7.21.0] - 2025-07-23

- added SSL configuration properties (DE-1010, #611)
- fixed support to Jackson `2.19`

## [7.20.0] - 2025-06-17

- added option `usePlanCache` to `AqlQueryOptions` (DE-973, #609)
- updated Jackson version to `2.19` (DE-1012, #607)

## [7.19.0] - 2025-05-28

- fixed connection pool load-balancing (DE-1016, #602), now the connection pool:
  - keeps track of busy connections (or busy HTTP/2 streams)
  - enqueues new requests only to connections that are not busy (or that have available HTTP/2 streams)
  - waits asynchronously if all the connections are busy (or all HTTP/2 streams are busy)
- added new option to configure HTTP/1.1 pipelining (`com.arangodb.ArangoDB.Builder.pipelining(Boolean)`), 
  `false` by default 
- changed default configuration HTTP/1.1 pipelining to `false`

## [7.18.0] - 2025-05-06

- updated `jackson-dataformat-velocypack` to version `4.6.0`
- exposed configuration properties keys in `ArangoConfigProperties`
- deprecated `CollectionStatus`
- fixed `equals()` and `hashCode()` in some entity classes

## [7.17.1] - 2025-03-27

- implemented `equals()` and `hashCode()` for all entity classes
- fixed overlapping resources in shaded package

## [7.17.0] - 2025-01-27

- allow construct ArangoConfigProperties from `java.util.Properties` (DE-976)
- made BaseDocument and BaseEdgeDocument serializable (#596)

## [7.16.0] - 2025-01-09

- improved deserialization of `RawBytes` and `RawJson` (#592, DE-969)
- added support to Jakarta JSON-P data types (#593, DE-968)
- fixed ArangoSearch `PrimarySort` serialization

## [7.15.0] - 2024-12-10

- added missing collection options (#590, DE-961) 
- improved serde performances (#588, DE-959)

## [7.14.0] - 2024-12-06

- support all AQL query options in `ArangoDatabase.explainAqlQuery()` (#589, ES-2266)

## [7.13.1] - 2024-11-29

- tolerate error responses with text content-type (#587, DE-960)

## [7.13.0] - 2024-11-15

- improved serialization and deserialization of `RawBytes` and `RawJson` (#586)

## [7.12.0] - 2024-11-07

- added new method `ArangoDatabase.explainAqlQuery()`, supporting arbitrary JSON-like response data
- deprecated `ArangoDatabase.explainQuery()`

## [7.11.0] - 2024-10-31

- added support to HTTP proxies (#584, DE-930)

## [7.10.0] - 2024-10-22

- udpated Jackson to version `2.18` (#581, DE-877)
- added missing statistics to `CursorStats` (#580, DE-876)
- fixed type of `AqlExecutionExplainEntity.warnings` (#579, DE-886)

## [7.9.0] - 2024-09-20

- updated `velocypack` to version `3.1.0`
- updated `jackson-dataformat-velocypack` to version `4.4.0`
- added `SHADED` flag in `PackageVersion` class (#576)
- added `serdeProviderClass` configuration property (#575, DE-837)
- added `skipFastLockRound` parameter to StreamTransactionOptions (#574, DE-832)
- added support to reset log levels (#573, DE-831)
- added `legacy` option to `GeoJSONAnalyzerProperties` (#572, DE-736)
- support resuming AQL cursor in transaction (#571, DE-592)
- fíxed `HostHandler` concurrency (DE-663)
- fíxed `ConnectionPoolImpl` concurrency (#570, DE-536)

## [7.8.0] - 2024-09-02

- added property `ignoreRevs` to DocumentDeleteOptions (#567, DE-844)

## [7.7.1] - 2024-06-12

- fixed deserialization of responses with no content (#560)

## [7.7.0] - 2024-06-07

- added configuration option to set Vert.x instance (#558, DE-535)
- added overloaded variant of `ArangoSerde#deserialize()` accepting `RequestContext` parameter (#555, #554, DE-771) 
- updated `jackson-dataformat-velocypack` to version `4.3.0`
- fixed support to Jackson 2.17
- fixed native image build for GraalVM 22

## [7.6.0] - 2024-03-22

- added support to external versioning (ArangoDB 3.12, #547)
- added support to `wildcard` analyzer (ArangoDB 3.12, #546)
- added support to `multi_delimiter` analyzer (ArangoDB 3.12, #545)
- added support to multi dimensional indexes (ArangoDB 3.12, #544)
- added support to WAND optimization (ArangoDB 3.12, #543)
- added support to content compression (ArangoDB 3.12, #535)
- fixed ALPN with H2 (DE-792, #551)
- tolerate SPI ServiceConfigurationError (DE-793, #552)
- added support to Jackson 2.17
- changed default TTL to 30 seconds for HTTP connections (DE-794, #553)

## [7.5.1] - 2024-01-24

- fixed inclusion of transitive dependency on `com.tngtech.archunit:archunit-junit5`


## [7.5.0] - 2024-01-23

- updated Vert.x to version 4.5 (#532)
- automatically configure Jackson stream constraints (DE-762, #537)
- fixed closing AQL cursor twice (#533)


## [7.4.0] - 2023-12-20

### Added

- added new methods to remove graph definitions and vertex collections, to align the naming with the documentation (DE-729)
- added support to Jackson 2.16 (DE-735)

### Changed

- deprecated ArangoDB.Builder.asyncExecutor() (DE-726)
- retry requests on response code 503 (DE-55, #530)
- changed `ArangoCursor#close()` and `ArangoCursorAsync#close()` to be idempotent (DE-727, #528)
- changed default Jackson dependencies versions to 2.16 (DE-735)

### Fixed
 
- fixed exception handling on sending HTTP requests
- fixed management of hosts marked for deletion (DE-723, #384)
- fixed VST resilience (#529, DE-725)
- fixed failover with round-robin load balancing (DE-724)
- fixed init cause of `ArangoDBException` 


## [7.3.0] - 2023-11-22

- changed types of documents and errors in `com.arangodb.entity.MultiDocumentEntity` to `java.util.List`

## [7.2.0] - 2023-11-02

- added asynchronous API, accessible via `ArangoDB.async()` (DE-496, #523)
- added configuration option to specify the asynchronous downstream executor via `ArangoDB.Builder#asyncExecutor(Executor)` (DE-697)
- added missing asynchronous API to ensure parity with synchronous API
- changed behavior for acquiring the host list to be asynchronous (#521)
- changed internal communication to be asynchronous
- fixed swallowed exceptions in `ArangoCollection.getDocument()`, `ArangoCollection#documentExists()`, `ArangoCollection#exists()`, `ArangoEdgeCollection.getEdge()` and `ArangoVertexCollection#getVertex()`
- fixed `ArangoCursorAsync` API to be fully asynchronous (#433, #520)
- fixed interference of Jackson annotations with other Jackson instances (DE-636, #513)
- fixed nested properties deserialization in `BaseDocument` (#517)

## [7.1.0] - 2023-05-26

- added support to Jackson 2.15
- changed default Jackson dependencies versions to 2.15
- updated transitive dependencies versions
- addeded `peakMemoryUsage` attribute to running and slow queries (ArangoDB 3.11, #507)
- added support for retriable batch results (ArangoDB 3.11, #505)
- added support for ArangoSearch WAND optimization (ArangoDB 3.11, #503)
- added support for cloneable AqlQueryOptions (#510)
- added support for `geo_s2` analyzer (#501)
- added support for serverId query parameter for `/_admin/log/level` (#498)
- added support for peakMemoryUsage and executionTime explain stats (#496)
- added support for Index cache refilling (#494)
- added support for ArangoSearch column cache (#492)

## [7.0.0] - 2023-04-20

Detailed changes documentation is available [here](https://github.com/arangodb/docs/blob/main/drivers/java-changes-v7.md).

### Added

- added `ArangoDB.Builder.loadProperties(ArangoConfigProperties)` to register custom configuration suppliers
- added `ArangoConfigProperties.fromFile()` to load properties from local files
- added support to `HTTP/2` communication protocol
- added optional transitive dependency on `io.vertx:vertx-web-client` (can be excluded if using VST only)
- added transitive dependency on Jackson Core, Databind and Annotations
- added wrapper class for raw JSON content (`RawJson`)
- added wrapper class for content already encoded as byte array (`RawBytes`)
- added support for Jackson types (`JsonNode`, `ArrayNode`, `ObjectNode`, ...)
- added support for Jackson annotations in data types
- added new user data custom serializer API based on `ArangoSerde`
- added new user data custom serializer implementation based on Jackson (`JacksonSerde`), supporting both `JSON` and `VPACK`
- added methods and parameters targets to meta binding annotations
- added overloaded methods for CRUD operations allowing specifying the return type
- added API to support CRUD operations from raw data (`RawBytes` and `RawJson`) containing multiple documents
- added `BaseDocument#removeAttribute(String)` and `BaseEdgeDocument#removeAttribute(String)`
- added request id to `ArangoDBException`
- shaded version of the driver (`com.arangodb:arangodb-java-driver-shaded`)
- added `ArangoEdgeCollection.drop()` and `ArangoVertexCollection.drop(VertexCollectionDropOptions)`

### Fixed

- removed `--allow-incomplete-classpath` from native image configuration (#397)
- ability to control whether `null` values are included in the serialization (#389)
- added support to `DocumentCreateOptions#keepNull` (#374)
- allow specifying the return type on insertDocuments (#373)
- credentials logging (#410)
- fixed `ArangoCollection.rename()` and `ArangoView.rename()` thread safety

### Changed

- configuration properties from local files are not loaded automatically anymore
- `ArangoDB.execute()` accepts now target deserialization type
- `Request<T>` and `Response<T>` support now generic body type
- removed default host configuration (`127.0.0.1:8529`)
- changed http client library to Vert.x WebClient
- changed default communication protocol from `VST` to `HTTP/2`
- changed default content-type format from `VPACK` to `JSON`
- changed internal serialization, now based on Jackson API
- `VPACK` support is now provided by `JacksonSerde` including the optional dependency
  `com.arangodb:jackson-dataformat-velocypack` (`VPACK` dataformat backend for Jackson)
- data objects passed as arguments to API methods are treated as immutable and the related metadata fields are not
  updated in place anymore (updated metadata can be found in the returned object)
- changed some API signatures which were using unnecessary generics from `ArangoCollection`, `ArangoVertexCollection` and `ArangoEdgeCollection`
- changed `ArangoCursor#getStats()` return type
- replication factor is now represented by a new interface (`ReplicationFactor`) with
  implementations: `NumericReplicationFactor` and `SatelliteReplicationFactor`
- all data definition classes are now `final` (packages `com.arangodb.entity` and `com.arangodb.model`)
- `BaseDocument` and `BaseEdgeDocument` are now `final`
- `BaseDocument#getProperties()` and `BaseEdgeDocument#getProperties()` return now an unmodifiable map
- `BaseDocument` and `BaseEdgeDocument` are not serializable anymore (using Java serialization)
- removed `throws ArangoDBException` from API method signatures (unchecked exception)
- removed passwords from debug level requests logs (#410)
- JPMS: explicit automatic module name
- updated `ArangoGraph.replaceEdgeDefinition()`
- CRUD methods to insert and replace multiple documents have now covariant argument types
- changed order of arguments in `ArangoDatabase.query()` overloads
- `ArangoCollection.rename()` and `ArangoView.rename()` do not change the collection or view name of the API class instance

### Removed

- removed user data custom serializer API based on `ArangoSerialization` (in favor of `ArangoSerde`)
- removed user data custom serializer implementation `ArangoJack` (in favor of `JacksonSerde`)
- removed support for interpreting raw strings as JSON (in favor of `RawJson`)
- removed support of data type `VPackSlice` (in favor of Jackson types: `JsonNode`, `ArrayNode`, `ObjectNode`, ...)
- removed client APIs already deprecated in Java Driver version `6`
- removed deprecated server APIs:
  - `MMFiles` related APIs
  - `ArangoDatabase.executeTraversal()`
  - `ArangoDB.getLogs()`
  - `minReplicationFactor` in collections and graphs
  - `overwrite` flag in `DocumentCreateOptions`
  - `hash` and `skipList` indexes
- removed `ArangoCursorInitializer`
- removed Asynchronous API (`com.arangodb.async`)
- removed `ArangoDatabase.getDocument()`
- removed automatic type inference in CRUD methods operating on multiple documents
- removed `DbName` in favor of plain strings

## [6.23.0] - 2023-04-20

- deprecated `DbName` in favor of plain strings

## [6.22.0] - 2023-04-18

- added support to `forceOneShardAttributeValue` query parameter (DE-541)

## [6.21.0] - 2023-03-07

- added `x-arango-driver` header (DE-479)

## [6.20.0] - 2022-11-29

- ArangoSearch cache (#472)
- support for `enterprise-hex-smart-vertex` shardingStrategy
- deprecated `com.arangodb.Function`

## [6.19.0] - 2022-10-04

- added support for `search-alias` views (ArangoDB 3.10 #461)
- added support for nested search (ArangoDB 3.10, #460)
- added support for `classification`, `nearest_neighbors` and `minhash` search analyzers (ArangoDB 3.10, #458)
- added support for inverted indexes (ArangoDB 3.10, #457)
- added support for cluster dirty reads (ArangoDB 3.10, #455)
- added support for index stored values (ArangoDB 3.10)
- added support for geo index legacy polygons (ArangoDB 3.10)
- added support for getting query optimizer rules (ArangoDB 3.10)
- added support for enhanced cursor stats (ArangoDB 3.10)
- added support for computed values (ArangoDB 3.10)
- added support for index cache (ArangoDB 3.10)
- deprecated fulltext indexes (ArangoDB 3.10, #454)
- fixed `ConsolidationPolicy` API
- deprecated MMFiles collection attributes (#442)
- deprecated for removal `ArangoCursorInitializer` and `GraphDocumentReadOptions#isCatchException()`
- documented thead safe classes (#445)

## [6.18.0] - 2022-06-07

- deprecated usage of deprecated server API (#440)
- introduced new field entity annotations: `@Id`, `@Key`, `@Rev`, `@From`, `@To` (#439)
- deprecated VPack serialization in favor of Jackson API
- added `deduplicate` option in `PersistentIndex` (#437)

## [6.17.0] - 2022-05-17

- updated dependencies
- fixed IndexOutOfBoundsException in RoundRobinHostHandler (#435)
- warn on json request payload size too big (#434)
- fixed various serialization bugs in native image (#425)

## [6.16.1] - 2022-02-23

- fixed retry behavior of HTTP connections in case of timeout exceptions (#429)
- fixed NPE when serializing ArangoSearch properties (#427)

## [6.16.0] - 2022-01-27

- deprecated hash and skiplist indexes (#424)
- fixed active failover concurrency bug (#423)
- added support for overload metrics (ArangoDB 3.9, #419)
- added support for Segmentation and Collation ArangoSearch analyzers (ArangoDB 3.9, #418)
- added support for ZKD indexes (ArangoDB 3.9, #417)
- added `all` log topic (ArangoDB 3.9, #416)
- added support for Hybrid SmartGraphs (ArangoDB 3.9, #415)
- added support for database unicode names, added `DbName` class to represent database names in public API parameters to
  ease unicode names normalization (ArangoDB 3.9, #405)

## [6.15.0] - 2021-12-29

- JWT authentication (#421)
- fixed swallowing connection exceptions (#420) 
- fixed `stopwords` analyzer (#414)
- set max retries for active failover redirects (#412)
- fixed deserializing `null` value as String (#411)

## [6.14.0] - 2021-10-01

- fixed issues with non-English locales (#407)
- implemented support for `GET /_admin/server/id`
- fixed `acquireHostListInterval` javadoc, interval in milliseconds
- fixed NPE in `CursorEntity.extra` and `CursorEntity.Extras.stats`

## [6.13.0] - 2021-08-25

- added support for `fillBlockCache` in AQL query options (ArangoDB v3.8.1)
- fixed exceptions handling during shutdown (#400)
- added native image configuration for HTTP communication
- updated native image reflection configuration

## [6.12.3] - 2021-06-24

- fixed host handler failures count (#DEVSUP-805, #398)

## [6.12.2] - 2021-06-17

- added missing enum value `QueryExecutionState.KILLED` (#391)
- fixed `acquireHostList` to loadBalancer or hostname alias (#385)

**WARNING**: The implementation of Stopwords analyzer is not final in ArangoDB 3.8.0, so using it might result in unpredictable behavior.
This will be fixed in ArangoDB 3.8.1 and will have a different API.
Any usage of the current Java driver API related to it is therefore discouraged.

## [6.12.1] - 2021-04-28

- fixed request timeout in async driver (#ES-837)

## [6.12.0] - 2021-04-28

- added support for modifying collection schema

## [6.11.1] - 2021-04-23

- velocypack v2.5.3

## [6.11.0] - 2021-04-21

- added support for getting db log entries via `GET /_admin/log/entries` (ArangoDB v3.8)
- added support for index estimates (ArangoDB v3.8)
- added support for ArangoSearch `AQL`, `Pipeline`, `Stopwords`, `GeoJSON`, `GeoPoint` analyzers (ArangoDB v3.8)
- fixed active failover behavior for the asynchronous driver (#381)
- deprecated `ArangoIterable` methods in favour of Java 8 Stream equivalents (#382)

## [6.10.0] - 2021-03-27

- closing VST connection after 3 consecutive keepAlive failures (#ES-837)

## [6.9.1] - 2021-03-23

- fixed `acquireHostList` in asynchronous driver (#377)
- fixed exception swallowing in `ArangoDatabaseAsync#exists`
- fixed performance issue when consuming big AQL cursor batches in stream mode (arangodb/arangodb#13476) 

## [6.9.0] - 2021-02-04

- added `com.arangodb.mapping.ArangoJack` to replace `com.arangodb.jackson.dataformat.velocypack.VelocyJack` (from 
  `com.arangodb:jackson-dataformat-velocypack`)

- fixed removing removed coordinators from the hostlist (#347)

## [6.8.2] - 2021-01-25

- fixed closing connection on failed authentication (#ES-772)

## [6.8.1] - 2020-12-22

- fixed ignoring internal endpoints in acquireHostList (#DEVSUP-673)

## [6.8.0] - 2020-12-10

- added configurable VST keep-alive

## [6.7.5] - 2020-09-22

- allow customizing httpRequestRetryHandler

## [6.7.4] - 2020-09-03

- fixed path escaping in `ArangoDatabase.route()`
- added ssl hostname verifier to ArangoDB builder

## [6.7.3] - 2020-08-14

- added `users` field to `DBCreateOptions`
- velocypack v2.4.1

## [6.7.2] - 2020-07-29

- velocypack v2.4.0

## [6.7.1] - 2020-07-07

- fixed VST communication adding `accept` and `content-type` headers to every message
- fixed missing classes in GraalVM native image reflection configuration

## [6.7.0] - 2020-07-01

- added support of schema validation (ArangoDB v3.7)
- added support of `overwriteMode` on document creation, to allow `insert-ignore`, `insert-replace` and `insert-update` (ArangoDB v3.7)
- added support of `mergeObjects` for insert document with `overwriteMode: update` (ArangoDB v3.7)
- added support of `storedValues` in `ArangoSearchProperties` (ArangoDB v3.7)
- added support of `primarySortCompression` in `ArangoSearchProperties` (ArangoDB v3.7)
- added support of `DisjointSmartGraphs` and `SatelliteGraphs` (ArangoDB v3.7)
- added support of `SatelliteGraphs` support (ArangoDB v3.7)
- allow specifying return type on document update
- added `peakMemoryUsage` to aql statistics

## [6.7.0_PREVIEW_3.7.1-alpha.1] - 2020-05-22

- added support of `DisjointSmartGraphs` and `SatelliteGraphs` (ArangoDB v3.7)
- added support of `storedValues` in `ArangoSearchProperties` (ArangoDB v3.7)
- added support of `primarySortCompression` in `ArangoSearchProperties` (ArangoDB v3.7)
- added support of `overwriteMode` on document creation, to allow `insert-ignore`, `insert-replace` and `insert-update` (ArangoDB v3.7)
- added support of `mergeObjects` for insert document with `overwriteMode: update` (ArangoDB v3.7)
- velocypack v2.3.1

## [6.6.3] - 2020-05-06

- velocypack v2.3.1

## [6.6.2] - 2020-04-07

- bugfix VelocyJack deserialization
- bugfix `allowImplicit` parameter in stream transactions

## [6.7.0_PREVIEW_3.7.0-alpha.2_0] - 2020-03-24

- added `overwriteMode` parameter to support insert-update (ArangoDB v3.7)
- satellite graphs support (ArangoDB v3.7)
- schema validation (ArangoDB v3.7)
- added `peakMemoryUsage` to aql statistics

## [6.6.1] - 2020-03-18

- GraalVM Native Image support
- fixed acquire host list (ArangoDB v3.7)

## [6.6.0] - 2020-02-03

- typed ArangoSearch analyzers
- updated dependecies
- bugfix asynchronous shutdown

## [6.5.0] - 2019-12-23

- createDatabase with options (replicationFactor, minReplicationFactor, sharding) (ArangoDB v3.6)
- extended DatabaseEntity with replicationFactor, minReplicationFactor, sharding (ArangoDB v3.6)
- timeout option for AQL queries (ArangoDB v3.6)
- enhancedNgramAnalyzer and enhancedTextAnalyzer (ArangoDB v3.6)
- velocypack v2.1.0

## [6.4.1] - 2019-10-23

- jackson v2.9.10

## [6.4.0] - 2019-10-09

### Added

- Stream Transactions support for graph APIs

### Fixed

- `catchExceptions` option in async `getEdge` and `getVertex`

## [6.3.0] - 2019-09-16

### Added

- support for keyType uuid & padded

### Fixed

- bugfix AqlExecutionExplainEntity indexes
- bugfix reconnection after more than 3 failures

## [6.2.0] - 2019-09-05

- merged async driver
- bugfix method chaining in IndexOptions

## [6.1.0] - 2019-08-29

### Added

- updated maven dependencies

### Fixed

- custom serde not always used
- `documentExists()` and `getDocument` behaviour on non existing `transactionId`

## [6.0.0] - 2019-08-20

### Added

- split `GraphDocumentReadOptions` from `DocumentReadOptions` (breaking change)
- added `ArangoCollection#getResponsibleShard(Object)`
- added support for Analyzers
- added support for Stream Transactions
- added support for named indices
- added support for TTL indices
- added minReplicationAttribute for collections and graphs

## [5.0.7] - 2019-07-19

### Fixed

- properly all load all configuration defaults

### Added

- added acquireHostListInterval configuration parameter

## [5.0.6] - 2019-05-24

### Added

- requests are now storing header information
- faster test code execution

## [5.0.5] - 2019-05-24

### Fixed

- host handling (issue #241)
- logging extended hostresolver

### Added

- add arangodb.httpCookieSpec
- added smartJoinAttribute and shardingStrategy collection attributes

## [5.0.4] - 2019-18-01

### Fixed

- fixed bug with multi document operations when using parameter `silent` (issue #241)

## [5.0.3] - 2018-11-12

### Fixed

- adaption to changed ArangoSearch API

## [5.0.2] - 2018-11-09

### Added

- added `ArangoGraph#drop(boolean dropCollections)`

### Changed

- changed `ArangoDB#timeout` to also set the request timeout when using VelocyStream (issue #230)

### Fixed

- fixed compatibility of `ArangoCursor#filter` with Java 6
- fixed replace-insert with `DocumentCreateOptions#overwrite(Boolean)` for `ArangoCollection#insertDocuments`
- removed unused dependency

## [5.0.1] - 2018-09-25

### Fixed

- fixed `ArangoCursor#next` when performing a dirty read
- fixed connection stickiness

## [5.0.0] - 2018-09-18

### Added

- added dirty read support ([reading from followers](https://docs.arangodb.com/stable/deploy/active-failover/administration/#reading-from-follower))

  - added option `AqlQueryOptions#allowDirtyRead` for `ArangoDatabase#query`.
  - added option `DocumentReadOptions#allowDirtyRead` for `ArangoCollection#getDocument`
  - added option `DocumentReadOptions#allowDirtyRead` for `ArangoCollection#getDocuments`
  - added option `DocumentReadOptions#allowDirtyRead` for `ArangoVertexCollection#getVertex`
  - added option `DocumentReadOptions#allowDirtyRead` for `ArangoEdgeCollection#getEdge`

### Changed

- changed the internal connection pool and host management. There now exists a connection pool for every configured host. This changes the behavior of `ArangoDB.Builder#maxConnections` which now allows to configure the maximal number of connection per host and not overall.
- changed `IndexEntity#selectivityEstimate` from `Integer` to `Double`
- upgraded dependency velocypack 1.4.1

  - added support for generic types

    Serialize the class name in a field \_class when necessary. Field name can be configured through VPack.Builder#typeKey(String)

## [4.7.3] - 2018-09-03

### Changed

- made `AqlQueryOptions#Options` serializable

## [4.7.2] - 2018-09-03

### Changed

- made `AqlQueryOptions` serializable

## [4.7.1] - 2018-09-03

### Fixed

- applied arangosearch API changes for ArangoDB 3.4.0
- fixed `ArangoCursor#close()`: check hasNext before close (issue #223)

## [4.7.0] - 2018-08-02

### Added

- added View support
  - added `ArangoDatabase#view(String): ArangoView`
  - added `ArangoDatabase#getViews(): Collection<ViewEntity>`
  - added `ArangoView`
- added arangosearch support
  - added `ArangoDatabase#arangoSearch(String): ArangoSearch`
  - added `ArangoSearch`
- added `ArangoCursor#first()`
- added `java.util.stream.Stream` like methods for `ArangoCursor`
  - added `ArangoCursor#foreach(Consumer)`
  - added `ArangoCursor#map(Function)`
  - added `ArangoCursor#filter(Predicate)`
  - added `ArangoCursor#anyMatch(Predicate)`
  - added `ArangoCursor#allMatch(Predicate)`
  - added `ArangoCursor#noneMatch(Predicate)`
  - added `ArangoCursor#collectInto(Collection)`
- added interface `Entity` for entities in `com.arangodb.entity`

### Changed

- upgraded dependency velocypack 1.3.0
  - `VPackDeserializationContext#deserialize(VPackSlice, Class)` to `VPackDeserializationContext#deserialize(VPackSlice, java.lang.reflect.Type)`

## [4.6.1] - 2018-07-12

### Added

- added convenience method `ArangoDatabase#query(String, Class)`
- added convenience method `ArangoDatabase#query(String, Map<String, Object>, Class)`
- added convenience method `ArangoDatabase#query(String, AqlQueryOptions, Class)`

### Fixed

- fixed `ArangoCollection#rename(String)`

  Change field `name` in `ArangoCollection` after rename so that future requests through the instance will be made with the new collection name.

- fixed missing `ArangoDatabase.util() : ArangoSerialization`
- fixed missing `ArangoCollection.util() : ArangoSerialization`
- fixed missing `ArangoGraph.util() : ArangoSerialization`
- fixed missing `ArangoVertexCollection.util() : ArangoSerialization`
- fixed missing `ArangoEdgeCollection.util() : ArangoSerialization`

## [4.6.0] - 2018-07-02

### Added

- added convenience methods for arbitrary requests
  - added `ArangoDatabase.route(String...)`
- added `DocumentCreateOptions#silent(Boolean)`
- added `DocumentReplaceOptions#silent(Boolean)`
- added `DocumentUpdateOptions#silent(Boolean)`
- added `DocumentDeleteOptions#silent(Boolean)`
- added support for exclusive write operations (issue #190)
  - added `TransactionOptions#exclusiveCollections(String[])`

### Removed

- removed unnecessary deserializer for internal `_id` field

### Fixed

- fixed serializing of documents/edges: use custom serializer

## [4.5.2] - 2018-06-25

### Added

- added support for custom serializer
  - added `ArangoDB.Builder#serializer(ArangoSerialization)`
  - added link to jackson-dataformat-velocypack in docs

## [4.5.1] - 2018-06-21

### Fixed

- fixed `exists()` method in `ArangoDatabase`, `ArangoCollection`, `ArangoGraph`: check for ArangoDB error num
- fixed `ArangoDB#aquireHostList(true)` with authentication

## [4.5.0] - 2018-06-11

### Added

- added replace-insert support: `DocumentCreateOptions#overwrite(Boolean)`
- added support for satellite collections: `CollectionCreateOptions#satellite(Boolean)`
- added `AqlQueryOptions#stream(boolean)` for Streaming AQL Cursors
- added `ArangoDatabase#create()`
- added `ArangoCollection#create()`
- added `ArangoCollection#create(CollectionCreateOptions)`
- added `ArangoGraph#create(Collection<EdgeDefinition>)`
- added `ArangoGraph#create(Collection<EdgeDefinition>, GraphCreateOptions)`
- added return type for `ArangoDatabase#deleteAqlFunction()`
- added field `AqlFunctionEntity#isDeterministic`

### Changed

- upgraded dependency velocypack 1.2.0
  - replaced dependency json-simple with jackson
- extracted interfaces for ArangoDB API

### Removed

- removed deprecated `ArangoDB.Builder#host(String)`
- removed deprecated `ArangoDB.Builder#port(Integer)`
- removed deprecated `ArangoCollection#create[IndexType]Index()`
- removed deprecated `ArangoDatabase#updateUserDefaultCollectionAccess()`
- removed deprecated `ArangoDB#updateUserDefaultDatabaseAccess()`
- removed deprecated `ArangoDB#updateUserDefaultCollectionAccess()`
- removed several deprecated APIs

## Fixed

- fixed `aquireHostList` bug when using active failover

## [4.4.1] - 2018-06-04

### Fixed

- fixed concurrency bug in VST when using connectionTtl

## [4.4.0] - 2018-04-19

### Changed

- changed dependency com.arangodb:velocypack to 1.1.0
  - fixed DateUtil does incorrect conversion of UTC time
  - serialize `BigInteger`/`BigDecimal` as `String`

### Fixed

- fixed reconnecting after ArangoDB restarts (issue #186)
- fixed `ArangoCollection#updateDocuments()` ignoring `DocumentUpdateOptions#serializeNull` (issue #180)

## [4.3.7] - 2018-04-17

### Fixed

- fixed property loading

## [4.3.6] - 2018-04-16

### Added

- added `ArangoDB.Builder#maxConnectionTtl(Integer)` (Issue #141, #186)

## [4.3.5] - 2018-04-11

### Fixed

- fixed compatibility for `ArangoDatabase#getAqlFunctions()` for ArangoDB 3.4
- fixed internal exception handling in VST connection

## [4.3.4] - 2018-03-21

### Changed

- made `ErrorEntity` serializable (Issue #178)

### Fixed

- fixed serialization of bind parameter with null values (Issue #176, #177)
- fixed VelocyStream multi-thread authentication bug
- fixed load balancing cursor stickiness bug

## [4.3.3] - 2018-02-01

### Added

- added `CollectionCreateOptions#distributeShardsLike(String)` (Issue #170)
- added `AqlQueryOptions#memoryLimit(Long)`
- added `AqlQueryOptions#failOnWarning(Boolean)`
- added `AqlQueryOptions#maxTransactionSize(Long)`
- added `AqlQueryOptions#maxWarningCount(Long)`
- added `AqlQueryOptions#intermediateCommitCount(Long)`
- added `AqlQueryOptions#intermediateCommitSize(Long)`
- added `AqlQueryOptions#satelliteSyncWait(Double)`
- added `AqlQueryOptions#skipInaccessibleCollections(Boolean)`
- added `TransactionOptions#maxTransactionSize(Long)`
- added `TransactionOptions#intermediateCommitCount(Long)`
- added `TransactionOptions#intermediateCommitSize(Long)`
- added `QueryEntity#getBindVars(): Map<String, Object>`
- added `QueryEntity#getState(): QueryExecutionState`

### Fixed

- fixed inconsistency of `ArangoCollection#getDocument()` variants (Issue #168)

## [4.3.2] - 2017-11-30

### Fixed

- fixed redirect header (uppercase)

## [4.3.1] - 2017-11-27

### Fixed

- fixed default JSON parsing, include null values (Issue #163)
- fixed JSON parsing of negative long (Issue #151)

## [4.3.0] - 2017-11-23

### Added

- added load balancing (`ArangoDB.Builder#loadBalancingStrategy()`)
- added automatic acquiring of hosts for load balancing or as fallback (`ArangoDB.Builder#acquireHostList()`)

## [4.2.7] - 2017-11-03

### Added

- added `ArangoGraph#exists()`

### Fixed

- fixed deserialization of `BigDecimal`

## [4.2.6] - 2017-10-23

### Changed

- exclude junit dependency of json-simple

### Fixed

- fixed de-/serialization of negative int values (issue #151)

## [4.2.5] - 2017-10-16

### Added

- added `ArangoCollection#exists()` (issue #146)
- added `ArangoDatabase#exists()`
- added `BaseDocument#setId(String)` (issue #152)
- added `GraphCreateOptions#replicationFactor(Integer)`

### Changed

- `ArangoDB#shutdown()` now closes all connections (issue #156)

## [4.2.4] - 2017-09-04

### Added

- added properties validation `arangodb.host`
- added `ArangoCollection#ensure<IndexType>Index()`

### Changed

- let `ArangoCursor` implement `Iterable`

### Deprecated

- deprecated `ArangoCollection#create<IndexType>Index()`

### Fixed

- fixed `ArangoDatabase#transaction()`: ignore null result
- fixed `ArangoCollection#updateDocument()` (issue #145)
- fixed `ArangoVertexCollection#updateVertex()` (issue #145)
- fixed `ArangoEdgeCollection#updateEdge()` (issue #145)

## [4.2.3] - 2017-07-31

### Added

- added `ArangoDatabase#getPermissions(String)`
- added `ArangoCollection#getPermissions(String)`
- added `ArangoDB#grantDefaultDatabaseAccess(String, Permissions)`
- added `ArangoDB#grantDefaultCollectionAccess(String, Permissions)`
- added `ArangoDatabase#grantDefaultCollectionAccess(String, Permissions)`

### Fixed

- fixed `DateUtil` (thread-safe)

## [4.2.2] - 2017-07-20

### Added

- added `ArangoDatabase#grantAccess(String, Permissions)`
- added `ArangoCollection#grantAccess(String, Permissions)`
- added `ArangoDatabase#resetAccess(String)`
- added `ArangoCollection#resetAccess(String)`
- added `ArangoDB#updateUserDefaultDatabaseAccess(String, Permissions)`
- added `ArangoDB#updateUserDefaultCollectionAccess(String, Permissions)`
- added `ArangoDatabase#updateUserDefaultCollectionAccess(String, Permissions)`
- added `ArangoCollection#getDocuments(Collection<String>, Class)`
- added connection/handshake retry on same host
- added deduplicate field for hash/skiplist index

## [4.2.1] - 2017-06-20

### Fixed

- fixed deserializing of internal field `_id`

## [4.2.0] - 2017-06-14

### Added

- added `ArangoDBVersion#getLicense()`
- added `ArangoDB#getRole()`
- added `ArangoDBException#getException()`
- added protocol switch (`ArangoDB.Builder#protocol(Protocol)`)
  - `Protocol#VST` = VeclocyStream (default)
  - `Protocol#HTTP_JSON` = JSON over HTTP
  - `Protocol#HTTP_VPACK` = VelocyPack over HTTP

## [4.1.12] - 2017-04-13

### Added

- added `ArangoDatabase#cursor()` (issue #116)

### Changed

- optimized `ArangoDB.Builder` for better multi thread support

### Fixed

- fixed `VPackSlice` `float`/`double` bug

## [4.1.11] - 2017-03-24

### Added

- added convenience methods `ArangoDatabase#arango()`, `ArangoCollection#db()`, `ArangoGraph#db()`
- added convenience methods `ArangoCollection#getIndex(String)`, `ArangoCollection#deleteIndex(key)`
- added connection pooling (issue #103)
- added extension point for `VelocyPack` serialization (`ArangoDB#registerModule()`)
- added support for replacing build-in VelocyPack serializer/deserializer
- added `ArangoDatabase#getVersion()`, `ArangoDatabase#getAccessibleDatabases()`

### Changed

- extracted VelocyPack implementation to https://github.com/arangodb/java-velocypack

### Fixed

- fixed exception handling in Connection (issue #110)
- fixed NPE in `ArangoCursor` (issue #112)

## [4.1.10] - 2017-02-22

### Added

- added support for multiple hosts as fallbacks
- added support serializing collections with null elements
- added support serializing non-generic classes that extend collections
- added support serializing/deserializing byte and Byte
- added default value "root" for user

### Changed

- changed velocystream message sending to async
- changed return value of getVertex/getEdge to null if not exists

### Fixed

- fixed serialization of additionalFields for objects and maps
- fixed VPack parsing (arrays of specific length)

## [4.1.9] - 2017-02-10

### Added

- added missing `IndexType#edge`

### Fixed

- fixed Connection (thread-safe)
- fixed URI encoding

## [4.1.8] - 2017-02-03

### Added

- added byte[] de-/serialization from/to `VPack.string` (Base64)
- added ArangoCollection.drop(isSystem)
- improved ArangoDBException with responseCode, errorNum, errorMessage

### Changed

- changed `java.util.Date` serialization from `VPack.date` to `VPack.string` (ISO 8601)
- changed `java.sql.Date` serialization from `VPack.date` to `VPack.string` (ISO 8601)
- changed `java.sql.Timestamp` serialization from `VPack.date` to `VPack.string` (ISO 8601)
- changed `ArangoCollection#deleteDocuments()` to work with keys and documents

### Fixed

- fixed URL encoding bug (#97)
- fixed update/replaceDocumets with JSON (#98)

## [4.1.7] - 2017-01-26

### Fixed

- fixed `importDocuments`, `insertDocuments` to work with raw JSONs (issue #91)

## [4.1.6] - 2017-01-18

### Added

- added serializer support for enclosing types

## [4.1.5] - 2017-01-12

### Added

- added configuration for custom annotations within `VPack` de-/serialization
- added support of transient modifier within `VPack` de-/serialization

### Fixed

- fixed `VPack` String serialization (UTF-8 encoding)
- fixed `VPack` parsing of fields of type Object
- fixed `VPack` serializing of array with null values (issue #88)

## [4.1.4] - 2016-12-19

### Added

- added `VPack` serializer/de-serializer for `java.util.UUID`

### Fixed

- fixed `VPack` parsing (issue #65, #80, #82)

## [4.1.3] - 2016-11-22

### Added

- added bulk import API

### Fixed

- fixed error while serializing long values with VPackBuilder

## [4.1.2] - 2016-11-10

### Added

- added `VelocyPack` UTC_DATE parsing to JSON String (ISO 8601)
- added configuration methods for `VPackParser` in `ArangoDB.Builder`
- added `VPackJsonSerializer` for `VPackParser`

### Fixed

- fixed `GraphEntity` for `ArangoDatabase#getGraphs()` (field `name` is null)

## [4.1.1] - 2016-11-09

### Added

- added option `CollectionCreateOptions#replicationFactor`
- added option `CollectionPropertiesEntity#replicationFactor`
- added option `DocumentUpdateOptions#serializeNull`

### Changed

- changed json parsing of VelocyPack types not known in json

### Fixed

- fixed VelocyPack bug with non-ASCII characters

## [4.1.0] - 2016-10-28

### Added

- added `ArangoUtil` for manually de-/serialization

### Changed

- changed VelocyStream communication (send protocol header)

## [4.0.0] - 2016-10-17

### Added

- added VelocyPack support
- added multi document operations (insert, delete, update, replace)

### Replaced

- replaced API
- replaced protocol http with VelocyStream

## [3.1.0] - 2016-10-17

### Added

- added profile flag to AqlQueryOptions (issue #47)

### Changed

- changed Revision from long to String

### Removed

- removed methods with collectionId (long) from `ArangoDriver` (Id is only for internal usage)
- removed methods with documentId (long) from `ArangoDriver`

## [3.0.4] - 2016-10-17

### Fixed

- fixed edges deserializer (issue #50)

## [3.0.3] - 2016-09-12

### Added

- added error handling in getBatchResponseByRequestId()
- added function createPersistentIndex() (issue #48)
- added deserializer for BaseDocument (issue #50)

## [3.0.2] - 2016-08-05

### Added

- added profile flag to AqlQueryOptions (issue #47)
- added getExtra() to DocumentCursor<> (issue #47)
- added IndexType.PERSISTENT (issue #48)

## [3.0.1] - 2016-07-08

### Added

- added flag complete and details in ImportOptions

### Fixed

- fixed issue #43 (ArangoDriver.getAqlFunctions(String) does not uses the defaultDatabase setting)

## [3.0.0] - 2016-06-17

### Added

- added User-Method grantDatabaseAccess(username, database)
- added Transaction attribute allowImplicit

### Changed

- refactored QueryCachePropertiesEntity, TransactionResultEntity

### Replaced

- replaced Graph-Functions (graph_edge, graph_vertices, graph_shortes_path) with AQL

### Removed

- removed ArangoDriver.EdgeEntity() (/\_api/edge withdrawn in Server)
- removed CAP-Index (Cap-constraints are withdrawn in Server)
- removed Param database in User-Methods (in 3.0 users are managed in \_users Collection in \_system Database only)
- removed deprecated Methods

## [2.7.4] - 2016-04-15

### Fixed

- fixed issue #35 (There is no replaceDocumentRaw similar to createDocumentRaw)

## [2.7.3] - 2016-03-25

### Fixed

- batch driver performance fix
- fixed issue #33 (typo in ArangoDriver.executeAqlQueryWithDocumentCursorResutl method)

## [2.7.2] - 2016-01-22

### Added

- added executeAqlQueryRaw(...). Example src/test/java/com/arangodb/example/document/RawDocumentExample.java

## [2.7.1] - 2016-01-21

### Added

- added examples for new AQL traversal functions (since ArangoDB 2.8)
- added AQL warnings to CursorResult<?> (hasWarning() and getWarnings())
- added createDocumentRaw(...) and getDocumentRaw(...). Examples src/test/java/com/arangodb/example/document/RawDocumentExample.java

### Changed

- Updated dependencies gson (2.5), httpclient (4.5.1) and slf4j-api (1.7.13)

## [2.7.0] - 2015-11-20

### Added

- added document examples in src/test/java/com/arangodb/example/document/
- added graph examples in src/test/java/com/arangodb/example/document/
- added new function executeAqlQueryJSON(): Executes an AQL query and returns the raw JSON response as a String
- initial support of HTTPS connections. Examples src/test/java/com/arangodb/example/ssl/

## [2.6.9] - 2015-10-16

### Added

- added support API: `/_api/query-cache/properties` (AQL query tracking properties: setQueryTrackingProperties(), getQueryTrackingProperties())
- added support API: `/_api/query-cache` (delete AQL query cache: deleteQueryCache())
- added support API: `/_api/query/current` (currently running AQL queries: getCurrentlyRunningQueries())
- added support API: `/_api/query/slow` (slow AQL queries: getSlowQueries(), deleteSlowQueries())
- added support API: `/_api/query` (kill AQL queries: killQuery())
- added boolean exists(long collectionId, long documentId)
- added boolean exists(String collectionName, long documentId)
- added boolean exists(long collectionId, String documentKey) throws ArangoException {
- added boolean exists(String collectionName, String documentKey)
- added boolean exists(String documentHandle)

## [2.6.8] - 2015-09-25

### Fixed

- fixed GRAPH_EDGES() 2.6-incompatibility

## [2.5.6] - 2015-07-04

### Added

- ArangoDB 2.6 support

### Fixed

- fixed issue #19 \* createEdge takes graphName but needs database name

## [2.5.5] - 2015-05-23

### Added

- added an examples for Transaction API
  (see src/test/java/com/arangodb/example/TransactionExample.java)
- added TraversalQueryOptions to avoid too many parameters

### Changed

- updated `driver.getTraversal(...);`
- changed TransactionEntity.ReadWriteCollections to a static class (issue #17)

### Removed

- removed VisitedEntity (Traversal)

## [2.5.4] - 2015-05-03

### Added

- added new cursor implementation for AQL queries
  - DocumentCursor<T> executeDocumentQuery(...)
  - VertexCursor<T> executeVertexQuery(...)
  - EdgeCursor<T> executeEdgeQuery(...)
- added new cursor implementation for simple queries
  - DocumentCursor<T> executeSimpleAllDocuments(...)
  - DocumentCursor<T> executeSimpleByExampleDocuments(...)
  - DocumentCursor<T> executeSimpleRangeWithDocuments(...)
  - DocumentCursor<T> executeSimpleFulltextWithDocuments(...)
- added some examples for AQL queries
  (see src/test/java/com/arangodb/example)

### Fixed

- fixed issue #12
  - added auto reconnection when connection breaks
  - added fallback server endpoints

## [2.5.3] - 2015-03-29

### Fixed

- fixed issue #9 \* added method to driver.getTraversal(...);

## [2.5.0]

### Added

Added support for sparse indexes

## [2.4.4]

### Fixed

- fixed issue #5
  - added method to driver.createGraph(GraphEntity g);
- fixed issue #6
- fixed issue #7

## [2.4.3]

### Added

- Some additional methods in GraphEntity:
  - public EdgeDefinitionsEntity getEdgeDefinitionsEntity()
  - public void setEdgeDefinitionsEntity(EdgeDefinitionsEntity edgeDefinitionsEntity)
- Some additional methods in EdgeDefinitionsEntity:
  - public int getSize()
  - public EdgeDefinitionEntity getEdgeDefinition(String collectionName)

### Changed

- GraphEntity has been changed, so that edge definitions are stored in an EdgeDefinitionsEntity.

### Fixed

- Fixed a graph bug: when retrieving a graph via the driver, "from" and "to" were emtpy. This is fixed now.

## [2.4.2]

### Fixed

- Fixed issue#2

## [2.4.1]

### Changed

- httpclient version 4.3.6

## [1.4.1] - 2014-02-04

### Added

- added support API: GET `/_api/database/user` (getDatabases)
- added debug property: enableCURLLogger
- added Annotation @DocumentKey and support user define document key.
- added document API support `_key`.

### Changed

- rename attribute: UserEntity#user -> UserEntity#username
- modify API: createDatabase(db) -> createDatabase(db, users...)

### Fixed

- Bug fixed: raise error if `_key` is not number.
- Fixed OraacleJDK build error.(#11)

## [1.4.0] - 2013-11-26

### Add

- support database (for 1.4 feature)
- stop the API of edge, edges in version 1.4.0
- stop the API of kvs(`/_api/key`) in version 1.4.0(because server does not support yet.)
- add support API: `/_api/database/`
- add configure: defaultDatabase
- add support API: `/_admin/statistics` (getStatistics)
- add support API: `/_admin/statistics-description` (statisticsDescription)
- add support API: `/_api/endpoint`
- add support API: `/_api/collection/{collection-name}/checksum` (getCollectionChecksum)
- add support API: `/_api/example/first`
- add support API: `/_api/example/last`
- add support API: `/_api/replication/inventory` (getReplicationInventory)
- add support API: `/_api/replication/dump` (getReplicationDump)
- add support API: `/_api/replication/server-id` (getReplicationServerId)
- add support API: `/_api/replication/logger-start` (startReplicationLogger)
- add support API: `/_api/replication/logger-stop` (stopReplicationLogger)
- add support API: GET `/_api/replication/logger-state` (getReplicationLoggerState)
- add support API: GET `/_api/replication/logger-config` (getReplicationLoggerConfig)
- add support API: PUT `/_api/replication/logger-config` (setReplicationLoggerConfig)
- add support API: GET `/_api/replication/applier-config` (getReplicationApplierConfig)
- add support API: PUT `/_api/replication/applier-config` (setReplicationApplierConfig)
- add support API: PUT `/_api/replication/applier-start` (startReplicationApplier)
- add support API: PUT `/_api/replication/applier-stop` (stopReplicationApplier)
- add support API: GET `/_api/replication/applier-state` (getReplicationApplierState)
- add support API: POST `/_admin/execute` (executeScript)
- add support API: POST `/_api/graph` (createGraph)
- add support API: GET `/_api/graph` (getGraphs)
- add support API: GET `/_api/graph/{graph-name}` (getGraph)
- add support API: DELETE `/_api/graph/{graph-name}` (deleteGraph)
- add support API: POST `/_api/graph/{graph-name}/vertex` (createVertex)
- add support API: GET `/_api/graph/{graph-name}/vertex/{document-key}` (getVertex)
- add support API: DELETE `/_api/graph/{graph-name}/vertex/{document-key}` (deleteVertex)
- add support API: PUT `/_api/graph/{graph-name}/vertex/{document-key}` (replaceVertex)
- add support API: PATCH `/_api/graph/{graph-name}/vertex/{document-key}` (updateVertex)
- add support API: POST `/_api/graph/{graph-name}/vertices` (getVertices)
- add support API: POST `/_api/graph/{graph-name}/vertices/{vertex-key}` (getVertices)
- add support API: POST `/_api/graph/{graph-name}/edge` (createEdge)
- add support API: GET `/_api/graph/{graph-name}/edge/{edge-key}` (getEdge)
- add support API: DELETE `/_api/graph/{graph-name}/edge/{edge-key}` (deleteEdge)
- add support API: PUT `/_api/graph/{graph-name}/edge/{edge-key}` (replaceEdge)
- add support API: POST `/_api/graph/{graph-name}/edges` (getEdges)
- add support API: POST `/_api/graph/{graph-name}/edges/{vertex-key}` (getEdges)
- add attribute: CollectionEntity#checksum
- add attribute: CollectionEntity#doCompact
- add attribute: CollectionEntity#keyOptions
- add attribute: CollectionEntity.Figures#(compactorsCount,compactorsFileSize,shapefilesCount,shapefilesFileSize,shapesCount,attributesCount)
- add doCompact to argument of createCollection

### Changed

- getDocuments was changed to return document-handle
- rename method: updateDocument -> replaceDocument
- rename method: partialUpdateDocument -> updateDocument
- changed the version API endpoint. (/\_admin/version -> /\_api/version)
- changed into createOptions to keyOptions of collection API
- refactoring deserialize of parameterized entity class.
- gson library upgrade to 2.2.4

### Removed

- remove attribute: CollectionEntity#createOptions
- remove: getServerStatus
- remove: getConnectionStatistics

#### Fixed

- fixed ArangoUnixTime bug.

## [1.2.2] - 2013-07-10

### Added

- add support API: `/_api/explain`
- add support API: `/_api/collection/collection-name/revision`
- add support API: `/_api/index of fulltext`
- add support API: `/_api/simple/fulltext`
- add support API: `/_admin/modules/flush`
- add support API: `/_admin/routing/reload`
- add support API: User Management
- add support: Basic Authentication
- `/_api/simple/all` and `/_api/simple/by-example` returns DocumentEntity
- add support import API

## [1.2.1] - 2013-07-02

### Added

- Add support: load configure from property-file in classpath.
- Add configure: timeout, connectionTimeout, retryCount.

### Changed

- Change google-gson scope in pom.xml
- Change logback-classic in pom.xml

### Remove

- Remove configure: autoUnknownCollections.
- Remove README.JA

## [1.2.0] - 2013-06-30

- Initial Release



[unreleased]: https://github.com/arangodb/arangodb-java-driver/compare/v7.0.0...HEAD
[7.0.0]: https://github.com/arangodb/arangodb-java-driver/compare/v6.23.0..v7.0.0
[6.23.0]: https://github.com/arangodb/arangodb-java-driver/compare/v6.22.0..v6.23.0
[6.22.0]: https://github.com/arangodb/arangodb-java-driver/compare/v6.21.0..v6.22.0
[6.21.0]: https://github.com/arangodb/arangodb-java-driver/compare/v6.20.0..v6.21.0
[6.20.0]: https://github.com/arangodb/arangodb-java-driver/compare/v6.19.0..v6.20.0
[6.19.0]: https://github.com/arangodb/arangodb-java-driver/compare/v6.18.0..v6.19.0
[6.18.0]: https://github.com/arangodb/arangodb-java-driver/compare/v6.17.0..v6.18.0
[6.17.0]: https://github.com/arangodb/arangodb-java-driver/compare/v6.16.1..v6.17.0
[6.16.1]: https://github.com/arangodb/arangodb-java-driver/compare/v6.16.0..v6.16.1
[6.16.0]: https://github.com/arangodb/arangodb-java-driver/compare/v6.15.0..v6.16.0
[6.15.0]: https://github.com/arangodb/arangodb-java-driver/compare/v6.14.0..v6.15.0
[6.14.0]: https://github.com/arangodb/arangodb-java-driver/compare/v6.13.0..v6.14.0
[6.13.0]: https://github.com/arangodb/arangodb-java-driver/compare/v6.12.3..v6.13.0
[6.12.3]: https://github.com/arangodb/arangodb-java-driver/compare/v6.12.2..v6.12.3
[6.12.2]: https://github.com/arangodb/arangodb-java-driver/compare/v6.12.1..v6.12.2
[6.12.1]: https://github.com/arangodb/arangodb-java-driver/compare/v6.12.0..v6.12.1
[6.12.0]: https://github.com/arangodb/arangodb-java-driver/compare/v6.11.1..v6.12.0
[6.11.1]: https://github.com/arangodb/arangodb-java-driver/compare/v6.11.0..v6.11.1
[6.11.0]: https://github.com/arangodb/arangodb-java-driver/compare/v6.10.0..v6.11.0
[6.10.0]: https://github.com/arangodb/arangodb-java-driver/compare/v6.9.1..v6.10.0
[6.9.1]: https://github.com/arangodb/arangodb-java-driver/compare/v6.9.0..v6.9.1
[6.9.0]: https://github.com/arangodb/arangodb-java-driver/compare/v6.8.2..v6.9.0
[6.8.2]: https://github.com/arangodb/arangodb-java-driver/compare/v6.8.1..v6.8.2
[6.8.1]: https://github.com/arangodb/arangodb-java-driver/compare/v6.8.0..v6.8.1
[6.8.0]: https://github.com/arangodb/arangodb-java-driver/compare/v6.7.5..v6.8.0
[6.7.5]: https://github.com/arangodb/arangodb-java-driver/compare/v6.7.4..v6.7.5
[6.7.4]: https://github.com/arangodb/arangodb-java-driver/compare/v6.7.3..v6.7.4
[6.7.3]: https://github.com/arangodb/arangodb-java-driver/compare/v6.7.2..v6.7.3
[6.7.2]: https://github.com/arangodb/arangodb-java-driver/compare/v6.7.1..v6.7.2
[6.7.1]: https://github.com/arangodb/arangodb-java-driver/compare/v6.7.0..v6.7.1
[6.7.0]: https://github.com/arangodb/arangodb-java-driver/compare/v6.6.3..v6.7.0
[6.6.3]: https://github.com/arangodb/arangodb-java-driver/compare/v6.6.2..v6.6.3
[6.6.2]: https://github.com/arangodb/arangodb-java-driver/compare/v6.6.1..v6.6.2
[6.6.1]: https://github.com/arangodb/arangodb-java-driver/compare/v6.6.0..v6.6.1
[6.6.0]: https://github.com/arangodb/arangodb-java-driver/compare/v6.5.0..v6.6.0
[6.5.0]: https://github.com/arangodb/arangodb-java-driver/compare/v6.4.1..v6.5.0
[6.4.1]: https://github.com/arangodb/arangodb-java-driver/compare/v6.4.0..v6.4.1
[6.4.0]: https://github.com/arangodb/arangodb-java-driver/compare/v6.3.0..v6.4.0
[6.3.0]: https://github.com/arangodb/arangodb-java-driver/compare/v6.2.0..v6.3.0
[6.2.0]: https://github.com/arangodb/arangodb-java-driver/compare/v6.1.0..v6.2.0
[6.1.0]: https://github.com/arangodb/arangodb-java-driver/compare/v6.0.0..v6.1.0
[6.0.0]: https://github.com/arangodb/arangodb-java-driver/compare/5.0.7..v6.0.0
[5.0.7]: https://github.com/arangodb/arangodb-java-driver/compare/5.0.6...5.0.7
[5.0.6]: https://github.com/arangodb/arangodb-java-driver/compare/5.0.5...5.0.6
[5.0.5]: https://github.com/arangodb/arangodb-java-driver/compare/5.0.4...5.0.5
[5.0.4]: https://github.com/arangodb/arangodb-java-driver/compare/5.0.3...5.0.4
[5.0.3]: https://github.com/arangodb/arangodb-java-driver/compare/5.0.2...5.0.3
[5.0.2]: https://github.com/arangodb/arangodb-java-driver/compare/5.0.1...5.0.2
[5.0.1]: https://github.com/arangodb/arangodb-java-driver/compare/5.0.0...5.0.1
[5.0.0]: https://github.com/arangodb/arangodb-java-driver/compare/4.7.3...5.0.0
[4.7.3]: https://github.com/arangodb/arangodb-java-driver/compare/4.7.2...4.7.3
[4.7.2]: https://github.com/arangodb/arangodb-java-driver/compare/4.7.1...4.7.2
[4.7.1]: https://github.com/arangodb/arangodb-java-driver/compare/4.7.0...4.7.1
[4.7.0]: https://github.com/arangodb/arangodb-java-driver/compare/4.6.1...4.7.0
[4.6.1]: https://github.com/arangodb/arangodb-java-driver/compare/4.6.0...4.6.1
[4.6.0]: https://github.com/arangodb/arangodb-java-driver/compare/4.5.2...4.6.0
[4.5.2]: https://github.com/arangodb/arangodb-java-driver/compare/4.5.1...4.5.2
[4.5.1]: https://github.com/arangodb/arangodb-java-driver/compare/4.5.0...4.5.1
[4.5.0]: https://github.com/arangodb/arangodb-java-driver/compare/4.4.1...4.5.0
[4.4.1]: https://github.com/arangodb/arangodb-java-driver/compare/4.4.0...4.4.1
[4.4.0]: https://github.com/arangodb/arangodb-java-driver/compare/4.3.7...4.4.0
[4.3.7]: https://github.com/arangodb/arangodb-java-driver/compare/4.3.6...4.3.7
[4.3.6]: https://github.com/arangodb/arangodb-java-driver/compare/4.3.5...4.3.6
[4.3.5]: https://github.com/arangodb/arangodb-java-driver/compare/4.3.4...4.3.5
[4.3.4]: https://github.com/arangodb/arangodb-java-driver/compare/4.3.3...4.3.4
[4.3.3]: https://github.com/arangodb/arangodb-java-driver/compare/4.3.2...4.3.3
[4.3.2]: https://github.com/arangodb/arangodb-java-driver/compare/4.3.1...4.3.2
[4.3.1]: https://github.com/arangodb/arangodb-java-driver/compare/4.3.0...4.3.1
[4.3.0]: https://github.com/arangodb/arangodb-java-driver/compare/4.2.7...4.3.0
[4.2.7]: https://github.com/arangodb/arangodb-java-driver/compare/4.2.6...4.2.7
[4.2.6]: https://github.com/arangodb/arangodb-java-driver/compare/4.2.5...4.2.6
[4.2.5]: https://github.com/arangodb/arangodb-java-driver/compare/4.2.4...4.2.5
[4.2.4]: https://github.com/arangodb/arangodb-java-driver/compare/4.2.3...4.2.4
[4.2.3]: https://github.com/arangodb/arangodb-java-driver/compare/4.2.2...4.2.3
[4.2.2]: https://github.com/arangodb/arangodb-java-driver/compare/4.2.1...4.2.2
[4.2.1]: https://github.com/arangodb/arangodb-java-driver/compare/4.2.0...4.2.1
[4.2.0]: https://github.com/arangodb/arangodb-java-driver/compare/4.1.12...4.2.0
[4.1.12]: https://github.com/arangodb/arangodb-java-driver/compare/4.1.11...4.1.12
[4.1.11]: https://github.com/arangodb/arangodb-java-driver/compare/4.1.10...4.1.11
[4.1.10]: https://github.com/arangodb/arangodb-java-driver/compare/4.1.9...4.1.10
[4.1.9]: https://github.com/arangodb/arangodb-java-driver/compare/4.1.8...4.1.9
[4.1.8]: https://github.com/arangodb/arangodb-java-driver/compare/4.1.7...4.1.8
[4.1.7]: https://github.com/arangodb/arangodb-java-driver/compare/4.1.6...4.1.7
[4.1.6]: https://github.com/arangodb/arangodb-java-driver/compare/4.1.5...4.1.6
[4.1.5]: https://github.com/arangodb/arangodb-java-driver/compare/4.1.4...4.1.5
[4.1.4]: https://github.com/arangodb/arangodb-java-driver/compare/4.1.3...4.1.4
[4.1.3]: https://github.com/arangodb/arangodb-java-driver/compare/4.1.2...4.1.3
[4.1.2]: https://github.com/arangodb/arangodb-java-driver/compare/4.1.1...4.1.2
[4.1.1]: https://github.com/arangodb/arangodb-java-driver/compare/4.1.0...4.1.1
[4.1.0]: https://github.com/arangodb/arangodb-java-driver/compare/4.0.0...4.1.0
[4.0.0]: https://github.com/arangodb/arangodb-java-driver/compare/3.1.0...4.0.0
[3.1.0]: https://github.com/arangodb/arangodb-java-driver/compare/3.0.4...3.1.0
[3.0.4]: https://github.com/arangodb/arangodb-java-driver/compare/3.0.3...3.0.4
[3.0.3]: https://github.com/arangodb/arangodb-java-driver/compare/3.0.2...3.0.3
[3.0.2]: https://github.com/arangodb/arangodb-java-driver/compare/3.0.1...3.0.2
[3.0.1]: https://github.com/arangodb/arangodb-java-driver/compare/3.0.0...3.0.1
[3.0.0]: https://github.com/arangodb/arangodb-java-driver/compare/2.7.4...3.0.0
[2.7.4]: https://github.com/arangodb/arangodb-java-driver/compare/2.7.3...2.7.4
[2.7.3]: https://github.com/arangodb/arangodb-java-driver/compare/2.7.2...2.7.3
[2.7.2]: https://github.com/arangodb/arangodb-java-driver/compare/2.7.1...2.7.2
[2.7.1]: https://github.com/arangodb/arangodb-java-driver/compare/2.7.0...2.7.1
[2.7.0]: https://github.com/arangodb/arangodb-java-driver/compare/2.6.9...2.7.0
[2.6.9]: https://github.com/arangodb/arangodb-java-driver/compare/2.6.8...2.6.9
[2.6.8]: https://github.com/arangodb/arangodb-java-driver/compare/2.5.6...2.6.8
[2.5.6]: https://github.com/arangodb/arangodb-java-driver/compare/2.5.5...2.5.6
[2.5.5]: https://github.com/arangodb/arangodb-java-driver/compare/2.5.4...2.5.5
[2.5.4]: https://github.com/arangodb/arangodb-java-driver/compare/2.5.3...2.5.4
[2.5.3]: https://github.com/arangodb/arangodb-java-driver/compare/2.5.0...2.5.3
[2.5.0]: https://github.com/arangodb/arangodb-java-driver/compare/2.4.4...2.5.0
[2.4.4]: https://github.com/arangodb/arangodb-java-driver/compare/2.4.3...2.4.4
[2.4.3]: https://github.com/arangodb/arangodb-java-driver/compare/2.4.2...2.4.3
[2.4.2]: https://github.com/arangodb/arangodb-java-driver/compare/2.4.1...2.4.2
[2.4.1]: https://github.com/arangodb/arangodb-java-driver/compare/1.4.1...2.4.1
[1.4.1]: https://github.com/arangodb/arangodb-java-driver/compare/1.4.0...1.4.1
[1.4.0]: https://github.com/arangodb/arangodb-java-driver/compare/1.2.2...1.4.0
[1.2.2]: https://github.com/arangodb/arangodb-java-driver/compare/1.2.1...1.2.2
[1.2.1]: https://github.com/arangodb/arangodb-java-driver/compare/1.2.0...1.2.1

