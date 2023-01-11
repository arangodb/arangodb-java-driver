# Version 7.0: detailed changes

## Maven Setup

In your `pom.xml`, set the snapshots repository:

```
<repositories>
    <repository>
        <id>snapshots</id>
        <url>https://oss.sonatype.org/content/repositories/snapshots</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

and include the dependency:

```
<dependencies>
    <dependency>
        <groupId>com.arangodb</groupId>
        <artifactId>arangodb-java-driver</artifactId>
        <version>7.0.0-SNAPSHOT</version>
    </dependency>
<dependencies>
```

## HTTP client

The HTTP client has been changed to Vert.x WebClient. 

`HTTP/2` is now supported. `HTTP/2` supports multiplexing and uses `1` connection per host by default.


## Configuration changes

The default communication protocol is now `HTTP2_JSON` (`HTTP/2` with `JSON` content type).

The default host configuration to `127.0.0.1:8529` has been removed.

Configuration properties are not read automatically from properties files anymore.
A new API for loading properties has been introduced: `ArangoDB.Builder.loadProperties(ArangoConfigProperties)`. 
Implementations could supply configuration properties coming from different sources, eg. system properties, remote 
stores, frameworks facilities, etc.
An implementation for loading properties from local files is provided by `ArangoConfigProperties.fromFile()` and its 
overloaded variants.

Here is an example to read config properties from `arangodb.properties` file (as in version `6`):

```java
ArangoDB adb = new ArangoDB.Builder()
        .loadProperties(ArangoConfigProperties.fromFile())
        // ...
        .build();
```

Here is an example to read config properties from `arangodb-with-prefix.properties` file, where the config properties
are prefixed with `adb`:

```java
// arangodb-with-prefix.properties content:
//
// adb.hosts=172.28.0.1:8529
// adb.acquireHostList=true
// ...

ArangoDB adb = new ArangoDB.Builder()
        .loadProperties(new FileConfigPropertiesProvider("arangodb-with-prefix.properties", "adb"))
        .build();
```

An example showing how to provide configuration using Eclipse MicroProfile Config can be found 
[here](../driver/src/test/java/com/arangodb/config).

## Transitive dependencies

A transitive dependency on `io.vertx:vertx-web-client` has been added. In can be excluded when using `VST` protocol
only.

The dependency on `com.arangodb:velocypack` has been removed.

When using protocol `VST`, `HTTP_VPACK` or `HTTP2_VPACK`, the optional dependency
on `com.arangodb:jackson-dataformat-velocypack` must be provided.

When using protocol `HTTP_JSON` or `HTTP2_JSON` (default), no dependencies on `VPACK` libraries are required.

Transitive dependencies on Jackson Core, Databind and Annotations have been added, using by default version `2.14`.
The versions of such libraries can be overridden, the driver is compatible with Jackson versions: `2.10`, `2.11`, `2.12`
, `2.13`, `2.14`.

If these dependency requirements cannot be satisfied, you might need to use the [shaded version](#arangodb-java-driver-shaded) of this driver.


## User Data Types

Before version `7.0` the driver always parsed raw strings as JSON, but unfortunately this does not allow distinguishing
the case when the intent is to use the raw string as such, without parsing it. Since version `7.0`, strings are not
interpreted as JSON anymore. To represent user data as raw JSON, the wrapper class `RawJson` has been added:

```java
RawJson rawJsonIn = RawJson.of("""
        {"foo":"bar"}
        """);
