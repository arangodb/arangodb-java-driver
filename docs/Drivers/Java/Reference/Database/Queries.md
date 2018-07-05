# Queries

This function implements the
[HTTP API for single roundtrip AQL queries](https://docs.arangodb.com/latest/HTTP/AqlQueryCursor/QueryResults.html).

## ArangoDatabase.query

```
ArangoDatabase.query(String query, Map<String, Object> bindVars, AqlQueryOptions options, Class<T> type) : ArangoCursor<T>
```

Performs a database query using the given _query_ and _bindVars_, then returns a new _ArangoCursor_ instance for the result list.

**Arguments**

- **query**: `String`

  An AQL query string

- **bindVars**: `Map<String, Object>`

  key/value pairs defining the variables to bind the query to

- **options**: `AqlQueryOptions`

  Additional options that will be passed to the query API

- **type**: `Class<T>`

  The type of the result (POJO class, `VPackSlice`, `String` for Json, or `Collection`/`List`/`Map`)

**Examples**

```Java
ArangoDB arango = new ArangoDB.Builder().build();
ArangoDatabase db = arango.db("myDB");
ArangoCursor<MyObject> cursor = db.query("FOR i IN @@collection RETURN i"
                                         new MapBuilder().put("@collection", "myCollection").get(),
                                         new AqlQueryOptions(),
                                         MyObject.class);
```
