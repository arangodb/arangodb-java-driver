# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]

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

- added dirty read support ([reading from followers](https://www.arangodb.com/docs/stable/administration-active-failover.html#reading-from-follower))

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
- added protocol switch (`ArangoDB.Builder#useProtocol(Protocol)`)
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

[unreleased]: https://github.com/arangodb/arangodb-java-driver/compare/5.0.7...HEAD
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
