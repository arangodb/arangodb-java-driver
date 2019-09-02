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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.*;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.arangodb.ArangoDB.Builder;
import com.arangodb.entity.ArangoDBVersion;
import com.arangodb.entity.LogEntity;
import com.arangodb.entity.LogLevel;
import com.arangodb.entity.LogLevelEntity;
import com.arangodb.entity.Permissions;
import com.arangodb.entity.UserEntity;
import com.arangodb.model.LogOptions;
import com.arangodb.model.LogOptions.SortOrder;
import com.arangodb.model.UserCreateOptions;
import com.arangodb.model.UserUpdateOptions;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;
import com.arangodb.velocystream.Response;

/**
 * @author Mark Vollmary
 * @author Reşat SABIQ
 */
@RunWith(Parameterized.class)
public class ArangoDBTest {

	@Parameters
	public static Collection<ArangoDB.Builder> builders() {
		return Arrays.asList(//
				new ArangoDB.Builder().useProtocol(Protocol.VST), //
				new ArangoDB.Builder().useProtocol(Protocol.HTTP_JSON), //
				new ArangoDB.Builder().useProtocol(Protocol.HTTP_VPACK) //
		);
	}

	private static final String ROOT = "root";
	private static final String USER = "mit dem mund";
	private static final String PW = "machts der hund";
	private final ArangoDB arangoDB;

	public ArangoDBTest(final Builder builder) {
		super();
		arangoDB = builder.build();
	}

	@Test
	public void getVersion() {
		final ArangoDBVersion version = arangoDB.getVersion();
		assertThat(version, is(notNullValue()));
		assertThat(version.getServer(), is(notNullValue()));
		assertThat(version.getVersion(), is(notNullValue()));
	}

	@Test
	public void createAndDeleteDatabase() {
		final String dbName = "testDB-" + UUID.randomUUID().toString();
		final Boolean resultCreate = arangoDB.createDatabase(dbName);
		assertThat(resultCreate, is(true));
		final Boolean resultDelete = arangoDB.db(dbName).drop();
		assertThat(resultDelete, is(true));
	}

	@Test
	public void getDatabases() {
		final String dbName = "testDB-" + UUID.randomUUID().toString();
		Collection<String> dbs = arangoDB.getDatabases();
		assertThat(dbs, is(notNullValue()));
		assertThat(dbs.size(), is(greaterThan(0)));
		final int dbCount = dbs.size();
		assertThat(dbs.contains("_system"), is(true));
		arangoDB.createDatabase(dbName);
		dbs = arangoDB.getDatabases();
		assertThat(dbs.size(), is(greaterThan(dbCount)));
		assertThat(dbs, hasItem("_system"));
		assertThat(dbs, hasItem(dbName));
		arangoDB.db(dbName).drop();
	}

	@Test
	public void getAccessibleDatabases() {
		final Collection<String> dbs = arangoDB.getAccessibleDatabases();
		assertThat(dbs, is(notNullValue()));
		assertThat(dbs.size(), greaterThan(0));
		assertThat(dbs, hasItem("_system"));
	}

	@Test
	public void getAccessibleDatabasesFor() {
		final Collection<String> dbs = arangoDB.getAccessibleDatabasesFor("root");
		assertThat(dbs, is(notNullValue()));
		assertThat(dbs.size(), greaterThan(0));
		assertThat(dbs, hasItem("_system"));
	}

	@Test
	public void createUser() {
		try {
			final UserEntity result = arangoDB.createUser(USER, PW, null);
			assertThat(result, is(notNullValue()));
			assertThat(result.getUser(), is(USER));
		} finally {
			arangoDB.deleteUser(USER);
		}
	}

	@Test
	public void deleteUser() {
		arangoDB.createUser(USER, PW, null);
		arangoDB.deleteUser(USER);
	}

	@Test
	public void getUserRoot() {
		final UserEntity user = arangoDB.getUser(ROOT);
		assertThat(user, is(notNullValue()));
		assertThat(user.getUser(), is(ROOT));
	}

	@Test
	public void getUser() {
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
		final Collection<UserEntity> users = arangoDB.getUsers();
		assertThat(users, is(notNullValue()));
		assertThat(users.size(), greaterThan(0));
	}

	@Test
	public void getUsers() {
		try {
			// Allow & account for pre-existing users other than ROOT:
			final Collection<UserEntity> initialUsers = arangoDB.getUsers();

			arangoDB.createUser(USER, PW, null);
			final Collection<UserEntity> users = arangoDB.getUsers();
			assertThat(users, is(notNullValue()));
			assertThat(users.size(), is(initialUsers.size() + 1));

			final List<Matcher<? super String>> matchers = new ArrayList<>(users.size());
			// Add initial users, including root:
			for (final UserEntity userEntity : initialUsers) {
				matchers.add(is(userEntity.getUser()));
			}
			// Add USER:
			matchers.add(is(USER));

			for (final UserEntity user : users) {
				assertThat(user.getUser(), anyOf(matchers));
			}
		} finally {
			arangoDB.deleteUser(USER);
		}
	}

