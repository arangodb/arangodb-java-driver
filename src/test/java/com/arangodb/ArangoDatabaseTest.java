package com.arangodb;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionResult;
import com.arangodb.entity.DatabaseResult;
import com.arangodb.entity.GraphResult;
import com.arangodb.entity.IndexResult;
import com.arangodb.model.AqlQueryOptions;
import com.arangodb.model.CollectionsReadOptions;
import com.arangodb.model.TransactionOptions;
import com.arangodb.velocypack.VPackBuilder;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.Value;
import com.arangodb.velocypack.exception.VPackException;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ArangoDatabaseTest extends BaseTest {

	private static final String COLLECTION_NAME = "db_test";
	private static final String GRAPH_NAME = "graph_test";

	@Test
	public void createCollection() {
		try {
			final CollectionResult result = db.createCollection(COLLECTION_NAME, null);
			assertThat(result, is(notNullValue()));
			assertThat(result.getId(), is(notNullValue()));
		} finally {
			db.collection(COLLECTION_NAME).drop();
		}
	}

	@Test
	public void deleteCollection() {
		db.createCollection(COLLECTION_NAME, null);
		db.collection(COLLECTION_NAME).drop();
		try {
			db.collection(COLLECTION_NAME).getInfo();
			fail();
		} catch (final ArangoDBException e) {
		}
	}

	@Test
	public void getIndex() {
		try {
			db.createCollection(COLLECTION_NAME, null);
			final Collection<String> fields = new ArrayList<>();
			fields.add("a");
			final IndexResult createResult = db.collection(COLLECTION_NAME).createHashIndex(fields, null);
			final IndexResult readResult = db.getIndex(createResult.getId());
			assertThat(readResult.getId(), is(createResult.getId()));
			assertThat(readResult.getType(), is(createResult.getType()));
		} finally {
			db.collection(COLLECTION_NAME).drop();
		}
	}

	@Test
	public void deleteIndex() {
		try {
			db.createCollection(COLLECTION_NAME, null);
			final Collection<String> fields = new ArrayList<>();
			fields.add("a");
			final IndexResult createResult = db.collection(COLLECTION_NAME).createHashIndex(fields, null);
			final String id = db.deleteIndex(createResult.getId());
			assertThat(id, is(createResult.getId()));
			try {
				db.getIndex(id);
				fail();
			} catch (final ArangoDBException e) {
			}
		} finally {
			db.collection(COLLECTION_NAME).drop();
		}
	}

	@Test
	public void getCollections() {
		try {
			final Collection<CollectionResult> systemCollections = db.getCollections(null);
			db.createCollection(COLLECTION_NAME + "1", null);
			db.createCollection(COLLECTION_NAME + "2", null);
			final Collection<CollectionResult> collections = db.getCollections(null);
			assertThat(collections.size(), is(2 + systemCollections.size()));
			assertThat(collections, is(notNullValue()));
		} finally {
			db.collection(COLLECTION_NAME + "1").drop();
			db.collection(COLLECTION_NAME + "2").drop();
		}
	}

	@Test
	public void getCollectionsExcludeSystem() {
		try {
			final CollectionsReadOptions options = new CollectionsReadOptions().excludeSystem(true);
			final Collection<CollectionResult> systemCollections = db.getCollections(options);
			assertThat(systemCollections.size(), is(0));
			db.createCollection(COLLECTION_NAME + "1", null);
			db.createCollection(COLLECTION_NAME + "2", null);
			final Collection<CollectionResult> collections = db.getCollections(options);
			assertThat(collections.size(), is(2));
			assertThat(collections, is(notNullValue()));
		} finally {
			db.collection(COLLECTION_NAME + "1").drop();
			db.collection(COLLECTION_NAME + "2").drop();

		}
	}

	@Test
	public void grantAccess() {
		try {
			arangoDB.createUser("user1", "1234", null);
			db.grandAccess("user1");
		} finally {
			arangoDB.deleteUser("user1");
		}
	}

	@Test(expected = ArangoDBException.class)
	public void grantAccessUserNotFound() {
		db.grandAccess("user1");
	}

	@Test
	public void revokeAccess() {
		try {
			arangoDB.createUser("user1", "1234", null);
			db.revokeAccess("user1");
		} finally {
			arangoDB.deleteUser("user1");
		}
	}

	@Test(expected = ArangoDBException.class)
	public void revokeAccessUserNotFound() {
		db.revokeAccess("user1");
	}

	@Test
	public void query() {
		try {
			db.createCollection(COLLECTION_NAME, null);
			for (int i = 0; i < 10; i++) {
				db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null);
			}
			final ArangoCursor<String> cursor = db.query("for i in db_test return i._id", null, null, String.class);
			assertThat(cursor, is(notNullValue()));
			final Iterator<String> iterator = cursor.iterator();
			assertThat(iterator, is(notNullValue()));
			for (int i = 0; i < 10; i++, iterator.next()) {
				assertThat(iterator.hasNext(), is(i != 10));
			}
		} finally {
			db.collection(COLLECTION_NAME).drop();
		}
	}

	@Test
	public void queryWithCount() {
		try {
			db.createCollection(COLLECTION_NAME, null);
			for (int i = 0; i < 10; i++) {
				db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null);
			}

			final ArangoCursor<String> cursor = db.query("for i in db_test Limit 6 return i._id", null,
				new AqlQueryOptions().count(true), String.class);
			assertThat(cursor, is(notNullValue()));
			final Iterator<String> iterator = cursor.iterator();
			assertThat(iterator, is(notNullValue()));
			for (int i = 0; i < 6; i++, iterator.next()) {
				assertThat(iterator.hasNext(), is(i != 6));
			}
			assertThat(cursor.getCount().isPresent(), is(true));
			assertThat(cursor.getCount().get(), is(6));

		} finally {
			db.collection(COLLECTION_NAME).drop();
		}
	}

	@Test
	public void queryWithLimitAndFullCount() {
		try {
			db.createCollection(COLLECTION_NAME, null);
			for (int i = 0; i < 10; i++) {
				db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null);
			}

			final ArangoCursor<String> cursor = db.query("for i in db_test Limit 5 return i._id", null,
				new AqlQueryOptions().fullCount(true), String.class);
			assertThat(cursor, is(notNullValue()));
			final Iterator<String> iterator = cursor.iterator();
			assertThat(iterator, is(notNullValue()));
			for (int i = 0; i < 5; i++, iterator.next()) {
				assertThat(iterator.hasNext(), is(i != 5));
			}
			assertThat(cursor.getFullCount().isPresent(), is(true));
			assertThat(cursor.getFullCount().get(), is(10L));

		} finally {
			db.collection(COLLECTION_NAME).drop();
		}
	}

	@Test
	public void queryWithBatchSize() {
		try {
			db.createCollection(COLLECTION_NAME, null);
			for (int i = 0; i < 10; i++) {
				db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null);
			}

			final ArangoCursor<String> cursor = db.query("for i in db_test return i._id", null,
				new AqlQueryOptions().batchSize(5).count(true), String.class);

			assertThat(cursor, is(notNullValue()));

			final Iterator<String> iterator = cursor.iterator();
			assertThat(iterator, is(notNullValue()));
			for (int i = 0; i < 10; i++, iterator.next()) {
				assertThat(iterator.hasNext(), is(i != 10));
			}

		} finally {
			db.collection(COLLECTION_NAME).drop();
		}
	}

	@Test
	public void queryWithTTL() throws InterruptedException {
		// set TTL to 5 seconds and get the second batch after 10 seconds!
		int ttl = 5;
		int wait = 10;
		try {
			db.createCollection(COLLECTION_NAME, null);
			for (int i = 0; i < 10; i++) {
				db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null);
			}

			final ArangoCursor<String> cursor = db.query("for i in db_test return i._id", null,
				new AqlQueryOptions().batchSize(5).ttl(ttl), String.class);

			assertThat(cursor, is(notNullValue()));

			final Iterator<String> iterator = cursor.iterator();
			assertThat(iterator, is(notNullValue()));
			for (int i = 0; i < 10; i++, iterator.next()) {
				assertThat(iterator.hasNext(), is(i != 10));
				if (i == 1) {
					Thread.sleep(wait * 1000);
				}
			}
			fail("this should fail");
		} catch (ArangoDBException ex) {
			assertThat(ex.getMessage(), is("Response: 404, Error: 1600 - cursor not found"));
		} finally {
			db.collection(COLLECTION_NAME).drop();
		}
	}

	@Test
	@Ignore
	public void queryWithCache() throws InterruptedException {
		try {
			db.createCollection(COLLECTION_NAME, null);
			for (int i = 0; i < 10; i++) {
				db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null);
			}

			// TODO: set query cache property to "on"!

			final ArangoCursor<String> cursor = db.query("FOR t IN db_test FILTER t.age >= 10 SORT t.age RETURN t._id",
				null, new AqlQueryOptions().cache(true), String.class);

			assertThat(cursor, is(notNullValue()));
			assertThat(cursor.isCached(), is(false));

			final ArangoCursor<String> cachedCursor = db.query(
				"FOR t IN db_test FILTER t.age >= 10 SORT t.age RETURN t._id", null, new AqlQueryOptions().cache(true),
				String.class);

			assertThat(cachedCursor, is(notNullValue()));
			assertThat(cachedCursor.isCached(), is(true));

		} finally {
			db.collection(COLLECTION_NAME).drop();
		}
	}

	@Test
	public void queryWithBindVars() throws InterruptedException {
		try {
			db.createCollection(COLLECTION_NAME, null);
			for (int i = 0; i < 10; i++) {
				BaseDocument baseDocument = new BaseDocument();
				baseDocument.addAttribute("age", 20 + i);
				db.collection(COLLECTION_NAME).insertDocument(baseDocument, null);
			}
			Map<String, Object> bindVars = new HashMap<>();
			bindVars.put("@coll", COLLECTION_NAME);
			bindVars.put("age", 25);

			final ArangoCursor<String> cursor = db.query("FOR t IN @@coll FILTER t.age >= @age SORT t.age RETURN t._id",
				bindVars, null, String.class);

			assertThat(cursor, is(notNullValue()));

			final Iterator<String> iterator = cursor.iterator();
			assertThat(iterator, is(notNullValue()));
			for (int i = 0; i < 5; i++, iterator.next()) {
				assertThat(iterator.hasNext(), is(i != 5));
			}

		} finally {
			db.collection(COLLECTION_NAME).drop();
		}
	}

	@Test
	public void queryWithWarning() {
		final ArangoCursor<String> cursor = arangoDB.db().query("return _users + 1", null, null, String.class);

		assertThat(cursor, is(notNullValue()));
		assertThat(cursor.getWarnings().isPresent(), is(true));
		assertThat(cursor.getWarnings().get().isEmpty(), is(false));
	}

	@Test
	@Ignore
	public void createGraph() {
		final GraphResult result = db.createGraph(GRAPH_NAME, null);
		assertThat(result, is(notNullValue()));
		assertThat(result.getName(), is(GRAPH_NAME));
	}

	@Test
	public void transactionString() {
		final TransactionOptions options = new TransactionOptions().params("test");
		final String result = db.transaction("function (params) {return params;}", String.class, options);
		assertThat(result, is("test"));
	}

	@Test
	public void transactionNumber() {
		final TransactionOptions options = new TransactionOptions().params(5);
		final Integer result = db.transaction("function (params) {return params;}", Integer.class, options);
		assertThat(result, is(5));
	}

	@Test
	public void transactionVPack() throws VPackException {
		final TransactionOptions options = new TransactionOptions()
				.params(new VPackBuilder().add(new Value("test")).slice());
		final VPackSlice result = db.transaction("function (params) {return params;}", VPackSlice.class, options);
		assertThat(result.isString(), is(true));
		assertThat(result.getAsString(), is("test"));
	}

	@Test
	public void transactionEmpty() {
		db.transaction("function () {}", null, null);
	}

	@Test
	public void transactionallowImplicit() {
		try {
			db.createCollection("someCollection", null);
			db.createCollection("someOtherCollection", null);
			final String action = "function (params) {" + "var db = require('internal').db;"
					+ "return {'a':db.someCollection.all().toArray()[0], 'b':db.someOtherCollection.all().toArray()[0]};"
					+ "}";
			final TransactionOptions options = new TransactionOptions().readCollections("someCollection");
			db.transaction(action, VPackSlice.class, options);
			try {
				options.allowImplicit(false);
				db.transaction(action, VPackSlice.class, options);
				fail();
			} catch (final ArangoDBException e) {
			}
		} finally {
			db.collection("someCollection").drop();
			db.collection("someOtherCollection").drop();
		}
	}

	@Test
	public void getInfo() {
		final DatabaseResult info = db.getInfo();
		assertThat(info, is(notNullValue()));
		assertThat(info.getId(), is(notNullValue()));
		assertThat(info.getName(), is(TEST_DB));
		assertThat(info.getPath(), is(notNullValue()));
		assertThat(info.getIsSystem(), is(false));
	}
}
