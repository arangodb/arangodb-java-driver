# View API

These functions implement the
[HTTP API for Views](https://www.arangodb.com/docs/stable/http/views.html).

## Getting information about the View

## ArangoView.exists

`ArangoView.exists() : boolean`

Checks whether the View exists

**Examples**

```Java
ArangoDB arango = new ArangoDB.Builder().build();
ArangoDatabase db = arango.db("myDB");
ArangoView view = db.view("potatoes");

boolean exists = view.exists();
```

## ArangoView.getInfo

`ArangoView.getInfo() : ViewEntity`

Returns information about the View.

**Examples**

```Java
ArangoDB arango = new ArangoDB.Builder().build();
ArangoDatabase db = arango.db("myDB");
ArangoView view = db.view("potatoes");

ViewEntity info = view.getInfo();
```
