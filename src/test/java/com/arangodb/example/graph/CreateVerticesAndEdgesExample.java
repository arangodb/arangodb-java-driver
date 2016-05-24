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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.entity.EdgeEntity;
import com.arangodb.entity.marker.VertexEntity;

/**
 * AQL example creating vertices and edges
 * 
 * @author a-brandt
 *
 */
public class CreateVerticesAndEdgesExample extends BaseExample {

	private static final String DATABASE_NAME = "CreateVerticesAndEdgesExample";

	private static final String GRAPH_NAME = "example_graph1";
	private static final String EDGE_COLLECTION_NAME = "edgeColl1";
	private static final String VERTEXT_COLLECTION_NAME = "vertexColl1";

	/**
	 * @param configure
	 * @param driver
	 */
	public CreateVerticesAndEdgesExample(final ArangoConfigure configure, final ArangoDriver driver) {
		super(configure, driver);
	}

	@Before
	public void _before() throws ArangoException {
		removeTestDatabase(DATABASE_NAME);

		createDatabase(driver, DATABASE_NAME);
		createGraph(driver, GRAPH_NAME, EDGE_COLLECTION_NAME, VERTEXT_COLLECTION_NAME);
	}

	@After
	public void _after() {
		removeTestDatabase(DATABASE_NAME);
	}

	@Test
	public void verticesAndEdges() throws ArangoException {

		//
		printHeadline("create two vertices");
		//

		// create some persons
		final Person personA = new Person("A", Person.MALE);
		final VertexEntity<Person> v1 = driver.graphCreateVertex(GRAPH_NAME, VERTEXT_COLLECTION_NAME, personA, true);
		Assert.assertNotNull(v1);
		Assert.assertNotNull(v1.getEntity());
		Person p = v1.getEntity();
		Assert.assertEquals(personA.getName(), p.getName());
		Assert.assertNotNull(p.getDocumentHandle());
		Assert.assertNotNull(p.getDocumentKey());
		Assert.assertNotNull(p.getDocumentRevision());

		final Person personB = new Person("B", Person.MALE);
		final VertexEntity<Person> v2 = driver.graphCreateVertex(GRAPH_NAME, VERTEXT_COLLECTION_NAME, personB, false);
		Assert.assertNotNull(v2);
		Assert.assertNotNull(v2.getEntity());
		p = v2.getEntity();
		Assert.assertEquals(personB.getName(), p.getName());
		Assert.assertNotNull(p.getDocumentHandle());
		Assert.assertNotNull(p.getDocumentKey());
		Assert.assertNotNull(p.getDocumentRevision());

		//
		printHeadline("create edge");
		//

		final Knows knows = new Knows(1984);
		final EdgeEntity<Knows> graphCreateEdge = driver.graphCreateEdge(GRAPH_NAME, EDGE_COLLECTION_NAME,
			v1.getDocumentHandle(), v2.getDocumentHandle(), knows, false);

		Assert.assertNotNull(graphCreateEdge);
		final Knows entity = graphCreateEdge.getEntity();
		Assert.assertNotNull(entity);
		Assert.assertNotNull(entity.getDocumentHandle());
		Assert.assertNotNull(entity.getDocumentKey());
		Assert.assertNotNull(entity.getDocumentRevision());
		Assert.assertNotNull(entity.getFromVertexHandle());
		Assert.assertNotNull(entity.getToVertexHandle());
		Assert.assertNotNull(entity.getSince());
		Assert.assertEquals(v1.getDocumentHandle(), entity.getFromVertexHandle());
		Assert.assertEquals(v2.getDocumentHandle(), entity.getToVertexHandle());
		Assert.assertEquals(knows.getSince(), entity.getSince());

	}

}
