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

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.arangodb.entity.ArangoDBVersion;
import com.arangodb.entity.UserEntity;
import com.arangodb.model.UserCreateOptions;
import com.arangodb.model.UserUpdateOptions;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;
import com.arangodb.velocystream.Response;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ArangoDBTest {

	private static final String ROOT = "root";
	private static final String USER = "mit dem mund";
	private static final String PW = "machts der hund";

	@Test
	public void getVersion() {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		final ArangoDBVersion version = arangoDB.getVersion();
		assertThat(version, is(notNullValue()));
		assertThat(version.getServer(), is(notNullValue()));
		assertThat(version.getVersion(), is(notNullValue()));
	}

	@Test
	public void createDatabase() {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		final Boolean result = arangoDB.createDatabase(BaseTest.TEST_DB);
		assertThat(result, is(true));
		try {
			arangoDB.db(BaseTest.TEST_DB).drop();
		} catch (final ArangoDBException e) {
		}
	}

	@Test
	public void deleteDatabase() {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		final Boolean resultCreate = arangoDB.createDatabase(BaseTest.TEST_DB);
		assertThat(resultCreate, is(true));
		final Boolean resultDelete = arangoDB.db(BaseTest.TEST_DB).drop();
		assertThat(resultDelete, is(true));
	}

	@Test
	public void getDatabases() {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		try {
			Collection<String> dbs = arangoDB.getDatabases();
			assertThat(dbs, is(notNullValue()));
			assertThat(dbs.size(), is(1));
			assertThat(dbs.stream().findFirst().get(), is("_system"));
			arangoDB.createDatabase(BaseTest.TEST_DB);
			dbs = arangoDB.getDatabases();
			assertThat(dbs.size(), is(2));
			assertThat(dbs, hasItem("_system"));
			assertThat(dbs, hasItem(BaseTest.TEST_DB));
		} finally {
			arangoDB.db(BaseTest.TEST_DB).drop();
		}
	}

	@Test
	public void getAccessibleDatabases() {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		final Collection<String> dbs = arangoDB.getAccessibleDatabases();
		assertThat(dbs, is(notNullValue()));
		assertThat(dbs.size(), is(1));
		assertThat(dbs.stream().findFirst().get(), is("_system"));
	}

	@Test
	public void createUser() {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		try {
			final UserEntity result = arangoDB.createUser(USER, PW, null);
			assertThat(result, is(notNullValue()));
			assertThat(result.getUser(), is(USER));
			assertThat(result.getChangePassword(), is(false));
		} finally {
			arangoDB.deleteUser(USER);
		}
	}

	@Test
	public void deleteUser() {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		arangoDB.createUser(USER, PW, null);
		arangoDB.deleteUser(USER);
	}

	@Test
	public void getUserRoot() {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		final UserEntity user = arangoDB.getUser(ROOT);
		assertThat(user, is(notNullValue()));
		assertThat(user.getUser(), is(ROOT));
	}

	@Test
	public void getUser() {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		try {
			arangoDB.createUser(USER, PW, null);
			final UserEntity user = arangoDB.getUser(USER);
			assertThat(user.getUser(), is(USER));
		} finally {
			arangoDB.deleteUser(USER);
		}

	}

	@Test
	public void getUsersOnlyRoot() {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		final Collection<UserEntity> users = arangoDB.getUsers();
		assertThat(users, is(notNullValue()));
		assertThat(users.size(), is(1));
		assertThat(users.stream().findFirst().get().getUser(), is(ROOT));
	}

	@Test
	public void getUsers() {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		try {
			arangoDB.createUser(USER, PW, null);
			final Collection<UserEntity> users = arangoDB.getUsers();
			assertThat(users, is(notNullValue()));
			assertThat(users.size(), is(2));
			users.stream().forEach(user -> {
				assertThat(user.getUser(), anyOf(is(ROOT), is(USER)));
			});
		} finally {
			arangoDB.deleteUser(USER);
		}
	}

	@Test
	public void updateUserNoOptions() {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		try {
			arangoDB.createUser(USER, PW, null);
			arangoDB.updateUser(USER, null);
		} finally {
			arangoDB.deleteUser(USER);
		}
	}

	@Test
	public void updateUser() {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		try {
			final Map<String, Object> extra = new HashMap<>();
			extra.put("hund", false);
			arangoDB.createUser(USER, PW, new UserCreateOptions().extra(extra));
			extra.put("hund", true);
			extra.put("mund", true);
			final UserEntity user = arangoDB.updateUser(USER, new UserUpdateOptions().extra(extra));
			assertThat(user, is(notNullValue()));
			assertThat(user.getExtra().size(), is(2));
			assertThat(user.getExtra().get("hund"), is(true));
			final UserEntity user2 = arangoDB.getUser(USER);
			assertThat(user2.getExtra().size(), is(2));
			assertThat(user2.getExtra().get("hund"), is(true));
		} finally {
			arangoDB.deleteUser(USER);
		}
	}

	@Test
	public void replaceUser() {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		try {
			final Map<String, Object> extra = new HashMap<>();
			extra.put("hund", false);
			arangoDB.createUser(USER, PW, new UserCreateOptions().extra(extra));
			extra.remove("hund");
			extra.put("mund", true);
			final UserEntity user = arangoDB.replaceUser(USER, new UserUpdateOptions().extra(extra));
			assertThat(user, is(notNullValue()));
			assertThat(user.getExtra().size(), is(1));
			assertThat(user.getExtra().get("mund"), is(true));
			final UserEntity user2 = arangoDB.getUser(USER);
			assertThat(user2.getExtra().size(), is(1));
			assertThat(user2.getExtra().get("mund"), is(true));
		} finally {
			arangoDB.deleteUser(USER);
		}
	}

	@Test
	public void authenticationFailPassword() {
		final ArangoDB arangoDB = new ArangoDB.Builder().password("no").build();
		try {
			arangoDB.getVersion();
			fail();
		} catch (final ArangoDBException e) {

		}
	}

	@Test
	public void authenticationFailUser() {
		final ArangoDB arangoDB = new ArangoDB.Builder().user("no").build();
		try {
			arangoDB.getVersion();
			fail();
		} catch (final ArangoDBException e) {

		}
	}

	@Test
	public void execute() throws VPackException {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		final Response response = arangoDB.execute(new Request("_system", RequestType.GET, "/_api/version"));
		assertThat(response.getBody().isPresent(), is(true));
		assertThat(response.getBody().get().get("version").isString(), is(true));
	}
}
