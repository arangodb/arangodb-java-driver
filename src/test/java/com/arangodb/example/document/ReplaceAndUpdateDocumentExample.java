/*
 * Copyright (C) 2015 ArangoDB GmbH
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

package com.arangodb.example.document;

import java.util.HashMap;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.arangodb.ArangoException;
import com.arangodb.ErrorNums;
import com.arangodb.entity.DocumentEntity;

public class ReplaceAndUpdateDocumentExample extends BaseExample {

	private static final String DATABASE_NAME = "ReplaceDocument";

	private static final String COLLECTION_NAME = "ReplaceDocument";

	private static final String KEY1 = "key1";

	@Before
	public void _before() {
		removeTestDatabase(DATABASE_NAME);

		createDatabase(driver, DATABASE_NAME);
		createCollection(driver, COLLECTION_NAME);
	}

	@After
	public void _after() {
		removeTestDatabase(DATABASE_NAME);
	}

	@Test
	public void replaceAndUpdateDocument() {
		//
		// You can find the ArangoDB Web interface here:
		// http://127.0.0.1:8529/
		//
		// change the log level to "debug" in /src/test/resource/logback.xml to
		// see the HTTP communication

		//
		printHeadline("create example document");
		//

		final HashMap<String, Object> myHashMap = new HashMap<String, Object>();
		myHashMap.put("_key", KEY1);
		// attributes are stored in a HashMap
		myHashMap.put("name", "Alice");
		myHashMap.put("gender", "female");
		myHashMap.put("age", 18);
		String documentHandleExample = null;
		long revision = 0L;

		try {
			final DocumentEntity<HashMap<String, Object>> entity = driver.createDocument(COLLECTION_NAME, myHashMap);

			Assert.assertNotNull(entity);
			Assert.assertNotNull(entity.getDocumentHandle());
			Assert.assertNotNull(entity.getDocumentKey());
			Assert.assertNotNull(entity.getDocumentRevision());
			revision = entity.getDocumentRevision();

			System.out.println("Key: " + entity.getDocumentKey());
			System.out.println("Id: " + entity.getDocumentHandle());
			System.out.println("Revision: " + entity.getDocumentRevision());
			documentHandleExample = entity.getDocumentHandle();
		} catch (final ArangoException e) {
			Assert.fail("Failed to create document. " + e.getMessage());
		}

		//
		printHeadline("replace document");
		//

		try {
			final DocumentPerson dp = new DocumentPerson("Moritz", "male", 6);
			driver.replaceDocument(documentHandleExample, dp);

			System.out.println("Key: " + dp.getDocumentKey());
			System.out.println("Revision: " + dp.getDocumentRevision() + " <- revision changed");
			System.out.println("Attribute 'name': " + dp.getName());

			Assert.assertTrue(dp.getDocumentRevision() != revision);

			revision = dp.getDocumentRevision();

		} catch (final ArangoException e) {
			Assert.fail("Failed to replace document. " + e.getMessage());
		}

		//
		printHeadline("update document");
		//

		// update one attribute
		final HashMap<String, Object> newHashMap = new HashMap<String, Object>();
		newHashMap.put("name", "Fritz");
		try {
			driver.updateDocument(documentHandleExample, newHashMap);

			final DocumentEntity<DocumentPerson> entity = driver.getDocument(documentHandleExample,
				DocumentPerson.class);

			Assert.assertNotNull(entity);
			Assert.assertNotNull(entity.getDocumentHandle());
			Assert.assertNotNull(entity.getDocumentKey());
			Assert.assertNotNull(entity.getDocumentRevision());
			Assert.assertNotNull(entity.getEntity());

			final DocumentPerson dp = entity.getEntity();

			System.out.println("Key: " + dp.getDocumentKey());
			System.out.println("Revision: " + dp.getDocumentRevision() + " <- revision changed");
			System.out.println("Attribute 'name': " + dp.getName());
			System.out.println("Attribute 'gender': " + dp.getGender());
			System.out.println("Attribute 'age': " + dp.getAge());

			Assert.assertTrue(dp.getDocumentRevision() != revision);

			revision = dp.getDocumentRevision();

		} catch (final ArangoException e) {
			Assert.fail("Failed to update document. " + e.getMessage());
		}

		//
		printHeadline("replace document with given revision");
		//
		try {
			final DocumentPerson dp = new DocumentPerson("Nina", "female", 9);
			// wrong revision
			driver.replaceDocument(documentHandleExample, dp, 22L, true);
			Assert.fail("replaceDocument should fail here!");
		} catch (final ArangoException e) {
			Assert.assertEquals(ErrorNums.ERROR_ARANGO_CONFLICT, e.getErrorNumber());
		}

		try {
			final DocumentPerson dp = new DocumentPerson("Nina", "female", 9);
			// current revision
			driver.replaceDocument(documentHandleExample, dp, revision, true);
		} catch (final ArangoException e) {
			Assert.fail("Failed to replace document. " + e.getMessage());
		}
	}

}
