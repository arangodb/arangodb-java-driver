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
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.DocumentEntity;

public class ReadDocumentExample extends BaseExample {

	private static final String DATABASE_NAME = "ReadDocument";

	private static final String COLLECTION_NAME = "ReadDocument";

	private static final String KEY1 = "key1";

	/**
	 * @param configure
	 * @param driver
	 */
	public ReadDocumentExample(final ArangoConfigure configure, final ArangoDriver driver) {
		super(configure, driver);
	}

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
	public void ReadDocuments() {
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

		try {
			final DocumentEntity<HashMap<String, Object>> entity = driver.createDocument(COLLECTION_NAME, myHashMap);
			Assert.assertNotNull(entity);
			Assert.assertNotNull(entity.getDocumentHandle());
			Assert.assertNotNull(entity.getDocumentKey());
			Assert.assertNotNull(entity.getDocumentRevision());

			documentHandleExample = entity.getDocumentHandle();
		} catch (final ArangoException e) {
			Assert.fail("Failed to create document. " + e.getMessage());
		}

		//
		printHeadline("read documents");
		//

		System.out.println("1. read document as BaseDocument object:");
		try {
			final DocumentEntity<BaseDocument> entity = driver.getDocument(COLLECTION_NAME, KEY1, BaseDocument.class);

			Assert.assertNotNull(entity);
			Assert.assertNotNull(entity.getDocumentHandle());
			Assert.assertNotNull(entity.getDocumentKey());
			Assert.assertNotNull(entity.getDocumentRevision());
			Assert.assertEquals(KEY1, entity.getDocumentKey());

			// the DocumentEntity contains the key, document handle and revision
			System.out.println("Key: " + entity.getDocumentKey());
			System.out.println("Id: " + entity.getDocumentHandle());
			System.out.println("Revision: " + entity.getDocumentRevision());

			final BaseDocument baseDocument2 = entity.getEntity();
			Assert.assertNotNull(baseDocument2);

			// the BaseDocument contains the key, document handle and revision
			System.out.println("Key: " + baseDocument2.getDocumentKey());
			System.out.println("Id: " + baseDocument2.getDocumentHandle());
			System.out.println("Revision: " + baseDocument2.getDocumentRevision());
			// get the attributes
			System.out.println("Attribute 'name': " + baseDocument2.getProperties().get("name"));
			System.out.println("Attribute 'gender': " + baseDocument2.getProperties().get("gender"));

			Assert.assertTrue(baseDocument2.getProperties().get("age") instanceof Double);

			// ArangoDb stores numeric values as double values
			System.out.println(
				"Attribute 'age': " + baseDocument2.getProperties().get("age") + " <- Data type changed to double");

			// printEntity(entity);
		} catch (final ArangoException e) {
			Assert.fail("Failed to read document. " + e.getMessage());
		}

		System.out.println("2. read document as HashMap object:");
		try {
			@SuppressWarnings("rawtypes")
			final DocumentEntity<HashMap> entity = driver.getDocument(COLLECTION_NAME, KEY1, HashMap.class);

			Assert.assertNotNull(entity);
			Assert.assertNotNull(entity.getDocumentHandle());
			Assert.assertNotNull(entity.getDocumentKey());
			Assert.assertNotNull(entity.getDocumentRevision());
			Assert.assertEquals(KEY1, entity.getDocumentKey());

			final HashMap<?, ?> map = entity.getEntity();
			Assert.assertNotNull(map);

			// get the attributes
			System.out.println("Key: " + map.get("_key"));
			System.out.println("Id: " + map.get("_id"));
			System.out.println("Revision: " + map.get("_rev"));
			System.out.println("Attribute 'name': " + map.get("name"));
			System.out.println("Attribute 'gender': " + map.get("gender"));

			Assert.assertTrue(map.get("age") instanceof Double);

			System.out.println("Attribute 'age': " + map.get("age") + " <- Data type changed to double");

			// printEntity(entity);
		} catch (final ArangoException e) {
			Assert.fail("Failed to read document. " + e.getMessage());
		}

		System.out.println("3. read document as SimplePerson object:");
		try {
			final DocumentEntity<SimplePerson> entity = driver.getDocument(COLLECTION_NAME, KEY1, SimplePerson.class);

			Assert.assertNotNull(entity);
			Assert.assertNotNull(entity.getDocumentHandle());
			Assert.assertNotNull(entity.getDocumentKey());
			Assert.assertNotNull(entity.getDocumentRevision());
			Assert.assertEquals(KEY1, entity.getDocumentKey());

			final SimplePerson sp = entity.getEntity();
			Assert.assertNotNull(sp);

			// get the attributes
			System.out.println("Attribute 'name': " + sp.getName());
			System.out.println("Attribute 'gender': " + sp.getGender());
			// ArangoDb stores numeric values as double values but the value is
			// converted back to integer
			System.out.println("Attribute 'age': " + sp.getAge());

			// printEntity(entity);
		} catch (final ArangoException e) {
			Assert.fail("Failed to read document. " + e.getMessage());
		}

		System.out.println("4. read document as DocumentPerson object:");
		try {
			final DocumentEntity<DocumentPerson> entity = driver.getDocument(COLLECTION_NAME, KEY1,
				DocumentPerson.class);

			Assert.assertNotNull(entity);
			Assert.assertNotNull(entity.getDocumentHandle());
			Assert.assertNotNull(entity.getDocumentKey());
			Assert.assertNotNull(entity.getDocumentRevision());
			Assert.assertEquals(KEY1, entity.getDocumentKey());

			final DocumentPerson dp = entity.getEntity();
			Assert.assertNotNull(dp);

			System.out.println("Key: " + dp.getDocumentKey());
			System.out.println("Id: " + dp.getDocumentHandle());
			System.out.println("Revision: " + dp.getDocumentRevision());
			// get the attributes
			System.out.println("Attribute 'name': " + dp.getName());
			System.out.println("Attribute 'gender': " + dp.getGender());
			// ArangoDb stores numeric values as double values but the value is
			// converted back to integer
			System.out.println("Attribute 'age': " + dp.getAge());

			// printEntity(entity);
		} catch (final ArangoException e) {
			Assert.fail("Failed to read document. " + e.getMessage());
		}

		//
		printHeadline("read document by document handle");
		//
		try {
			final DocumentEntity<DocumentPerson> entity = driver.getDocument(documentHandleExample,
				DocumentPerson.class);

			Assert.assertNotNull(entity);
			Assert.assertNotNull(entity.getDocumentHandle());
			Assert.assertNotNull(entity.getDocumentKey());
			Assert.assertNotNull(entity.getDocumentRevision());
			Assert.assertEquals(KEY1, entity.getDocumentKey());

			final DocumentPerson dp = entity.getEntity();
			Assert.assertNotNull(dp);

			System.out.println("Key: " + dp.getDocumentKey());
			System.out.println("Attribute 'name': " + dp.getName());
		} catch (final ArangoException e) {
			Assert.fail("Failed to read document. " + e.getMessage());
		}

		//
		printHeadline("read collection count");
		//
		try {
			final CollectionEntity collectionCount = driver.getCollectionCount(COLLECTION_NAME);

			Assert.assertNotNull(collectionCount);
			Assert.assertEquals(1, collectionCount.getCount());

			System.out.println("Collection count: " + collectionCount.getCount());
		} catch (final ArangoException e) {
			Assert.fail("Failed to read collection count. " + e.getMessage());
		}

		//
		printHeadline("read all document handles of a collection");
		//
		try {
			final List<String> documentHandles = driver.getDocuments(COLLECTION_NAME);

			Assert.assertNotNull(documentHandles);
			Assert.assertEquals(1, documentHandles.size());

			for (final String documentHandle : documentHandles) {
				System.out.println("document handle: " + documentHandle);
			}

		} catch (final ArangoException e) {
			Assert.fail("Failed to read all documents. " + e.getMessage());
		}
	}

}
