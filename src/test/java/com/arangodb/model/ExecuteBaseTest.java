package com.arangodb.model;

import org.junit.Test;

import com.arangodb.ArangoDBException;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ExecuteBaseTest {

	@Test
	public void validateDBNameValid() {
		checkDBName("test1");
		checkDBName("test-1");
		checkDBName("test_1");
	}

	@Test(expected = ArangoDBException.class)
	public void validateDBNameInvalidFirstLetterNumber() {
		checkDBName("1test");
	}

	@Test(expected = ArangoDBException.class)
	public void validateDBNameInvalidIllegalCharacter() {
		checkDBName("test/");
	}

	@Test(expected = ArangoDBException.class)
	public void validateDBNameInvalidUnderscore() {
		checkDBName("_test");
	}

	@Test(expected = ArangoDBException.class)
	public void validateDBNameInvalidEmpty() {
		checkDBName("");
	}

	@Test
	public void validateDBNameSystem() {
		checkDBName("_system");
	}

	private void checkDBName(final String name) throws ArangoDBException {
		final ExecuteBase executeBase = new ExecuteBase(null, null, null, null) {
		};
		executeBase.validateDBName(name);
	}

	@Test
	public void validateCollectionNameValid() {
		checkCollectionName("test1");
		checkCollectionName("test-1");
		checkCollectionName("test_1");
	}

	@Test(expected = ArangoDBException.class)
	public void validateCollectionNameInvalidFirstLetterNumber() {
		checkCollectionName("1test");
	}

	@Test(expected = ArangoDBException.class)
	public void validateCollectionNameInvalidIllegalCharacter() {
		checkCollectionName("test/");
	}

	@Test
	public void validateCollectionNameValidUnderscore() {
		checkCollectionName("_test");
	}

	@Test(expected = ArangoDBException.class)
	public void validateCollectionNameInvalidEmpty() {
		checkCollectionName("");
	}

	private void checkCollectionName(final String name) throws ArangoDBException {
		final ExecuteBase executeBase = new ExecuteBase(null, null, null, null) {
		};
		executeBase.validateCollectionName(name);
	}

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
		final ExecuteBase executeBase = new ExecuteBase(null, null, null, null) {
		};
		executeBase.validateDocumentKey(key);
	}
}
