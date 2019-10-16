# View API

These functions implement the
[HTTP API for Views](https://www.arangodb.com/docs/devel/http/views.html).

## Getting information about the view

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
