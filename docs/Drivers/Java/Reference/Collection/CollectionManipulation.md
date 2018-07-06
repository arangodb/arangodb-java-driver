# Manipulating the collection

These functions implement
[the HTTP API for modifying collections](https://docs.arangodb.com/latest/HTTP/Collection/Modifying.html).

## ArangoDatabase.createCollection

```
ArangoDatabase.createCollection(String name, CollectionCreateOptions options) : CollectionEntity
```

Creates a collection with the given _options_ for this collection's name, then returns collection information from the server.

**Arguments**

- **name**: `String`

  The name of the collection

- **options**: `CollectionCreateOptions`

  Additional options

**Examples**

```Java
ArangoDB arango = new ArangoDB.Builder().build();
ArangoDatabase db = arango.db("myDB");
db.createCollection("potatos", new CollectionCreateOptions());
// the document collection "potatos" now exists
```

## ArangoCollection.create

```
ArangoCollection.create(CollectionCreateOptions options) : CollectionEntity
```

Creates a collection with the given _options_ for this collection's name, then returns collection information from the server. Alternative for [ArangoDatabase.createCollection](#ArangoDatabase.createCollection).

**Arguments**

- **options**: `CollectionCreateOptions`

  Additional options

**Examples**

```Java
ArangoDB arango = new ArangoDB.Builder().build();
ArangoDatabase db = arango.db("myDB");
ArangoCollection collection = db.collection("potatos");
collection.create(new CollectionCreateOptions());
// the document collection "potatos" now exists
```

## ArangoCollection.load

```
ArangoCollection.load() : CollectionEntity
```

Tells the server to load the collection into memory.

**Examples**

```Java
ArangoDB arango = new ArangoDB.Builder().build();
ArangoDatabase db = arango.db("myDB");
ArangoCollection collection = db.collection("some-collection");
collection.load();
// the collection has now been loaded into memory
```

## ArangoCollection.unload

```
ArangoCollection.unload() : CollectionEntity
```

Tells the server to remove the collection from memory. This call does not delete any documents. You can use the collection afterwards; in which case it will be loaded into memory, again.

**Examples**

```Java
ArangoDB arango = new ArangoDB.Builder().build();
ArangoDatabase db = arango.db("myDB");
ArangoCollection collection = db.collection("some-collection");
collection.unload();
// the collection has now been unloaded from memory
```

## ArangoCollection.changeProperties

```
ArangoCollection.changeProperties(CollectionPropertiesOptions options) : CollectionPropertiesEntity
```

Changes the properties of the collection.

**Arguments**

- **options**: `CollectionPropertiesEntity`

  For information on the _properties_ argument see
  [the HTTP API for modifying collections](https://docs.arangodb.com/latest/HTTP/Collection/Modifying.html).

**Examples**

```Java
ArangoDB arango = new ArangoDB.Builder().build();
ArangoDatabase db = arango.db("myDB");
ArangoCollection collection = db.collection("some-collection");

CollectionPropertiesEntity result = collection.changeProperties(new CollectionPropertiesEntity().waitForSync(true));
assertThat(result.getWaitForSync(), is(true));
// the collection will now wait for data being written to disk
// whenever a document is changed
```

## ArangoCollection.rename

```
ArangoCollection.rename(String newName) : CollectionEntity
```

Renames the collection

**Arguments**

- **newName**: `String`

  The new name

**Examples**

```Java
ArangoDB arango = new ArangoDB.Builder().build();
ArangoDatabase db = arango.db("myDB");
ArangoCollection collection = db.collection("some-collection");

CollectionEntity result = collection.rename("new-collection-name")
assertThat(result.getName(), is("new-collection-name");
// result contains additional information about the collection
```

## ArangoCollection.truncate

```
ArangoCollection.truncate() : CollectionEntity
```

Removes all documents from the collection, but leaves the indexes intact.

**Examples**

```Java
ArangoDB arango = new ArangoDB.Builder().build();
ArangoDatabase db = arango.db("myDB");
ArangoCollection collection = db.collection("some-collection");

collection.truncate();
// the collection "some-collection" is now empty
```

## ArangoCollection.drop

```
ArangoCollection.drop() : void
```

Deletes the collection from the database.

**Examples**

```Java
ArangoDB arango = new ArangoDB.Builder().build();
ArangoDatabase db = arango.db("myDB");
ArangoCollection collection = db.collection("some-collection");

collection.drop();
// the collection "some-collection" no longer exists
```
