# Accessing graphs

These functions implement the
[HTTP API for accessing general graphs](https://docs.arangodb.com/latest/HTTP/Gharial/index.html).

## ArangoDatabase.graph

`ArangoDatabase.graph(String name) : ArangoGraph`

Returns a _ArangoGraph_ instance for the given graph name.

**Arguments**

- **name**: `String`

  Name of the graph

**Examples**

```Java
ArangoDB arango = new ArangoDB.Builder().build();
ArangoDatabase db = arango.db("myDB");
ArangoGraph graph = db.graph("myGraph");
```
