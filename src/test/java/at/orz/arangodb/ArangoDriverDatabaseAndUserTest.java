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

package at.orz.arangodb;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import at.orz.arangodb.entity.BooleanResultEntity;
import at.orz.arangodb.entity.DatabaseEntity;
import at.orz.arangodb.entity.StringsResultEntity;
import at.orz.arangodb.entity.UserEntity;
import at.orz.arangodb.util.MapBuilder;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ArangoDriverDatabaseAndUserTest {

	ArangoConfigure configure;
	ArangoDriver driver;
	
	@Before
	public void before() {

		configure = new ArangoConfigure();
		configure.init();
		driver = new ArangoDriver(configure);

	}
	
	@After
	public void after() {
		configure.shutdown();
	}

	@Test
	public void test_create_database_with_users_and_database_user() throws ArangoException {
		
		String database = "db-1";
		
		try {
			driver.deleteDatabase(database);
		} catch (ArangoException e) {}
		
		BooleanResultEntity entity = driver.createDatabase(
				database, 
				new UserEntity("user1", "pass1", true, null),
				new UserEntity("user2", "pass2", false, null),
				new UserEntity("user3", "pass3", true, new MapBuilder().put("attr1", "value1").get()),
				new UserEntity("user4", "pass4", false, new MapBuilder().put("attr2", "value2").get())
				);
		assertThat(entity.getResult(), is(true));
		
		// change default db
		driver.setDefaultDatabase(database);

		// root user cannot access
		try {
			driver.getUsers();
			fail();
		} catch (ArangoException e) {
			assertThat(e.isUnauthorized(), is(true));
		}
		
		// user1 can access
		configure.setUser("user1");
		configure.setPassword("pass1");
		StringsResultEntity res2 = driver.getDatabases(true);
		assertThat(res2.getResult(), is(Arrays.asList("_system", "db-1")));
		
		// user2 cannot access
		configure.setUser("user2");
		configure.setPassword("pass2");
		try {
			driver.getUsers();
			fail();
		} catch (ArangoException e) {
			assertThat(e.isUnauthorized(), is(true));
		}
		
		StringsResultEntity res3 = driver.getDatabases("user1", "pass1");
		assertThat(res3.getResult(), is(Arrays.asList("_system", "db-1")));

		
	}
	
	
}
