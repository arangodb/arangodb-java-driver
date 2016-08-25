package com.arangodb;

import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public abstract class BaseTest {

	protected static final String TEST_DB = "java_driver_test_db";
	protected static ArangoDB arangoDB;
	protected static ArangoDatabase db;

	@BeforeClass
	public static void init() {
		if (arangoDB == null) {
			arangoDB = new ArangoDB.Builder().build();
		}
		try {
			arangoDB.db(TEST_DB).drop();
		} catch (final ArangoDBException e) {
		}
		arangoDB.createDatabase(TEST_DB);
		BaseTest.db = arangoDB.db(TEST_DB);
	}

	@AfterClass
	public static void shutdown() {
		arangoDB.db(TEST_DB).drop();
		arangoDB.shutdown();
		arangoDB = null;
	}

}
