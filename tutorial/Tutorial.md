# ArangoDB Java driver

The official ArangoDB Java Driver.

- Repository: <https://github.com/arangodb/arangodb-java-driver>
- [Code examples](https://github.com/arangodb/arangodb-java-driver/tree/main/test-non-functional/src/test/java/example)
- [Reference](reference-version-7/_index.md) (driver setup, serialization, changes in version 7)
- [JavaDoc](https://www.javadoc.io/doc/com.arangodb/arangodb-java-driver/latest/index.html) (generated reference documentation)
- [ChangeLog](https://github.com/arangodb/arangodb-java-driver/blob/main/ChangeLog.md)

## Supported versions

Version 7 is the latest supported and actively developed release.

The driver is compatible with all supported stable versions of ArangoDB server, see
[Product Support End-of-life Announcements](https://arangodb.com/subscriptions/end-of-life-notice/).

The driver is compatible with JDK 8 and higher versions.

{{< warning >}}
Version 6 reached End of Life (EOL) and is not actively developed anymore.
Upgrading to version 7 is recommended.

The API changes between version 6 and 7 are documented in
[Changes in version 7](reference-version-7/changes-in-version-7.md).
{{< /warning >}}

## Project configuration

To use the ArangoDB Java driver, you need to import `arangodb-java-driver` as a
library into your project. This is described below for the popular Java build
automation systems Maven and Gradle.

### Maven

To add the driver to your project with Maven, add the following code to your
`pom.xml` (substitute `7.x.x` with the latest driver version):

```xml
<dependencies>
  <dependency>
    <groupId>com.arangodb</groupId>
    <artifactId>arangodb-java-driver</artifactId>
    <version>7.x.x</version>
  </dependency>
</dependencies>
```

### Gradle

To add the driver to your project with Gradle, add the following code to your
`build.gradle` (substitute `7.x.x` with the latest driver version):

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'com.arangodb:arangodb-java-driver:7.x.x'
}
```

## Tutorial

### Connect to ArangoDB

Let's configure and open a connection to ArangoDB. The default connection is to
`127.0.0.1:8529`. Change the connection details to point to your specific instance.

```java
ArangoDB arangoDB = new ArangoDB.Builder()
        .host("localhost", 8529)
        .user("root")
        .password("")
        .build();
```

For more connections options and details, see
[Driver setup](reference-version-7/driver-setup.md).

### Create a database

Let's create a new database:

```java
ArangoDatabase db = arangoDB.db("mydb");
System.out.println("Creating database...");
db.create();
```

### Create a collection

Now let's create our first collection:

```java
ArangoCollection collection = db.collection("firstCollection");
System.out.println("Creating collection...");
collection.create();
```

### Create a document

Let's create a document in the collection. Any object can be added as a document
to the database and be retrieved from the database as an object.

This example uses the `BaseDocument` class, provided with the driver. The
attributes of the document are stored in a map as `key<String>`/`value<Object>` pair:

```java
String key = "myKey";
BaseDocument doc = new BaseDocument(key);
doc.addAttribute("a", "Foo");
doc.addAttribute("b", 42);
System.out.println("Inserting document...");
collection.insertDocument(doc);
```

Some details you should know about the code:

- The document key is passed to the `BaseDocument` constructor
- The `addAttribute()` method puts a new key/value pair into the document
- Each attribute is stored as a single key value pair in the document root

### Read a document

Read the created document:

```java
System.out.println("Reading document...");
BaseDocument readDocument = collection.getDocument(key, BaseDocument.class);
System.out.println("Key: " + readDocument.getKey());
System.out.println("Attribute a: " + readDocument.getAttribute("a"));
System.out.println("Attribute b: " + readDocument.getAttribute("b"));
```

After executing this program, the console output should be:

```text
Key: myKey
Attribute a: Foo
Attribute b: 42
```

Some details you should know about the code:

- The `getDocument()` method reads the stored document data and deserializes it
  into the given class (`BaseDocument`)

### Create a document from Jackson JsonNode

You can also create a document from a Jackson
[JsonNode](https://fasterxml.github.io/jackson-databind/javadoc/2.13/com/fasterxml/jackson/databind/JsonNode.html)
object:

```java
System.out.println("Creating a document from Jackson JsonNode...");
String keyJackson = "myJacksonKey";
JsonNode jsonNode = JsonNodeFactory.instance.objectNode()
        .put("_key", keyJackson)
        .put("a", "Bar")
        .put("b", 53);
System.out.println("Inserting document from Jackson JsonNode...");
collection.insertDocument(jsonNode);
```

### Read a document as Jackson JsonNode

You can also read a document as a Jackson
[JsonNode](https://fasterxml.github.io/jackson-databind/javadoc/2.13/com/fasterxml/jackson/databind/JsonNode.html):

```java
System.out.println("Reading document as Jackson JsonNode...");
JsonNode readJsonNode = collection.getDocument(keyJackson, JsonNode.class);
System.out.println("Key: " + readJsonNode.get("_key").textValue());
System.out.println("Attribute a: " + readJsonNode.get("a").textValue());
System.out.println("Attribute b: " + readJsonNode.get("b").intValue());
```

After executing this program, the console output should be:

```text
Key: myKey
Attribute a: Bar
Attribute b: 53
```

Some details you should know about the code:

- The `getDocument()` method returns the stored document as instance of
  `com.fasterxml.jackson.databind.JsonNode`.

### Create a document from JSON String

You can also create a document from raw JSON string:

```java
System.out.println("Creating a document from JSON String...");
String keyJson = "myJsonKey";
RawJson json = RawJson.of("{\"_key\":\"" + keyJson + "\",\"a\":\"Baz\",\"b\":64}");
System.out.println("Inserting document from JSON String...");
collection.insertDocument(json);
```

### Read a document as JSON String

You can also read a document as raw JSON string:

```java
System.out.println("Reading document as JSON String...");
RawJson readJson = collection.getDocument(keyJson, RawJson.class);
System.out.println(readJson.get());
```

After executing this program, the console output should be:

```text
{"_key":"myJsonKey","_id":"firstCollection/myJsonKey","_rev":"_e0nEe2y---","a":"Baz","b":64}
```

### Update a document

Let's update the document:

```java
doc.addAttribute("c", "Bar");
System.out.println("Updating document ...");
collection.updateDocument(key, doc);
```

### Read the document again

Let's read the document again:

```java
System.out.println("Reading updated document ...");
BaseDocument updatedDocument = collection.getDocument(key, BaseDocument.class);
System.out.println("Key: " + updatedDocument.getKey());
System.out.println("Attribute a: " + updatedDocument.getAttribute("a"));
System.out.println("Attribute b: " + updatedDocument.getAttribute("b"));
System.out.println("Attribute c: " + updatedDocument.getAttribute("c"));
```

After executing this program, the console output should look like this:

```text
Key: myKey
Attribute a: Foo
Attribute b: 42
Attribute c: Bar
```

### Delete a document

Let's delete a document:

```java
System.out.println("Deleting document ...");
collection.deleteDocument(key);
```

### Execute AQL queries

First, you need to create some documents with the name `Homer` in the
collection called `firstCollection`:

```java
for (int i = 0; i < 10; i++) {
    BaseDocument value = new BaseDocument(String.valueOf(i));
    value.addAttribute("name", "Homer");
    collection.insertDocument(value);
}
```

Get all documents with the name `Homer` from the collection using an AQL query
and iterate over the results:

```java
String query = "FOR t IN firstCollection FILTER t.name == @name RETURN t";
Map<String, Object> bindVars = Collections.singletonMap("name", "Homer");
System.out.println("Executing read query ...");
ArangoCursor<BaseDocument> cursor = db.query(query, bindVars, null, BaseDocument.class);
cursor.forEach(aDocument -> System.out.println("Key: " + aDocument.getKey()));
```

After executing this program, the console output should look something like this:

```text
Key: 1
Key: 0
Key: 5
Key: 3
Key: 4
Key: 9
Key: 2
Key: 7
Key: 8
Key: 6
```

Some details you should know about the code:

- The AQL query uses the placeholder `@name` that has to be bound to a value
- The `query()` method executes the defined query and returns an `ArangoCursor`
  with the given class (here: `BaseDocument`)
- The order of is not guaranteed

### Delete documents with AQL

Delete previously created documents:

```java
String query = "FOR t IN firstCollection FILTER t.name == @name "
    + "REMOVE t IN firstCollection LET removed = OLD RETURN removed";
Map<String, Object> bindVars = Collections.singletonMap("name", "Homer");
System.out.println("Executing delete query ...");
ArangoCursor<BaseDocument> cursor = db.query(query, bindVars, null, BaseDocument.class);
cursor.forEach(aDocument -> System.out.println("Removed document " + aDocument.getKey()));
```

After executing this program, the console output should look something like this:

```text
Removed document: 1
Removed document: 0
Removed document: 5
Removed document: 3
Removed document: 4
Removed document: 9
Removed document: 2
Removed document: 7
Removed document: 8
Removed document: 6
```

### Learn more

- Have a look at the [AQL documentation](../../../aql/) to lear about the
  query language
- See [Serialization](reference-version-7/serialization.md) for details about
  user-data serde
- For the full reference documentation, see
  [JavaDoc](https://www.javadoc.io/doc/com.arangodb/arangodb-java-driver/latest/index.html)

## GraalVM Native Image

The driver supports GraalVM Native Image compilation.
To compile with `--link-at-build-time` when `http-protocol` module is present in
the classpath, additional substitutions are required for transitive dependencies
`Netty` and `Vert.x`. See this
[example](https://github.com/arangodb/arangodb-java-driver/tree/main/test-functional/src/test-default/java/graal)
for reference. Such substitutions are not required when compiling the shaded driver.

### Framework compatibility

The driver can be used in the following frameworks that support
GraalVM Native Image generation:

- [Quarkus](https://quarkus.io), see [arango-quarkus-native-example](https://github.com/arangodb-helper/arango-quarkus-native-example)
- [Helidon](https://helidon.io), see [arango-helidon-native-example](https://github.com/arangodb-helper/arango-helidon-native-example)
- [Micronaut](https://micronaut.io), see [arango-micronaut-native-example](https://github.com/arangodb-helper/arango-micronaut-native-example)

## ArangoDB Java Driver Shaded

A shaded variant of the driver is also published with
Maven coordinates: `com.arangodb:arangodb-java-driver-shaded`.

It bundles and relocates the following packages:
- `com.fasterxml.jackson`
- `com.arangodb.jackson.dataformat.velocypack`
- `io.vertx`
- `io.netty`

Note that the **internal serde** internally uses Jackson classes from
`com.fasterxml.jackson` that are relocated to `com.arangodb.shaded.fasterxml.jackson`.
Therefore, the **internal serde** of the shaded driver is not compatible with
Jackson annotations and modules from package`com.fasterxml.jackson`, but only
with their relocated variants. In case the **internal serde** is used as
**user-data serde**, the annotations from package `com.arangodb.serde` can be
used to annotate fields, parameters, getters and setters for mapping values
representing ArangoDB documents metadata (`_id`, `_key`, `_rev`, `_from`, `_to`):
- `@InternalId`
- `@InternalKey`
- `@InternalRev`
- `@InternalFrom`
- `@InternalTo`

These annotations are compatible with relocated Jackson classes.
Note that the **internal serde** is not part of the public API and could change
in future releases without notice, thus breaking client applications relying on
it to serialize or deserialize user-data. It is therefore recommended also in
this case either:
- using the default user-data serde `JacksonSerde`
  (from packages `com.arangodb:jackson-serde-json` or `com.arangodb:jackson-serde-vpack`), or
- providing a custom user-data serde implementation via `ArangoDB.Builder.serde(ArangoSerde)`.

## Support for extended naming constraints

The driver supports ArangoDB's **extended** naming constraints/convention,
allowing most UTF-8 characters in the names of:
- Databases
- Collections
- Views
- Indexes

These names must be NFC-normalized, otherwise the server returns an error.
To normalize a string, use the function
`com.arangodb.util.UnicodeUtils.normalize(String): String`:

```java 
String normalized = UnicodeUtils.normalize("𝔸𝕣𝕒𝕟𝕘𝕠𝔻𝔹");
```

To check if a string is already normalized, use the
function `com.arangodb.util.UnicodeUtils.isNormalized(String): boolean`:

```java 
boolean isNormalized = UnicodeUtils.isNormalized("𝔸𝕣𝕒𝕟𝕘𝕠𝔻𝔹");
```

## Async API

The asynchronous API is accessible via `ArangoDB#async()`, for example:

```java
ArangoDB adb = new ArangoDB.Builder()
    // ...
    .build();
ArangoDBAsync adbAsync = adb.async();
CompletableFuture<ArangoDBVersion> version = adbAsync.getVersion();
// ...
```

Under the hood, both synchronous and asynchronous API use the same internal
communication layer, which has been reworked and re-implemented in an
asynchronous way. The synchronous API blocks and waits for the result, while the
asynchronous one returns a `CompletableFuture<>` representing the pending
operation being performed.
Each asynchronous API method is equivalent to the corresponding synchronous
variant, except for the Cursor API.

### Async Cursor API

The Cursor API (`ArangoCursor` and `ArangoCursorAsync`) is intrinsically different,
because the synchronous Cursor API is based on Java's `java.util.Iterator`, which
is an interface only suitable for synchronous scenarios.
On the other side, the asynchronous Cursor API provides a method
`com.arangodb.ArangoCursorAsync#nextBatch()`, which returns a
`CompletableFuture<ArangoCursorAsync<T>>` and can be used to consume the next
batch of the cursor, for example:

```java
CompletableFuture<ArangoCursorAsync<Integer>> future1 = adbAsync.db()
        .query("FOR i IN i..10000", Integer.class);
CompletableFuture<ArangoCursorAsync<Integer>> future2 = future1
        .thenCompose(c -> {
            List<Integer> batch = c.getResult();
            // ...
            // consume batch
            // ...
            return c.nextBatch();
        });
// ...
```

## Data Definition Classes

Classes used to exchange data definitions, in particular classes in the packages
`com.arangodb.entity.**` and `com.arangodb.model.**`, are meant to be serialized
and deserialized internally by the driver.

The behavior to serialize and deserialize these classes is considered an internal
implementation detail, and as such, it might change without prior notice.
The API with regard to the public members of these classes is kept compatible.
