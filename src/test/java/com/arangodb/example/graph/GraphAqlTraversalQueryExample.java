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

import java.util.HashMap;
import java.util.Iterator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.arangodb.ArangoException;
import com.arangodb.CursorResult;
import com.arangodb.entity.EdgeEntity;
import com.arangodb.entity.marker.VertexEntity;
import com.arangodb.util.AqlQueryOptions;
import com.arangodb.util.TestUtils;

/**
 * traversal example (since ArangoDB 2.8)
 * 
 * @author a-brandt
 *
 */
public class GraphAqlTraversalQueryExample extends BaseExample {

	private static final String DATABASE_NAME = "GraphAqlTraversalQueryExample";

	private static final String GRAPH_NAME = "traversalGraph";
	private static final String EDGE_COLLECTION_NAME = "edges";
	private static final String VERTEXT_COLLECTION_NAME = "circles";

	@Before
	public void _before() throws ArangoException {
		removeTestDatabase(DATABASE_NAME);

		createDatabase(driver, DATABASE_NAME);
		createGraph(driver, GRAPH_NAME, EDGE_COLLECTION_NAME, VERTEXT_COLLECTION_NAME);
		addExampleElements();
	}

	@After
	public void _after() {
		removeTestDatabase(DATABASE_NAME);
	}

	@Test
	public void graphAqlQuery() throws ArangoException {

		if (isMinimumVersion(driver, TestUtils.VERSION_2_8)) {
			//
			printHeadline(
				"To get started we select the full graph; for better overview we only return the vertex ids:");
			//

			String queryString = "FOR v IN 1..3 OUTBOUND 'circles/A' GRAPH 'traversalGraph' RETURN v._key";
			CursorResult<String> cursor = executeAndPrintResultKeys(queryString);
			Assert.assertEquals(10, cursor.getCount());

			queryString = "FOR v IN 1..3 OUTBOUND 'circles/A' edges RETURN v._key";
			cursor = executeAndPrintResultKeys(queryString);
			Assert.assertEquals(10, cursor.getCount());

			//
			printHeadline(
				"Now we only want the elements of a specific depth - 2 - the ones that are right behind the fork:");
			//

			queryString = "FOR v IN 2..2 OUTBOUND 'circles/A' GRAPH 'traversalGraph' return v._key";
			cursor = executeAndPrintResultKeys(queryString);
			Assert.assertEquals(4, cursor.getCount());

			queryString = "FOR v IN 2 OUTBOUND 'circles/A' GRAPH 'traversalGraph' return v._key";
			cursor = executeAndPrintResultKeys(queryString);
			Assert.assertEquals(4, cursor.getCount());

			//
			printHeadline("Now lets start to add some filters:");
			//

			queryString = "FOR v, e, p IN 1..3 OUTBOUND 'circles/A' GRAPH 'traversalGraph' FILTER p.vertices[1]._key != 'G' RETURN v._key";
			cursor = executeAndPrintResultKeys(queryString);
			Assert.assertEquals(5, cursor.getCount());

			queryString = "FOR v, e, p IN 1..3 OUTBOUND 'circles/A' GRAPH 'traversalGraph' FILTER p.edges[0].label != 'right_foo' RETURN v._key";
			cursor = executeAndPrintResultKeys(queryString);
			Assert.assertEquals(5, cursor.getCount());

			queryString = "FOR v,e,p IN 1..3 OUTBOUND 'circles/A' GRAPH 'traversalGraph' FILTER p.vertices[1]._key != 'G' FILTER p.edges[1].label != 'left_blub' return v._key";
			cursor = executeAndPrintResultKeys(queryString);
			Assert.assertEquals(3, cursor.getCount());

			queryString = "FOR v,e,p IN 1..3 OUTBOUND 'circles/A' GRAPH 'traversalGraph' FILTER p.vertices[1]._key != 'G' AND    p.edges[1].label != 'left_blub' return v._key";
			cursor = executeAndPrintResultKeys(queryString);
			Assert.assertEquals(3, cursor.getCount());

			//
			printHeadline("Comparing OUTBOUND / INBOUND / ANY:");
			//

			queryString = "FOR v IN 1..3 OUTBOUND 'circles/E' GRAPH 'traversalGraph' return v._key";
			cursor = executeAndPrintResultKeys(queryString);
			Assert.assertEquals(1, cursor.getCount());

			queryString = "FOR v IN 1..3 INBOUND 'circles/E' GRAPH 'traversalGraph' return v._key";
			cursor = executeAndPrintResultKeys(queryString);
			Assert.assertEquals(2, cursor.getCount());

			queryString = "FOR v IN 1..3 ANY 'circles/E' GRAPH 'traversalGraph' return v._key";
			cursor = executeAndPrintResultKeys(queryString);
			Assert.assertEquals(6, cursor.getCount());
		}
	}

