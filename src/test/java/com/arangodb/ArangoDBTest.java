package com.arangodb;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

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
		final ArangoDBVersion version = arangoDB.getVersion().execute();
		assertThat(version, is(notNullValue()));
		assertThat(version.getServer(), is(notNullValue()));
		assertThat(version.getVersion(), is(notNullValue()));
	}

	@Test
	public void createDB() {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		final Boolean result = arangoDB.createDB(BaseTest.TEST_DB).execute();
		assertThat(result, is(true));
		try {
			arangoDB.deleteDB(BaseTest.TEST_DB).execute();
		} catch (final ArangoDBException e) {
		}
	}

	@Test
	public void deleteDB() {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		final Boolean resultCreate = arangoDB.createDB(BaseTest.TEST_DB).execute();
		assertThat(resultCreate, is(true));
		final Boolean resultDelete = arangoDB.deleteDB(BaseTest.TEST_DB).execute();
		assertThat(resultDelete, is(true));
	}

	@Test
	public void createUser() {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		try {
			final UserResult result = arangoDB.createUser("mit dem mund", "machts der hund", null).execute();
			assertThat(result, is(notNullValue()));
			assertThat(result.getUser(), is("mit dem mund"));
			assertThat(result.getChangePassword(), is(false));
		} finally {
			arangoDB.deleteUser("mit dem mund").execute();
		}
	}

	@Test
	public void deleteUser() {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		arangoDB.createUser("mit dem mund", "machts der hund", null).execute();
		arangoDB.deleteUser("mit dem mund").execute();
	}

}
