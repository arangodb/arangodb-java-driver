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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.arangodb.entity.BooleanResultEntity;
import com.arangodb.entity.StringsResultEntity;
import com.arangodb.entity.UserEntity;
import com.arangodb.util.MapBuilder;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ArangoDriverDatabaseAndUserTest {

	private ArangoConfigure configure;
	private ArangoDriver driver;
	private static final String DATABASE = "db-1";

	@Before
	public void before() {

		configure = new ArangoConfigure();
		configure.init();
		driver = new ArangoDriver(configure);

	}

	@After
	public void after() {
		try {
			driver.deleteDatabase(DATABASE);
		} catch (final ArangoException e) {
		}
		configure.shutdown();
	}

	@Test
	public void test_create_database_with_users_and_database_user() throws ArangoException {

		try {
			driver.deleteDatabase(DATABASE);
		} catch (final ArangoException e) {
		}

		try {
			driver.deleteDatabase("unitTestDatabase");
		} catch (final ArangoException e) {
		}

		final BooleanResultEntity entity = driver.createDatabase(DATABASE, new UserEntity("user1", "pass1", true, null),
			new UserEntity("user2", "pass2", false, null),
			new UserEntity("user3", "pass3", true, new MapBuilder().put("attr1", "value1").get()),
			new UserEntity("user4", "pass4", false, new MapBuilder().put("attr2", "value2").get()));
		assertThat(entity.getResult(), is(true));
		driver.grantDatabaseAccess("user1", "_system");
		driver.grantDatabaseAccess("user4", DATABASE);

		// change default db
		driver.setDefaultDatabase(DATABASE);

		// user1 can access
		configure.setUser("user1");
		configure.setPassword("pass1");
		final StringsResultEntity res2 = driver.getDatabases(true);
		assertThat(res2.getResult(), is(Arrays.asList("_system", DATABASE)));

		// user2 cannot access (inactive)
		configure.setUser("user2");
		configure.setPassword("pass2");
		try {
			driver.getUsers();
			fail();
		} catch (final ArangoException e) {
			assertThat(e.isUnauthorized(), is(true));
		}

		configure.setUser("root");
		configure.setPassword("");

		final StringsResultEntity res3 = driver.getDatabases("user1", "pass1");
		assertThat(res3.getResult(), is(Arrays.asList("_system", DATABASE)));
	}

}
