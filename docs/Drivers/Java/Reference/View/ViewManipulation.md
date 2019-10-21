# Manipulating the View

These functions implement the
[HTTP API for modifying Views](https://www.arangodb.com/docs/stable/http/views.html).

## ArangoDatabase.createView

`ArangoDatabase.createView(String name, ViewType type) : ViewEntity`

Creates a View of the given _type_, then returns View information from the server.

**Arguments**

- **name**: `String`

  The name of the View

- **type**: `ViewType`

  The type of the View

**Examples**

```Java
ArangoDB arango = new ArangoDB.Builder().build();
ArangoDatabase db = arango.db("myDB");
db.createView("myView", ViewType.ARANGO_SEARCH);
// the view "potatoes" now exists
```

## ArangoView.rename

`ArangoView.rename(String newName) : ViewEntity`

Renames the View.

**Arguments**

- **newName**: `String`

  The new name

**Examples**

```Java
ArangoDB arango = new ArangoDB.Builder().build();
ArangoDatabase db = arango.db("myDB");
ArangoView view = db.view("some-view");

ViewEntity result = view.rename("new-view-name")
assertThat(result.getName(), is("new-view-name");
// result contains additional information about the View
```

## ArangoView.drop

`ArangoView.drop() : void`

Deletes the View from the database.

**Examples**

```Java
ArangoDB arango = new ArangoDB.Builder().build();
ArangoDatabase db = arango.db("myDB");
ArangoView view = db.view("some-view");

view.drop();
// the View "some-view" no longer exists
```
