
## Driver Setup

Setup with default configuration, this automatically loads a properties file arangodb.properties if exists in the classpath:

``` Java
  // this instance is thread-safe
  ArangoDB arangoDB = new ArangoDB.Builder().build();
  
```


The driver is configured with some default values:

<table>
<tr><th>property-key</th><th>description</th><th>default value</th></tr>
<tr><th>host</th><td>ArangoDB host</td><td>127.0.0.1</td></tr>
<tr><th>port</th><td>ArangoDB port</td><td>8529</td></tr>
<tr><th>user</th><td>Basic Authentication User</td><td></td></tr>
<tr><th>password</th><td>Basic Authentication Password</td><td></td></tr>
<tr><th>timeout</th><td>socket connect timeout(millisecond)</td><td>0</td></tr>
<tr><th>useSsl</th><td>use SSL connection</td><td>false</td></tr>
</table>

To customize the configuration the parameters can be changed in the code...

``` Java
  ArangoDB arangoDB = new ArangoDB.Builder().host("192.168.182.50").port(8888).build();
  
```
... or with a custom properties file (my.properties)

``` Java
  InputStream in = MyClass.class.getResourceAsStream("my.properties");
  ArangoDB arangoDB = new ArangoDB.Builder().loadProperties(in).build();
  
```

Example for arangodb.properties:
``` Java
arangodb.host=127.0.0.1
arangodb.port=8529
arangodb.user=root
arangodb.password=

```


# Basic database operations
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

# Basic collection operations
## create collection
``` Java
  // create collection
  arangoDB.db("myDatabase").createCollection("myCollection", null);
  
```

## delete collection by name
``` Java
  // delete collection 
  arangoDB.db("myDatabase").collection("myCollection").drop();
  
```

# Basic document operations

For the next examples we use a small object:

``` Java
public class MyObject {

    private String key;
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

## insert document
``` Java
  // insert document 
  MyObject myObject = new MyObject("Homer", 38);
  arangoDB.db("myDatabase").collection("myCollection").insertDocument(myObject, null);
  
  
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
  arangoDB.db("myDatabase").collection("myCollection").deleteDocument(myObject.getKey, null, null);
  
```

# AQL
## Executing an AQL statement

E.g. get all Simpsons aged 3 or older in ascending order:

``` Java
    arangoDB.createDatabase("myDatabase");
    ArangoDatabase db = arangoDB.db("myDatabase");
    
    db.createCollection("myCollection", null);
    ArangoCollection collection = db.collection("myCollection");
    
    collection.insertDocument(new MyObject("Homer", 38), null);
    collection.insertDocument(new MyObject("Marge", 36), null);
    collection.insertDocument(new MyObject("Bart", 10), null);
    collection.insertDocument(new MyObject("Lisa", 8), null);
    collection.insertDocument(new MyObject("Maggie", 2), null);
    
    Map<String, Object> bindVars = new HashMap<>();
    bindVars.put("age", 3);    
    ArangoCursor<MyObject> cursor = db.query(query, bindVars, null, MyObject.class);
    
    for (Iterator<MyObject> iterator = cursor.iterator(); iterator.hasNext();) {
      MyObject obj = iterator.next();
      System.out.println(obj.getName());
    }
```

#User Management
If you are using [authentication] (https://docs.arangodb.com/Manual/GettingStarted/Authentication.html) you can manage users with the driver.

##add user
``` Java
  //username, password, extras
  arangoDB.createUser("myUser", "myPassword", null);
```

##list users
``` Java
  Collection<UserResult> users = arangoDB.getUsers();
  for(UserResult user : users) {
    System.out.println(user.getUser())
  }
```


##DELETE user
``` Java
  arangoDB.deleteUser("myUser");
```


#Graphs
This driver supports the [graph api](https://docs.arangodb.com/HTTP/Gharial/index.html).

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

##delete graph

A graph can be deleted by its name

``` Java
  arangoDB.db("myDatabase").graph("myGraph").drop();
```

##add vertex

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