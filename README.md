
![ArangoDB-Logo](https://docs.arangodb.com/assets/arangodb_logo_2016_inverted.png)

# arangodb-java-driver

2.7: [![Build Status](https://secure.travis-ci.org/arangodb/arangodb-java-driver.svg?branch=2.7)](https://travis-ci.org/arangodb/arangodb-java-driver) 
3.0: [![Build Status](https://secure.travis-ci.org/arangodb/arangodb-java-driver.svg?branch=3.0)](https://travis-ci.org/arangodb/arangodb-java-driver) 
3.1: [![Build Status](https://secure.travis-ci.org/arangodb/arangodb-java-driver.svg?branch=3.1)](https://travis-ci.org/arangodb/arangodb-java-driver) 
4.1: [![Build Status](https://secure.travis-ci.org/arangodb/arangodb-java-driver.svg?branch=4.1)](https://travis-ci.org/arangodb/arangodb-java-driver) 
4.2: [![Build Status](https://secure.travis-ci.org/arangodb/arangodb-java-driver.svg?branch=4.2)](https://travis-ci.org/arangodb/arangodb-java-driver) 
master: [![Build Status](https://secure.travis-ci.org/arangodb/arangodb-java-driver.svg?branch=master)](https://travis-ci.org/arangodb/arangodb-java-driver)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.arangodb/arangodb-java-driver/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.arangodb/arangodb-java-driver)

## Supported versions

<table>
<tr><th>arangodb-java-driver</th><th>ArangoDB</th><th>network protocol</th><th>Java version</th></tr>
<tr><td>4.2.x</td><td>3.0.x, 3.1.x, 3.2.x</td><td>VelocyStream, HTTP</td><td>1.6+</td></tr>
<tr><td>4.1.x</td><td>3.1.x, 3.2.x</td><td>VelocyStream</td><td>1.6+</td></tr>
<tr><td>3.1.x</td><td>3.1.x, 3.2.x</td><td>HTTP</td><td>1.6+</td></tr>
<tr><td>3.0.x</td><td>3.0.x</td><td>HTTP</td><td>1.6+</td></tr>
<tr><td>2.7.4</td><td>2.7.x, 2.8.x</td><td>HTTP</td><td>1.6+</td></tr>
</table>

**Note**: VelocyStream is only supported in ArangoDB 3.1 and above.

## Maven

To add the driver to your project with maven, add the following code to your pom.xml
(please use a driver with a version number compatible to your ArangoDB server's version):

ArangoDB 3.x.x
```XML
<dependencies>
  <dependency>
    <groupId>com.arangodb</groupId>
    <artifactId>arangodb-java-driver</artifactId>
    <version>4.2.0</version>
  </dependency>
	....
</dependencies>
```

If you want to test with a snapshot version (e.g. 4.2.0-SNAPSHOT), add the staging repository of oss.sonatype.org to your pom.xml:

```XML
<repositories>
  <repository>
    <id>arangodb-snapshots</id>
    <url>https://oss.sonatype.org/content/groups/staging</url>
  </repository>
</repositories>
```

## Compile java driver

```
mvn clean install -DskipTests=true -Dgpg.skip=true -Dmaven.javadoc.skip=true -B
```	


## Table of Contents

* [Driver setup](#driver-setup)
  * [Network protocol](#network-protocol)
  * [SSL](#ssl)
  * [Connection pooling](#connection-pooling)
  * [configure VelocyPack serialization](#configure-velocypack-serialization)
    * [Java 8 types](#java-8-types) 
    * [Scala types](#scala-types)
    * [Joda-Time](#joda-time)
    * [custom serializer](#custom-serializer)
* [Manipulating databases](#manipulating-databases)
  * [create database](#create-database)
  * [drop database](#drop-database)
* [Manipulating collections](#manipulating-collections)
  * [create collection](#create-collection)
  * [drop collection](#drop-collection)
  * [truncate collection](#truncate-collection)
* [Basic Document operations](#basic-document-operations)
  * [insert document](#insert-document)
  * [delete document](#delete-document)
  * [update document](#update-document)
  * [replace document](#replace-document)
  * [read document as JavaBean](#read-document-as-javabean)
  * [read document as VelocyPack](#read-document-as-velocypack)
  * [read document as Json](#read-document-as-json)
  * [read document by key](#read-document-by-key)
  * [read document by id](#read-document-by-id)
* [Multi document operations](#multi-document-operations)
  * [insert documents](#insert-documents)
  * [delete documents](#delete-documents)
  * [update documents](#update-documents)
  * [replace documents](#replace-documents)
* [AQL](#aql)
  * [executing an AQL statement](#executing-an-aql-statement)
* [Graphs](#graphs)
  * [add graph](#add-graph)
  * [delete graph](#delete-graph)
  * [add vertex](#add-vertex)
  * [add edge](#add-edge)
* [Foxx](#foxx)
  * [call a service](#call-a-service)
* [User management](#user-management)
  * [add user](#add-user)
  * [delete user](#delete-user)
  * [list users](#list-users)
  * [grant user access](#grant-user-access)
  * [revoke user access](#revoke-user-access)
* [Serialization](#serialization)
  * [JavaBeans](#javabeans)
  * [internal fields](#internal-fields)
  * [serialized fieldnames](#serialized-fieldnames)
  * [ignore fields](#ignore-fields)
  * [custom serializer](#custom-serializer)
  * [manually serialization](#manually-serialization)
* [Learn more](#learn-more)


# Driver setup
Setup with default configuration, this automatically loads a properties file arangodb.properties if exists in the classpath:

``` Java
  // this instance is thread-safe
  ArangoDB arangoDB = new ArangoDB.Builder().build();

```


The driver is configured with some default values:

<table>
<tr><th>property-key</th><th>description</th><th>default value</th></tr>
<tr><td>arangodb.hosts</td><td>ArangoDB hosts</td><td>127.0.0.1:8529</td></tr>
<tr><td>arangodb.timeout</td><td>socket connect timeout(millisecond)</td><td>0</td></tr>
<tr><td>arangodb.user</td><td>Basic Authentication User</td><td></td></tr>
<tr><td>arangodb.password</td><td>Basic Authentication Password</td><td></td></tr>
<tr><td>arangodb.useSsl</td><td>use SSL connection</td><td>false</td></tr>
<tr><td>arangodb.chunksize</td><td>VelocyStream Chunk content-size(bytes)</td><td>30000</td></tr>
<tr><td>arangodb.connections.max</td><td>max number of connections</td><td>1 VST, 20 HTTP</td></tr>
<tr><td>arangodb.protocol</td><td>used network protocol</td><td>VST</td></tr>
</table>

To customize the configuration the parameters can be changed in the code...

``` Java
  ArangoDB arangoDB = new ArangoDB.Builder().host("192.168.182.50", 8888).build();
  
```
... or with a custom properties file (my.properties)

``` Java
  InputStream in = MyClass.class.getResourceAsStream("my.properties");
  ArangoDB arangoDB = new ArangoDB.Builder().loadProperties(in).build();
  
```

Example for arangodb.properties:
``` Java
  arangodb.hosts=127.0.0.1:8529,127.0.0.1:8529
  arangodb.user=root
  arangodb.password=

```

## Network protocol

The drivers default used network protocol is the binary protocol VelocyStream which offers the best performance within the driver. To use HTTP, you have to set the configuration `useProtocol` to `Protocol.HTTP_JSON` for HTTP with Json content or `Protocol.HTTP_VPACK` for HTTP with [VelocyPack](https://github.com/arangodb/velocypack/blob/master/VelocyPack.md) content.

``` Java
  
  ArangoDB arangoDB = new ArangoDB.Builder().useProtocol(Protocol.VST).build();
  
```

**Note**: If you are using ArangoDB 3.0.x you have to set the protocol to `Protocol.HTTP_JSON` because it is the only one supported.

## SSL

To use SSL, you have to set the configuration `useSsl` to `true` and set a `SSLContext`. (see [example code](../src/test/java/com/arangodb/example/ssl/SslExample.java))

``` Java
  
  ArangoDB arangoDB = new ArangoDB.Builder().useSsl(true).sslContext(sc).build();
  
```

## Connection Pooling

The driver supports connection pooling with a default of 1 maximum connections. To change this value use the method `maxConnections(Integer)` in `ArangoDB.Builder`.

``` Java

  ArangoDB arangoDB = new ArangoDB.Builder().maxConnections(8).build();

```
## configure VelocyPack serialization

Since version `4.1.11` you can extend the VelocyPack serialization by registering additional `VPackModule`s on `ArangoDB.Builder`.

### Java 8 types

Added support for:
* java.time.Instant
* java.time.LocalDate
* java.time.LocalDateTime
* java.util.Optional;
* java.util.OptionalDouble;
* java.util.OptionalInt;
* java.util.OptionalLong;

```XML
<dependencies>
  <dependency>
    <groupId>com.arangodb</groupId>
    <artifactId>velocypack-module-jdk8</artifactId>
    <version>1.0.1</version>
  </dependency>
</dependencies>
```

``` Java
ArangoDB arangoDB = new ArangoDB.Builder().registerModule(new VPackJdk8Module()).build();
``` 

### Scala types

Added support for:
* scala.Option
* scala.collection.immutable.List
* scala.collection.immutable.Map

```XML
<dependencies>
  <dependency>
    <groupId>com.arangodb</groupId>
    <artifactId>velocypack-module-scala</artifactId>
    <version>1.0.0</version>
  </dependency>
</dependencies>
```

``` Scala
val arangoDB: ArangoDB = new ArangoDB.Builder().registerModule(new VPackScalaModule).build
``` 

### Joda-Time

Added support for:
* org.joda.time.DateTime;
* org.joda.time.Instant;
* org.joda.time.LocalDate;
* org.joda.time.LocalDateTime;

```XML
<dependencies>
  <dependency>
    <groupId>com.arangodb</groupId>
    <artifactId>velocypack-module-joda</artifactId>
    <version>1.0.0</version>
  </dependency>
</dependencies>
```

``` Java
ArangoDB arangoDB = new ArangoDB.Builder().registerModule(new VPackJodaModule()).build();
``` 

## custom serializer
``` Java
  ArangoDB arangoDB = new ArangoDB.Builder()
    .registerDeserializer(MyObject.class, new VPackDeserializer<MyObject>() {
      @Override
      public MyObject deserialize(
        final VPackSlice parent,
        final VPackSlice vpack,
        final VPackDeserializationContext context) throws VPackException {
        
          final MyObject obj = new MyObject();
          obj.setName(vpack.get("name").getAsString());
          return obj;
      }
    }).registerSerializer(MyObject.class, new VPackSerializer<MyObject>() {
      @Override
      public void serialize(
        final VPackBuilder builder,
        final String attribute,
        final MyObject value,
        final VPackSerializationContext context) throws VPackException {
        
          builder.add(attribute, ValueType.OBJECT);
          builder.add("name", value.getName());
          builder.close();
      }
    }).build();
``` 


# Manipulating databases

## create database
``` Java
  // create database 
  arangoDB.createDatabase("myDatabase");
  
```

## drop database
``` Java
  // drop database 
  arangoDB.db("myDatabase").drop();
  
```

# Manipulating collections

## create collection
``` Java
  // create collection
  arangoDB.db("myDatabase").createCollection("myCollection", null);
  
```

## drop collection
``` Java
  // delete collection 
  arangoDB.db("myDatabase").collection("myCollection").drop();
  
```

## truncate collection
``` Java
  arangoDB.db("myDatabase").collection("myCollection").truncate();
```

# Basic Document operations

Every document operations works with POJOs (e.g. MyObject), VelocyPack (VPackSlice) and Json (String).

For the next examples we use a small object:

``` Java
  public class MyObject {

    private String key;
    private String name;
    private int age;

    public MyObject(String name, int age) {
      this();
      this.name = name;
      this.age = age;
    }

    public MyObject() {
      super();
    }

    /*
     *  + getter and setter
     */
  
  }  
```

## insert document
``` Java
  MyObject myObject = new MyObject("Homer", 38);
  arangoDB.db("myDatabase").collection("myCollection").insertDocument(myObject);
    
```

When creating a document, the attributes of the object will be stored as key-value pair
E.g. in the previous example the object was stored as follows:
``` properties
  "name" : "Homer"
  "age" : "38"
```

## delete document
``` Java
  arangoDB.db("myDatabase").collection("myCollection").deleteDocument(myObject.getKey());
  
```

## update document
``` Java
  arangoDB.db("myDatabase").collection("myCollection").updateDocument(myObject.getKey(), myUpdatedObject);
  
```

## replace document
``` Java
  arangoDB.db("myDatabase").collection("myCollection").replaceDocument(myObject.getKey(), myObject2);
  
```

## read document as JavaBean
``` Java
  MyObject document = arangoDB.db("myDatabase").collection("myCollection").getDocument(myObject.getKey(), MyObject.class);
  document.getName();
  document.getAge();
  
```

## read document as VelocyPack
``` Java
  VPackSlice document = arangoDB.db("myDatabase").collection("myCollection").getDocument(myObject.getKey(), VPackSlice.class);
  document.get("name").getAsString();
  document.get("age").getAsInt();
  
```

## read document as Json
``` Java
  String json = arangoDB.db("myDatabase").collection("myCollection").getDocument(myObject.getKey(), String.class);
  
```

## read document by key
``` Java
  arangoDB.db("myDatabase").collection("myCollection").getDocument("myKey", MyObject.class);
  
```

## read document by id
``` Java
  arangoDB.db("myDatabase").getDocument("myCollection/myKey", MyObject.class);
  
```

# Multi Document operations

## insert documents
``` Java
  Collection<MyObject> documents = new ArrayList<>;
  documents.add(myObject1);
  documents.add(myObject2);
  documents.add(myObject3);
  arangoDB.db("myDatabase").collection("myCollection").insertDocuments(documents);
  
```

## delete documents
``` Java
  Collection<String> keys = new ArrayList<>;
  keys.add(myObject1.getKey());
  keys.add(myObject2.getKey());
  keys.add(myObject3.getKey());
  arangoDB.db("myDatabase").collection("myCollection").deleteDocuments(keys);
  
```

## update documents
``` Java
  Collection<MyObject> documents = new ArrayList<>;
  documents.add(myObject1);
  documents.add(myObject2);
  documents.add(myObject3);
  arangoDB.db("myDatabase").collection("myCollection").updateDocuments(documents);
  
```

## replace documents
``` Java
  Collection<MyObject> documents = new ArrayList<>;
  documents.add(myObject1);
  documents.add(myObject2);
  documents.add(myObject3);
  arangoDB.db("myDatabase").collection("myCollection").replaceDocuments(documents);
  
```

# AQL

## Executing an AQL statement

Every AQL operations works with POJOs (e.g. MyObject), VelocyPack (VPackSlice) and Json (String).

E.g. get all Simpsons aged 3 or older in ascending order:

``` Java
  arangoDB.createDatabase("myDatabase");
  ArangoDatabase db = arangoDB.db("myDatabase");
  
  db.createCollection("myCollection");
  ArangoCollection collection = db.collection("myCollection");
  
  collection.insertDocument(new MyObject("Homer", 38));
  collection.insertDocument(new MyObject("Marge", 36));
  collection.insertDocument(new MyObject("Bart", 10));
  collection.insertDocument(new MyObject("Lisa", 8));
  collection.insertDocument(new MyObject("Maggie", 2));
  
  Map<String, Object> bindVars = new HashMap<>();
  bindVars.put("age", 3);
  
  ArangoCursor<MyObject> cursor = db.query(query, bindVars, null, MyObject.class);
  
  for(; cursor.hasNext;) {
    MyObject obj = cursor.next();
    System.out.println(obj.getName());
  }
```

or return the AQL result as VelocyPack:

``` Java
  ArangoCursor<VPackSlice> cursor = db.query(query, bindVars, null, VPackSlice.class);
  
  for(; cursor.hasNext;) {
    VPackSlice obj = cursor.next();
    System.out.println(obj.get("name").getAsString());
  }
```

**Note**: The parameter `type` in `query()` has to match the result of the query, otherwise you get an VPackParserException. E.g. you set `type` to `BaseDocument` or a POJO and the query result is an array or simple type, you get an VPackParserException caused by VPackValueTypeException: Expecting type OBJECT.

# Graphs

The driver supports the [graph api](https://docs.arangodb.com/HTTP/Gharial/index.html).

Some of the basic graph operations are described in the following:

##add graph
A graph consists of vertices and edges (stored in collections). Which collections are used within a graph is defined via edge definitions. A graph can contain more than one edge definition, at least one is needed.

``` Java
  Collection<EdgeDefinition> edgeDefinitions = new ArrayList<>();
  EdgeDefinition edgeDefinition = new EdgeDefinition();
  // define the edgeCollection to store the edges
  edgeDefinition.collection("myEdgeCollection");
  // define a set of collections where an edge is going out...
  edgeDefinition.from("myCollection1", "myCollection2");
   
  // repeat this for the collections where an edge is going into  
  edgeDefinition.to("myCollection1", "myCollection3");
  
  edgeDefinitions.add(edgeDefinition);
  
  // A graph can contain additional vertex collections, defined in the set of orphan collections
  GraphCreateOptions options = new GraphCreateOptions();
  options.orphanCollections("myCollection4", "myCollection5");
  
  // now it's possible to create a graph
  arangoDB.db("myDatabase").createGraph("myGraph", edgeDefinitions, options);
  
```

## delete graph

A graph can be deleted by its name

``` Java
  arangoDB.db("myDatabase").graph("myGraph").drop();
```

## add vertex

Vertices are stored in the vertex collections defined above.

``` Java
  MyObject myObject1 = new MyObject("Homer", 38);
  MyObject myObject2 = new MyObject("Marge", 36);
  arangoDB.db("myDatabase").graph("myGraph").vertexCollection("collection1").insertVertex(myObject1, null);
  arangoDB.db("myDatabase").graph("myGraph").vertexCollection("collection3").insertVertex(myObject2, null);
  
```

## add edge

Now an edge can be created to set a relation between vertices

``` Java
  arangoDB.db("myDatabase").graph("myGraph").edgeCollection("myEdgeCollection").insertEdge(myEdgeObject, null);
 
``` 

# Foxx

## call a service
``` Java
  Request request = new Request("mydb", RequestType.GET, "/my/foxx/service")
  Response response = arangoDB.execute(request);
 
``` 

# User management

If you are using [authentication] (https://docs.arangodb.com/Manual/GettingStarted/Authentication.html) you can manage users with the driver.

## add user
``` Java
  //username, password
  arangoDB.createUser("myUser", "myPassword");
```

## delete user
``` Java
  arangoDB.deleteUser("myUser");
```

## list users
``` Java
  Collection<UserResult> users = arangoDB.getUsers();
  for(UserResult user : users) {
    System.out.println(user.getUser())
  }
```

## grant user access
``` Java
  arangoDB.db("myDatabase").grantAccess("myUser");
````

## revoke user access
``` Java
  arangoDB.db("myDatabase").revokeAccess("myUser");
````

# Serialization

## JavaBeans
The driver can serialize/deserialize JavaBeans. They need at least a constructor without parameter.

``` Java
  public class MyObject {

    private String name;
    private Gender gender;
    private int age;

    public MyObject() {
      super();
    }

  }  
```

## internal fields
To use Arango-internal fields (like _id, _key, _rev, _from, _to) in your JavaBeans, use the annotation `DocumentField`.

``` Java
  public class MyObject {

    @DocumentField(Type.KEY)
    private String key;
    
    private String name;
    private Gender gender;
    private int age;

    public MyObject() {
      super();
    }

  }  
```

## serialized fieldnames
To use a different serialized name for a field, use the annotation `SerializedName`.

``` Java
  public class MyObject {

    @SerializedName("title")
    private String name;
    
    private Gender gender;
    private int age;

    public MyObject() {
      super();
    }

  }  
```

## ignore fields
To ignore fields at serialization/deserialization, use the annotation `Expose`

``` Java
  public class MyObject {

    @Expose
    private String name;
    @Expose(serialize = true, deserialize = false)
    private Gender gender;
    private int age;

    public MyObject() {
      super();
    }

  }  
```

## custom serializer
``` Java
  ArangoDB arangoDB = new ArangoDB.Builder()
    .registerDeserializer(MyObject.class, new VPackDeserializer<MyObject>() {
      @Override
      public MyObject deserialize(
        final VPackSlice parent,
        final VPackSlice vpack,
        final VPackDeserializationContext context) throws VPackException {
        
          final MyObject obj = new MyObject();
          obj.setName(vpack.get("name").getAsString());
          return obj;
      }
    }).registerSerializer(MyObject.class, new VPackSerializer<MyObject>() {
      @Override
      public void serialize(
        final VPackBuilder builder,
        final String attribute,
        final MyObject value,
        final VPackSerializationContext context) throws VPackException {
        
          builder.add(attribute, ValueType.OBJECT);
          builder.add("name", value.getName());
          builder.close();
      }
    }).build();
``` 

## manually serialization
To de-/serialize from and to VelocyPack before or after a database call, use the `ArangoUtil` from the method `util()` in `ArangoDB`, `ArangoDatabase`, `ArangoCollection`, `ArangoGraph`, `ArangoEdgeCollection`or `ArangoVertexCollection`.

``` Java
  ArangoDB arangoDB = new ArangoDB.Builder();
  VPackSlice vpack = arangoDB.util().serialize(myObj);
```

``` Java
  ArangoDB arangoDB = new ArangoDB.Builder();
  MyObject myObj = arangoDB.util().deserialize(vpack, MyObject.class);
```

# Learn more
* [ArangoDB](https://www.arangodb.com/)
* [ChangeLog](ChangeLog)
* [Examples](src/test/java/com/arangodb/example)
* [Tutorial](https://www.arangodb.com/tutorials/tutorial-sync-java-driver/)
* [JavaDoc](http://arangodb.github.io/arangodb-java-driver/javadoc-4_1/index.html)
* [JavaDoc VelocyPack](http://arangodb.github.io/java-velocypack/javadoc-1_0/index.html)
