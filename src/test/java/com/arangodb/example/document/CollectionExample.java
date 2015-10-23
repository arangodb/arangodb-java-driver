package com.arangodb.example.document;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.CollectionsEntity;

public class CollectionExample extends BaseExample {

	private static final String DATABASE_NAME = "CreateCollectionExample";

	public static void main(String[] args) {
		//
		// You can find the ArangoDB Web interface here:
		// http://127.0.0.1:8529/
		//
		// change the log level to "debug" in /src/test/resource/logback.xml to
		// see the HTTP communication

		ArangoDriver arangoDriver = createDatabase(DATABASE_NAME);

		//
		printHeadline("create a collection");
		//
		try {
			CollectionEntity entity = arangoDriver.createCollection("collection1");
			printCollectionEntity(entity);
			// System.out.println(entity);
		} catch (ArangoException e) {
			System.err.println(e.getMessage());
		}

		createCollection(arangoDriver, "collection2");
		createCollection(arangoDriver, "collection3");

		//
		printHeadline("get list of all collections");
		//
		try {
			CollectionsEntity collectionsEntity = arangoDriver.getCollections();
			for (CollectionEntity entity : collectionsEntity.getCollections()) {
				printCollectionEntity(entity);
				// System.out.println(entity);
			}

		} catch (ArangoException e) {
			System.err.println(e.getMessage());
		}

		//
		printHeadline("get one collection");
		//
		try {
			CollectionEntity entity = arangoDriver.getCollection("collection2");
			printCollectionEntity(entity);
			// System.out.println(entity);
		} catch (ArangoException e) {
			System.err.println(e.getMessage());
		}

		//
		printHeadline("rename collection");
		//
		try {
			CollectionEntity entity = arangoDriver.renameCollection("collection2", "collection4");
			printCollectionEntity(entity);
			// System.out.println(entity);
		} catch (ArangoException e) {
			System.err.println(e.getMessage());
		}

		//
		printHeadline("truncate collection");
		//
		try {
			arangoDriver.truncateCollection("collection4");
		} catch (ArangoException e) {
			System.err.println(e.getMessage());
		}

		//
		printHeadline("delete collection");
		//
		try {
			CollectionEntity entity = arangoDriver.deleteCollection("collection4");
			printCollectionEntity(entity);
			// System.out.println(entity);
		} catch (ArangoException e) {
			System.err.println(e.getMessage());
		}

	}

	private static void printCollectionEntity(CollectionEntity collection) {
		if (collection == null) {
			System.out.println("Collection not found");
		} else if (collection.getName() == null) {
			// collection is deleted
			System.out.println("Collection '" + collection.getName() + "' with id '" + collection.getId() + "'");
		} else {
			System.out.println(
				"Collection '" + collection.getName() + "' (" + (collection.getIsSystem() ? "system" : "normal")
						+ " collection) with id '" + collection.getId() + "'");
		}
	}

}