ArangoCursor<RawJson> res = adb.db().query("RETURN @v", Map.of("v", rawJsonIn), RawJson.class);
RawJson rawJsonOut = res.next();
String json = rawJsonOut.getValue();  // {"foo":"bar"}
```

To represent user data already encoded as byte array, the wrapper class `RawBytes` has been added.
The byte array can either represent a `JSON` string (UTF-8 encoded) or a `VPACK` value, but the format must be the
same used for the driver protocol configuration (`JSON` for `HTTP_JSON` and `HTTP2_JSON`, `VPACK` otherwise).

The following changes have been applied to `BaseDocument` and `BaseEdgeDocument`:
- `final` classes
- not serializable anymore (Java serialization)
- new method `removeAttribute(String)`
- `getProperties()` returns an unmodifiable map

Before version `7.0` when performing write operations, the metadata of the input data objects was updated in place with 
the metadata received in the response.
Since version `7.0`, the input data objects passed as arguments to API methods are treated as immutable and the related 
metadata fields are not updated anymore. The updated metadata can be found in the returned object.


## Serialization

Up to version 6, the (de)serialization was always performed to/from `VPACK`. In case the JSON format was
required, the raw `VPACK` was then converted to `JSON`. Since version 7, the serialization module is a dataformat
agnostic API, by default using `JSON` format. 

Support of data type `VPackSlice` has been removed (in favor of Jackson types: `JsonNode`, `ArrayNode`, `ObjectNode`,
...), for example:

```java
JsonNode jsonNodeIn = JsonNodeFactory.instance.objectNode()
        .put("foo", "bar");

ArangoCursor<JsonNode> res = adb.db().query("RETURN @v", Map.of("v", jsonNodeIn), JsonNode.class);
JsonNode jsonNodeOut = res.next();
String foo = jsonNodeOut.get("foo").textValue();    // bar
```

The dependency on `com.arangodb:velocypack` has been removed.

The user data custom serializer implementation `ArangoJack` has been removed in favor of `JacksonSerde`.

Updated reference documentation can be found [here](v7_java-reference-serialization.md). 


## ArangoDB Java Driver Shaded

Since version `7`, a shaded variant of the driver is also published with maven coordinates:
`com.arangodb:arangodb-java-driver-shaded`.

It bundles and relocates the following packages from transitive dependencies:
- `com.fasterxml.jackson`
- `com.arangodb.jackson.dataformat.velocypack`
- `io.vertx`
- `io.netty`


## Removed APIs

The following client APIs have been removed:

- client APIs already deprecated in Java Driver version `6`
- client API to interact with deprecated server APIs:
    - `MMFiles` related APIs
    - `ArangoDatabase.executeTraversal()`
    - `ArangoDB.getLogs()`
    - `minReplicationFactor` in collections and graphs
    - `overwrite` flag in `DocumentCreateOptions`

The user data custom serializer implementation `ArangoJack` has been removed in favor of `JacksonSerde`.

Support for interpreting raw strings as JSON has been removed (in favor of `RawJson`).

Support of data type `VPackSlice` has been removed (in favor of Jackson types: `JsonNode`, `ArrayNode`, `ObjectNode`,
...).

Support for custom initialization of
cursors (`ArangoDB._setCursorInitializer(ArangoCursorInitializer cursorInitializer)`) has been removed.


## API methods changes

Before version `7.0` some CRUD API methods inferred the return type from the type of the data object passed as input.
Now the return type can be explicitly set for each CRUD API method.

CRUD operations operating with multiple documents have now an overloaded variant which accepts raw data (`RawBytes`
and `RawJson`) containing multiple documents.

`ArangoCursor#getStats()` returns now an untyped map.

`Request` and `Response` classes have been refactored to support generic body type. 
`ArangoDB.execute(Request<T>, Class<U>): Response<U>` accepts now the target deserialization type for the response body.

`ArangoDBException` has been enhanced with the id of the request causing it.


## API entities

All entities and options classes (in packages `com.arangodb.model` and `com.arangodb.entity`) are now `final`.

The replication factor is now modeled with a new interface (`ReplicationFactor`) with
implementations: `NumericReplicationFactor` and `SatelliteReplicationFactor`.


## Migration

To migrate your existing project to Java Driver version `7.0`, it is recommended updating to the latest version of 
branch `6` and make sure that your code does not use any deprecated API. 
This can be done by checking the presence of deprecation warnings in the Java compiler output.

The deprecation notes in the related javadoc contain information about the reason of the deprecation and suggest
migration alternatives to use.
