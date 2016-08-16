package com.arangodb;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Test;

import com.arangodb.entity.CollectionResult;

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
}
