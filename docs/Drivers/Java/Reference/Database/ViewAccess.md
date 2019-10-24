# Accessing Views

These functions implement the
[HTTP API for accessing Views](https://www.arangodb.com/docs/stable/http/views.html).

## ArangoDatabase.view

`ArangoDatabase.view(String name) : ArangoView`

Returns a _ArangoView_ instance for the given View name.

**Arguments**

- **name**: `String`

  Name of the View

**Examples**

```Java
ArangoDB arango = new ArangoDB.Builder().build();
ArangoDatabase db = arango.db("myDB");
ArangoView view = db.view("myView");
```

## ArangoDatabase.arangoSearch

`ArangoDatabase.arangoSearch(String name) : ArangoSearch`

Returns a _ArangoSearch_ instance for the given ArangoSearch View name.

**Arguments**

- **name**: `String`

  Name of the View

**Examples**

```Java
ArangoDB arango = new ArangoDB.Builder().build();
ArangoDatabase db = arango.db("myDB");
ArangoSearch view = db.arangoSearch("myArangoSearchView");
```

## ArangoDatabase.getViews

`ArangoDatabase.getViews() : Collection<ViewEntity>`

Fetches all Views from the database and returns an list of collection descriptions.

**Examples**

```Java
ArangoDB arango = new ArangoDB.Builder().build();
ArangoDatabase db = arango.db("myDB");
Collection<ViewEntity> infos = db.getViews();
```
