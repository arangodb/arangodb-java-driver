/*
 * Copyright (C) 2012,2013 tamtam180
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

package com.arangodb;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Collections;

import org.junit.AfterClass;
import org.junit.Test;

import com.arangodb.entity.BooleanResultEntity;
import com.arangodb.entity.DatabaseEntity;
import com.arangodb.entity.StringsResultEntity;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * 
 */
public class ArangoDriverDatabaseTest extends BaseTest {

	private static final String DB_NAME = "abcdefghi1abcdefghi2abcdefghi3abcdefghi4abcdefghi5abcdefghi61234";
	private static final String[] DATABASES = new String[] { "db-1", "db_2", "db-_-3", "mydb", // other
			// testcase
			"mydb2", // other testcase
			"repl_scenario_test1", // other test case
			"unitTestDatabase", // other test case
	};

	@AfterClass
	public static void _afterClass() {
		try {
			driver.deleteDatabase(DB_NAME);
		} catch (final ArangoException e) {
		}
		for (final String database : DATABASES) {
			try {
				driver.deleteDatabase(database);
			} catch (final ArangoException e) {
			}
		}
	}

	@Test
	public void test_invalid_dbname1() throws ArangoException {
		try {
			driver.createDatabase(null);
			fail();
		} catch (final ArangoException e) {
			assertThat(e.getMessage(), is("invalid format database:null"));
		}
	}

	@Test
	public void test_invalid_dbname2() throws ArangoException {
		try {
			driver.createDatabase("0");
			fail();
		} catch (final ArangoException e) {
			assertThat(e.getMessage(), is("invalid format database:0"));
		}
	}

	@Test
	public void test_invalid_dbname3() throws ArangoException {
		try {
			driver.createDatabase("abcdefghi1abcdefghi2abcdefghi3abcdefghi4abcdefghi5abcdefghi612345"); // len=65
			fail();
		} catch (final ArangoException e) {
			assertThat(e.getMessage(),
				is("invalid format database:abcdefghi1abcdefghi2abcdefghi3abcdefghi4abcdefghi5abcdefghi612345"));
		}
	}

	@Test
	public void test_invalid_dbname_for_delete() throws ArangoException {
		try {
			driver.deleteDatabase("abcdefghi1abcdefghi2abcdefghi3abcdefghi4abcdefghi5abcdefghi612345"); // len=65
			fail();
		} catch (final ArangoException e) {
			assertThat(e.getMessage(),
				is("invalid format database:abcdefghi1abcdefghi2abcdefghi3abcdefghi4abcdefghi5abcdefghi612345"));
		}
	}

	@Test
	public void test_current_database() throws ArangoException {
		driver.setDefaultDatabase("_system");
		final DatabaseEntity entity = driver.getCurrentDatabase();
		assertThat(entity.isError(), is(false));
		assertThat(entity.getCode(), is(200));
		assertThat(entity.getName(), is("_system"));
		assertThat(entity.getId(), is(notNullValue()));
		assertThat(entity.getPath(), is(notNullValue()));
		assertThat(entity.isSystem(), is(true));

	}

	@Test
	public void test_current_database2() throws ArangoException {
		try {
			try {
				driver.deleteDatabase(DB_NAME);
			} catch (final ArangoException e) {
			}
			driver.createDatabase(DB_NAME);
			driver.setDefaultDatabase(DB_NAME);
			final DatabaseEntity entity = driver.getCurrentDatabase();
			assertThat(entity.getName(), is(DB_NAME));
		} finally {
			driver.deleteDatabase(DB_NAME);
		}
	}

	@Test
	public void test_createDatabase() throws ArangoException {

		final String database = DB_NAME;

		try {
			driver.deleteDatabase(database);
		} catch (final ArangoException e) {
		}

		final BooleanResultEntity entity = driver.createDatabase(database); // len=64
		assertThat(entity.getResult(), is(true));

	}

	@Test
	public void test_createDatabase_duplicate() throws ArangoException {

		final String database = DB_NAME;

		try {
			driver.deleteDatabase(database);
		} catch (final ArangoException e) {
		}

		final BooleanResultEntity entity = driver.createDatabase(database); // len=64
		assertThat(entity.getResult(), is(true));

		try {
			driver.createDatabase(database);
			fail();
		} catch (final ArangoException e) {
			assertThat(e.getCode(), is(409));
			assertThat(e.getErrorNumber(), is(1207));
		}

	}

	@Test
	public void test_delete() throws ArangoException {

		final String database = DB_NAME;

		try {
			driver.deleteDatabase(database);
		} catch (final ArangoException e) {
		}

		BooleanResultEntity entity = driver.createDatabase(database); // len=64
		assertThat(entity.getResult(), is(true));
		assertThat(entity.getCode(), is(201));
		assertThat(entity.isError(), is(false));

		entity = driver.deleteDatabase(database);
		assertThat(entity.getResult(), is(true));
		assertThat(entity.getCode(), is(200));
		assertThat(entity.isError(), is(false));

	}

	@Test
	public void test_delete_404() throws ArangoException {

		final String database = DB_NAME;

		try {
			driver.deleteDatabase(database);
		} catch (final ArangoException e) {
		}

		try {
			driver.deleteDatabase(database);
			fail();
		} catch (final ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorNumber(), is(1228));
		}

	}

	@Test
	public void test_get_databases() throws ArangoException {

		for (final String database : DATABASES) {
			try {
				driver.deleteDatabase(database);
			} catch (final ArangoException e) {
			}
			try {
				driver.createDatabase(database);
			} catch (final ArangoException e) {
			}
		}

		final StringsResultEntity entity = driver.getDatabases();
		assertThat(entity.isError(), is(false));
		assertThat(entity.getCode(), is(200));

		Collections.sort(entity.getResult());
		assertThat(entity.getResult().indexOf("_system"), not(-1));
		assertThat(entity.getResult().indexOf("db-1"), not(-1));
		assertThat(entity.getResult().indexOf("db_2"), not(-1));
		assertThat(entity.getResult().indexOf("db-_-3"), not(-1));
		assertThat(entity.getResult().indexOf("mydb"), not(-1));
		assertThat(entity.getResult().indexOf("mydb2"), not(-1));
		assertThat(entity.getResult().indexOf("repl_scenario_test1"), not(-1));
		assertThat(entity.getResult().indexOf("unitTestDatabase"), not(-1));
	}

}
