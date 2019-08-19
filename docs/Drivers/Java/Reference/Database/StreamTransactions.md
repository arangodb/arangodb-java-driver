# Transactions

See [HTTP Interface for Stream Transactions](https://docs.arangodb.com/latest/HTTP/transaction-stream-transaction.html).


## ArangoDatabase.beginStreamTransaction

`ArangoDatabase.beginStreamTransaction(StreamTransactionOptions options) : StreamTransactionEntity`

Begins a server-side transaction and returns information about it.

**Arguments**

- **options**: `StreamTransactionOptions`

  transaction options


## ArangoDatabase.getStreamTransaction

`ArangoDatabase.getStreamTransaction(String id) : StreamTransactionEntity`

Gets information about a Stream Transaction.

**Arguments**

- **id**: `String`

  transaction id


## ArangoDatabase.getStreamTransactions

`ArangoDatabase.getStreamTransactions() : Collection<TransactionEntity>`

Gets all the currently running Stream Transactions.


## ArangoDatabase.commitStreamTransaction

`ArangoDatabase.commitStreamTransaction(String id) : StreamTransactionEntity`

Commits a Stream Transaction.

**Arguments**

- **id**: `String`

  transaction id


## ArangoDatabase.abortStreamTransaction

`ArangoDatabase.abortStreamTransaction(String id) : StreamTransactionEntity`

Aborts a Stream Transaction.

**Arguments**

- **id**: `String`

  transaction id


## Examples

```Java
ArangoDB arango = new ArangoDB.Builder().build();
ArangoDatabase db = arango.db("myDB");

StreamTransactionEntity tx1 = db.beginStreamTransaction(
    new StreamTransactionOptions().readCollections("collection").writeCollections("collection"));
db.collection("collection")
    .insertDocument(new BaseDocument(), new DocumentCreateOptions().streamTransactionId(tx1.getId()));
db.commitStreamTransaction(tx1.getId());
StreamTransactionEntity tx = db.getStreamTransaction(tx1.getId());
assertThat(tx.getStatus(), is(StreamTransactionEntity.StreamTransactionStatus.committed));

StreamTransactionEntity tx2 = db.beginStreamTransaction(
    new StreamTransactionOptions().readCollections("collection").writeCollections("collection"));
final Map<String, Object> bindVars = new HashMap<>();
bindVars.put("@collection", COLLECTION_NAME);
bindVars.put("key", "myKey");
ArangoCursor<BaseDocument> cursor = db
        .query("FOR doc IN @@collection FILTER doc._key == @key RETURN doc", bindVars,
                new AqlQueryOptions().streamTransactionId(tx2.getId()), BaseDocument.class);
db.abortStreamTransaction(tx2.getId());
```
