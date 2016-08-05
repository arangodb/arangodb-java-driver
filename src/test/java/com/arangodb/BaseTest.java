package com.arangodb;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.arangodb.model.DB;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class BaseTest {

	private static final String TEST_DB = "java-driver-test-db";
	private static ArangoDB arangoDB;
	protected static DB db;

	@BeforeClass
	public static void setup() {
		arangoDB = new ArangoDB.Builder().build();
		try {
			arangoDB.createDB(TEST_DB).execute();
		} catch (final ArangoDBException e) {
		}
		BaseTest.db = arangoDB.db(TEST_DB);
	}

	@AfterClass
	public static void shutdown() {
		try {
			arangoDB.deleteDB(TEST_DB).execute();
		} catch (final ArangoDBException e) {
		}
	}

}
