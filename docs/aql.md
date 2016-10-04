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
  
  cursor.forEachRemaining(obj -> {
    System.out.println(obj.getName());
  });
```

or return the AQL result as VelocyPack:

``` Java
  ArangoCursor<VPackSlice> cursor = db.query(query, bindVars, null, VPackSlice.class);
  
  cursor.forEachRemaining(obj -> {
    System.out.println(obj.get("name").getAsString());
  });
```
