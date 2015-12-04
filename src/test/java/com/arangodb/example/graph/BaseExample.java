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

package com.arangodb.example.graph;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.entity.ArangoVersion;
import com.arangodb.entity.BooleanResultEntity;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.CollectionOptions;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.EdgeDefinitionEntity;
import com.arangodb.entity.GraphEntity;
import com.arangodb.util.TestUtils;

public class BaseExample {

	protected ArangoConfigure getConfiguration() {
		ArangoConfigure configure = new ArangoConfigure();
		// configure.setUser("myUser");
		// configure.setPassword("password");
		// configuration file: src/test/resources/arangodb.properties
		configure.init();

		return configure;
	}

	protected ArangoDriver getArangoDriver(ArangoConfigure configuration) {
		return new ArangoDriver(configuration);
	}

	protected void removeTestDatabase(String name) {
		ArangoDriver arangoDriver = getArangoDriver(getConfiguration());
		try {
			arangoDriver.deleteDatabase(name);
		} catch (Exception e) {
		}
	}

	protected void createDatabase(ArangoDriver arangoDriver, String name) {
		try {
			BooleanResultEntity createDatabase = arangoDriver.createDatabase(name);
			Assert.assertNotNull(createDatabase);
			Assert.assertNotNull(createDatabase.getResult());
			Assert.assertTrue(createDatabase.getResult());
		} catch (Exception e) {
			Assert.fail("Failed to create database " + name + "; " + e.getMessage());
		}

		arangoDriver.setDefaultDatabase(name);
	}

	protected void deleteDatabase(ArangoDriver arangoDriver, String name) {
		try {
			arangoDriver.deleteDatabase(name);
		} catch (Exception e) {
		}
	}

	protected void createCollection(ArangoDriver arangoDriver, String name) {
		try {
			CollectionEntity createCollection = arangoDriver.createCollection(name);
			Assert.assertNotNull(createCollection);
			Assert.assertNotNull(createCollection.getName());
			Assert.assertEquals(name, createCollection.getName());
		} catch (ArangoException e) {
			Assert.fail("create collection failed. " + e.getMessage());
		}
	}

	protected void printEntity(Object object) {
		if (object == null) {
			System.out.println("Document not found");
		} else {
			System.out.println(object);
		}
	}

	protected void printHeadline(String name) {
		System.out.println("---------------------------------------------");
		System.out.println(name);
		System.out.println("---------------------------------------------");
	}

	public void createGraph(
		ArangoDriver arangoDriver,
		String grapName,
		String nameEdgeCollection,
		String nameVertexCollection) throws ArangoException {

		//
		printHeadline("create edge collection");
		//

		CollectionEntity createCollection = arangoDriver.createCollection(nameEdgeCollection,
			new CollectionOptions().setType(CollectionType.EDGE));
		Assert.assertNotNull(createCollection);
		Assert.assertNotNull(createCollection.getId());
		Assert.assertTrue(createCollection.getId() > 0L);

		//
		printHeadline("create vertex collection");
		//

		createCollection = arangoDriver.createCollection(nameVertexCollection,
			new CollectionOptions().setType(CollectionType.DOCUMENT));
		Assert.assertNotNull(createCollection);
		Assert.assertNotNull(createCollection.getId());
		Assert.assertTrue(createCollection.getId() > 0L);

		//
		printHeadline("create edge definition");
		//

		EdgeDefinitionEntity ed = new EdgeDefinitionEntity();
		// add edge collection name
		ed.setCollection(nameEdgeCollection);

		// add vertex collection names
		ed.getFrom().add(nameVertexCollection);

		// add vertex collection names
		ed.getTo().add(nameVertexCollection);

		//
		printHeadline("create edge definition list");
		//
		List<EdgeDefinitionEntity> edgeDefinitions = new ArrayList<EdgeDefinitionEntity>();
		edgeDefinitions.add(ed);

		//
		printHeadline("create graph");
		//
		GraphEntity createGraph = arangoDriver.createGraph(grapName, edgeDefinitions, null, true);
		Assert.assertNotNull(createGraph);
		Assert.assertEquals(grapName, createGraph.getName());
	}

	public boolean isMinimumVersion(ArangoDriver arangoDriver, String version) throws ArangoException {
		ArangoVersion ver = arangoDriver.getVersion();
		int b = TestUtils.compareVersion(ver.getVersion(), version);
		return b > -1;
	}

}
