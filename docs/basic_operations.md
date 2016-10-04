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

## delete all documents in the collection
``` Java
  arangoDB.db("myDatabase").collection("myCollection").truncate();
```

# Basic document operations

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
  arangoDB.db("myDatabase").collection("myCollection").deleteDocument(myObject.getKey);
  
```

## update document
``` Java
  arangoDB.db("myDatabase").collection("myCollection").updateDocument(myObject.getKey, myUpdatedObject);
  
```

## replace document
``` Java
  arangoDB.db("myDatabase").collection("myCollection").replaceDocument(myObject.getKey, myObject2);
  
```

## read document by key (as JavaBean)
``` Java
  MyObject document = arangoDB.db("myDatabase").collection("myCollection").getDocument(myObject.getKey, MyObject.class).get();
  document.getName();
  document.getAge();
  
```

## read document (as VelocyPack)
``` Java
  VPackSlice document = arangoDB.db("myDatabase").collection("myCollection").getDocument(myObject.getKey, VPackSlice.class).get();
  document.get("name").getAsString();
  document.get("age").getAsInt();
  
```

## read document (as Json)
``` Java
  arangoDB.db("myDatabase").collection("myCollection").getDocument(myObject.getKey, String.class).get();
  
```

## read document by id
``` Java
  arangoDB.db("myDatabase").getDocument("myCollection/myKey", MyObject.class).get();
  
```
