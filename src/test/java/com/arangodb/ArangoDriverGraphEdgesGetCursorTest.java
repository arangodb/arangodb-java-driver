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

package com.arangodb;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.arangodb.entity.EdgeEntity;
import com.arangodb.entity.GraphEntity;
import com.arangodb.entity.PlainEdgeEntity;
import com.arangodb.entity.ShortestPathEntity;
import com.arangodb.entity.marker.VertexEntity;
import com.arangodb.util.AqlQueryOptions;
import com.arangodb.util.MapBuilder;
import com.arangodb.util.ShortestPathOptions;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ArangoDriverGraphEdgesGetCursorTest extends BaseGraphTest {

	public ArangoDriverGraphEdgesGetCursorTest(ArangoConfigure configure, ArangoDriver driver) {
		super(configure, driver);
	}

	private static final String GRAPH_NAME = "UnitTestGraph";

	@Before
	public void setup() throws ArangoException {
		try {
			driver.createGraph(GRAPH_NAME, this.createEdgeDefinitions(1, 0), this.createOrphanCollections(0), true);
		} catch (ArangoException e) {
		}
	}

	@Test
	public void graphGetEdgeCursorTest() throws ArangoException {
		GraphEntity graph = this.createTestGraph();
		EdgeCursor<PlainEdgeEntity> cursor = driver.graphGetEdgeCursor(graph.getName());
		assertThat(cursor.getCount(), is(8));
		assertThat(cursor.getCode(), is(201));
		assertThat(cursor.hasMore(), is(false));
		assertThat(cursor.getCursorId(), is(-1L));

		List<PlainEdgeEntity> results = cursor.asEntityList();
		assertThat(results.size(), is(8));
		assertThat(results.get(0).getFromCollection(), startsWith("Country/"));
		assertThat(results.get(0).getToCollection(), startsWith("Country/"));
	}

	@Test
	public void graphGetEdgeCursorIteratorTest() throws ArangoException {
		GraphEntity graph = this.createTestGraph();
		EdgeCursor<PlainEdgeEntity> cursor = driver.graphGetEdgeCursor(graph.getName());
		assertThat(cursor.getCount(), is(8));
		assertThat(cursor.getCode(), is(201));
		assertThat(cursor.hasMore(), is(false));
		assertThat(cursor.getCursorId(), is(-1L));

		int count = 0;

		Iterator<EdgeEntity<PlainEdgeEntity>> iterator = cursor.iterator();
		while (iterator.hasNext()) {
			EdgeEntity<PlainEdgeEntity> edgeEntity = iterator.next();
			++count;
			assertThat(edgeEntity.getEntity().getFromCollection(), startsWith("Country/"));
			assertThat(edgeEntity.getEntity().getToCollection(), startsWith("Country/"));
		}

		assertThat(count, is(8));
	}

	@Test
	public void graphGetEdgeCursorByExampleVertexHandle() throws ArangoException {

		TestComplexEntity01 v1 = new TestComplexEntity01("Homer", "A Simpson", 38);
		TestComplexEntity01 v2 = new TestComplexEntity01("Marge", "A Simpson", 36);
		TestComplexEntity01 v3 = new TestComplexEntity01("Bart", "A Simpson", 10);

		VertexEntity<TestComplexEntity01> vertex1 = driver.graphCreateVertex(GRAPH_NAME, "from1-1", v1, true);
		VertexEntity<TestComplexEntity01> vertex2 = driver.graphCreateVertex(GRAPH_NAME, "to1-1", v2, true);
		VertexEntity<TestComplexEntity01> vertex3 = driver.graphCreateVertex(GRAPH_NAME, "to1-1", v3, true);

		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex1.getDocumentHandle(), vertex2.getDocumentHandle(),
			new TestComplexEntity02(1, 2, 3), null);

		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex1.getDocumentHandle(), vertex3.getDocumentHandle(),
			new TestComplexEntity02(4, 5, 6), null);

		EdgeCursor<TestComplexEntity02> cursor = driver.graphGetEdgeCursorByExample(GRAPH_NAME,
			TestComplexEntity02.class, vertex1.getDocumentHandle());
		assertThat(cursor.getCount(), is(2));

		cursor = driver.graphGetEdgeCursorByExample(GRAPH_NAME, TestComplexEntity02.class, vertex2.getDocumentHandle());
		assertThat(cursor.getCount(), is(1));
	}

	@Test
	public void graphGetEdgeCursorByExampleObjectTest() throws ArangoException {
		TestComplexEntity01 v1 = new TestComplexEntity01("Homer", "A Simpson", 38);
		TestComplexEntity01 v2 = new TestComplexEntity01("Marge", "A Simpson", 36);
		TestComplexEntity01 v3 = new TestComplexEntity01("Bart", "A Simpson", 10);
		TestComplexEntity01 v4 = new TestComplexEntity01("Remoh", "Homer's twin", 38);

		VertexEntity<TestComplexEntity01> vertex1 = driver.graphCreateVertex(GRAPH_NAME, "from1-1", v1, true);

		VertexEntity<TestComplexEntity01> vertex2 = driver.graphCreateVertex(GRAPH_NAME, "to1-1", v2, true);

		VertexEntity<TestComplexEntity01> vertex3 = driver.graphCreateVertex(GRAPH_NAME, "to1-1", v3, true);

		VertexEntity<TestComplexEntity01> vertex4 = driver.graphCreateVertex(GRAPH_NAME, "from1-1", v4, true);

		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex1.getDocumentHandle(), vertex2.getDocumentHandle(),
			new TestComplexEntity02(1, 2, 3), null);

		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex1.getDocumentHandle(), vertex3.getDocumentHandle(),
			new TestComplexEntity02(4, 5, 6), null);

		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex4.getDocumentHandle(), vertex2.getDocumentHandle(),
			new TestComplexEntity02(7, 8, 9), null);

		EdgeCursor<TestComplexEntity02> cursor = driver.graphGetEdgeCursorByExample(GRAPH_NAME,
			TestComplexEntity02.class, new TestComplexEntity01(null, null, 38));
		assertThat(cursor.getCount(), is(3));

		cursor = driver.graphGetEdgeCursorByExample(GRAPH_NAME, TestComplexEntity02.class, v3);
		assertThat(cursor.getCount(), is(1));

		List<TestComplexEntity02> results = cursor.asEntityList();

		assertThat(results.get(0).getClass().getName(), is(TestComplexEntity02.class.getName()));
		assertThat(results.get(0).getX(), is(4));

	}

	@Test
	public void graphGetEdgeCursorByExampleMapTest() throws ArangoException {
		TestComplexEntity01 v1 = new TestComplexEntity01("Homer", "A Simpson", 38);
		TestComplexEntity01 v2 = new TestComplexEntity01("Marge", "A Simpson", 36);
		TestComplexEntity01 v3 = new TestComplexEntity01("Bart", "A Simpson", 10);
		TestComplexEntity01 v4 = new TestComplexEntity01("Remoh", "Homer's twin", 38);

		VertexEntity<TestComplexEntity01> vertex1 = driver.graphCreateVertex(GRAPH_NAME, "from1-1", v1, true);
		VertexEntity<TestComplexEntity01> vertex2 = driver.graphCreateVertex(GRAPH_NAME, "to1-1", v2, true);
		VertexEntity<TestComplexEntity01> vertex3 = driver.graphCreateVertex(GRAPH_NAME, "to1-1", v3, true);
		VertexEntity<TestComplexEntity01> vertex4 = driver.graphCreateVertex(GRAPH_NAME, "from1-1", v4, true);

		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex1.getDocumentHandle(), vertex2.getDocumentHandle(),
			new TestComplexEntity02(1, 2, 3), null);

		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex1.getDocumentHandle(), vertex3.getDocumentHandle(),
			new TestComplexEntity02(4, 5, 6), null);

		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex4.getDocumentHandle(), vertex2.getDocumentHandle(),
			new TestComplexEntity02(7, 8, 9), null);

		Map<String, Object> exampleVertex = new HashMap<String, Object>();
		exampleVertex.put("user", "Homer");

		EdgeCursor<TestComplexEntity02> cursor = driver.graphGetEdgeCursorByExample(GRAPH_NAME,
			TestComplexEntity02.class, exampleVertex);
		assertThat(cursor.getCount(), is(2));

		exampleVertex.put("user", "Bart");
		cursor = driver.graphGetEdgeCursorByExample(GRAPH_NAME, TestComplexEntity02.class, exampleVertex);
		assertThat(cursor.getCount(), is(1));

		List<TestComplexEntity02> results = cursor.asEntityList();

		assertThat(results.get(0).getClass().getName(), is(TestComplexEntity02.class.getName()));
		assertThat(results.get(0).getX(), is(4));

	}

	private AqlQueryOptions getAqlQueryOptions(Boolean count, Integer batchSize, Boolean fullCount) {
		return new AqlQueryOptions().setCount(count).setBatchSize(batchSize).setFullCount(fullCount);
	}

	@Test
	public void batchSizeAndLimitTest() throws ArangoException {

		TestComplexEntity01 v1 = new TestComplexEntity01("Homer", "A Simpson", 38);
		TestComplexEntity01 v2 = new TestComplexEntity01("Marge", "A Simpson", 36);
		TestComplexEntity01 v3 = new TestComplexEntity01("Bart", "A Simpson", 10);
		TestComplexEntity01 v4 = new TestComplexEntity01("Remoh", "Homer's twin", 38);

		VertexEntity<TestComplexEntity01> vertex1 = driver.graphCreateVertex(GRAPH_NAME, "from1-1", v1, true);
		VertexEntity<TestComplexEntity01> vertex2 = driver.graphCreateVertex(GRAPH_NAME, "to1-1", v2, true);
		VertexEntity<TestComplexEntity01> vertex3 = driver.graphCreateVertex(GRAPH_NAME, "to1-1", v3, true);
		VertexEntity<TestComplexEntity01> vertex4 = driver.graphCreateVertex(GRAPH_NAME, "from1-1", v4, true);

		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex1.getDocumentHandle(), vertex2.getDocumentHandle(),
			new TestComplexEntity02(1, 2, 3), null);

		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex1.getDocumentHandle(), vertex3.getDocumentHandle(),
			new TestComplexEntity02(4, 5, 6), null);

		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex4.getDocumentHandle(), vertex2.getDocumentHandle(),
			new TestComplexEntity02(7, 8, 9), null);

		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex4.getDocumentHandle(), vertex3.getDocumentHandle(),
			new TestComplexEntity02(10, 11, 12), null);

		Integer batchSize = 2;
		Boolean count = true;
		Boolean fullCount = true;

		String query = "for i in graph_edges(@graphName, null) LIMIT 3 return i";
		Map<String, Object> bindVars = new MapBuilder().put("graphName", GRAPH_NAME).get();

		EdgeCursor<TestComplexEntity02> cursor = driver.executeEdgeQuery(query, bindVars,
			getAqlQueryOptions(count, batchSize, fullCount), TestComplexEntity02.class);

		assertEquals(3, cursor.getCount());
		assertEquals(201, cursor.getCode());
		assertTrue(cursor.hasMore());
		assertEquals(4, cursor.getFullCount());
		assertTrue(cursor.getCursorId() > -1L);
	}

	@Test
	public void edgesAqlTest() throws ArangoException {

		TestComplexEntity01 v1 = new TestComplexEntity01("Homer", "A Simpson", 38);
		TestComplexEntity01 v2 = new TestComplexEntity01("Marge", "A Simpson", 36);
		TestComplexEntity01 v3 = new TestComplexEntity01("Bart", "A Simpson", 10);
		TestComplexEntity01 v4 = new TestComplexEntity01("Remoh", "Homer's twin", 38);

		VertexEntity<TestComplexEntity01> vertex1 = driver.graphCreateVertex(GRAPH_NAME, "from1-1", v1, true);
		VertexEntity<TestComplexEntity01> vertex2 = driver.graphCreateVertex(GRAPH_NAME, "to1-1", v2, true);
		VertexEntity<TestComplexEntity01> vertex3 = driver.graphCreateVertex(GRAPH_NAME, "to1-1", v3, true);
		VertexEntity<TestComplexEntity01> vertex4 = driver.graphCreateVertex(GRAPH_NAME, "from1-1", v4, true);

		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex1.getDocumentHandle(), vertex2.getDocumentHandle(),
			new TestComplexEntity02(1, 2, 3), null);

		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex1.getDocumentHandle(), vertex3.getDocumentHandle(),
			new TestComplexEntity02(4, 5, 6), null);

		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex4.getDocumentHandle(), vertex2.getDocumentHandle(),
			new TestComplexEntity02(7, 8, 9), null);

		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex4.getDocumentHandle(), vertex3.getDocumentHandle(),
			new TestComplexEntity02(10, 11, 12), null);

		Integer batchSize = 10;
		Boolean count = true;
		Boolean fullCount = true;
		Integer ttl = null;

		// get outbound vertices of vertex1 (the should be 2)
		String query = "for i in graph_edges(@graphName, @vertex, @options) return i";

		// options bindVars
		Map<String, Object> options = new MapBuilder().put("direction", "outbound").get();

		// bindVars
		Map<String, Object> bindVars = new MapBuilder().put("graphName", GRAPH_NAME)
				.put("vertex", vertex1.getDocumentHandle()).put("options", options).get();

		EdgeCursor<TestComplexEntity02> cursor = driver.executeEdgeQuery(query, bindVars,
			getAqlQueryOptions(count, batchSize, fullCount), TestComplexEntity02.class);

		assertEquals(2, cursor.getCount());
		assertEquals(201, cursor.getCode());
		assertFalse(cursor.hasMore());
		assertEquals(new Long(-1L), cursor.getCursorId());

		// get outbound vertices of vertex2 (the should be no)
		bindVars = new MapBuilder().put("graphName", GRAPH_NAME).put("vertex", vertex2.getDocumentHandle())
				.put("options", options).get();

		cursor = driver.executeEdgeQuery(query, bindVars, getAqlQueryOptions(count, batchSize, fullCount),
			TestComplexEntity02.class);

		assertEquals(0, cursor.getCount());
		assertEquals(201, cursor.getCode());
		assertFalse(cursor.hasMore());
		assertEquals(new Long(-1L), cursor.getCursorId());

	}

	@Test
	public void shortesPathTest() throws ArangoException {

		TestComplexEntity01 v1 = new TestComplexEntity01("Homer", "A Simpson", 38);
		TestComplexEntity01 v2 = new TestComplexEntity01("Marge", "A Simpson", 36);

		VertexEntity<TestComplexEntity01> vertex1 = driver.graphCreateVertex(GRAPH_NAME, "from1-1", v1, true);
		VertexEntity<TestComplexEntity01> vertex2 = driver.graphCreateVertex(GRAPH_NAME, "to1-1", v2, true);

		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex1.getDocumentHandle(), vertex2.getDocumentHandle(),
			new TestComplexEntity02(1, 2, 3), null);

		ShortestPathOptions shortestPathOptions = new ShortestPathOptions();
		shortestPathOptions.setDirection(Direction.OUTBOUND);

		//
		ShortestPathEntity<TestComplexEntity01, TestComplexEntity02> entity = driver.graphGetShortesPath(GRAPH_NAME,
			vertex1.getDocumentHandle(), vertex2.getDocumentHandle(), shortestPathOptions, TestComplexEntity01.class,
			TestComplexEntity02.class);

		assertEquals(201, entity.getCode());

		assertEquals(1L, entity.getDistance().longValue());
		assertEquals(vertex1.getDocumentHandle(), entity.getStartVertex());
		assertEquals(vertex2.getDocumentHandle(), entity.getVertex());
		// assertEquals(1, entity.getPaths().get(0).getEdges().size());
		// assertEquals(2, entity.getPaths().get(0).getVertices().size());
	}
}
