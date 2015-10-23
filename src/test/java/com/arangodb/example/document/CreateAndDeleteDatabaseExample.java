package com.arangodb.example.document;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoDriver;

public class CreateAndDeleteDatabaseExample extends BaseExample {

	private static final String DATABASE_NAME = "CreateDatabaseExample";

	public static void main(String[] args) {
		//
		// You can find the ArangoDB Web interface here:
		// http://127.0.0.1:8529/
		//
		// change the log level to "debug" in /src/test/resource/logback.xml to
		// see the HTTP communication

		//
		printHeadline("create a driver");
		//

		ArangoConfigure configure = new ArangoConfigure();
		// configure.setUser("myUser");
		// configure.setPassword("password");
		configure.init();

		ArangoDriver arangoDriver = new ArangoDriver(configure);

		//
		printHeadline("create a database");
		//
		try {
			arangoDriver.createDatabase(DATABASE_NAME);
			System.out.println("Database created: " + DATABASE_NAME);
		} catch (Exception e) {
			System.out.println("Failed to create database " + DATABASE_NAME + "; " + e.getMessage());
		}
		// set a default database for the connection
		arangoDriver.setDefaultDatabase(DATABASE_NAME);

		// do something ...

		//
		printHeadline("create a driver with default database");
		//
		ArangoConfigure configure2 = new ArangoConfigure();
		configure2.init();
		ArangoDriver arangoDriver2 = new ArangoDriver(configure, DATABASE_NAME);

		// do something ...

		//
		printHeadline("delete database");
		//
		try {
			arangoDriver.deleteDatabase(DATABASE_NAME);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

	}
}
