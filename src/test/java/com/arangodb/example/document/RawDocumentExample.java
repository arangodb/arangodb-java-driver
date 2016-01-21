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

import org.json.JSONML;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.entity.DocumentEntity;

public class RawDocumentExample extends BaseExample {

	private static final String DATABASE_NAME = "RawDocument";

	private static final String COLLECTION_NAME = "RawDocument";

	public ArangoDriver arangoDriver;

	@Before
	public void _before() {
		removeTestDatabase(DATABASE_NAME);

		arangoDriver = getArangoDriver(getConfiguration());
		createDatabase(arangoDriver, DATABASE_NAME);
		createCollection(arangoDriver, COLLECTION_NAME);
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
		printHeadline("create example document 1");
		//

		String documentHandle1 = null;
		String documentHandle2 = null;
		String documentHandle3 = null;

		String x = "{\"test\":123}";
		try {
			DocumentEntity<String> entity = arangoDriver.createDocumentRaw(COLLECTION_NAME, x, true, false);
			// the DocumentEntity contains the key, document handle and revision
			System.out.println("Key: " + entity.getDocumentKey());
			System.out.println("Id: " + entity.getDocumentHandle());
			System.out.println("Revision: " + entity.getDocumentRevision());
			documentHandle1 = entity.getDocumentHandle();
		} catch (ArangoException e) {
			Assert.fail("Failed to create document. " + e.getMessage());
		}

		//
		printHeadline("read example document 1");
		//

		try {
			String str = arangoDriver.getDocumentRaw(documentHandle1, null, null);
			System.out.println("value: " + str);
		} catch (ArangoException e) {
			Assert.fail("Failed to read document. " + e.getMessage());
		}

		//
		printHeadline("create example document 2 with key");
		//

		x = "{\"_key\":\"key2\",\"test\":123}";
		try {
			DocumentEntity<String> entity = arangoDriver.createDocumentRaw(COLLECTION_NAME, x, true, false);
			// the DocumentEntity contains the key, document handle and revision
			System.out.println("Key: " + entity.getDocumentKey());
			System.out.println("Id: " + entity.getDocumentHandle());
			System.out.println("Revision: " + entity.getDocumentRevision());
			documentHandle2 = entity.getDocumentHandle();
		} catch (ArangoException e) {
			Assert.fail("Failed to create document. " + e.getMessage());
		}

		//
		printHeadline("read example document 2");
		//

		try {
			String str = arangoDriver.getDocumentRaw(documentHandle2, null, null);
			System.out.println("value: " + str);
		} catch (ArangoException e) {
			Assert.fail("Failed to read document. " + e.getMessage());
		}

		//
		printHeadline("using org.json.JSONML to save a xml file");
		//
		String string = "<recipe name=\"bread\" prep_time=\"5 mins\" cook_time=\"3 hours\"> <title>Basic bread</title> <ingredient amount=\"8\" unit=\"dL\">Flour</ingredient> <ingredient amount=\"10\" unit=\"grams\">Yeast</ingredient> <ingredient amount=\"4\" unit=\"dL\" state=\"warm\">Water</ingredient> <ingredient amount=\"1\" unit=\"teaspoon\">Salt</ingredient> <instructions> <step>Mix all ingredients together.</step> <step>Knead thoroughly.</step> <step>Cover with a cloth, and leave for one hour in warm room.</step> <step>Knead again.</step> <step>Place in a bread baking tin.</step> <step>Cover with a cloth, and leave for one hour in warm room.</step> <step>Bake in the oven at 180(degrees)C for 30 minutes.</step> </instructions> </recipe> ";
		System.out.println("Orig XML value: " + string);
		JSONObject jsonObject = JSONML.toJSONObject(string);
		try {
			DocumentEntity<String> entity = arangoDriver.createDocumentRaw(COLLECTION_NAME, jsonObject.toString(), true,
				false);
			// the DocumentEntity contains the key, document handle and revision
			System.out.println("Key: " + entity.getDocumentKey());
			System.out.println("Id: " + entity.getDocumentHandle());
			System.out.println("Revision: " + entity.getDocumentRevision());
			documentHandle3 = entity.getDocumentHandle();
		} catch (ArangoException e) {
			Assert.fail("Failed to create document. " + e.getMessage());
		}

		//
		printHeadline("read example and convert it back to XML");
		//

		try {
			String str = arangoDriver.getDocumentRaw(documentHandle3, null, null);
			System.out.println("JSON value: " + str);
			JSONObject jsonObject2 = new JSONObject(str);
			System.out.println("XML value: " + JSONML.toString(jsonObject2));
		} catch (ArangoException e) {
			Assert.fail("Failed to read document. " + e.getMessage());
		}

	}

}
