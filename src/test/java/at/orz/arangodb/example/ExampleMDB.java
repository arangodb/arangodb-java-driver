/*
 * Copyright (C) 2012,2013 tamtam180
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

package at.orz.arangodb.example;

import java.util.Map;

import at.orz.arangodb.ArangoConfigure;
import at.orz.arangodb.ArangoDriver;
import at.orz.arangodb.ArangoException;
import at.orz.arangodb.entity.CollectionType;
import at.orz.arangodb.entity.DocumentEntity;
import at.orz.arangodb.util.MapBuilder;

/**
 * Switch many database. 
 * @author tamtam180 - kirscheless at gmail.com
 * @since 1.4.0
 */
public class ExampleMDB {

	public static void main(String[] args) {

		// Initialize configure
		ArangoConfigure configure = new ArangoConfigure();
		configure.init();
		
		// Create Driver (this instance is thread-safe)
		// If you use a multi database, you need create each instance.
		ArangoDriver driverA = new ArangoDriver(configure); // db = _system (configure#defaultDatabase)
		ArangoDriver driverB = new ArangoDriver(configure, "mydb2");
		
		try {
			
			try {
				driverA.deleteCollection("example1");
			} catch (Exception e) {}
			try {
				driverB.deleteDatabase("mydb2");
			} catch (Exception e) {}
			try {
				driverB.deleteCollection("example2");
			} catch (Exception e) {}
			
			// Create Collection at db(_system)
			driverA.createCollection("example1", false, null, null, null, null, CollectionType.DOCUMENT);
			driverA.createDocument("example1", 
					new MapBuilder().put("attr1", "value1").put("attr2", "value2").get(), 
					false, false);

			// Create Database mydb2
			driverB.createDatabase("mydb2");
			
			// Create Collection at db(mydb2)
			driverB.createCollection("example2", false, null, null, null, null, CollectionType.DOCUMENT);
			driverB.createDocument("example2", 
					new MapBuilder().put("attr1-B", "value1").put("attr2-B", "value2").get(), 
					false, false);
			
			// print all database names.
			System.out.println(driverA.getDatabases());
			// -> _system, mydb2

			// get all document-handle, and print get & print document. (_system DB)
			for (String documentHandle: driverA.getDocuments("example1", true)) {
				DocumentEntity<Map<String, Object>> doc = driverA.getDocument(documentHandle, Map.class);
				System.out.println(doc.getEntity());
			}

			for (String documentHandle: driverB.getDocuments("example2", true)) {
				DocumentEntity<Map<String, Object>> doc = driverB.getDocument(documentHandle, Map.class);
				System.out.println(doc.getEntity());
			}

		} catch (ArangoException e) {
			e.printStackTrace();
		} finally {
			configure.shutdown();
		}

	}

}
