# Version 7.0: detailed changes


## Default protocol 

The default communication protocol is now `HTTP_JSON` (`HTTP/1.1` with `JSON` content type). 


## Transitive dependencies

A transitive dependency on `org.apache.httpcomponents:httpclient:4.5.x` has been added and the dependency
on `com.arangodb:velocypack` has been removed.
The dependency on `org.apache.httpcomponents:httpclient` can be excluded when using `VST` communication protocol only.
When using `VST` or `HTTP_VPACK`, the optional dependency on `com.arangodb:jackson-dataformat-velocypack` must be
provided.
When using `HTTP_JSON` (default), no dependencies on `VPACK` libraries are required.
Transitive dependencies on Jackson Core, Databind and Annotations have been added, using by default version `2.13`.
The versions of such libraries can be overridden, the driver is compatible with Jackson versions: `2.10`, `2.11`, `2.12`
, `2.13`.


## User Data Types

Before version `7.0` the driver always parsed raw strings as JSON, but unfortunately this does not allow distinguishing
the case when the intent is to use the raw string as such, without parsing it. Since version `7.0`, strings are not
interpreted as JSON anymore. To represent user data as raw JSON, the wrapper class `com.arangodb.util.RawJson` has been
added.

To represent user data already encoded as byte array, the wrapper class `com.arangodb.util.RawBytes` has been added.
The byte array can either represent a `JSON` string (UTF-8 encoded) or a `VPACK` value, but the encoding must be the 
same used for the driver protocol configuration (`JSON` for `HTTP_JSON`, `VPACK` otherwise).

`BaseDocument` and `BaseEdgeDocument` are now `final`, they have a new method `removeAttribute(String)`
and `getProperties()` returns now an unmodifiable map.

Before version `7.0` when performing write operations, the metadata of the input data objects was updated with the
metadata received in the response. Since version `7.0`, the input data objects passed as arguments to API methods are
treated as immutable and the related metadata fields are not updated anymore. The updated metadata can still be found in
the object returned by the API method.


## Serialization

The serialization module has been changed and is now based on the Jackson API.

Up to version 6, the (de)serialization was always performed to/from `VPACK`. In case the JSON representation was required,
the raw `VPACK` was then converted to `JSON`. Since version 7, the serialization module is a dataformat agnostic API, based
on the Jackson API. By default, it reads and writes `JSON` format. `VPACK` support is provided by the optional
dependency `com.arangodb:jackson-dataformat-velocypack`, which is a dataformat backend implementation for Jackson.

The (de)serialization of user data can be customized by registering an implementation
of `com.arangodb.serde.ArangoSerde` via `com.arangodb.ArangoDB.Builder#serializer()`.
The default user data serializer is `com.arangodb.serde.JacksonSerde`, which is based on Jackson API and is available
for both `JSON` and `VPACK`. It (de)serializes user data using Jackson Databind and can handle Jackson Annotations.
It can be customized through `com.arangodb.serde.JacksonSerde#configure(Consumer<ObjectMapper>)`,
i.e. registering Kotlin or Scala modules. Furthermore, meta binding annotations (`@Id`, `@Key`, `@Rev`, `@From`, `@To`)
are supported for mapping documents and edges metadata fields (`_id`, `_key`, `_rev`, `_from`, `_to`).

`com.arangodb.serde.ArangoSerde` interface is not constrained to Jackson. It is instead an abstract API that can be
implemented using any custom serialization library, e.g. an example of `JSON-B` implementation can be found in
the [tests](../src/test/java/com/arangodb/serde/JsonbSerdeImpl.java).

Independently of the user data serializer, the following data types are (de)serialized with specific handlers (not
customizable):
- `JsonNode` and its children (`ArrayNode`, `ObjectNode`, ...)
- `RawJson`
- `RawBytes`
- `BaseDocument`
- `BaseEdgeDocument`


## Removed APIs

The following client APIs have been removed:
- client APIs already deprecated in Java Driver version `6.19.0`
- client API to interact with deprecated server APIs:
  - `MMFiles` related APIs
  - `ArangoDatabase.executeTraversal()`
  - `ArangoDB.getLogs()`
  - `minReplicationFactor` in collections and graphs
  - `overwrite` flag in `DocumentCreateOptions`

The deprecation notes in the related javadoc contain information about the reason of the deprecation and suggest
migration alternatives to use.

To migrate your existing project to Java Driver version `7.0`, it is recommended updating to version `6.19` first and
make sure that your code does not use any deprecated API. This can be done by checking the presence of deprecation
warnings in the Java compiler output.

The user data custom serializer implementation `com.arangodb.mapping.ArangoJack` has been removed in favor of `com.arangodb.serde.JacksonSerde`.

Support for interpreting raw strings as JSON has been removed (in favor of `com.arangodb.util.RawJson`).

Support of data type `com.arangodb.velocypack.VPackSlice` has been removed (in favor of Jackson types: `JsonNode`, `ArrayNode`, `ObjectNode`, ...).


## API methods changes

Before version `7.0` some CRUD API methods inferred the return type from the type of the data object passed as input. Now the return type can be explicitly set for each CRUD API method.

CRUD operations operating with multiple documents have now an overloaded variant which accepts raw data (`RawBytes`
and `RawJson`) containing multiple documents.

`com.arangodb.ArangoCursor#getStats()` returns now an untyped map.


## API entities

All entities and options classes are now `final`.

The replication factor is now modeled with a new interface (`com.arangodb.entity.ReplicationFactor`) with
implementations: `NumericReplicationFactor` and `SatelliteReplicationFactor`.