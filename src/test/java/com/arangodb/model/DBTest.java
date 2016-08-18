package com.arangodb.model;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.After;
import org.junit.Test;

import com.arangodb.ArangoDBException;
import com.arangodb.BaseTest;
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
			db.deleteCollection(COLLECTION_NAME).execute();
		} catch (final ArangoDBException e) {
		}
	}

	@Test
	public void createCollection() {
		final CollectionResult result = db.createCollection(COLLECTION_NAME, null).execute();
		assertThat(result, is(notNullValue()));
		assertThat(result.getId(), is(notNullValue()));
	}

	@Test
	public void readCollection() {
		final CollectionResult createResult = db.createCollection(COLLECTION_NAME, null).execute();
		final CollectionResult readResult = db.readCollection(COLLECTION_NAME).execute();
		assertThat(readResult, is(notNullValue()));
		assertThat(readResult.getId(), is(createResult.getId()));
	}

	@Test
	public void deleteCollection() {
		db.createCollection(COLLECTION_NAME, null).execute();
		db.deleteCollection(COLLECTION_NAME).execute();
		try {
			db.readCollection(COLLECTION_NAME).execute();
			fail();
		} catch (final ArangoDBException e) {
		}
	}

	@Test
	public void readIndex() {
		db.createCollection(COLLECTION_NAME, null).execute();
		final Collection<String> fields = new ArrayList<>();
		fields.add("a");
		final IndexResult createResult = db.collection(COLLECTION_NAME).createHashIndex(fields, null).execute();
		final IndexResult readResult = db.readIndex(createResult.getId()).execute();
		assertThat(readResult.getId(), is(createResult.getId()));
		assertThat(readResult.getType(), is(createResult.getType()));
	}

	@Test
	public void deleteIndex() {
		db.createCollection(COLLECTION_NAME, null).execute();
		final Collection<String> fields = new ArrayList<>();
		fields.add("a");
		final IndexResult createResult = db.collection(COLLECTION_NAME).createHashIndex(fields, null).execute();
		final String id = db.deleteIndex(createResult.getId()).execute();
		assertThat(id, is(createResult.getId()));
		try {
			db.readIndex(id).execute();
			fail();
		} catch (final ArangoDBException e) {
		}
	}
}
