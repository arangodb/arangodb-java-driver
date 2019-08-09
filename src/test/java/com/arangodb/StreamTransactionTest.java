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
import com.arangodb.model.DocumentReplaceOptions;
import com.arangodb.model.StreamTransactionOptions;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
			if (db.collection(COLLECTION_NAME).exists())
				db.collection(COLLECTION_NAME).drop();

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

	@Test
	public void replaceDocument() {
		assumeTrue(requireVersion(3, 5));
		assumeTrue(requireStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

		BaseDocument doc = new BaseDocument();
		doc.addAttribute("test", "foo");

		DocumentCreateEntity<BaseDocument> createdDoc = db.collection(COLLECTION_NAME).insertDocument(doc, null);

		StreamTransactionEntity tx = db.beginStreamTransaction(
				new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME));

		// replace document from within the tx
		doc.getProperties().clear();
		doc.addAttribute("test", "bar");
		DocumentUpdateEntity<BaseDocument> replacedDoc = db.collection(COLLECTION_NAME)
				.replaceDocument(createdDoc.getKey(), doc,
						new DocumentReplaceOptions().streamTransactionId(tx.getId()));

		// assert that the document has not been replaced from outside the tx
		assertThat(db.collection(COLLECTION_NAME).getDocument(createdDoc.getKey(), BaseDocument.class, null)
				.getProperties().get("test"), is("foo"));

		// assert that the document has been replaced from within the tx
		assertThat(db.collection(COLLECTION_NAME).getDocument(createdDoc.getKey(), BaseDocument.class,
				new DocumentReadOptions().streamTransactionId(tx.getId())).getProperties().get("test"), is("bar"));

		db.commitStreamTransaction(tx.getId());

		// assert that the document has been replaced after commit
		assertThat(db.collection(COLLECTION_NAME).getDocument(createdDoc.getKey(), BaseDocument.class, null)
				.getProperties().get("test"), is("bar"));
	}

	@Test
	public void replaceDocuments() {
		assumeTrue(requireVersion(3, 5));
		assumeTrue(requireStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

		List<String> ids = Arrays.asList("1", "2");
		List<BaseDocument> docs = ids.stream().map(BaseDocument::new).peek(doc -> doc.addAttribute("test", "foo"))
				.collect(Collectors.toList());

		MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> createdDocs = db.collection(COLLECTION_NAME)
				.insertDocuments(docs, null);

		StreamTransactionEntity tx = db.beginStreamTransaction(
				new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME));

		List<BaseDocument> modifiedDocs = docs.stream().peek(doc -> {
			doc.getProperties().clear();
			doc.addAttribute("test", "bar");
		}).collect(Collectors.toList());

		// replace document from within the tx
		MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> replacedDocs = db.collection(COLLECTION_NAME)
				.replaceDocuments(modifiedDocs, new DocumentReplaceOptions().streamTransactionId(tx.getId()));

		// assert that the documents has not been replaced from outside the tx
		assertThat(db.collection(COLLECTION_NAME).getDocuments(ids, BaseDocument.class, null).getDocuments().stream()
				.map(it -> ((String) it.getAttribute("test"))).collect(Collectors.toList()), everyItem(is("foo")));

		// assert that the document has been replaced from within the tx
		assertThat(db.collection(COLLECTION_NAME)
						.getDocuments(ids, BaseDocument.class, new DocumentReadOptions().streamTransactionId(tx.getId()))
						.getDocuments().stream().map(it -> ((String) it.getAttribute("test"))).collect(Collectors.toList()),
				everyItem(is("bar")));

		db.commitStreamTransaction(tx.getId());

		// assert that the document has been replaced after commit
		assertThat(db.collection(COLLECTION_NAME).getDocuments(ids, BaseDocument.class, null).getDocuments().stream()
				.map(it -> ((String) it.getAttribute("test"))).collect(Collectors.toList()), everyItem(is("bar")));
	}

}
