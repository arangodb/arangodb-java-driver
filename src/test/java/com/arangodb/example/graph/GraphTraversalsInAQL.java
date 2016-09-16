/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb.example.graph;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDBException;
import com.arangodb.BaseTest;
import com.arangodb.entity.EdgeDefinition;
import com.arangodb.entity.EdgeResult;
import com.arangodb.entity.VertexResult;

/**
 * Graph traversals in AQL
 * 
 * @see <a href="https://docs.arangodb.com/current/AQL/Graphs/Traversals.html">Graph traversals in AQL</a>
 * 
 * @author a-brandt
 *
 */
public class GraphTraversalsInAQL extends BaseTest {

	private static final String GRAPH_NAME = "traversalGraph";
	private static final String EDGE_COLLECTION_NAME = "edges";
	private static final String VERTEXT_COLLECTION_NAME = "circles";

	@Before
	public void _before() {
		final Collection<EdgeDefinition> edgeDefinitions = new ArrayList<>();
		EdgeDefinition edgeDefinition = new EdgeDefinition().collection(EDGE_COLLECTION_NAME)
				.from(VERTEXT_COLLECTION_NAME).to(VERTEXT_COLLECTION_NAME);
		edgeDefinitions.add(edgeDefinition);
		try {
			db.createGraph(GRAPH_NAME, edgeDefinitions, null);
			addExampleElements();
		} catch (ArangoDBException ex) {

		}
	}

	@After
	public void _after() {
		try {
			db.graph(GRAPH_NAME).drop();
		} catch (ArangoDBException ex) {

		}
	}

	@Test
	public void queryAllVertices() throws ArangoDBException {
		String queryString = "FOR v IN 1..3 OUTBOUND 'circles/A' GRAPH 'traversalGraph' RETURN v._key";
		ArangoCursor<String> cursor = db.query(queryString, null, null, String.class);
		Collection<String> collection = toCollection(cursor);
		assertThat(collection.size(), is(10));

		queryString = "FOR v IN 1..3 OUTBOUND 'circles/A' edges RETURN v._key";
		cursor = db.query(queryString, null, null, String.class);
		collection = toCollection(cursor);
		assertThat(collection.size(), is(10));
	}

	@Test
	public void queryDepthTwo() throws ArangoDBException {
		String queryString = "FOR v IN 2..2 OUTBOUND 'circles/A' GRAPH 'traversalGraph' return v._key";
		ArangoCursor<String> cursor = db.query(queryString, null, null, String.class);
		Collection<String> collection = toCollection(cursor);
		assertThat(collection.size(), is(4));
		assertThat(collection, hasItems("C", "E", "H", "J"));

		queryString = "FOR v IN 2 OUTBOUND 'circles/A' GRAPH 'traversalGraph' return v._key";
		cursor = db.query(queryString, null, null, String.class);
		collection = toCollection(cursor);
		assertThat(collection.size(), is(4));
		assertThat(collection, hasItems("C", "E", "H", "J"));
	}

	@Test
	public void queryWithFilter() throws ArangoDBException {
		String queryString = "FOR v, e, p IN 1..3 OUTBOUND 'circles/A' GRAPH 'traversalGraph' FILTER p.vertices[1]._key != 'G' RETURN v._key";
		ArangoCursor<String> cursor = db.query(queryString, null, null, String.class);
		Collection<String> collection = toCollection(cursor);
		assertThat(collection.size(), is(5));
		assertThat(collection, hasItems("B", "C", "D", "E", "F"));

		queryString = "FOR v, e, p IN 1..3 OUTBOUND 'circles/A' GRAPH 'traversalGraph' FILTER p.edges[0].label != 'right_foo' RETURN v._key";
		cursor = db.query(queryString, null, null, String.class);
		collection = toCollection(cursor);
		assertThat(collection.size(), is(5));
		assertThat(collection, hasItems("B", "C", "D", "E", "F"));

		queryString = "FOR v,e,p IN 1..3 OUTBOUND 'circles/A' GRAPH 'traversalGraph' FILTER p.vertices[1]._key != 'G' FILTER p.edges[1].label != 'left_blub' return v._key";
		cursor = db.query(queryString, null, null, String.class);

		collection = toCollection(cursor);
		assertThat(collection.size(), is(3));
		assertThat(collection, hasItems("B", "C", "D"));

		queryString = "FOR v,e,p IN 1..3 OUTBOUND 'circles/A' GRAPH 'traversalGraph' FILTER p.vertices[1]._key != 'G' AND    p.edges[1].label != 'left_blub' return v._key";
		cursor = db.query(queryString, null, null, String.class);
		collection = toCollection(cursor);
		assertThat(collection.size(), is(3));
		assertThat(collection, hasItems("B", "C", "D"));
	}

