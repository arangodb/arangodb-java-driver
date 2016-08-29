package com.arangodb;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Ignore;
import org.junit.Test;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionResult;
import com.arangodb.entity.DatabaseResult;
import com.arangodb.entity.GraphResult;
import com.arangodb.entity.IndexResult;
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
				db.collection(COLLECTION_NAME).insert(new BaseDocument(), null);
			}
			final Cursor<String> cursor = db.query("for i in db_test return i._id", null, null, String.class);
			assertThat(cursor, is(notNullValue()));
			final Iterator<String> iterator = cursor.iterator();
			assertThat(iterator, is(notNullValue()));
			for (int i = 0; i < 10; i++) {
				assertThat(iterator.hasNext(), is(i != 10));
			}
		} finally {
			db.collection(COLLECTION_NAME).drop();
		}
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
