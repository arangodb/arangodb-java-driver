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
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.util.MapBuilder;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackException;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class AqlQueryWithSpecialReturnTypesExample {

	private static final String DB_NAME = "document_example_db";
	private static final String COLLECTION_NAME = "document_example_collection";

	private static ArangoDB arangoDB;
	private static ArangoDatabase db;

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
		createExamples();
	}

	@AfterClass
	public static void tearDown() {
		db.drop();
		arangoDB.shutdown();
	}

	public enum Gender {
		MALE, FEMALE
	}

	private static void createExamples() {
		for (int i = 0; i < 100; i++) {
			final BaseDocument value = new BaseDocument();
			value.addAttribute("name", "TestUser" + i);
			value.addAttribute("gender", (i % 2) == 0 ? Gender.MALE : Gender.FEMALE);
			value.addAttribute("age", i + 10);
			db.collection(COLLECTION_NAME).insertDocument(value);
		}
	}

	@Test
	public void aqlWithLimitQueryAsVPackObject() {
		final String query = "FOR t IN " + COLLECTION_NAME
				+ " FILTER t.age >= 20 && t.age < 30 && t.gender == @gender RETURN t";
		final Map<String, Object> bindVars = new MapBuilder().put("gender", Gender.FEMALE).get();
		final ArangoCursor<VPackSlice> cursor = db.query(query, bindVars, null, VPackSlice.class);
		assertThat(cursor, is(notNullValue()));
		cursor.forEachRemaining(vpack -> {
			try {
				assertThat(vpack.get("name").getAsString(),
					isOneOf("TestUser11", "TestUser13", "TestUser15", "TestUser17", "TestUser19"));
				assertThat(vpack.get("gender").getAsString(), is(Gender.FEMALE.name()));
				assertThat(vpack.get("age").getAsInt(), isOneOf(21, 23, 25, 27, 29));
			} catch (final VPackException e) {
			}
		});
	}

	@Test
	public void aqlWithLimitQueryAsVPackArray() {
		final String query = "FOR t IN " + COLLECTION_NAME
				+ " FILTER t.age >= 20 && t.age < 30 && t.gender == @gender RETURN [t.name, t.gender, t.age]";
		final Map<String, Object> bindVars = new MapBuilder().put("gender", Gender.FEMALE).get();
		final ArangoCursor<VPackSlice> cursor = db.query(query, bindVars, null, VPackSlice.class);
		assertThat(cursor, is(notNullValue()));
		cursor.forEachRemaining(vpack -> {
			assertThat(vpack.get(0).getAsString(),
				isOneOf("TestUser11", "TestUser13", "TestUser15", "TestUser17", "TestUser19"));
			assertThat(vpack.get(1).getAsString(), is(Gender.FEMALE.name()));
			assertThat(vpack.get(2).getAsInt(), isOneOf(21, 23, 25, 27, 29));
		});
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void aqlWithLimitQueryAsMap() {
		final String query = "FOR t IN " + COLLECTION_NAME
				+ " FILTER t.age >= 20 && t.age < 30 && t.gender == @gender RETURN t";
		final Map<String, Object> bindVars = new MapBuilder().put("gender", Gender.FEMALE).get();
		final ArangoCursor<Map> cursor = db.query(query, bindVars, null, Map.class);
		assertThat(cursor, is(notNullValue()));
		cursor.forEachRemaining(map -> {
			assertThat(map.get("name"), isOneOf("TestUser11", "TestUser13", "TestUser15", "TestUser17", "TestUser19"));
			assertThat(map.get("gender"), is(Gender.FEMALE.name()));
			assertThat(map.get("age"), isOneOf(21L, 23L, 25L, 27L, 29L));
		});
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void aqlWithLimitQueryAsList() {
		final String query = "FOR t IN " + COLLECTION_NAME
				+ " FILTER t.age >= 20 && t.age < 30 && t.gender == @gender RETURN [t.name, t.gender, t.age]";
		final Map<String, Object> bindVars = new MapBuilder().put("gender", Gender.FEMALE).get();
		final ArangoCursor<List> cursor = db.query(query, bindVars, null, List.class);
		assertThat(cursor, is(notNullValue()));
		cursor.forEachRemaining(vpack -> {
			assertThat(vpack.get(0), isOneOf("TestUser11", "TestUser13", "TestUser15", "TestUser17", "TestUser19"));
			assertThat(vpack.get(1), is(Gender.FEMALE.name()));
			assertThat(vpack.get(2), isOneOf(21L, 23L, 25L, 27L, 29L));
		});
	}
}
