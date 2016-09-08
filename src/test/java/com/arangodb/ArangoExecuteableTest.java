package com.arangodb;

import org.junit.Test;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ArangoExecuteableTest {

	@Test
	public void validateDocumentKeyValid() {
		checkDocumentKey("1test");
		checkDocumentKey("test1");
		checkDocumentKey("test-1");
		checkDocumentKey("test_1");
		checkDocumentKey("_test");
	}

	@Test(expected = ArangoDBException.class)
	public void validateDocumentKeyInvalidSlash() {
		checkDocumentKey("test/test");
	}

	@Test(expected = ArangoDBException.class)
	public void validateDocumentKeyEmpty() {
		checkDocumentKey("");
	}

	private void checkDocumentKey(final String key) throws ArangoDBException {
		final ArangoExecuteable executeBase = new ArangoExecuteable(null, null, null, null, null, null) {
		};
		executeBase.validateDocumentKey(key);
	}
}
