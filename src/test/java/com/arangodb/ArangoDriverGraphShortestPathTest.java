package com.arangodb;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.arangodb.entity.EdgeEntity;
import com.arangodb.entity.ShortestPathEntity;
import com.arangodb.entity.marker.VertexEntity;
import com.arangodb.util.ShortestPathOptions;

/**
 * @author Mark - mark@arangodb.com
 *
 */
public class ArangoDriverGraphShortestPathTest extends BaseGraphTest {

	private static final String GRAPH_NAME = "UnitTestGraph";

	@Before
	public void setup() {
		try {
			driver.deleteGraph(GRAPH_NAME);
		} catch (ArangoException e1) {
		}
		try {
			driver.createGraph(GRAPH_NAME, this.createEdgeDefinitions(1, 0), this.createOrphanCollections(0), true);
		} catch (final ArangoException e) {
		}
	}

	@After
	public void cleanup() {
		try {
			driver.deleteGraph(GRAPH_NAME);
		} catch (ArangoException e1) {
		}
	}

	@Test
	public void shortestPath() throws ArangoException {
		final TestComplexEntity01 v1 = new TestComplexEntity01("Homer", "A Simpson", 38);
		final TestComplexEntity01 v2 = new TestComplexEntity01("Marge", "A Simpson", 36);
		final TestComplexEntity01 v3 = new TestComplexEntity01("Bart", "A Simpson", 10);
		final TestComplexEntity01 v4 = new TestComplexEntity01("Remoh", "Homer's twin", 38);

		final VertexEntity<TestComplexEntity01> vertex1 = driver.graphCreateVertex(GRAPH_NAME, "from1-1", v1, true);
		final VertexEntity<TestComplexEntity01> vertex2 = driver.graphCreateVertex(GRAPH_NAME, "to1-1", v2, true);
		final VertexEntity<TestComplexEntity01> vertex3 = driver.graphCreateVertex(GRAPH_NAME, "to1-1", v3, true);
		final VertexEntity<TestComplexEntity01> vertex4 = driver.graphCreateVertex(GRAPH_NAME, "from1-1", v4, true);

		final TestComplexEntity02 e1 = new TestComplexEntity02(1, 2, 3);
		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex1.getDocumentHandle(), vertex2.getDocumentHandle(), e1,
			null);

