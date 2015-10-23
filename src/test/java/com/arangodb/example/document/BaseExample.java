package com.arangodb.example.document;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.entity.CollectionEntity;

public class BaseExample {

	protected static ArangoDriver createDatabase(String name) {

		ArangoConfigure configure = new ArangoConfigure();
		// configure.setUser("myUser");
		// configure.setPassword("password");
		// configuration file: src/test/resources/arangodb.properties
		configure.init();

		ArangoDriver arangoDriver = new ArangoDriver(configure);

		deleteDatabase(arangoDriver, name);

		try {
			arangoDriver.createDatabase(name);
			System.out.println("Database created: " + name);
		} catch (Exception e) {
			System.out.println("Failed to create database " + name + "; " + e.getMessage());
		}

		arangoDriver.setDefaultDatabase(name);

		return arangoDriver;
	}

	protected static void deleteDatabase(ArangoDriver arangoDriver, String name) {
		try {
			arangoDriver.deleteDatabase(name);
		} catch (Exception e) {
		}
	}

	protected static void createCollection(ArangoDriver arangoDriver, String name) {
		try {
			CollectionEntity createCollection = arangoDriver.createCollection(name);
			System.out.println(
				"created collection '" + createCollection.getName() + "' with id '" + createCollection.getId() + "'");
		} catch (ArangoException e) {
			System.err.println(e.getMessage());
		}
	}

	protected static void printEntity(Object object) {
		if (object == null) {
			System.out.println("Document not found");
		} else {
			System.out.println(object);
		}
	}

	protected static void printHeadline(String name) {
		System.out.println("---------------------------------------------");
		System.out.println(name);
		System.out.println("---------------------------------------------");
	}

}