	@Test
	public void queryOutboundInbound() throws ArangoDBException {
		String queryString = "FOR v IN 1..3 OUTBOUND 'circles/E' GRAPH 'traversalGraph' return v._key";
		ArangoCursor<String> cursor = db.query(queryString, null, null, String.class);
		Collection<String> collection = toCollection(cursor);
		assertThat(collection.size(), is(1));
		assertThat(collection, hasItems("F"));

		queryString = "FOR v IN 1..3 INBOUND 'circles/E' GRAPH 'traversalGraph' return v._key";
		cursor = db.query(queryString, null, null, String.class);
		collection = toCollection(cursor);
		assertThat(collection.size(), is(2));
		assertThat(collection, hasItems("B", "A"));

		queryString = "FOR v IN 1..3 ANY 'circles/E' GRAPH 'traversalGraph' return v._key";
		cursor = db.query(queryString, null, null, String.class);

		collection = toCollection(cursor);
		assertThat(collection.size(), is(6));
		assertThat(collection, hasItems("F", "B", "C", "D", "A", "G"));
	}

	//
	// private functions
	//

	private <T> Collection<T> toCollection(ArangoCursor<T> cursor) {
		List<T> result = new ArrayList<>();
		if (cursor != null) {
			for (Iterator<T> iterator = cursor.iterator(); iterator.hasNext();) {
				result.add(iterator.next());
			}
		}
		return result;
	}

	private void addExampleElements() throws ArangoDBException {

		// Add circle circles
		final VertexResult vA = createVertex(new Circle("A", "1"));
		final VertexResult vB = createVertex(new Circle("B", "2"));
		final VertexResult vC = createVertex(new Circle("C", "3"));
		final VertexResult vD = createVertex(new Circle("D", "4"));
		final VertexResult vE = createVertex(new Circle("E", "5"));
		final VertexResult vF = createVertex(new Circle("F", "6"));
		final VertexResult vG = createVertex(new Circle("G", "7"));
		final VertexResult vH = createVertex(new Circle("H", "8"));
		final VertexResult vI = createVertex(new Circle("I", "9"));
		final VertexResult vJ = createVertex(new Circle("J", "10"));
		final VertexResult vK = createVertex(new Circle("K", "11"));

		// Add relevant edges - left branch:
		saveEdge(new CircleEdge(vA.getId(), vB.getId(), false, true, "left_bar"));
		saveEdge(new CircleEdge(vB.getId(), vC.getId(), false, true, "left_blarg"));
		saveEdge(new CircleEdge(vC.getId(), vD.getId(), false, true, "left_blorg"));
		saveEdge(new CircleEdge(vB.getId(), vE.getId(), false, true, "left_blub"));
		saveEdge(new CircleEdge(vE.getId(), vF.getId(), false, true, "left_schubi"));

		// Add relevant edges - right branch:
		saveEdge(new CircleEdge(vA.getId(), vG.getId(), false, true, "right_foo"));
		saveEdge(new CircleEdge(vG.getId(), vH.getId(), false, true, "right_blob"));
		saveEdge(new CircleEdge(vH.getId(), vI.getId(), false, true, "right_blub"));
		saveEdge(new CircleEdge(vG.getId(), vJ.getId(), false, true, "right_zip"));
		saveEdge(new CircleEdge(vJ.getId(), vK.getId(), false, true, "right_zup"));
	}

	private EdgeResult saveEdge(final CircleEdge edge) throws ArangoDBException {
		return db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(edge);
	}

	private VertexResult createVertex(final Circle vertex) throws ArangoDBException {
		return db.graph(GRAPH_NAME).vertexCollection(VERTEXT_COLLECTION_NAME).insertVertex(vertex);
	}

}
