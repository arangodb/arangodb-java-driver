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

import org.junit.Test;

import at.orz.arangodb.entity.ArangoUnixTime;

/**
 * Basic Auth test.
 * Must "disable-authentication=no" in server configure.
 * 
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ArangoDriverAuthTest {
	
	@Test
	public void test_auth() throws ArangoException {
		
		ArangoConfigure configure = new ArangoConfigure();
		configure.setUser(null);
		configure.setPassword(null);
		configure.init();
		
		ArangoDriver driver = new ArangoDriver(configure);
		try {
			driver.getTime();
			fail();
		} catch (ArangoException e) {
			assertThat(e.isUnauthorized(), is(true));
			assertThat(e.getEntity().getStatusCode(), is(401));
			assertThat(e.getMessage(), containsString("Unauthorized"));
		}
		
		configure.shutdown();
	}

	@Test
	public void test_auth_root() throws ArangoException {
		
		ArangoConfigure configure = new ArangoConfigure();
		configure.setUser("root");
		configure.setPassword("");
		configure.init();
		
		ArangoDriver driver = new ArangoDriver(configure);
		driver.getTime();
		
		configure.shutdown();
	}

	@Test
	public void test_auth_added_user() throws ArangoException {
		
		ArangoConfigure configure = new ArangoConfigure();
		configure.setUser("root");
		configure.setPassword("");
		configure.init();
		
		ArangoDriver driver = new ArangoDriver(configure);
		
		// Create User
		try {
			driver.createUser("userA", "passA", true, null);
		} catch (ArangoException e) {
			driver.replaceUser("userA", "passA", true, null);
		}
		
		configure.shutdown();
		
		configure = new ArangoConfigure();
		configure.setUser("userA");
		configure.setPassword("passA");
		configure.init();
		driver = new ArangoDriver(configure);

		ArangoUnixTime time = driver.getTime();
		configure.shutdown();
		
	}

	@Test
	public void test_auth_added_user_inactive() throws ArangoException {
		
		ArangoConfigure configure = new ArangoConfigure();
		configure.setUser("root");
		configure.setPassword("");
		configure.init();
		
		ArangoDriver driver = new ArangoDriver(configure);
		
		// Create User
		try {
			driver.createUser("userB", "passB", false, null);
		} catch (ArangoException e) {
			driver.replaceUser("userB", "passB", false, null);
		}
		
		configure.shutdown();
		
		configure = new ArangoConfigure();
		configure.setUser("userB");
		configure.setPassword("passB");
		configure.init();
		driver = new ArangoDriver(configure);

		// Memo: Failed version 1.2.3
		try {
			ArangoUnixTime time = driver.getTime();
			fail("");
		} catch (ArangoException e) {
			assertThat(e.getErrorNumber(), is(401));
			assertThat(e.getMessage(), containsString("Unauthorized"));
		}
		configure.shutdown();

	}

	@Test
	public void test_auth_multibyte_username() throws ArangoException {

		ArangoConfigure configure = new ArangoConfigure();
		configure.setUser("root");
		configure.setPassword("");
		configure.init();
		
		ArangoDriver driver = new ArangoDriver(configure);
		
		// Create User
		try {
			driver.createUser("ゆーざーA", "pass", false, null);
		} catch (ArangoException e) {
			driver.replaceUser("ゆーざーA", "pass", false, null);
		}
		
		configure.shutdown();
		
		configure = new ArangoConfigure();
		configure.setUser("ゆーざーA");
		configure.setPassword("pass");
		configure.init();
		driver = new ArangoDriver(configure);

		try {
			ArangoUnixTime time = driver.getTime();
			fail("");
		} catch (ArangoException e) {
			assertThat(e.getErrorNumber(), is(401));
			assertThat(e.getMessage(), containsString("Unauthorized"));
		}
		configure.shutdown();

	}

	@Test
	public void test_auth_multibyte_password() throws ArangoException {

		ArangoConfigure configure = new ArangoConfigure();
		configure.setUser("root");
		configure.setPassword("");
		configure.init();
		
		ArangoDriver driver = new ArangoDriver(configure);
		
		// Create User
		try {
			driver.createUser("user-A", "パスワード", false, null);
		} catch (ArangoException e) {
			driver.replaceUser("user-A", "パスワード", false, null);
		}
		
		configure.shutdown();
		
		configure = new ArangoConfigure();
		configure.setUser("user-A");
		configure.setPassword("パスワード");
		configure.init();
		driver = new ArangoDriver(configure);

		try {
			ArangoUnixTime time = driver.getTime();
			fail("");
		} catch (ArangoException e) {
			assertThat(e.getErrorNumber(), is(401));
			assertThat(e.getMessage(), containsString("Unauthorized"));
		}
		configure.shutdown();

	}

}
