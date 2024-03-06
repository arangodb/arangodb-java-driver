# Tutorial: Java in 10 Minutes

This is a short tutorial with the [Java Driver](https://github.com/arangodb/arangodb-java-driver) and ArangoDB. In less
than 10 minutes you can learn how to use ArangoDB Java driver in Maven and Gradle projects.


## Project configuration

To use the ArangoDB Java driver, you need to import
[arangodb-java-driver](https://github.com/arangodb/arangodb-java-driver)
as a library into your project.

In a Maven project, you need to add the following dependency to `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>com.arangodb</groupId>
        <artifactId>arangodb-java-driver</artifactId>
        <version>...</version>
    </dependency>
</dependencies>
```

In a Gradle project, you need to add the following to `build.gradle`:

```groovy
dependencies {
    implementation 'com.arangodb:arangodb-java-driver:...'
}
```


## Connection

Let's configure and open a connection to start ArangoDB.

```java
ArangoDB arangoDB = new ArangoDB.Builder()
        .host("localhost", 8529)
        .build();
```

> **Hint:** The default connection is to 127.0.0.1:8529.


## Creating a database

Let’s create a new database:

```java
ArangoDatabase db = arangoDB.db("mydb");
System.out.println("Creating database...");
db.create();
```


## Creating a collection

Now let’s create our first collection:

```java
ArangoCollection collection = db.collection("firstCollection");
System.out.println("Creating collection...");
collection.create();
```


## Creating a document

Now we create a document in the collection. Any object can be added as a document to the database and be retrieved from
the database as an object.

For this example we use the class BaseDocument, provided with the driver. The attributes of the document are stored in a
map as key<String>/value<Object> pair:

```java
String key = "myKey";
BaseDocument doc = new BaseDocument(key);
doc.addAttribute("a", "Foo");
doc.addAttribute("b", 42);
System.out.println("Inserting document...");
collection.insertDocument(doc);
```

Some details you should know about the code:

- the document key is passed to the `BaseDocument` constructor
- `addAttribute()` puts a new key/value pair into the document
- each attribute is stored as a single key value pair in the document root


## Read a document

To read the created document:

```java
System.out.println("Reading document...");
BaseDocument readDocument = collection.getDocument(key, BaseDocument.class);
System.out.println("Key: " + readDocument.getKey());
System.out.println("Attribute a: " + readDocument.getAttribute("a"));
System.out.println("Attribute b: " + readDocument.getAttribute("b"));
```

After executing this program the console output should be:

```text
Key: myKey
Attribute a: Foo
Attribute b: 42
```

Some details you should know about the code:

- `getDocument()` reads the stored document data and deserilizes it into the given class (`BaseDocument`)


## Creating a document from Jackson JsonNode

We can also create a document from a Jackson [JsonNode](https://fasterxml.github.io/jackson-databind/javadoc/2.13/com/fasterxml/jackson/databind/JsonNode.html) object:

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


## Read a document as Jackson JsonNode

Documents can also be read as Jackson [JsonNode](https://fasterxml.github.io/jackson-databind/javadoc/2.13/com/fasterxml/jackson/databind/JsonNode.html):

```java
System.out.println("Reading document as Jackson JsonNode...");
JsonNode readJsonNode = collection.getDocument(keyJackson, JsonNode.class);
System.out.println("Key: " + readJsonNode.get("_key").textValue());
System.out.println("Attribute a: " + readJsonNode.get("a").textValue());
System.out.println("Attribute b: " + readJsonNode.get("b").intValue());
```

After executing this program the console output should be:

```text
Key: myKey
Attribute a: Bar
Attribute b: 53
```

Some details you should know about the code:

- `getDocument()` returns the stored document as instance of `com.fasterxml.jackson.databind.JsonNode`.


## Creating a document from JSON String

Documents can also be created from raw JSON strings:

```java
System.out.println("Creating a document from JSON String...");
String keyJson = "myJsonKey";
RawJson json = RawJson.of("{\"_key\":\"" + keyJson + "\",\"a\":\"Baz\",\"b\":64}");
System.out.println("Inserting document from JSON String...");
collection.insertDocument(json);
```

## Read a document as JSON String

Documents can also be read as raw JSON strings:

```java
System.out.println("Reading document as JSON String...");
RawJson readJson = collection.getDocument(keyJson, RawJson.class);
System.out.println(readJson.get());
```

After executing this program the console output should be:

```text
{"_key":"myJsonKey","_id":"firstCollection/myJsonKey","_rev":"_e0nEe2y---","a":"Baz","b":64}
```


## Update a document

Let's update the document:

```java
doc.addAttribute("c", "Bar");
System.out.println("Updating document ...");
collection.updateDocument(key, doc);
```


## Read the document again

Let’s read the document again:

```java
System.out.println("Reading updated document ...");
BaseDocument updatedDocument = collection.getDocument(key, BaseDocument.class);
System.out.println("Key: " + updatedDocument.getKey());
System.out.println("Attribute a: " + updatedDocument.getAttribute("a"));
System.out.println("Attribute b: " + updatedDocument.getAttribute("b"));
System.out.println("Attribute c: " + updatedDocument.getAttribute("c"));
```

After executing this program the console output should look like this:

```text
Key: myKey
Attribute a: Foo
Attribute b: 42
Attribute c: Bar
```


## Delete a document

Let’s delete a document:

```java
System.out.println("Deleting document ...");
collection.deleteDocument(key);
```


## Execute AQL queries

First we need to create some documents with the name Homer in collection firstCollection:

```java
for (int i = 0; i < 10; i++) {
    BaseDocument value = new BaseDocument(String.valueOf(i));
    value.addAttribute("name", "Homer");
    collection.insertDocument(value);
}
```

Get all documents with the name Homer from collection firstCollection and iterate over the result:

```java
String query = "FOR t IN firstCollection FILTER t.name == @name RETURN t";
Map<String, Object> bindVars = Collections.singletonMap("name", "Homer");
System.out.println("Executing read query ...");
ArangoCursor<BaseDocument> cursor = db.query(query, bindVars, null, BaseDocument.class);
cursor.forEach(aDocument -> System.out.println("Key: " + aDocument.getKey()));
```

After executing this program the console output should look something like this:

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

- the AQL query uses the placeholder `@name` which has to be bind to a value
- `query()` executes the defined query and returns a `ArangoCursor` with the given class (here: `BaseDocument`)
- the order is not guaranteed


## Delete a document with AQL

Now we will delete the document created before:

```java
String query = "FOR t IN firstCollection FILTER t.name == @name "
    + "REMOVE t IN firstCollection LET removed = OLD RETURN removed";
Map<String, Object> bindVars = Collections.singletonMap("name", "Homer");
System.out.println("Executing delete query ...");
ArangoCursor<BaseDocument> cursor = db.query(query, bindVars, null, BaseDocument.class);
cursor.forEach(aDocument -> System.out.println("Removed document " + aDocument.getKey()));
```

After executing this program the console output should look something like this:

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

## Learn more

- Have a look at the [AQL documentation](https://docs.arangodb.com/stable/aql/) to learn more about the query language.
- Also check out the documentation about ArangoDB's [Data Models](https://docs.arangodb.com/stable/concepts/data-models/)
