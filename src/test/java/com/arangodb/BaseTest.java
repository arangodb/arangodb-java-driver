package com.arangodb;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.arangodb.model.DB;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public abstract class BaseTest {

	private static final String TEST_DB = "java-driver-test-db";
	private static final AtomicInteger tests = new AtomicInteger(0);
	private static ArangoDB arangoDB;
	protected static DB db;

	@BeforeClass
	public static void init() {
		if (arangoDB == null) {
			arangoDB = new ArangoDB.Builder().build();
		}
		try {
			arangoDB.createDB(TEST_DB).execute();
		} catch (final ArangoDBException e) {
		}
		BaseTest.db = arangoDB.db(TEST_DB);
		tests.incrementAndGet();
	}

	@AfterClass
	public static void shutdown() {
		try {
			arangoDB.deleteDB(TEST_DB).execute();
		} catch (final ArangoDBException e) {
		}
		if (tests.decrementAndGet() == 0) {
			arangoDB.shutdown();
			arangoDB = null;
		}
	}

}