	@Test
	public void updateUserNoOptions() {
		try {
			arangoDB.createUser(USER, PW, null);
			arangoDB.updateUser(USER, null);
		} finally {
			arangoDB.deleteUser(USER);
		}
	}

	@Test
	public void updateUser() {
		try {
			final Map<String, Object> extra = new HashMap<>();
			extra.put("hund", false);
			arangoDB.createUser(USER, PW, new UserCreateOptions().extra(extra));
			extra.put("hund", true);
			extra.put("mund", true);
			final UserEntity user = arangoDB.updateUser(USER, new UserUpdateOptions().extra(extra));
			assertThat(user, is(notNullValue()));
			assertThat(user.getExtra().size(), is(2));
			assertThat(user.getExtra().get("hund"), is(notNullValue()));
			assertThat(Boolean.valueOf(String.valueOf(user.getExtra().get("hund"))), is(true));
			final UserEntity user2 = arangoDB.getUser(USER);
			assertThat(user2.getExtra().size(), is(2));
			assertThat(user2.getExtra().get("hund"), is(notNullValue()));
			assertThat(Boolean.valueOf(String.valueOf(user2.getExtra().get("hund"))), is(true));
		} finally {
			arangoDB.deleteUser(USER);
		}
	}

	@Test
	public void replaceUser() {
		try {
			final Map<String, Object> extra = new HashMap<>();
			extra.put("hund", false);
			arangoDB.createUser(USER, PW, new UserCreateOptions().extra(extra));
			extra.remove("hund");
			extra.put("mund", true);
			final UserEntity user = arangoDB.replaceUser(USER, new UserUpdateOptions().extra(extra));
			assertThat(user, is(notNullValue()));
			assertThat(user.getExtra().size(), is(1));
			assertThat(user.getExtra().get("mund"), is(notNullValue()));
			assertThat(Boolean.valueOf(String.valueOf(user.getExtra().get("mund"))), is(true));
			final UserEntity user2 = arangoDB.getUser(USER);
			assertThat(user2.getExtra().size(), is(1));
			assertThat(user2.getExtra().get("mund"), is(notNullValue()));
			assertThat(Boolean.valueOf(String.valueOf(user2.getExtra().get("mund"))), is(true));
		} finally {
			arangoDB.deleteUser(USER);
		}
	}

	@Test
	public void updateUserDefaultDatabaseAccess() {
		try {
			arangoDB.createUser(USER, PW);
			arangoDB.grantDefaultDatabaseAccess(USER, Permissions.RW);
		} finally {
			arangoDB.deleteUser(USER);
		}
	}

	@Test
	public void updateUserDefaultCollectionAccess() {
		try {
			arangoDB.createUser(USER, PW);
			arangoDB.grantDefaultCollectionAccess(USER, Permissions.RW);
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
			assertThat(e.getResponseCode(), is(401));
		}
	}

	@Test
	public void authenticationFailUser() {
		final ArangoDB arangoDB = new ArangoDB.Builder().user("no").build();
		try {
			arangoDB.getVersion();
			fail();
		} catch (final ArangoDBException e) {
			assertThat(e.getResponseCode(), is(401));
		}
	}

	@Test
	public void execute() throws VPackException {
		final Response response = arangoDB.execute(new Request("_system", RequestType.GET, "/_api/version"));
		assertThat(response.getBody(), is(notNullValue()));
		assertThat(response.getBody().get("version").isString(), is(true));
	}

	@Test
	public void getLogs() {
		final LogEntity logs = arangoDB.getLogs(null);
		assertThat(logs, is(notNullValue()));
		assertThat(logs.getTotalAmount(), greaterThan(0L));
		assertThat((long) logs.getLid().size(), is(logs.getTotalAmount()));
		assertThat((long) logs.getLevel().size(), is(logs.getTotalAmount()));
		assertThat((long) logs.getTimestamp().size(), is(logs.getTotalAmount()));
		assertThat((long) logs.getText().size(), is(logs.getTotalAmount()));
	}

	@Test
	public void getLogsUpto() {
		final LogEntity logsUpto = arangoDB.getLogs(new LogOptions().upto(LogLevel.WARNING));
		assertThat(logsUpto, is(notNullValue()));
		assertThat(logsUpto.getLevel(), not(contains(LogLevel.INFO)));
	}

	@Test
	public void getLogsLevel() {
		final LogEntity logsInfo = arangoDB.getLogs(new LogOptions().level(LogLevel.INFO));
		assertThat(logsInfo, is(notNullValue()));
		assertThat(logsInfo.getLevel(), everyItem(is(LogLevel.INFO)));
	}

