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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.CursorResult;
import com.arangodb.util.AqlQueryOptions;

public class AqlQueryWithSpecialReturnTypesExample extends BaseExample {

	private static final String DATABASE_NAME = "SimplePersonAqlQueryWithLimitExample";
	private static final String COLLECTION_NAME = "SimplePersonAqlQueryWithLimitExample";

	/**
	 * @param configure
	 * @param driver
	 */
	public AqlQueryWithSpecialReturnTypesExample(final ArangoConfigure configure, final ArangoDriver driver) {
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
	public void simplePersonAqlWithLimitQuery() {
		//
		// You can find the ArangoDB Web interface here:
		// http://127.0.0.1:8529/
		//
		// change the log level to "debug" in /src/test/resource/logback.xml to
		// see the HTTP communication

		try {
			//
			printHeadline("create example documents");
			//
			createExamples();

			//
			printHeadline("build query");
			//

			// bind @gender to WOMAN
			final HashMap<String, Object> bindVars = new HashMap<String, Object>();
			bindVars.put("gender", FEMALE);

			// query (count = true, batchSize = 5)
			final AqlQueryOptions aqlQueryOptions = new AqlQueryOptions().setCount(true).setBatchSize(5);

			//
			printHeadline("get query results in a map");
			//

			String queryString = "FOR t IN " + COLLECTION_NAME
					+ " FILTER t.age >= 20 && t.age < 30 && t.gender == @gender RETURN t";
			System.out.println(queryString);

			@SuppressWarnings("rawtypes")
			final CursorResult<Map> cursor = driver.executeAqlQuery(queryString, bindVars, aqlQueryOptions, Map.class);
			Assert.assertNotNull(cursor);

			@SuppressWarnings("rawtypes")
			final Iterator<Map> iterator = cursor.iterator();
			while (iterator.hasNext()) {
				final Map<?, ?> map = iterator.next();

				Assert.assertNotNull(map);
				Assert.assertNotNull(map.get("name"));
				Assert.assertNotNull(map.get("gender"));
				Assert.assertNotNull(map.get("age"));

				System.out.printf("%15s (%5s): %s%n", map.get("name"), map.get("gender"), map.get("age").toString());
			}

			//
			printHeadline("get query results in a list");
			//

			queryString = "FOR t IN " + COLLECTION_NAME
					+ " FILTER t.age >= 20 && t.age < 30 && t.gender == @gender RETURN [t.name, t.gender, t.age]";
			System.out.println(queryString);

			@SuppressWarnings("rawtypes")
			final CursorResult<List> cursor2 = driver.executeAqlQuery(queryString, bindVars, aqlQueryOptions,
				List.class);
			Assert.assertNotNull(cursor2);

			@SuppressWarnings("rawtypes")
			final Iterator<List> iterator2 = cursor2.iterator();
			while (iterator2.hasNext()) {
				final List<?> list = iterator2.next();

				Assert.assertNotNull(list);
				Assert.assertNotNull(list.get(0));
				Assert.assertNotNull(list.get(1));
				Assert.assertNotNull(list.get(2));

				System.out.printf("%15s (%5s): %s%n", list.get(0), list.get(1), list.get(2).toString());
			}

		} catch (final ArangoException e) {
			Assert.fail("Example failed. " + e.getMessage());
		}

	}

	private void createExamples() throws ArangoException {
		// create some persons
		for (int i = 0; i < 100; i++) {
			final SimplePerson person = new SimplePerson();
			person.setName("TestUser" + i);
			person.setGender((i % 2) == 0 ? MALE : FEMALE);
			person.setAge((int) (Math.random() * 100) + 10);

			driver.createDocument(COLLECTION_NAME, person, true, null);
		}

	}

}
