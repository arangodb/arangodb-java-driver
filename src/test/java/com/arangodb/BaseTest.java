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

import java.util.Arrays;
import java.util.Collection;

import org.junit.AfterClass;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Mark Vollmary
 *
 */
public abstract class BaseTest {

	@Parameters
	public static Collection<ArangoDB.Builder> builders() {
		return Arrays.asList(//
			new ArangoDB.Builder().useProtocol(Protocol.VST), //
			new ArangoDB.Builder().useProtocol(Protocol.HTTP_JSON), //
			new ArangoDB.Builder().useProtocol(Protocol.HTTP_VPACK) //
		);
	}

	protected static final String TEST_DB = "java_driver_test_db";
	protected static final String TEST_DB_CUSTOM = "java_driver_test_db_custom";
	protected static ArangoDB arangoDB;
	protected static ArangoDatabase db;

	public BaseTest(final ArangoDB.Builder builder) {
		super();
		if (arangoDB != null) {
			shutdown();
		}
		arangoDB = builder.build();		
		db = arangoDB.db(TEST_DB);	
		
		// only create the database if not existing
		try {
			db.getVersion().getVersion();
		} catch (final ArangoDBException e) {
			if (e.getErrorNum() == 1228) { // DATABASE NOT FOUND
				arangoDB.createDatabase(TEST_DB);
			}
		}
	}

	@AfterClass
	public static void shutdown() {
		arangoDB.shutdown();
		arangoDB = null;
	}

	protected boolean requireVersion(final int major, final int minor) {
		final String[] split = arangoDB.getVersion().getVersion().split("\\.");
		return Integer.valueOf(split[0]) >= major && Integer.valueOf(split[1]) >= minor;
	}

}
