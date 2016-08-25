package com.arangodb;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.junit.Test;

import com.arangodb.entity.ArangoDBVersion;
import com.arangodb.entity.UserResult;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ArangoDBTest {

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
			final UserResult result = arangoDB.createUser("mit dem mund", "machts der hund", null);
			assertThat(result, is(notNullValue()));
			assertThat(result.getUser(), is("mit dem mund"));
			assertThat(result.getChangePassword(), is(false));
		} finally {
			arangoDB.deleteUser("mit dem mund");
		}
	}

	@Test
	public void deleteUser() {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		arangoDB.createUser("mit dem mund", "machts der hund", null);
		arangoDB.deleteUser("mit dem mund");
	}

	@Test
	public void getUsersOnlyRoot() {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		final Collection<UserResult> users = arangoDB.getUsers();
		assertThat(users, is(notNullValue()));
		assertThat(users.size(), is(1));
		assertThat(users.stream().findFirst().get().getUser(), is("root"));
	}

	@Test
	public void getUsers() {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		try {
			arangoDB.createUser("mit dem mund", "machts der hund", null);
			final Collection<UserResult> users = arangoDB.getUsers();
			assertThat(users, is(notNullValue()));
			assertThat(users.size(), is(2));
			users.stream().forEach(user -> {
				assertThat(user.getUser(), anyOf(is("root"), is("mit dem mund")));
			});
		} finally {
			arangoDB.deleteUser("mit dem mund");
		}
	}
}
