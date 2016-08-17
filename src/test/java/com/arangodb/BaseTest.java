package com.arangodb;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.arangodb.model.DB;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public abstract class BaseTest {

	protected static final String TEST_DB = "java_driver_test_db";
	private static ArangoDB arangoDB;
	protected static DB db;

	@BeforeClass
	public static void init() {
		if (arangoDB == null) {
			arangoDB = new ArangoDB.Builder().build();
		}
		try {
			arangoDB.deleteDB(TEST_DB).execute();
		} catch (final ArangoDBException e) {
		}
		arangoDB.createDB(TEST_DB).execute();
		BaseTest.db = arangoDB.db(TEST_DB);
	}

	@AfterClass
	public static void shutdown() {
		arangoDB.deleteDB(TEST_DB).execute();
		arangoDB.shutdown();
	}

}
