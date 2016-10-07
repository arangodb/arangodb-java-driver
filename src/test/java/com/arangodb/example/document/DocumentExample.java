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

package com.arangodb.example.document;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.DocumentCreateEntity;
import com.arangodb.velocypack.VPackBuilder;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.ValueType;
import com.arangodb.velocypack.exception.VPackBuilderException;
import com.arangodb.velocypack.exception.VPackException;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DocumentExample {

	private static final String DB_NAME = "json_document_example_db";
	private static final String COLLECTION_NAME = "json_document_example_collection";

	private static ArangoDB arangoDB;
	private static ArangoDatabase db;
	private static ArangoCollection collection;

	@BeforeClass
	public static void setUp() {
		arangoDB = new ArangoDB.Builder().build();
		try {
			arangoDB.db(DB_NAME).drop();
		} catch (final ArangoDBException e) {
		}
		arangoDB.createDatabase(DB_NAME);
		db = arangoDB.db(DB_NAME);
		db.createCollection(COLLECTION_NAME);
		collection = db.collection(COLLECTION_NAME);
	}

	@AfterClass
	public static void tearDown() {
		db.drop();
		arangoDB.shutdown();
	}

	@Test
	public void insertVPackGetVPack() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT).add("test", 123).close();
		final DocumentCreateEntity<VPackSlice> doc = collection.insertDocument(builder.slice());

		final Optional<VPackSlice> vpack = collection.getDocument(doc.getKey(), VPackSlice.class);
		assertThat(vpack.isPresent(), is(true));
		final VPackSlice test = vpack.get().get("test");
		assertThat(test.isInt(), is(true));
		assertThat(test.getAsInt(), is(123));
	}

	@Test
	public void insertJsonGetVPack() throws VPackException {
		final DocumentCreateEntity<String> doc = collection.insertDocument("{\"test\":123}");

		final Optional<VPackSlice> vpack = collection.getDocument(doc.getKey(), VPackSlice.class);
		assertThat(vpack.isPresent(), is(true));
		final VPackSlice test = vpack.get().get("test");
		assertThat(test.isInt(), is(true));
		assertThat(test.getAsInt(), is(123));
	}

	@Test
	public void insertVPackGetBaseDocument() throws VPackBuilderException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT).add("test", 123).close();
		final DocumentCreateEntity<VPackSlice> doc = collection.insertDocument(builder.slice());

		final Optional<BaseDocument> document = collection.getDocument(doc.getKey(), BaseDocument.class);
		assertThat(document.isPresent(), is(true));
		assertThat(document.get().getAttribute("test"), is(notNullValue()));
		assertThat((long) document.get().getAttribute("test"), is(123L));
	}

}
