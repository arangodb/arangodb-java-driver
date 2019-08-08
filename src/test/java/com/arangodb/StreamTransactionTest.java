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

import com.arangodb.ArangoDB.Builder;
import com.arangodb.entity.*;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentReadOptions;
import com.arangodb.model.StreamTransactionOptions;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;

/**
 * @author Mark Vollmary
 */
@RunWith(Parameterized.class)
public class StreamTransactionTest extends BaseTest {

	private static final String COLLECTION_NAME = "db_stream_transaction_test";

	public StreamTransactionTest(final Builder builder) {
		super(builder);
		try {
			db.createCollection(COLLECTION_NAME, null);
		} catch (final ArangoDBException e) {

		}
	}

	@After
	public void teardown() {
		try {
			db.collection(COLLECTION_NAME).drop();
		} catch (final ArangoDBException e) {
		}
	}

	@Test
	public void getDocument() {
		assumeTrue(requireVersion(3, 5));
		assumeTrue(requireStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

		StreamTransactionEntity tx = db.beginStreamTransaction(
				new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME));

		// insert a document from outside the tx
		DocumentCreateEntity<BaseDocument> externalDoc = db.collection(COLLECTION_NAME)
				.insertDocument(new BaseDocument(), null);

		// assert that the document is not found from within the tx
		assertThat(db.collection(COLLECTION_NAME).getDocument(externalDoc.getKey(), BaseDocument.class,
				new DocumentReadOptions().streamTransactionId(tx.getId())), is(nullValue()));

		db.abortStreamTransaction(tx.getId());
	}

	@Test
	public void getDocuments() {
		assumeTrue(requireVersion(3, 5));
		assumeTrue(requireStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

		StreamTransactionEntity tx = db.beginStreamTransaction(
				new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME));

		// insert documents from outside the tx
		DocumentCreateEntity<BaseDocument> externalDoc1 = db.collection(COLLECTION_NAME)
				.insertDocument(new BaseDocument(), null);

		DocumentCreateEntity<BaseDocument> externalDoc2 = db.collection(COLLECTION_NAME)
				.insertDocument(new BaseDocument(), null);

		// assert that the documents are not found from within the tx
		MultiDocumentEntity<BaseDocument> documents = db.collection(COLLECTION_NAME)
				.getDocuments(Arrays.asList(externalDoc1.getId(), externalDoc2.getId()), BaseDocument.class,
						new DocumentReadOptions().streamTransactionId(tx.getId()));

		assertThat(documents.getDocuments(), is(empty()));

		db.abortStreamTransaction(tx.getId());
	}

	@Test
	public void insertDocumentWithinStreamTransaction() {
		assumeTrue(requireVersion(3, 5));
		assumeTrue(requireStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

		StreamTransactionEntity tx = db.beginStreamTransaction(
				new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME));

		// insert a document from within the tx
		DocumentCreateEntity<BaseDocument> txDoc = db.collection(COLLECTION_NAME)
				.insertDocument(new BaseDocument(), new DocumentCreateOptions().streamTransactionId(tx.getId()));

		// assert that the document is not found from outside the tx
		assertThat(db.collection(COLLECTION_NAME).getDocument(txDoc.getKey(), BaseDocument.class, null),
				is(nullValue()));

		// assert that the document is found from within the tx
		assertThat(db.collection(COLLECTION_NAME).getDocument(txDoc.getKey(), BaseDocument.class,
				new DocumentReadOptions().streamTransactionId(tx.getId())), is(notNullValue()));

		db.commitStreamTransaction(tx.getId());

		// assert that the document is found after commit
		assertThat(db.collection(COLLECTION_NAME).getDocument(txDoc.getKey(), BaseDocument.class, null),
				is(notNullValue()));
	}

	@Test
	public void insertDocumentsWithinStreamTransaction() {
		assumeTrue(requireVersion(3, 5));
		assumeTrue(requireStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

		StreamTransactionEntity tx = db.beginStreamTransaction(
				new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME));

		// insert documents from within the tx
		MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> txDocs = db.collection(COLLECTION_NAME)
				.insertDocuments(Arrays.asList(new BaseDocument(), new BaseDocument(), new BaseDocument()),
						new DocumentCreateOptions().streamTransactionId(tx.getId()));

		//noinspection OptionalGetWithoutIsPresent
		String id1 = txDocs.getDocuments().stream().findFirst().get().getKey();

		// assert that the document is not found from outside the tx
		assertThat(db.collection(COLLECTION_NAME).getDocument(id1, BaseDocument.class, null), is(nullValue()));

		// assert that the document is found from within the tx
		assertThat(db.collection(COLLECTION_NAME)
						.getDocument(id1, BaseDocument.class, new DocumentReadOptions().streamTransactionId(tx.getId())),
				is(notNullValue()));

		db.commitStreamTransaction(tx.getId());

		// assert that the document is found after commit
		assertThat(db.collection(COLLECTION_NAME).getDocument(id1, BaseDocument.class, null), is(notNullValue()));
	}

}
