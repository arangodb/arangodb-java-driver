/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

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
