
This library ia a Java driver for ArangoDB.

Support version: ArangoDB-1.4.x

# Required

* Java 5 later

# Maven

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

# JavaDoc

Not Ready. Please wait.

# Library Structure

This library has 4 layers.

* Low layer
    * ArangoDriver
    * Corresponding to 1:1 and Rest-API.
    * All exception is raised other than normal processing.
    * **Multithread-safety**
* Middle layer **(Not yet implemented)**
    * ArangoClient
    * It is a wrapper class that easy to use ArangoDriver.
    * For example, you can not be an error to delete the ones that do not exist in the delete command,
    That it may not be an error to generate a duplicate Collection,
    it is provide you with an easy to use interface for general use.
* High layer **(Not yet implemented)**
    * object-oriented programming layer.
    * Each class is CRUD.
* JDBC layer **(Not yet implemented)**
    * AQL for JDBC driver

# How to use.

## ArangoConfigure (/arangodb.properties)

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

## Basic usage ArangoDriver

``` Java
  // Initialize configure
  ArangoConfigure configure = new ArangoConfigure();
  configure.setHost("127.0.0.1");
  configure.setPort(8159);
  configure.init();

  // Create Driver (this instance is thread-safe)
  ArangoDriver client = new ArangoDriver(configure);
  
  String collectionName = "mytest";
  TestComplexEntity01 value = new TestComplexEntity01("name", "desc", 10); // any POJO class

  // Create Collection
  CollectionEntity collection = client.createCollection(collectionName);

  // Create Document
  DocumentEntity<TestComplexEntity01> ret1 = client.createDocument(collectionName, value, null, null);
  String documentHandle = ret1.getDocumentHandle();
  
  // Get Document
  DocumentEntity<TestComplexEntity01> ret2 =
    client.getDocument(documentHandle, TestComplexEntity01.class);

  // Delete Document
  driver.deleteDocument(documentHandle, -1, DeletePolicy.LAST);  

  // finalize library
  configure.shutdown();
```

## Database Change

Since ArangoDB-1.4, support multi database.

ArangoDriver is thread-safe. But ArangoDriver#setDefaultDatabase() is not safety.
So, if you wants to switch the database, you need to create an another instance.

```Java
public class ExampleMDB {

	public static void main(String[] args) {

		// Initialize configure
		ArangoConfigure configure = new ArangoConfigure();
		configure.init();
		
		// Create Driver (this instance is thread-safe)
		// If you use a multi database, you need create each instance.
		ArangoDriver driverA = new ArangoDriver(configure); // db = _system (configure#defaultDatabase)
		ArangoDriver driverB = new ArangoDriver(configure, "mydb2");
		
		try {
			
			// Create Collection at db(_system)
			driverA.createCollection("example1", false, null, null, null, null, CollectionType.DOCUMENT);
			driverA.createDocument("example1", 
					new MapBuilder().put("attr1", "value1").put("attr2", "value2").get(), 
					false, false);

			// Create Database mydb2
			driverB.createDatabase("mydb2");
			
			// Create Collection at db(mydb2)
			driverB.createCollection("example2", false, null, null, null, null, CollectionType.DOCUMENT);
			driverB.createDocument("example2", 
					new MapBuilder().put("attr1-B", "value1").put("attr2-B", "value2").get(), 
					false, false);
			
			// print all database names.
			System.out.println(driverA.getDatabases());
			// -> _system, mydb2

			// get all document-handle, and print get & print document. (_system DB)
			for (String documentHandle: driverA.getDocuments("example1", true)) {
				DocumentEntity<Map> doc = driverA.getDocument(documentHandle, Map.class);
				System.out.println(doc.getEntity());
			}

			for (String documentHandle: driverB.getDocuments("example2", true)) {
				DocumentEntity<Map> doc = driverB.getDocument(documentHandle, Map.class);
				System.out.println(doc.getEntity());
			}

		} catch (ArangoException e) {
			e.printStackTrace();
		} finally {
			configure.shutdown();
		}

	}

}
```



## Use AQL

Use ForEach

```Java
// Query
String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
// Bind Variables
Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

// Execute Query
CursorResultSet<TestComplexEntity01> rs = driver.executeQueryWithResultSet(
		query, bindVars, TestComplexEntity01.class, true, 20);

for (TestComplexEntity01 obj: rs) {
	System.out.println(obj);
}

```

Not use ForEach

```Java
String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

CursorResultSet<TestComplexEntity01> rs = driver.executeQueryWithResultSet(
		query, bindVars, TestComplexEntity01.class, true, 20);

while (rs.hasNext()) {
	TestComplexEntity01 obj = rs.next();
	System.out.println(obj);
}
rs.close();
```

## More example

# Support API

[PDF File](support_api.pdf)

# TODO

* Exact ETAG support 
* Batch process
* Maven Repo and download packages.
* Online JavaDoc.
* Multi Server connection (ex. Consistent Hash)

* PUT /_api/simple/near
* PUT /_api/simple/within
* Blueprints
* Document of Serialization control by annotation (@Exclude)

This library does not support admin/_echo

# Develop Note

## UnitTest environment

    master: arango-test-server:9999 (auth=true)
    slave: arango-test-server-slave:8529

# License

Apache License 2.0

# Author

Twitter: @tamtam180
