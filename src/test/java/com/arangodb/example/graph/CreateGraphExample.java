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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.CollectionOptions;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.EdgeDefinitionEntity;
import com.arangodb.entity.GraphEntity;

/**
 * Create graph example
 * 
 * @author a-brandt
 *
 */
public class CreateGraphExample extends BaseExample {

	private static final String DATABASE_NAME = "CreateGraphExample";

	private static final String GRAPH_NAME = "example_graph1";
	private static final String EDGE_COLLECTION_NAME = "edgeColl1";
	private static final String VERTEXT_COLLECTION_NAME = "vertexColl1";

	/**
	 * @param configure
	 * @param driver
	 */
	public CreateGraphExample(final ArangoConfigure configure, final ArangoDriver driver) {
		super(configure, driver);
	}

	@Before
	public void _before() {
		removeTestDatabase(DATABASE_NAME);

		createDatabase(driver, DATABASE_NAME);
	}

	@After
	public void _after() {
		removeTestDatabase(DATABASE_NAME);
	}

	@Test
	public void createGraph() throws ArangoException {

		//
		printHeadline("create edge collection");
		//

		CollectionEntity createCollection = driver.createCollection(EDGE_COLLECTION_NAME,
			new CollectionOptions().setType(CollectionType.EDGE));
		Assert.assertNotNull(createCollection);
		Assert.assertNotNull(createCollection.getId());
		Assert.assertTrue(createCollection.getId() > 0L);

		//
		printHeadline("create vertex collection");
		//

		createCollection = driver.createCollection(VERTEXT_COLLECTION_NAME,
			new CollectionOptions().setType(CollectionType.DOCUMENT));
		Assert.assertNotNull(createCollection);
		Assert.assertNotNull(createCollection.getId());
		Assert.assertTrue(createCollection.getId() > 0L);

		//
		printHeadline("create edge definition");
		//

		final EdgeDefinitionEntity ed = new EdgeDefinitionEntity();
		// add edge collection name
		ed.setCollection(EDGE_COLLECTION_NAME);

		// add vertex collection names
		ed.getFrom().add(VERTEXT_COLLECTION_NAME);

		// add vertex collection names
		ed.getTo().add(VERTEXT_COLLECTION_NAME);

		//
		printHeadline("create edge definition list");
		//
		final List<EdgeDefinitionEntity> edgeDefinitions = new ArrayList<EdgeDefinitionEntity>();
		edgeDefinitions.add(ed);

		//
		printHeadline("create graph");
		//
		final GraphEntity createGraph = driver.createGraph(GRAPH_NAME, edgeDefinitions, null, true);
		Assert.assertNotNull(createGraph);
		Assert.assertEquals(GRAPH_NAME, createGraph.getName());
	}

}
