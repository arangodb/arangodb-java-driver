
This library is a Java driver for ArangoDB.

Support version: ArangoDB-2.2.x

# Required

* [ArangoDB](https://github.com/triAGENS/ArangoDB)
* Java 5 later


# Basics

## Maven

To add the driver to your project with maven, add the following code to your pom.xml: 

```XML
<repositories>
  <repository>
    <id>at.orz</id>
    <name>tamtam180 Repository</name>
    <url>http://maven.orz.at/</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>at.orz</groupId>
    <artifactId>arangodb-java-driver</artifactId>
    <version>[1.4,1.5)</version>
  </dependency>
</dependencies>
```

Central Repository in preparation. Please wait.

## Driver Setup

Setup with default configuration:

``` Java
  // Initialize configure
  ArangoConfigure configure = new ArangoConfigure();
  configure.init();

  // Create Driver (this instance is thread-safe)
  ArangoDriver arangoDriver = new ArangoDriver(configure);
  
```


The driver is configured with some default values (wich match the dafault of ArangoDB):

<table>
<tr><th>property-key</th><th>description</th><th>default value</th></tr>
<tr><th>host</th><td>ArangoDB host</td><td>127.0.0.1</td></tr>
<tr><th>port</th><td>ArangoDB port</td><td>8159</td></tr>
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
  configure.setHost("0.0.0.0");
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
## create document
``` Java
  // create document 
  MyObject myObject = new MyObject();
  DocumentEntity<MyObject> myDocument = arangoDriver.createDocument("myCollection", myObject);
  
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
		System.out.println(obj);
	}
	rs.close();
```

#User Management


#Graphs
(not implemented yet)