	private CursorResult<String> executeAndPrintResultKeys(final String queryString) throws ArangoException {
		final HashMap<String, Object> bindVars = new HashMap<String, Object>();
		final AqlQueryOptions aqlQueryOptions = new AqlQueryOptions().setCount(true);

		System.out.println("Query: " + queryString);

		final CursorResult<String> cursor = driver.executeAqlQuery(queryString, bindVars, aqlQueryOptions,
			String.class);
		Assert.assertNotNull(cursor);

		final Iterator<String> iterator = cursor.iterator();
		while (iterator.hasNext()) {
			final String key = iterator.next();

			Assert.assertNotNull(key);

			System.out.println("_key = " + key);
		}

		return cursor;
	}

	private void addExampleElements() throws ArangoException {

		// Add circle circles
		final VertexEntity<Circle> vA = createVertex(new Circle("A", "1"));
		final VertexEntity<Circle> vB = createVertex(new Circle("B", "2"));
		final VertexEntity<Circle> vC = createVertex(new Circle("C", "3"));
		final VertexEntity<Circle> vD = createVertex(new Circle("D", "4"));
		final VertexEntity<Circle> vE = createVertex(new Circle("E", "5"));
		final VertexEntity<Circle> vF = createVertex(new Circle("F", "6"));
		final VertexEntity<Circle> vG = createVertex(new Circle("G", "7"));
		final VertexEntity<Circle> vH = createVertex(new Circle("H", "8"));
		final VertexEntity<Circle> vI = createVertex(new Circle("I", "9"));
		final VertexEntity<Circle> vJ = createVertex(new Circle("J", "10"));
		final VertexEntity<Circle> vK = createVertex(new Circle("K", "11"));

		// Add relevant edges - left branch:
		saveEdge(vA, vB, new CircleEdge(false, true, "left_bar"));
		saveEdge(vB, vC, new CircleEdge(false, true, "left_blarg"));
		saveEdge(vC, vD, new CircleEdge(false, true, "left_blorg"));
		saveEdge(vB, vE, new CircleEdge(false, true, "left_blub"));
		saveEdge(vE, vF, new CircleEdge(false, true, "left_schubi"));

		// Add relevant edges - right branch:
		saveEdge(vA, vG, new CircleEdge(false, true, "right_foo"));
		saveEdge(vG, vH, new CircleEdge(false, true, "right_blob"));
		saveEdge(vH, vI, new CircleEdge(false, true, "right_blub"));
		saveEdge(vG, vJ, new CircleEdge(false, true, "right_zip"));
		saveEdge(vJ, vK, new CircleEdge(false, true, "right_zup"));
	}

	private EdgeEntity<CircleEdge> saveEdge(
		final VertexEntity<Circle> from,
		final VertexEntity<Circle> to,
		final CircleEdge edge) throws ArangoException {
		return driver.graphCreateEdge(GRAPH_NAME, EDGE_COLLECTION_NAME, from.getDocumentHandle(),
			to.getDocumentHandle(), edge, false);
	}

	private VertexEntity<Circle> createVertex(final Circle vertex) throws ArangoException {
		return driver.graphCreateVertex(GRAPH_NAME, VERTEXT_COLLECTION_NAME, vertex, true);
	}

}
