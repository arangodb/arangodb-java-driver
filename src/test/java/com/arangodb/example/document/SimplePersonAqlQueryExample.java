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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.arangodb.ArangoException;
import com.arangodb.DocumentCursor;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.util.AqlQueryOptions;

public class SimplePersonAqlQueryExample extends BaseExample {

	private static final String DATABASE_NAME = "SimplePersonAqlQueryExample";

	private static final String COLLECTION_NAME = "SimplePersonAqlQueryExample";

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
	public void simplePersonAqlQuery() {
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

			// build an AQL query string
			final String queryString = "FOR t IN " + COLLECTION_NAME
					+ " FILTER t.age >= 20 && t.age < 30 && t.gender == @gender RETURN t";
			System.out.println(queryString);

			// bind @gender to female
			final HashMap<String, Object> bindVars = new HashMap<String, Object>();
			bindVars.put("gender", FEMALE);

			// query (count = true, batchSize = 5)
			final AqlQueryOptions aqlQueryOptions = new AqlQueryOptions().setCount(true).setBatchSize(5);

			//
			printHeadline("execute query");
			//

			DocumentCursor<SimplePerson> rs = driver.executeDocumentQuery(queryString, bindVars, aqlQueryOptions,
				SimplePerson.class);
			Assert.assertNotNull(rs);

			//
			printHeadline("get number of results");
			//

			// get total number of results
			System.out.println(rs.getCount());

			//
			printHeadline("print results");
			//

			// using the DocumentEntity iterator
			final Iterator<DocumentEntity<SimplePerson>> iterator = rs.iterator();
			while (iterator.hasNext()) {
				final DocumentEntity<SimplePerson> documentEntity = iterator.next();

				Assert.assertNotNull(documentEntity);
				Assert.assertNotNull(documentEntity.getDocumentKey());
				Assert.assertNotNull(documentEntity.getDocumentHandle());
				Assert.assertNotNull(documentEntity.getDocumentRevision());
				Assert.assertNotNull(documentEntity.getEntity());

				final SimplePerson person = documentEntity.getEntity();
				System.out.printf("%20s  %15s(%5s): %d%n", documentEntity.getDocumentKey(), person.getName(),
					person.getGender(), person.getAge());
			}

			//
			printHeadline("execute query again");
			//

			rs = driver.executeDocumentQuery(queryString, bindVars, aqlQueryOptions, SimplePerson.class);
			Assert.assertNotNull(rs);

			// get total number of results
			System.out.println(rs.getCount());

			// using the entity iterator
			final Iterator<SimplePerson> iterator2 = rs.entityIterator();
			while (iterator2.hasNext()) {
				final SimplePerson person = iterator2.next();

				Assert.assertNotNull(person);

				System.out.printf("  %15s(%5s): %d%n", person.getName(), person.getGender(), person.getAge());
			}

		} catch (final ArangoException e) {
			Assert.fail("Example failed. " + e.getMessage());
		}

	}

	private void createExamples() throws ArangoException {
		// create some persons
		for (int i = 0; i < 1000; i++) {
			final SimplePerson person = new SimplePerson();
			person.setName("TestUser" + i);
			person.setGender((i % 2) == 0 ? MALE : FEMALE);
			person.setAge((int) (Math.random() * 100) + 10);

			driver.createDocument(COLLECTION_NAME, person, null);
		}

	}

}
