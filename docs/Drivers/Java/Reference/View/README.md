# View API

These functions implement the
[HTTP API for views](https://docs.arangodb.com/latest/HTTP/Views/index.html).

## Getting information about the view

See
[the HTTP API documentation](https://docs.arangodb.com/latest/HTTP/Views/Getting.html)
for details.

## ArangoView.exists

`ArangoView.exists() : boolean`

Checks whether the view exists

**Examples**

```Java
ArangoDB arango = new ArangoDB.Builder().build();
ArangoDatabase db = arango.db("myDB");
ArangoView view = db.view("potatoes");

boolean exists = view.exists();
```

## ArangoView.getInfo

`ArangoView.getInfo() : ViewEntity`

Returns information about the view.

**Examples**

```Java
ArangoDB arango = new ArangoDB.Builder().build();
ArangoDatabase db = arango.db("myDB");
ArangoView view = db.view("potatoes");

ViewEntity info = view.getInfo();
```
