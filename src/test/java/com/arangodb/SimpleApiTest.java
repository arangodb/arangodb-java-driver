package com.arangodb;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.internal.ArangoDBConstants;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class SimpleApiTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleApiTest.class);

	@Test
	public void simplyAnyTest() {
		Map myMap = null;
		try {
			final ArangoDB arangoDB = new ArangoDB.Builder().build();
			myMap = arangoDB.db().collection("_users").simpleAnyDocument(Map.class).execute();
		} catch (ArangoDBException ex) {
			LOGGER.error(ArangoDBConstants.PATH_API_SIMPLE_ANY + " failed", ex);
		}
		assertThat(myMap, is(notNullValue()));
	}

}
