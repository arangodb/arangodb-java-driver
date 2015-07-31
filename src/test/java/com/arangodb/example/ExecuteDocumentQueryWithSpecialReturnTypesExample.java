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

package com.arangodb.example;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.CursorResult;
import com.arangodb.util.AqlQueryOptions;

/**
 * AQL example with new cursor implementation
 * 
 * @author tamtam180 - kirscheless at gmail.com
 * @author a-brandt
 *
 */
public class ExecuteDocumentQueryWithSpecialReturnTypesExample {

	private static final String COLLECTION_NAME = "example_collection1";

	public static class Person {
		public String name;
		public String gender;
		public int age;
	}

	public static void main(String[] args) {

		// Initialize configure
		ArangoConfigure configure = new ArangoConfigure();
		configure.init();

		// Create Driver (this instance is thread-safe)
		ArangoDriver driver = new ArangoDriver(configure);

		// create test collection
		try {
			driver.createCollection(COLLECTION_NAME);
		} catch (ArangoException e) {
		}

		try {
			// remove all elements of test collection
			driver.truncateCollection(COLLECTION_NAME);

			// create some persons
			for (int i = 0; i < 1000; i++) {
				Person value = new Person();
				value.name = "TestUser" + i;
				switch (i % 3) {
				case 0:
					value.gender = "MAN";
					break;
				case 1:
					value.gender = "WOMAN";
					break;
				case 2:
					value.gender = "OTHER";
					break;
				}
				value.age = (int) (Math.random() * 100) + 10;
				driver.createDocument(COLLECTION_NAME, value, true, null);
			}

			// bind @gender to WOMAN
			HashMap<String, Object> bindVars = new HashMap<String, Object>();
			bindVars.put("gender", "WOMAN");

			// query (count = true, batchSize = 5)
			AqlQueryOptions aqlQueryOptions = new AqlQueryOptions().setCount(true).setBatchSize(5);

			// map
			System.out.println("get AQL query results in a map");

			String queryString = "FOR t IN " + COLLECTION_NAME
					+ " FILTER t.age >= 20 && t.age < 30 && t.gender == @gender RETURN t";
			@SuppressWarnings("rawtypes")
			CursorResult<Map> cursor = driver.executeAqlQuery(queryString, bindVars, aqlQueryOptions, Map.class);
			@SuppressWarnings("rawtypes")
			Iterator<Map> iterator = cursor.iterator();
			while (iterator.hasNext()) {
				Map<?, ?> map = iterator.next();
				System.out.printf("%15s (%5s): %s%n", map.get("name"), map.get("gender"), map.get("age").toString());
			}

			// list
			System.out.println("get AQL query results in a list");

			queryString = "FOR t IN " + COLLECTION_NAME
					+ " FILTER t.age >= 20 && t.age < 30 && t.gender == @gender RETURN [t.name, t.gender, t.age]";
			@SuppressWarnings("rawtypes")
			CursorResult<List> cursor2 = driver.executeAqlQuery(queryString, bindVars, aqlQueryOptions, List.class);
			@SuppressWarnings("rawtypes")
			Iterator<List> iterator2 = cursor2.iterator();
			while (iterator2.hasNext()) {
				List<?> list = iterator2.next();
				System.out.printf("%15s (%5s): %s%n", list.get(0), list.get(1), list.get(2).toString());
			}

		} catch (ArangoException e) {
			e.printStackTrace();
		} finally {
			configure.shutdown();
		}

	}
}
