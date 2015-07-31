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

import java.util.Iterator;
import java.util.Map;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.DocumentCursor;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.util.MapBuilder;

/**
 * My first ArangoDB project
 */
public class FirstProject {

	public static void main(String[] args) {

		//
		// You can find the ArangoDB Web interface here:
		// http://127.0.0.1:8529/_db/mydb/
		//

		// Lets configure and open a connection to start ArangoDB.
		ArangoConfigure configure = new ArangoConfigure();
		configure.init();
		ArangoDriver arangoDriver = new ArangoDriver(configure);
		// The default connection is to http://127.0.0.1:8529.

		// Lets configure and open a connection to start ArangoDB.
		String dbName = "mydb";
		try {
			arangoDriver.createDatabase(dbName);
			System.out.println("Database created: " + dbName);
		} catch (Exception e) {
			System.out.println("Failed to create database " + dbName + "; " + e.getMessage());
		}

		// You can set the new database as default database for the driver:
		arangoDriver.setDefaultDatabase(dbName);

		String collectionName = "firstCollection";
		try {
			CollectionEntity myArangoCollection = arangoDriver.createCollection(collectionName);
			System.out.println("Collection created: " + myArangoCollection.getName());
		} catch (Exception e) {
			System.out.println("Failed to create colleciton " + collectionName + "; " + e.getMessage());
		}

		// create a document
		BaseDocument myObject = new BaseDocument();
		myObject.setDocumentKey("myKey");
		myObject.addAttribute("a", "Foo");
		myObject.addAttribute("b", 42);
		try {
			arangoDriver.createDocument(collectionName, myObject);
			System.out.println("Document created");
		} catch (ArangoException e) {
			System.out.println("Failed to create document. " + e.getMessage());
		}

		// read a document
		DocumentEntity<BaseDocument> myDocument = null;
		BaseDocument myObject2 = null;
		try {
			myDocument = arangoDriver.getDocument(collectionName, "myKey", BaseDocument.class);
			myObject2 = myDocument.getEntity();
			System.out.println("Key: " + myObject2.getDocumentKey());
			System.out.println("Attribute 'a': " + myObject2.getProperties().get("a"));
			System.out.println("Attribute 'b': " + myObject2.getProperties().get("b"));
			System.out.println("Attribute 'c': " + myObject2.getProperties().get("c"));
		} catch (ArangoException e) {
			System.out.println("Failed to get document. " + e.getMessage());
		}

		// update a document
		try {
			myObject2.addAttribute("c", "Bar");
			arangoDriver.updateDocument(myDocument.getDocumentHandle(), myObject2);
		} catch (ArangoException e) {
			System.out.println("Failed to update document. " + e.getMessage());
		}

		// read document again
		try {
			myDocument = arangoDriver.getDocument(collectionName, "myKey", BaseDocument.class);
			System.out.println("Key: " + myObject2.getDocumentKey());
			System.out.println("Attribute 'a': " + myObject2.getProperties().get("a"));
			System.out.println("Attribute 'b': " + myObject2.getProperties().get("b"));
			System.out.println("Attribute 'c': " + myObject2.getProperties().get("c"));
		} catch (ArangoException e) {
			System.out.println("Failed to get document. " + e.getMessage());
		}

		// delete document
		try {
			arangoDriver.deleteDocument(myDocument.getDocumentHandle());
		} catch (ArangoException e) {
			System.out.println("Failed to delete document. " + e.getMessage());
		}

		// create some example entries
		try {
			for (Integer i = 0; i < 10; i++) {
				BaseDocument baseDocument = new BaseDocument();
				baseDocument.setDocumentKey(i.toString());
				baseDocument.addAttribute("name", "Homer");
				baseDocument.addAttribute("b", i + 42);
				arangoDriver.createDocument(collectionName, baseDocument);
			}
		} catch (ArangoException e) {
			System.out.println("Failed to create document. " + e.getMessage());
		}

		// Get all documents with the name "Homer" from collection
		// "firstCollection" and iterate over the result:
		try {
			String query = "FOR t IN firstCollection FILTER t.name == @name RETURN t";
			Map<String, Object> bindVars = new MapBuilder().put("name", "Homer").get();
			DocumentCursor<BaseDocument> cursor = arangoDriver.executeDocumentQuery(query, bindVars, null,
				BaseDocument.class);

			Iterator<BaseDocument> iterator = cursor.entityIterator();
			while (iterator.hasNext()) {
				BaseDocument aDocument = iterator.next();
				System.out.println("Key: " + aDocument.getDocumentKey());
			}
		} catch (ArangoException e) {
			System.out.println("Failed to execute query. " + e.getMessage());
		}

		// Now we will delete the document created before:
		try {
			String query = "FOR t IN firstCollection FILTER t.name == @name "
					+ "REMOVE t IN firstCollection LET removed = OLD RETURN removed";
			Map<String, Object> bindVars = new MapBuilder().put("name", "Homer").get();
			DocumentCursor<BaseDocument> cursor = arangoDriver.executeDocumentQuery(query, bindVars, null,
				BaseDocument.class);

			Iterator<BaseDocument> iterator = cursor.entityIterator();
			while (iterator.hasNext()) {
				BaseDocument aDocument = iterator.next();
				System.out.println("Removed document: " + aDocument.getDocumentKey());
			}

		} catch (ArangoException e) {
			System.out.println("Failed to execute query. " + e.getMessage());
		}
	}

}
