package com.arangodb;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.arangodb.entity.ArangoDBVersion;
import com.arangodb.entity.UserResult;
import com.arangodb.model.UserCreateOptions;
import com.arangodb.model.UserUpdateOptions;

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
	public void createDB() {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		final Boolean result = arangoDB.createDB(BaseTest.TEST_DB);
		assertThat(result, is(true));
		try {
			arangoDB.db(BaseTest.TEST_DB).drop();
		} catch (final ArangoDBException e) {
		}
	}

	@Test
	public void deleteDB() {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		final Boolean resultCreate = arangoDB.createDB(BaseTest.TEST_DB);
		assertThat(resultCreate, is(true));
		final Boolean resultDelete = arangoDB.db(BaseTest.TEST_DB).drop();
		assertThat(resultDelete, is(true));
	}

	@Test
	public void getDBs() {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		try {
			Collection<String> dbs = arangoDB.getDBs();
			assertThat(dbs, is(notNullValue()));
			assertThat(dbs.size(), is(1));
			assertThat(dbs.stream().findFirst().get(), is("_system"));
			arangoDB.createDB(BaseTest.TEST_DB);
			dbs = arangoDB.getDBs();
			assertThat(dbs.size(), is(2));
			assertThat(dbs, hasItem("_system"));
			assertThat(dbs, hasItem(BaseTest.TEST_DB));
		} finally {
			arangoDB.db(BaseTest.TEST_DB).drop();
		}
	}

	@Test
	public void createUser() {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		try {
			final UserResult result = arangoDB.createUser(USER, PW, null);
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
		final UserResult user = arangoDB.getUser(ROOT);
		assertThat(user, is(notNullValue()));
		assertThat(user.getUser(), is(ROOT));
	}

	@Test
	public void getUser() {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		try {
			arangoDB.createUser(USER, PW, null);
			final UserResult user = arangoDB.getUser(USER);
			assertThat(user.getUser(), is(USER));
		} finally {
			arangoDB.deleteUser(USER);
		}

	}

	@Test
	public void getUsersOnlyRoot() {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		final Collection<UserResult> users = arangoDB.getUsers();
		assertThat(users, is(notNullValue()));
		assertThat(users.size(), is(1));
		assertThat(users.stream().findFirst().get().getUser(), is(ROOT));
	}

	@Test
	public void getUsers() {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		try {
			arangoDB.createUser(USER, PW, null);
			final Collection<UserResult> users = arangoDB.getUsers();
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
	public void updateUser() {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		try {
			final Map<String, Object> extra = new HashMap<>();
			extra.put("hund", false);
			arangoDB.createUser(USER, PW, new UserCreateOptions().extra(extra));
			extra.put("hund", true);
			extra.put("mund", true);
			final UserResult user = arangoDB.updateUser(USER, new UserUpdateOptions().extra(extra));
			assertThat(user, is(notNullValue()));
			assertThat(user.getExtra().size(), is(2));
			assertThat(user.getExtra().get("hund"), is(true));
			final UserResult user2 = arangoDB.getUser(USER);
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
			final UserResult user = arangoDB.replaceUser(USER, new UserUpdateOptions().extra(extra));
			assertThat(user, is(notNullValue()));
			assertThat(user.getExtra().size(), is(1));
			assertThat(user.getExtra().get("mund"), is(true));
			final UserResult user2 = arangoDB.getUser(USER);
			assertThat(user2.getExtra().size(), is(1));
			assertThat(user2.getExtra().get("mund"), is(true));
		} finally {
			arangoDB.deleteUser(USER);
		}
	}
}
