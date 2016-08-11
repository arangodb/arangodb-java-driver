package com.arangodb;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.arangodb.entity.ArangoDBVersion;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class VersionTest {

	@Test
	public void version() {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		final ArangoDBVersion version = arangoDB.getVersion().execute();
		assertThat(version, is(notNullValue()));
		assertThat(version.getServer(), is(notNullValue()));
		assertThat(version.getVersion(), is(notNullValue()));
	}

}
