
This library is a Java driver for ArangoDB.

Supported version: ArangoDB-2.4.x

# Required

* [ArangoDB](https://github.com/triAGENS/ArangoDB) version 2.4.x
* Java 1.6 later


# Basics

## Maven

To add the driver to your project with maven, add the following code to your pom.xml:

```XML
<dependencies>
  <dependency>
    <groupId>com.arangodb</groupId>
    <artifactId>arangodb-java-driver</artifactId>
    <version>[2.3-SNAPSHOT,2.3]</version>
  </dependency>
	....
</dependencies>
```

If you want to test with a snapshot version (e.g. 2.3-SNAPSHOT), add the staging repository of oss.sonatype.org to your pom.xml:

```XML
<repositories>
  <repository>
    <id>arangodb-snapshots</id>
    <url>https://oss.sonatype.org/content/groups/staging</url>
  </repository>
</repositories>
```


## Driver Setup

Setup with default configuration:

``` Java
  // Initialize configure
  ArangoConfigure configure = new ArangoConfigure();
  configure.init();

  // Create Driver (this instance is thread-safe)
  ArangoDriver arangoDriver = new ArangoDriver(configure);
  
```


The driver is configured with some default values:

<table>
<tr><th>property-key</th><th>description</th><th>default value</th></tr>
<tr><th>host</th><td>ArangoDB host</td><td>127.0.0.1</td></tr>
<tr><th>port</th><td>ArangoDB port</td><td>8529</td></tr>
<tr><th>maxPerConnection</th><td>Max http connection per host.</td><td>20</td></tr>
<tr><th>maxTotalConnection</th><td>Max http connection per configure.</td><td>20</td></tr>
<tr><th>user</th><td>Basic Authentication User</td><td></td></tr>
<tr><th>password</th><td>Basic Authentication Password</td><td></td></tr>
<tr><th>proxy.host</th><td>proxy host</td><td></td></tr>
<tr><th>proxy.port</th><td>proxy port</td><td></td></tr>
<tr><th>connectionTimeout</th><td>socket connect timeout(millisecond)</td><td>-1</td></tr>
<tr><th>timeout</th><td>socket read timeout(millisecond)</td><td>-1</td></tr>
<tr><th>retryCount</th><td>http retry count</td><td>3</td></tr>
<tr><th>defaultDatabase</th><td>default database</td><td></td></tr>
<tr><th>enableCURLLogger</th><td>logging flag by curl format for debug</td><td>false</td></tr>
</table>


To customize the configuration the parameters can be changed in the code...

``` Java
  // Initialize configure
  ArangoConfigure configure = new ArangoConfigure();
  configure.setHost("192.168.182.50");
  configure.setPort(8888);
  configure.init();

  // Create Driver (this instance is thread-safe)
  ArangoDriver arangoDriver = new ArangoDriver(configure);
  
```
... or with a properties file (arangodb.properties)

``` Java
  // Initialize configure
  ArangoConfigure configure = new ArangoConfigure();
  configure.loadProperties();
  configure.init();

  // Create Driver (this instance is thread-safe)
  ArangoDriver arangoDriver = new ArangoDriver(configure);
  
```

Example for arangodb.properties:
``` Java
port=8888
host=192.168.182.50
user=root
password=
enableCURLLogger=true

```


# Basic database operations
## create database
``` Java
  // create database 
  arangoDriver.createDatabase("myDatabase");
  // and set as default
  arangoDriver.setDefaultDatabase("myDatabase");
  
```

## changing the database
This ArangoDB driver is thread-safe. Unfortunately the ArangoDriver#setDefaultDatabase() is not (yet). So its recommended to create a new driver instance, if you want to change the database.

``` Java
  //Driver instance to database "_system" (default database)
  ArangoDriver driverSystem = new ArangoDriver(configure);
  //Driver instance to database "mydb2"
  ArangoDriver driverMyDB = new ArangoDriver(configure, "mydb2");
  
```

## drop database
``` Java
  // drop database 
  arangoDriver.deleteDatabase("myDatabase");
  
```

# Basic collection operations
## create collection
``` Java
  // create collection
  CollectionEntity myArangoCollection = ArangoCollectionarangoDriver.createCollection("myCollection");
  
```

## delete collection by name
``` Java
  // delete database 
  arangoDriver.deleteCollection("myCollection");
  
```

## delete collection by id
``` Java
  // delete database 
  arangoDriver.deleteCollection(myArangoCollection.getId());
  
```

# Basic document operations

For the next examples we use a small object:

``` Java
public class MyObject {

    private String name;
    private int age;

    public MyObject(String name, int age) {
        this.name = name;
        this.age = age;
    }
    
    /*
    *  + getter and setter
    */
   

}  
```

## create document
``` Java
  // create document 
  MyObject myObject = new MyObject("Homer", 38);
  DocumentEntity<MyObject> myDocument = arangoDriver.createDocument("myCollection", myObject);
  
```

When creating a document, the attributes of the object will be stored as key-value pair
E.g. in the previous example the object was stored as follows:
``` properties
"name" : "Homer"
"age" : "38"
```
  

## delete document
``` Java
  // delete document 
  arangoDriver.deleteDocument(myDocument.getDocumentHandle());
  
```

# AQL
## Executing an AQL statement

E.g. get all Simpsons aged 3 or older in ascending order:

``` Java
    arangoDriver.deleteDatabase("myDatabase");
    arangoDriver.createDatabase("myDatabase");
    arangoDriver.setDefaultDatabase("myDatabase");
    CollectionEntity myArangoCollection = arangoDriver.createCollection("myCollection");
    
    arangoDriver.createDocument("myCollection", new MyObject("Homer", 38));
    arangoDriver.createDocument("myCollection", new MyObject("Marge", 36));
    arangoDriver.createDocument("myCollection", new MyObject("Bart", 10));
    arangoDriver.createDocument("myCollection", new MyObject("Lisa", 8));
    arangoDriver.createDocument("myCollection", new MyObject("Maggie", 2));
    
    String query = "FOR t IN myCollection FILTER t.age >= @age SORT t.age RETURN t";
    Map<String, Object> bindVars = new MapBuilder().put("age", 3).get();
    CursorResultSet<MyObject> rs = arangoDriver.executeQueryWithResultSet(
      query, bindVars, MyObject.class, true, 20
    );
    
    for (MyObject obj: rs) {
      System.out.println(obj.getName());
    }
   
  
```

instead of using a for statement you can also use an iterator:
``` Java
  while (rs.hasNext()) {
    MyObject obj = rs.next();
    System.out.println(obj.getName());
  }
  rs.close();
```

#User Management
If you are using [authentication] (http://docs.arangodb.com/ConfigureArango/Authentication.html) you can manage users with the driver.

##add user
``` Java
  //username, password, active, extras
  arangoDriver.createUser("myUser", "myPassword", true, null);
```

##list users
``` Java
  List<UserEntity> users = arangoDriver.getUsers();
  for(UserEntity user : users) {
    System.out.println(user.getName())
  }
```


##DELETE user
``` Java
  arangoDriver.createUser("myUser");
```


#Graphs
This driver supports the new [graph api](https://docs.arangodb.com/HttpGharial/README.html).

Some of the basic graph operations are described in the following:

##add graph
A graph consists of vertices and edges (stored in collections). Which collections are used within a graph is defined via edge definitions. A graph can contain more than one edge definition, at least one is needed.

``` Java
  List<EdgeDefinitionEntity> edgeDefinitions = new ArrayList<EdgeDefinitionEntity>();
  EdgeDefinitionEntity edgeDefinition = new EdgeDefinitionEntity();
  // define the edgeCollection to store the edges
  edgeDefinition.setCollection("myEdgeCollection");
  // define a set of collections where an edge is going out...
  List<String> from = new ArrayList<String>();
  // and add one or more collections
  from.add("myCollection1");
  from.add("myCollection2");
  edgeDefinition.setFrom(from)
   
  // repeat this for the collections where an edge is going into  
  List<String> to = new ArrayList<String>();
  to.add("myCollection1");
  to.add("myCollection3");
  edgeDefinition.setTo(to);
  
  edgeDefinitions.add(edgeDefinition);
  
  // A graph can contain additional vertex collections, defined in the set of orphan collections
  List<String> orphanCollections = new ArrayList<String>(); 
  orphanCollections.add("myCollection4");
  orphanCollections.add("myCollection5");
  
  // now it's possible to create a graph (the last parameter is the waitForSync option)
  GraphEntity graph = arangoDriver.createGraph("myGraph", edgeDefinitions, orphanCollections, true);
```

##delete graph

A graph can be deleted by its name

``` Java
  arangoDriver.deleteGraph("myGraph");
```

##add vertex

Vertices are stored in the vertex collections defined above.

``` Java
  MyObject myObject1 = new MyObject("Homer", 38);
  MyObject myObject2 = new MyObject("Marge", 36);
  DocumentEntity<MyObject> vertexFrom = arangoDriver.graphCreateVertex(
      "myGraph",
      "collection1",
      myObject1,
      true); 
  
  DocumentEntity<MyObject> vertexTo = arangoDriver.graphCreateVertex(
      "myGraph",
      "collection3",
      myObject2,
      true); 
```

## add edge

Now an edge can be created to set a relation between vertices

``` Java
    EdgeEntity<?> edge = arangoDriver.graphCreateEdge(
      "myGraph",
      "myEdgeCollection",
      null,
      vertexFrom.getDocumentHandle(),
      vertexTo.getDocumentHandle(),
      null,
      null);
``` 
