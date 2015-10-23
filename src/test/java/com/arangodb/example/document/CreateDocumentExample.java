package com.arangodb.example.document;

import java.util.HashMap;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.DocumentEntity;

public class CreateDocumentExample extends BaseExample {

	private static final String DATABASE_NAME = "CreateDocument";

	private static final String COLLECTION_NAME = "CreateDocument";

	private static final String KEY1 = "key1";

	private static final String KEY2 = "key2";

	private static final String KEY3 = "key3";

	private static final String KEY4 = "key4";

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
		printHeadline("create documents");
		//

		System.out.println("1. create a document by a BaseDocument object:");
		BaseDocument myBaseDocument = new BaseDocument();
		myBaseDocument.setDocumentKey(KEY1);
		// attributes are stored in a HashMap
		myBaseDocument.addAttribute("name", "Alice");
		myBaseDocument.addAttribute("gender", "female");
		myBaseDocument.addAttribute("age", 18);

		try {
			DocumentEntity<BaseDocument> entity = arangoDriver.createDocument(COLLECTION_NAME, myBaseDocument);
			// or DocumentEntity<BaseDocument> entity =
			// arangoDriver.createDocument(COLLECTION_NAME, KEY1,
			// myBaseDocument);

			// the DocumentEntity contains the key, document handle and revision
			System.out.println("Key: " + entity.getDocumentKey());
			System.out.println("Id: " + entity.getDocumentHandle());
			System.out.println("Revision: " + entity.getDocumentRevision());

			BaseDocument baseDocument = entity.getEntity();
			// the BaseDocument contains the key, document handle and revision
			System.out.println("Key: " + baseDocument.getDocumentKey());
			System.out.println("Id: " + baseDocument.getDocumentHandle());
			System.out.println("Revision: " + baseDocument.getDocumentRevision());
			// get the attributes
			System.out.println("Attribute 'name': " + baseDocument.getProperties().get("name"));
			System.out.println("Attribute 'gender': " + baseDocument.getProperties().get("gender"));
			System.out.println("Attribute 'age': " + baseDocument.getProperties().get("age"));

			// printEntity(entity);
		} catch (ArangoException e) {
			System.out.println("Failed to create document. " + e.getMessage());
		}

		System.out.println("2. create a document by a HashMap object:");
		HashMap<String, Object> myHashMap = new HashMap<String, Object>();
		myHashMap.put("_key", KEY2);
		// attributes are stored in a HashMap
		myHashMap.put("name", "Alice");
		myHashMap.put("gender", "female");
		myHashMap.put("age", 18);

		try {
			DocumentEntity<HashMap<String, Object>> entity = arangoDriver.createDocument(COLLECTION_NAME, myHashMap);
			// or DocumentEntity<HashMap<String, Object>> entity =
			// arangoDriver.createDocument(COLLECTION_NAME, KEY2, myHashMap);

			// the DocumentEntity contains the key, document handle and revision
			System.out.println("Key: " + entity.getDocumentKey());
			System.out.println("Id: " + entity.getDocumentHandle());
			System.out.println("Revision: " + entity.getDocumentRevision());

			HashMap<String, Object> hashMap = entity.getEntity();
			// the HashMap contains the key, document handle and revision
			System.out.println("Key: " + hashMap.get("_key"));
			System.out.println("Id: " + hashMap.get("_id"));
			System.out.println("Revision: " + hashMap.get("_rev"));
			// get the attributes
			System.out.println("Attribute 'name': " + hashMap.get("name"));
			System.out.println("Attribute 'gender': " + hashMap.get("gender"));
			System.out.println("Attribute 'age': " + hashMap.get("age"));

			// printEntity(entity);
		} catch (ArangoException e) {
			System.out.println("Failed to create document. " + e.getMessage());
		}

		System.out.println("3. create a document by an object:");
		SimplePerson mySimplePerson = new SimplePerson("Angela", "female", 42);
		try {
			DocumentEntity<SimplePerson> entity = arangoDriver.createDocument(COLLECTION_NAME, KEY3, mySimplePerson);

			// the DocumentEntity contains the key, document handle and revision
			System.out.println("Key: " + entity.getDocumentKey());
			System.out.println("Id: " + entity.getDocumentHandle());
			System.out.println("Revision: " + entity.getDocumentRevision());

			SimplePerson simplePerson = entity.getEntity();
			// get the attributes
			System.out.println("Attribute 'name': " + simplePerson.getName());
			System.out.println("Attribute 'gender': " + simplePerson.getGender());
			System.out.println("Attribute 'age': " + simplePerson.getAge());

			// printEntity(entity);
		} catch (ArangoException e) {
			System.out.println("Failed to create document. " + e.getMessage());
		}

		System.out.println("4. create a document by an object with document attributes:");
		DocumentPerson myDocumentPerson = new DocumentPerson("Peter", "male", 24);
		try {
			DocumentEntity<DocumentPerson> entity = arangoDriver.createDocument(COLLECTION_NAME, KEY4,
				myDocumentPerson);

			// the DocumentEntity contains the key, document handle and revision
			System.out.println("Key: " + entity.getDocumentKey());
			System.out.println("Id: " + entity.getDocumentHandle());
			System.out.println("Revision: " + entity.getDocumentRevision());

			DocumentPerson documentPerson = entity.getEntity();
			// the DocumentPerson contains the key, document handle and revision
			System.out.println("Key: " + documentPerson.getDocumentKey());
			System.out.println("Id: " + documentPerson.getDocumentHandle());
			System.out.println("Revision: " + documentPerson.getDocumentRevision());
			// get the attributes
			System.out.println("Attribute 'name': " + documentPerson.getName());
			System.out.println("Attribute 'gender': " + documentPerson.getGender());
			System.out.println("Attribute 'age': " + documentPerson.getAge());

			// printEntity(entity);
		} catch (ArangoException e) {
			System.out.println("Failed to create document. " + e.getMessage());
		}

	}

}
