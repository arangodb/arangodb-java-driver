package com.arangodb.example.document;

import java.util.HashMap;
import java.util.List;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.DocumentEntity;

public class ReadDocumentExample extends BaseExample {

	private static final String DATABASE_NAME = "ReadDocument";

	private static final String COLLECTION_NAME = "ReadDocument";

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
			documentHandleExample = entity.getDocumentHandle();
		} catch (ArangoException e) {
			System.out.println("Failed to create document. " + e.getMessage());
		}

		//
		printHeadline("read documents");
		//

		System.out.println("1. read document as BaseDocument object:");
		try {
			DocumentEntity<BaseDocument> entity = arangoDriver.getDocument(COLLECTION_NAME, KEY1, BaseDocument.class);

			// the DocumentEntity contains the key, document handle and revision
			System.out.println("Key: " + entity.getDocumentKey());
			System.out.println("Id: " + entity.getDocumentHandle());
			System.out.println("Revision: " + entity.getDocumentRevision());

			BaseDocument baseDocument2 = entity.getEntity();
			// the BaseDocument contains the key, document handle and revision
			System.out.println("Key: " + baseDocument2.getDocumentKey());
			System.out.println("Id: " + baseDocument2.getDocumentHandle());
			System.out.println("Revision: " + baseDocument2.getDocumentRevision());
			// get the attributes
			System.out.println("Attribute 'name': " + baseDocument2.getProperties().get("name"));
			System.out.println("Attribute 'gender': " + baseDocument2.getProperties().get("gender"));
			// ArangoDb stores numeric values as double values
			System.out.println(
				"Attribute 'age': " + baseDocument2.getProperties().get("age") + " <- Data type changed to double");

			// printEntity(entity);
		} catch (ArangoException e) {
			System.out.println("Failed to read document. " + e.getMessage());
		}

		System.out.println("2. read document as HashMap object:");
		try {
			DocumentEntity<HashMap> entity = arangoDriver.getDocument(COLLECTION_NAME, KEY1, HashMap.class);
			HashMap<?, ?> map = entity.getEntity();
			// get the attributes
			System.out.println("Key: " + map.get("_key"));
			System.out.println("Id: " + map.get("_id"));
			System.out.println("Revision: " + map.get("_rev"));
			System.out.println("Attribute 'name': " + map.get("name"));
			System.out.println("Attribute 'gender': " + map.get("gender"));
			System.out.println("Attribute 'age': " + map.get("age") + " <- Data type changed to double");

			// printEntity(entity);
		} catch (ArangoException e) {
			System.out.println("Failed to read document. " + e.getMessage());
		}

		System.out.println("3. read document as SimplePerson object:");
		try {
			DocumentEntity<SimplePerson> entity = arangoDriver.getDocument(COLLECTION_NAME, KEY1, SimplePerson.class);
			SimplePerson sp = entity.getEntity();
			// get the attributes
			System.out.println("Attribute 'name': " + sp.getName());
			System.out.println("Attribute 'gender': " + sp.getGender());
			// ArangoDb stores numeric values as double values but the value is
			// converted back to integer
			System.out.println("Attribute 'age': " + sp.getAge());

			// printEntity(entity);
		} catch (ArangoException e) {
			System.out.println("Failed to read document. " + e.getMessage());
		}

		System.out.println("4. read document as DocumentPerson object:");
		try {
			DocumentEntity<DocumentPerson> entity = arangoDriver.getDocument(COLLECTION_NAME, KEY1,
				DocumentPerson.class);
			DocumentPerson dp = entity.getEntity();
			System.out.println("Key: " + dp.getDocumentKey());
			System.out.println("Id: " + dp.getDocumentHandle());
			System.out.println("Revision: " + dp.getDocumentRevision());
			// get the attributes
			System.out.println("Attribute 'name': " + dp.getName());
			System.out.println("Attribute 'gender': " + dp.getGender());
			// ArangoDb stores numeric values as double values but the value is
			// converted back to integer
			System.out.println("Attribute 'age': " + dp.getAge());

			// printEntity(entity);
		} catch (ArangoException e) {
			System.out.println("Failed to read document. " + e.getMessage());
		}

		//
		printHeadline("read document by document handle");
		//
		try {
			DocumentEntity<DocumentPerson> entity = arangoDriver.getDocument(documentHandleExample,
				DocumentPerson.class);
			DocumentPerson dp = entity.getEntity();
			System.out.println("Key: " + dp.getDocumentKey());
			System.out.println("Attribute 'name': " + dp.getName());
		} catch (ArangoException e) {
			System.out.println("Failed to create document. " + e.getMessage());
		}

		//
		printHeadline("read collection count");
		//
		try {
			CollectionEntity collectionCount = arangoDriver.getCollectionCount(COLLECTION_NAME);
			System.out.println("Collection count: " + collectionCount.getCount());
		} catch (ArangoException e) {
			System.out.println("Failed to read count. " + e.getMessage());
		}

		//
		printHeadline("read all document handles of a collection");
		//
		try {
			List<String> documentHandles = arangoDriver.getDocuments(COLLECTION_NAME);
			for (String documentHandle : documentHandles) {
				System.out.println("document handle: " + documentHandle);
			}

		} catch (ArangoException e) {
			System.out.println("Failed to read all documents. " + e.getMessage());
		}
	}

}
