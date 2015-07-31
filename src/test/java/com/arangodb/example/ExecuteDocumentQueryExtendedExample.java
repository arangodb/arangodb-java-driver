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

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.DocumentCursor;
import com.google.gson.annotations.SerializedName;

/**
 * AQL example with new cursor implementation
 * 
 * @author tamtam180 - kirscheless at gmail.com
 * @author a-brandt
 *
 */
public class ExecuteDocumentQueryExtendedExample {

	private static final String COLLECTION_NAME = "example_collection2";

	/**
	 * Person class with attributes for handle, key and revision.
	 */
	public static class Person {
		@SerializedName("_id")
		String documentHandle;

		@SerializedName("_key")
		String documentKey;

		@SerializedName("_rev")
		long documentRevision;

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

			// build an AQL query string
			String queryString = "FOR t IN " + COLLECTION_NAME
					+ " FILTER t.age >= 20 && t.age < 30 && t.gender == @gender RETURN t";

			// bind @gender to WOMAN
			HashMap<String, Object> bindVars = new HashMap<String, Object>();
			bindVars.put("gender", "WOMAN");

			DocumentCursor<Person> rs = driver.executeDocumentQuery(queryString, bindVars,
				driver.getDefaultAqlQueryOptions(), Person.class);

			// using the entity iterator
			Iterator<Person> iterator2 = rs.entityIterator();
			while (iterator2.hasNext()) {
				Person person = iterator2.next();
				System.out.printf("%20s  %15s(%5s): %d%n", person.documentHandle, person.name, person.gender,
					person.age);
			}

		} catch (ArangoException e) {
			e.printStackTrace();
		} finally {
			configure.shutdown();
		}

	}

}
