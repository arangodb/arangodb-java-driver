# Version 7.0: detailed changes

## Maven Setup

```
<dependencies>
    <dependency>
        <groupId>com.arangodb</groupId>
        <artifactId>arangodb-java-driver</artifactId>
        <version>7.0.0-RC.3</version>
    </dependency>
<dependencies>
```

## Gradle Setup

```
repositories {
    mavenCentral()
}

dependencies {
    implementation 'com.arangodb:arangodb-java-driver:7.0.0-RC.3'
}
```


## HTTP client

The HTTP client has been changed to [Vert.x WebClient](https://vertx.io/docs/vertx-web-client/java). 

`HTTP/2` is now supported. 
`HTTP/2` supports multiplexing and uses `1` connection per host by default.

Cookies are not supported anymore: cookies received in the response will be ignored.


## Configuration changes

The default communication protocol is now `HTTP2_JSON` (`HTTP/2` with `JSON` content type).

The default host configuration to `127.0.0.1:8529` has been removed.

Configuration properties are not read automatically from properties files anymore.
A new API for loading properties has been introduced: `ArangoDB.Builder.loadProperties(ArangoConfigProperties)`. 
Implementations could supply configuration properties coming from different sources, eg. system properties, remote 
stores, frameworks integrations, etc.
An implementation for loading properties from local files is provided by `ArangoConfigProperties.fromFile()` and its 
overloaded variants.

To read config properties prefixed with `arangodb` from `arangodb.properties` file (as in version `6`):

```java
// ## src/main/resources/arangodb.properties
// arangodb.hosts=172.28.0.1:8529
// arangodb.password=test
// ...

ArangoConfigProperties props = ArangoConfigProperties.fromFile();
```

To read config properties from `arangodb-with-prefix.properties` file, where the config properties
are prefixed with `adb`:

```java
// ## src/main/resources/arangodb-with-prefix.properties
// adb.hosts=172.28.0.1:8529
// adb.password=test
// ...

ArangoConfigProperties props = ArangoConfigProperties.fromFile("arangodb-with-prefix.properties", "adb");
```

Related reference documentation can be found [here](./v7_java-reference-setup.md#config-file-properties).

Here are some examples showing how to provide configuration properties from different sources:
- [Eclipse MicroProfile Config](https://github.com/arangodb-helper/arango-quarkus-native-example/blob/master/src/main/java/org/acme/quickstart/ArangoConfig.java)
- [Micronaut Configuration](https://github.com/arangodb-helper/arango-micronaut-native-example/blob/main/src/main/kotlin/com/example/ArangoConfig.kt)


## Modules

Support for different serdes and communication protocols is offered by separate modules. 
Defaults modules are transitively included, but they could be excluded if not needed.

The main driver artifact `com.arangodb:arangodb-java-driver` has transitive dependencies on default modules:
- `com.arangodb:http-protocol`: `HTTP` communication protocol (HTTP/1.1 and HTTP/2)
- `com.arangodb:jackson-serde-json`: `JSON` user-data serde module based on Jackson Databind

Alternative modules are respectively:
- `com.arangodb:vst-protocol`: `VST` communication protocol
- `com.arangodb:jackson-serde-vpack`: `VPACK` user-data serde module based on Jackson Databind

The modules above are discovered and loaded using SPI (Service Provider Interface).

In case a non-default communication protocol or user serde are used, the related module(s) must be explicitly included 
and the corresponding default module(s) can be excluded.

For example, to use the driver with `VPACK` over `VST`, we must include:
- `com.arangodb:vst-protocol` and
- `com.arangodb:jackson-serde-vpack`

and can exclude:
- `com.arangodb:http-protocol` and
- `com.arangodb:jackson-serde-json`
 
For example in Maven:

```xml
<dependencies>
    <dependency>
        <groupId>com.arangodb</groupId>
        <artifactId>arangodb-java-driver</artifactId>
        <exclusions>
            <exclusion>
                <groupId>com.arangodb</groupId>
                <artifactId>http-protocol</artifactId>
            </exclusion>
            <exclusion>
                <groupId>com.arangodb</groupId>
                <artifactId>jackson-serde-json</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    <dependency>
        <groupId>com.arangodb</groupId>
        <artifactId>vst-protocol</artifactId>
    </dependency>
    <dependency>
        <groupId>com.arangodb</groupId>
        <artifactId>jackson-serde-vpack</artifactId>
    </dependency>
</dependencies>
```


## Transitive dependencies

`com.arangodb:arangodb-java-driver` has transitive dependencies on `jackson-core`, `jackson-databind`
and `jackson-annotations`, using by default version `2.14`.

The versions of such libraries can be overridden, the driver is compatible with Jackson 2 (at least `2.10` or greater).

To do this, you might need to include [jackson-bom](https://github.com/FasterXML/jackson-bom)
to ensure dependency convergence across the entire project, for example in case
there are in your project other libraries depending on different versions of Jackson:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.fasterxml.jackson</groupId>
            <artifactId>jackson-bom</artifactId>
            <version>...</version>
            <scope>import</scope>
            <type>pom</type>
        </dependency>
    </dependencies>
</dependencyManagement>
```

The module `http-protocol` has transitive dependency on `io.vertx:vertx-web-client:4.3.5`, which in turn depends on 
packages from `io.netty`.

If these dependency requirements cannot be satisfied, i.e. they cause convergence conflicts with other versions of same
packages in the classpath, you might need to use the
[shaded version](#arangodb-java-driver-shaded) of this driver, which bundles all modules together with relocated
external dependencies.

The dependency on `com.arangodb:velocypack` has been removed from core module and is now only used as internal
dependency by `com.arangodb:vst-protocol` and `com.arangodb:jackson-serde-vpack`, thus transitively imported only when
using `VST` protocol or `VPACK` content type.

## User Data

Before version `7.0` the driver always parsed raw strings as JSON, but unfortunately this did not allow to distinguish
it from the case when the intent is to use the raw string as such, without parsing it. Since version `7.0`, strings are 
not interpreted as JSON anymore. To represent user-data as raw JSON, the wrapper class `com.arangodb.util.RawJson` has 
been added:

```java
RawJson rawJsonIn = RawJson.of("""
        {"foo":"bar"}
        """);
ArangoCursor<RawJson> res = adb.db().query("RETURN @v", Map.of("v", rawJsonIn), RawJson.class);
RawJson rawJsonOut = res.next();
String json = rawJsonOut.get();  // {"foo":"bar"}
```

To represent user-data already encoded as byte array, the wrapper class `RawBytes` has been added.
The byte array can either represent a `JSON` string (UTF-8 encoded) or a `VPACK` value. The format used should match the
driver protocol configuration (`JSON` for `HTTP_JSON` and `HTTP2_JSON`, `VPACK` otherwise).

The following changes have been applied to `com.arangodb.entity.BaseDocument` and `com.arangodb.entity.BaseEdgeDocument`:
- not serializable anymore (using Java serialization)
- new method `removeAttribute(String)`
- `getProperties()` returns an unmodifiable map

Before version `7.0` when performing write operations, the metadata of the input data objects was updated in place with 
the metadata received in the response.
Since version `7.0`, the input data objects passed as arguments to API methods are treated as immutable and the related 
metadata fields are not updated anymore. The updated metadata can be found in the returned object.


## Serialization / Deserialization

Up to version 6, the (de)serialization was always performed to/from `VPACK`. In case the JSON format was
required, the raw `VPACK` was then converted to `JSON`. Since version 7, the serialization module is a dataformat
agnostic API. Implementations can serialize and deserialize directly to the target dataformat, without intermediate 
conversion to `VPACK`.
The default implementation uses the `JSON` format. 

Support for data type `VPackSlice` has been removed in favor of Jackson type `com.fasterxml.jackson.databind.JsonNode`
and its subclasses (`ArrayNode`, `ObjectNode`, ...), for example:

```java
JsonNode jsonNodeIn = JsonNodeFactory.instance
        .objectNode()
        .put("foo", "bar");

ArangoCursor<JsonNode> res = adb.db().query("RETURN @v", Map.of("v", jsonNodeIn), JsonNode.class);
JsonNode jsonNodeOut = res.next();
String foo = jsonNodeOut.get("foo").textValue();    // bar
```

The dependency on `com.arangodb:velocypack` has been removed. Since version 3.0, `com.arangodb:velocypack` does not 
include databind functionalities anymore. It only offers a lower level API to deal with primitive `VPACK` types.
`VPACK` databind functionalities are now offered by 
[`jackson-dataformat-velocypack`](https://github.com/arangodb/jackson-dataformat-velocypack), which is a Jackson backend
for `VPACK` dataformat.

The user-data custom serializer implementation `ArangoJack` has been removed in favor of 
`com.arangodb.serde.jackson.JacksonSerde`. This allows using Jackson API to serialize and deserialize user-data, 
compatible with both `JSON` and `VPACK`.

Detailed documentation about serialization can be found [here](v7_java-reference-serialization.md). 


## ArangoDB Java Driver Shaded

Since version `7`, a shaded variant of the driver is also published with maven coordinates:
`com.arangodb:arangodb-java-driver-shaded`.

It bundles and relocates the following packages:
- `com.fasterxml.jackson`
- `com.arangodb.jackson.dataformat.velocypack`
- `io.vertx`
- `io.netty`

Note that the **internal serde** uses internally Jackson classes from `com.fasterxml.jackson` that are relocated 
to `com.arangodb.shaded.fasterxml.jackson`. Therefore, the **internal serde** of the shaded driver is not
compatible with Jackson annotations and modules from package`com.fasterxml.jackson`, but only with their relocated 
variants. In case the **internal serde** is used as **user-data serde**, the annotations from package 
`com.arangodb.serde` can be used to annotate fields, parameters, getters and setters for mapping values representing 
ArangoDB documents metadata (`_id`, `_key`, `_rev`, `_from`, `_to`):
- `@InternalId`
- `@InternalKey`
- `@InternalRev`
- `@InternalFrom`
- `@InternalTo`

These annotations are compatible with relocated Jackson classes.
Note that the **internal serde** is not part of the public API and could change in future releases without notice, thus
breaking client applications relying on it to serialize or deserialize user-data. It is therefore recommended also in
this case either:
- using the default user-data serde `JacksonSerde` (from packages `com.arangodb:jackson-serde-json` or
`com.arangodb:jackson-serde-vpack`), or
- providing a custom user-data serde implementation via `ArangoDB.Builder.serde(ArangoSerde)`. 

## Removed APIs

The following client APIs have been removed:

- client APIs already deprecated in Java Driver version `6`
- client API to interact with deprecated server APIs:
    - `MMFiles` related APIs
    - `ArangoDatabase.executeTraversal()`
    - `ArangoDB.getLogs()`
    - `minReplicationFactor` in collections and graphs
    - `overwrite` flag in `DocumentCreateOptions`
    - `hash` and `skipList` indexes

The user-data custom serializer implementation `ArangoJack` has been removed in favor of 
`com.arangodb.serde.jackson.JacksonSerde`.

Support for interpreting raw strings as JSON has been removed (in favor of `com.arangodb.util.RawJson`).

Support of data type `com.arangodb.velocypack.VPackSlice` has been removed in favor of Jackson type 
`com.fasterxml.jackson.databind.JsonNode` and its subclasses (`ArrayNode`, `ObjectNode`, ...). 
Raw `VPACK` data can be used now with `com.arangodb.util.RawBytes`).

Support for custom initialization of
cursors (`ArangoDB._setCursorInitializer(ArangoCursorInitializer cursorInitializer)`) has been removed.


### Async API

The asynchronous API (before under the package `com.arangodb.async`) has been removed from version 7.0 (#487).
This has been done because the asynchronous API needs a substantial refactoring, i.e. supporting 
HTTP protocol, fixing #433 and a better alignment with the synchronous API.
It will be reworked and re-added in a future version 7.x.


## API methods changes

Before version `7.0` some CRUD API methods inferred the return type from the type of the data object passed as input.
Now the return type can be explicitly set for each CRUD API method. This type is used as deserialization target by
the user-data serde. In particular, no automatic type inference is performed anymore in:
- `ArangoCollection.insertDocuments()`
- `ArangoCollection.replaceDocuments()`
- `ArangoCollection.updateDocuments()`
- `ArangoCollection.deleteDocuments()`
Unless specified explicitly, the target deserialization type is `Void.class`, thus allowing only `null` values. 

CRUD operations operating with multiple documents have now an overloaded variant which accepts raw data (`RawBytes`
and `RawJson`) containing multiple documents.

CRUD operations operating with multiple documents with parametric types are now covariant:
- `ArangoCollection.insertDocuments(Collection<? extends T>, DocumentCreateOptions, Class<T>)`
- `ArangoCollection.replaceDocuments(Collection<? extends T>, DocumentReplaceOptions, Class<T>)`

`com.arangodb.Request` and `com.arangodb.Response` classes have been refactored to support generic body type. 
`ArangoDB.execute(Request<T>, Class<U>): Response<U>` accepts now the target deserialization type for the response body.

`com.arangodb.ArangoDBException` has been enhanced with the id of the request causing it. This can be useful to 
correlate it with the debug level logs.

Graph API has been updated (#486):
- added `com.arangodb.ArangoEdgeCollection.drop()` and overloads
- added `com.arangodb.ArangoVertexCollection.drop(VertexCollectionDropOptions)`
- updated `com.arangodb.ArangoGraph.replaceEdgeDefinition()`

`ArangoDatabase.getDocument()` has been removed, in favor of `ArangoCollection.getDocument()`.

`ArangoDatabase.query()` overloaded variants have now a different order of parameters:
- `query(String query, Class<T> type)`
- `query(String query, Class<T> type, AqlQueryOptions options)`
- `query(String query, Class<T> type, Map<String,Object> bindVars)`
- `query(String query, Class<T> type, Map<String,Object> bindVars, AqlQueryOptions options)`


## API entities

All entities and options classes (in packages `com.arangodb.model` and `com.arangodb.entity`) are now `final`.

The replication factor is now modeled with a new interface (`com.arangodb.entity.ReplicationFactor`) with
implementations: `NumericReplicationFactor` and `SatelliteReplicationFactor`.

Cursor statistics are now in `com.arangodb.entity.CursorStats`.


## GraalVM Native Image

The driver supports GraalVM Native Image compilation.
To compile with `--link-at-build-time` when `http-protocol` module is present in the classpath, additional substitutions
are be required for its transitive dependencies (`Netty` and `Vert.x`). An example of this can be found
[here](../driver/src/test/java/graal). Such substitutions are not required when compiling the shaded driver.


### Quarkus and Micronaut examples

The driver can be used from Quarkus and Micronaut applications and does not
require any additional configuration for GraalVM native image generation.
Examples can be found here:

- [arango-quarkus-native-example](https://github.com/arangodb-helper/arango-quarkus-native-example)
- [arango-micronaut-native-example](https://github.com/arangodb-helper/arango-micronaut-native-example)


## Migration

To migrate your existing project to Java Driver version `7.0`, it is recommended first updating to the latest version of 
branch `6` and make sure that your code does not use any deprecated API. 
This can be done by checking the presence of deprecation warnings in the Java compiler output.

The deprecation notes in the related javadoc contain information about the reason of the deprecation and suggest
migration alternatives to use.
