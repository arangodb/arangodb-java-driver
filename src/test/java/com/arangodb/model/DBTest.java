package com.arangodb.model;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import com.arangodb.ArangoDBException;
import com.arangodb.BaseTest;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionResult;
import com.arangodb.entity.IndexResult;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DBTest extends BaseTest {

	private static final String COLLECTION_NAME = "db_test";

	@After
	public void teardown() {
		try {
			db.collection(COLLECTION_NAME).drop();
		} catch (final ArangoDBException e) {
		}
	}

	@Test
	public void createCollection() {
		final CollectionResult result = db.createCollection(COLLECTION_NAME, null);
		assertThat(result, is(notNullValue()));
		assertThat(result.getId(), is(notNullValue()));
	}

	@Test
	public void readCollection() {
		final CollectionResult createResult = db.createCollection(COLLECTION_NAME, null);
		final CollectionResult readResult = db.readCollection(COLLECTION_NAME);
		assertThat(readResult, is(notNullValue()));
		assertThat(readResult.getId(), is(createResult.getId()));
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
		db.createCollection(COLLECTION_NAME, null);
		final Collection<String> fields = new ArrayList<>();
		fields.add("a");
		final IndexResult createResult = db.collection(COLLECTION_NAME).createHashIndex(fields, null);
		final IndexResult readResult = db.readIndex(createResult.getId());
		assertThat(readResult.getId(), is(createResult.getId()));
		assertThat(readResult.getType(), is(createResult.getType()));
	}

	@Test
	public void deleteIndex() {
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
	@Ignore
	public void executeAQL() {
		try {
			db.createCollection(COLLECTION_NAME, null);
			for (int i = 0; i < 10; i++) {
				db.collection(COLLECTION_NAME).insert(new BaseDocument(), null);
			}
			final Cursor<String> cursor = db.executeAQL("for i in db_test return i._id", null, null, String.class);
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
}
