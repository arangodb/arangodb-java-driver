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
import com.arangodb.entity.GraphResult;
import com.arangodb.entity.IndexResult;
import com.arangodb.model.CollectionsReadOptions;

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
	public void readCollection() {
		try {
			final CollectionResult createResult = db.createCollection(COLLECTION_NAME, null);
			final CollectionResult readResult = db.readCollection(COLLECTION_NAME);
			assertThat(readResult, is(notNullValue()));
			assertThat(readResult.getId(), is(createResult.getId()));
		} finally {
			db.collection(COLLECTION_NAME).drop();
		}
	}

	@Test
	public void deleteCollection() {
		db.createCollection(COLLECTION_NAME, null);
		db.collection(COLLECTION_NAME).drop();
		try {
			db.readCollection(COLLECTION_NAME);
			fail();
		} catch (final ArangoDBException e) {
		}
	}

	@Test
	public void readIndex() {
		try {
			db.createCollection(COLLECTION_NAME, null);
			final Collection<String> fields = new ArrayList<>();
			fields.add("a");
			final IndexResult createResult = db.collection(COLLECTION_NAME).createHashIndex(fields, null);
			final IndexResult readResult = db.readIndex(createResult.getId());
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
				db.readIndex(id);
				fail();
			} catch (final ArangoDBException e) {
			}
		} finally {
			db.collection(COLLECTION_NAME).drop();
		}
	}

	@Test
	public void readCollections() {
		try {
			final Collection<CollectionResult> systemCollections = db.readCollections(null);
			db.createCollection(COLLECTION_NAME + "1", null);
			db.createCollection(COLLECTION_NAME + "2", null);
			final Collection<CollectionResult> collections = db.readCollections(null);
			assertThat(collections.size(), is(2 + systemCollections.size()));
			assertThat(collections, is(notNullValue()));
		} finally {
			db.collection(COLLECTION_NAME + "1").drop();
			db.collection(COLLECTION_NAME + "2").drop();
		}
	}

	@Test
	public void readCollectionsExcludeSystem() {
		try {
			final CollectionsReadOptions options = new CollectionsReadOptions().excludeSystem(true);
			final Collection<CollectionResult> systemCollections = db.readCollections(options);
			assertThat(systemCollections.size(), is(0));
			db.createCollection(COLLECTION_NAME + "1", null);
			db.createCollection(COLLECTION_NAME + "2", null);
			final Collection<CollectionResult> collections = db.readCollections(options);
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
	@Ignore
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
}
