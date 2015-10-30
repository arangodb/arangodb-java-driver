/*
 * Copyright (C) 2015 ArangoDB GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arangodb.example.document;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.entity.BooleanResultEntity;
import com.arangodb.entity.StringsResultEntity;

public class CreateAndDeleteDatabaseExample extends BaseExample {

	private static final String DATABASE_NAME = "CreateDatabaseExample";

	@Before
	public void _before() {
		removeTestDatabase(DATABASE_NAME);
	}

	@Test
	public void createAndDeleteDatabase() {
		//
		// You can find the ArangoDB Web interface here:
		// http://127.0.0.1:8529/
		//
		// change the log level to "debug" in /src/test/resource/logback.xml to
		// see the HTTP communication

		//
		printHeadline("create a driver");
		//

		ArangoDriver arangoDriver = new ArangoDriver(getConfiguration());

		Assert.assertNotNull(arangoDriver);

		//
		printHeadline("create a database");
		//
		try {
			BooleanResultEntity createDatabase = arangoDriver.createDatabase(DATABASE_NAME);
			Assert.assertNotNull(createDatabase);
			Assert.assertNotNull(createDatabase.getResult());
			Assert.assertTrue(createDatabase.getResult());

			System.out.println("Database created: " + DATABASE_NAME);
		} catch (ArangoException e) {
			Assert.fail("Failed to create database " + DATABASE_NAME + "; " + e.getMessage());
		}
		// set a default database for the connection
		arangoDriver.setDefaultDatabase(DATABASE_NAME);

		// do something ...

		//
		printHeadline("read names of all databases");
		//
		try {
			StringsResultEntity databases = arangoDriver.getDatabases();
			Assert.assertNotNull(databases);
			Assert.assertNotNull(databases.getResult());
			Assert.assertTrue(databases.getResult().size() > 0);

			for (String str : databases.getResult()) {
				System.out.println("Database: " + str);
			}

		} catch (ArangoException e) {
			Assert.fail("Failed to read databases. " + e.getMessage());
		}

		//
		printHeadline("create a driver with default database");
		//
		ArangoConfigure configure2 = new ArangoConfigure();
		configure2.init();
		ArangoDriver arangoDriver2 = new ArangoDriver(configure2, DATABASE_NAME);
		Assert.assertNotNull(arangoDriver2);

		// do something ...

		//
		printHeadline("delete database");
		//
		try {
			BooleanResultEntity deleteDatabase = arangoDriver.deleteDatabase(DATABASE_NAME);
			Assert.assertNotNull(deleteDatabase);
			Assert.assertNotNull(deleteDatabase.getResult());
			Assert.assertTrue(deleteDatabase.getResult());
		} catch (ArangoException e) {
			Assert.fail("Failed to delete database " + DATABASE_NAME + "; " + e.getMessage());
		}

	}
}