	@Test
	public void getLogsStart() {
		final LogEntity logs = arangoDB.getLogs(null);
		assertThat(logs.getLid(), not(empty()));
		final LogEntity logsStart = arangoDB.getLogs(new LogOptions().start(logs.getLid().get(0) + 1));
		assertThat(logsStart, is(notNullValue()));
		assertThat(logsStart.getLid(), not(contains(logs.getLid().get(0))));
	}

	@Test
	public void getLogsSize() {
		final LogEntity logs = arangoDB.getLogs(null);
		assertThat(logs.getLid().size(), greaterThan(0));
		final LogEntity logsSize = arangoDB.getLogs(new LogOptions().size(logs.getLid().size() - 1));
		assertThat(logsSize, is(notNullValue()));
		assertThat(logsSize.getLid().size(), is(logs.getLid().size() - 1));
	}

	@Test
	public void getLogsOffset() {
		final LogEntity logs = arangoDB.getLogs(null);
		assertThat(logs.getTotalAmount(), greaterThan(0L));
		final LogEntity logsOffset = arangoDB.getLogs(new LogOptions().offset(1));
		assertThat(logsOffset, is(notNullValue()));
		assertThat(logsOffset.getLid(), not(hasItem(logs.getLid().get(0))));
	}

	@Test
	public void getLogsSearch() {
		final LogEntity logs = arangoDB.getLogs(null);
		final LogEntity logsSearch = arangoDB.getLogs(new LogOptions().search(BaseTest.TEST_DB));
		assertThat(logsSearch, is(notNullValue()));
		assertThat(logs.getTotalAmount(), greaterThan(logsSearch.getTotalAmount()));
	}

	@Test
	public void getLogsSortAsc() {
		final LogEntity logs = arangoDB.getLogs(new LogOptions().sort(SortOrder.asc));
		assertThat(logs, is(notNullValue()));
		long lastId = -1;
		for (final Long id : logs.getLid()) {
			assertThat(id, greaterThan(lastId));
			lastId = id;
		}
	}

	@Test
	public void getLogsSortDesc() {
		final LogEntity logs = arangoDB.getLogs(new LogOptions().sort(SortOrder.desc));
		assertThat(logs, is(notNullValue()));
		long lastId = Long.MAX_VALUE;
		for (final Long id : logs.getLid()) {
			assertThat(lastId, greaterThan(id));
			lastId = id;
		}
	}

	@Test
	public void getLogLevel() {
		final LogLevelEntity logLevel = arangoDB.getLogLevel();
		assertThat(logLevel, is(notNullValue()));
		assertThat(logLevel.getAgency(), is(LogLevelEntity.LogLevel.INFO));
	}

	@Test
	public void setLogLevel() {
		final LogLevelEntity entity = new LogLevelEntity();
		try {
			entity.setAgency(LogLevelEntity.LogLevel.ERROR);
			final LogLevelEntity logLevel = arangoDB.setLogLevel(entity);
			assertThat(logLevel, is(notNullValue()));
			assertThat(logLevel.getAgency(), is(LogLevelEntity.LogLevel.ERROR));
		} finally {
			entity.setAgency(LogLevelEntity.LogLevel.INFO);
			arangoDB.setLogLevel(entity);
		}
	}

	@Test
	public void arangoDBException() {
		try {
			arangoDB.db("no").getInfo();
			fail();
		} catch (final ArangoDBException e) {
			assertThat(e.getResponseCode(), is(404));
			assertThat(e.getErrorNum(), is(1228));
		}
	}

	@Test
	public void fallbackHost() {
		final ArangoDB arangoDB = new ArangoDB.Builder().host("not-accessible", 8529).host("127.0.0.1", 8529).build();
		final ArangoDBVersion version = arangoDB.getVersion();
		assertThat(version, is(notNullValue()));
	}

	@Test(expected = ArangoDBException.class)
	public void loadproperties() {
		new ArangoDB.Builder().loadProperties(ArangoDBTest.class.getResourceAsStream("/arangodb-bad.properties"));
	}

	@Test(expected = ArangoDBException.class)
	public void loadproperties2() {
		new ArangoDB.Builder().loadProperties(ArangoDBTest.class.getResourceAsStream("/arangodb-bad2.properties"));
	}

	@Test
	public void accessMultipleDatabases() {
		String db1 = "multipledb1";
		String db2 = "multipledb2";

		try {
			arangoDB.createDatabase(db1);
			arangoDB.createDatabase(db2);

			final ArangoDBVersion version1 = arangoDB.db(db1).getVersion();
			assertThat(version1, is(notNullValue()));
			final ArangoDBVersion version2 = arangoDB.db(db2).getVersion();
			assertThat(version2, is(notNullValue()));
		} finally {
			arangoDB.db(db1).drop();
			arangoDB.db(db2).drop();
		}
	}

  /*
	@Test
	public void acquireHostList() {
		final ArangoDB arango = new ArangoDB.Builder().acquireHostList(true).build();
		final ArangoDBVersion version = arango.getVersion();
		assertThat(version, is(notNullValue()));
	}
  */
}
