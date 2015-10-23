package com.arangodb.example.document;

import java.util.HashMap;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.entity.DocumentEntity;

public class ReplaceAndUpdateDocumentExample extends BaseExample {

	private static final String DATABASE_NAME = "ReplaceDocument";

	private static final String COLLECTION_NAME = "ReplaceDocument";

	private static final String KEY1 = "key1";

	public static void main(String[] args) {
		//
		// You can find the ArangoDB Web interface here:
		// http://127.0.0.1:8529/
		//
		// change the log level to "debug" in /src/test/resource/logback.xml to
		// see the HTTP communication

		ArangoDriver arangoDriver = createDatabase(DATABASE_NAME);
		createCollection(arangoDriver, COLLECTION_NAME);

		//
		printHeadline("create example document");
		//

		HashMap<String, Object> myHashMap = new HashMap<String, Object>();
		myHashMap.put("_key", KEY1);
		// attributes are stored in a HashMap
		myHashMap.put("name", "Alice");
		myHashMap.put("gender", "female");
		myHashMap.put("age", 18);
		String documentHandleExample = null;

		try {
			DocumentEntity<HashMap<String, Object>> entity = arangoDriver.createDocument(COLLECTION_NAME, myHashMap);
			System.out.println("Key: " + entity.getDocumentKey());
			System.out.println("Id: " + entity.getDocumentHandle());
			System.out.println("Revision: " + entity.getDocumentRevision());
			documentHandleExample = entity.getDocumentHandle();
		} catch (ArangoException e) {
			System.out.println("Failed to create document. " + e.getMessage());
		}

		//
		printHeadline("replace document");
		//

		try {
			DocumentPerson dp = new DocumentPerson("Moritz", "male", 6);
			arangoDriver.replaceDocument(documentHandleExample, dp);
			System.out.println("Key: " + dp.getDocumentKey());
			System.out.println("Revision: " + dp.getDocumentRevision() + " <- revision changed");
			System.out.println("Attribute 'name': " + dp.getName());

		} catch (ArangoException e) {
			System.out.println("Failed to replace document. " + e.getMessage());
		}

		//
		printHeadline("update document");
		//

		// update one attribute
		HashMap<String, Object> newHashMap = new HashMap<String, Object>();
		newHashMap.put("name", "Fritz");
		try {
			arangoDriver.updateDocument(documentHandleExample, newHashMap);

			DocumentEntity<DocumentPerson> entity = arangoDriver.getDocument(documentHandleExample,
				DocumentPerson.class);
			DocumentPerson dp = entity.getEntity();

			System.out.println("Key: " + dp.getDocumentKey());
			System.out.println("Revision: " + dp.getDocumentRevision() + " <- revision changed");
			System.out.println("Attribute 'name': " + dp.getName());
			System.out.println("Attribute 'gender': " + dp.getGender());
			System.out.println("Attribute 'age': " + dp.getAge());

		} catch (ArangoException e) {
			System.out.println("Failed to update document. " + e.getMessage());
		}
	}

}