		final TestComplexEntity02 e2 = new TestComplexEntity02(4, 5, 6);
		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex1.getDocumentHandle(), vertex3.getDocumentHandle(), e2,
			null);

		final TestComplexEntity02 e3 = new TestComplexEntity02(7, 8, 9);
		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex4.getDocumentHandle(), vertex2.getDocumentHandle(), e3,
			null);

		final TestComplexEntity02 e4 = new TestComplexEntity02(10, 11, 12);
		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex4.getDocumentHandle(), vertex3.getDocumentHandle(), e4,
			null);

		final ShortestPathOptions shortestPathOptions = new ShortestPathOptions();
		{
			final ShortestPathEntity<TestComplexEntity01, TestComplexEntity02> entity = driver.graphGetShortestPath(
				GRAPH_NAME, v1, v2, shortestPathOptions, TestComplexEntity01.class, TestComplexEntity02.class);
			Assert.assertEquals(201, entity.getCode());
			Assert.assertEquals(1, entity.getEdges().size());
			Assert.assertEquals(2, entity.getVertices().size());
			Assert.assertEquals(1L, entity.getDistance().longValue());
		}
		{
			final ShortestPathEntity<TestComplexEntity01, TestComplexEntity02> entity = driver.graphGetShortestPath(
				GRAPH_NAME, v1, v4, shortestPathOptions, TestComplexEntity01.class, TestComplexEntity02.class);
			Assert.assertEquals(201, entity.getCode());
			Assert.assertEquals(2, entity.getEdges().size());
			Assert.assertEquals(3, entity.getVertices().size());
			Assert.assertEquals(2L, entity.getDistance().longValue());
		}
	}

	@Test
	public void shortestPathDirectionOut() throws ArangoException {
		final TestComplexEntity01 v1 = new TestComplexEntity01("Homer", "A Simpson", 38);
		final TestComplexEntity01 v2 = new TestComplexEntity01("Marge", "A Simpson", 36);
		final TestComplexEntity01 v3 = new TestComplexEntity01("Bart", "A Simpson", 10);
		final TestComplexEntity01 v4 = new TestComplexEntity01("Remoh", "Homer's twin", 38);

		final VertexEntity<TestComplexEntity01> vertex1 = driver.graphCreateVertex(GRAPH_NAME, "from1-1", v1, true);
		final VertexEntity<TestComplexEntity01> vertex2 = driver.graphCreateVertex(GRAPH_NAME, "to1-1", v2, true);
		final VertexEntity<TestComplexEntity01> vertex3 = driver.graphCreateVertex(GRAPH_NAME, "to1-1", v3, true);
		final VertexEntity<TestComplexEntity01> vertex4 = driver.graphCreateVertex(GRAPH_NAME, "from1-1", v4, true);

		final TestComplexEntity02 e1 = new TestComplexEntity02(1, 2, 3);
		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex1.getDocumentHandle(), vertex2.getDocumentHandle(), e1,
			null);

		final TestComplexEntity02 e2 = new TestComplexEntity02(4, 5, 6);
		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex1.getDocumentHandle(), vertex3.getDocumentHandle(), e2,
			null);

		final TestComplexEntity02 e3 = new TestComplexEntity02(7, 8, 9);
		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex4.getDocumentHandle(), vertex2.getDocumentHandle(), e3,
			null);

		final TestComplexEntity02 e4 = new TestComplexEntity02(10, 11, 12);
		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex4.getDocumentHandle(), vertex3.getDocumentHandle(), e4,
			null);

		final ShortestPathOptions shortestPathOptions = new ShortestPathOptions();
		shortestPathOptions.setDirection(Direction.OUTBOUND);
		{
			final ShortestPathEntity<TestComplexEntity01, TestComplexEntity02> entity = driver.graphGetShortestPath(
				GRAPH_NAME, v1, v2, shortestPathOptions, TestComplexEntity01.class, TestComplexEntity02.class);
			Assert.assertEquals(201, entity.getCode());
			Assert.assertEquals(1, entity.getEdges().size());
			Assert.assertEquals(2, entity.getVertices().size());
		}
		{
			final ShortestPathEntity<TestComplexEntity01, TestComplexEntity02> entity = driver.graphGetShortestPath(
				GRAPH_NAME, v1, v4, shortestPathOptions, TestComplexEntity01.class, TestComplexEntity02.class);
			Assert.assertEquals(201, entity.getCode());
			Assert.assertNull(entity.getEdges());
			Assert.assertNull(entity.getVertices());
		}
	}

	@Test
	public void shortestPathDirectionIn() throws ArangoException {
		final TestComplexEntity01 v1 = new TestComplexEntity01("Homer", "A Simpson", 38);
		final TestComplexEntity01 v2 = new TestComplexEntity01("Marge", "A Simpson", 36);
		final TestComplexEntity01 v3 = new TestComplexEntity01("Bart", "A Simpson", 10);
		final TestComplexEntity01 v4 = new TestComplexEntity01("Remoh", "Homer's twin", 38);

		final VertexEntity<TestComplexEntity01> vertex1 = driver.graphCreateVertex(GRAPH_NAME, "from1-1", v1, true);
		final VertexEntity<TestComplexEntity01> vertex2 = driver.graphCreateVertex(GRAPH_NAME, "to1-1", v2, true);
		final VertexEntity<TestComplexEntity01> vertex3 = driver.graphCreateVertex(GRAPH_NAME, "to1-1", v3, true);
		final VertexEntity<TestComplexEntity01> vertex4 = driver.graphCreateVertex(GRAPH_NAME, "from1-1", v4, true);

		final TestComplexEntity02 e1 = new TestComplexEntity02(1, 2, 3);
		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex1.getDocumentHandle(), vertex2.getDocumentHandle(), e1,
			null);

		final TestComplexEntity02 e2 = new TestComplexEntity02(4, 5, 6);
		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex1.getDocumentHandle(), vertex3.getDocumentHandle(), e2,
			null);

		final TestComplexEntity02 e3 = new TestComplexEntity02(7, 8, 9);
		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex4.getDocumentHandle(), vertex2.getDocumentHandle(), e3,
			null);

		final TestComplexEntity02 e4 = new TestComplexEntity02(10, 11, 12);
		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex4.getDocumentHandle(), vertex3.getDocumentHandle(), e4,
			null);

		final ShortestPathOptions shortestPathOptions = new ShortestPathOptions();
		shortestPathOptions.setDirection(Direction.INBOUND);
		{
			final ShortestPathEntity<TestComplexEntity01, TestComplexEntity02> entity = driver.graphGetShortestPath(
				GRAPH_NAME, v1, v2, shortestPathOptions, TestComplexEntity01.class, TestComplexEntity02.class);
			Assert.assertEquals(201, entity.getCode());
			Assert.assertNull(entity.getEdges());
			Assert.assertNull(entity.getVertices());
		}
		{
			final ShortestPathEntity<TestComplexEntity01, TestComplexEntity02> entity = driver.graphGetShortestPath(
				GRAPH_NAME, v1, v4, shortestPathOptions, TestComplexEntity01.class, TestComplexEntity02.class);
			Assert.assertEquals(201, entity.getCode());
			Assert.assertNull(entity.getEdges());
			Assert.assertNull(entity.getVertices());
		}
	}

	@Test
	public void shortestPathWeigth() throws ArangoException {
		final TestComplexEntity01 v1 = new TestComplexEntity01("Homer", "A Simpson", 38);
		final TestComplexEntity01 v2 = new TestComplexEntity01("Marge", "A Simpson", 36);
		final TestComplexEntity01 v3 = new TestComplexEntity01("Bart", "A Simpson", 10);
		final TestComplexEntity01 v4 = new TestComplexEntity01("Remoh", "Homer's twin", 38);

		final VertexEntity<TestComplexEntity01> vertex1 = driver.graphCreateVertex(GRAPH_NAME, "from1-1", v1, true);
		final VertexEntity<TestComplexEntity01> vertex2 = driver.graphCreateVertex(GRAPH_NAME, "to1-1", v2, true);
		final VertexEntity<TestComplexEntity01> vertex3 = driver.graphCreateVertex(GRAPH_NAME, "to1-1", v3, true);
		final VertexEntity<TestComplexEntity01> vertex4 = driver.graphCreateVertex(GRAPH_NAME, "from1-1", v4, true);

		int x1 = 1;
		final TestComplexEntity02 e1 = new TestComplexEntity02(x1, 2, 3);
		EdgeEntity<TestComplexEntity02> edge1 = driver.graphCreateEdge(GRAPH_NAME, "edge-1", null,
			vertex1.getDocumentHandle(), vertex2.getDocumentHandle(), e1, null);

		final TestComplexEntity02 e2 = new TestComplexEntity02(4, 5, 6);
		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex1.getDocumentHandle(), vertex3.getDocumentHandle(), e2,
			null);

		int x3 = 7;
		final TestComplexEntity02 e3 = new TestComplexEntity02(x3, 8, 9);
		EdgeEntity<TestComplexEntity02> edge3 = driver.graphCreateEdge(GRAPH_NAME, "edge-1", null,
			vertex4.getDocumentHandle(), vertex2.getDocumentHandle(), e3, null);

		final TestComplexEntity02 e4 = new TestComplexEntity02(10, 11, 12);
		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex4.getDocumentHandle(), vertex3.getDocumentHandle(), e4,
			null);

		final ShortestPathOptions shortestPathOptions = new ShortestPathOptions();
		shortestPathOptions.setWeight("x");
		{
			final ShortestPathEntity<TestComplexEntity01, TestComplexEntity02> entity = driver.graphGetShortestPath(
				GRAPH_NAME, v1, v4, shortestPathOptions, TestComplexEntity01.class, TestComplexEntity02.class);
			Assert.assertEquals(201, entity.getCode());
			Assert.assertEquals(2, entity.getEdges().size());
			Assert.assertEquals(3, entity.getVertices().size());
			Assert.assertEquals(x1 + x3, entity.getDistance().longValue());
			for (EdgeEntity<TestComplexEntity02> edge : entity.getEdges()) {
				Assert.assertTrue(edge.getDocumentHandle().equals(edge1.getDocumentHandle())
						|| edge.getDocumentHandle().equals(edge3.getDocumentHandle()));
			}
		}
	}
}
