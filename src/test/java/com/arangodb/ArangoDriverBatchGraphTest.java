package com.arangodb;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.Test;

import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.EdgeEntity;
import com.arangodb.entity.GraphEntity;

public class ArangoDriverBatchGraphTest extends BaseGraphTest {

	private final String graphName = "ArangoDriverBatchGraphTest";
	private final String edgeCollectionName = "edge-1";

	@Test
	public void createGraph() throws ArangoException {
		driver.startBatchMode();
		final GraphEntity tmpResult = driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0),
			this.createOrphanCollections(2), true);
		assertThat(tmpResult, is(notNullValue()));
		assertThat(tmpResult.getRequestId(), is(notNullValue()));

		driver.executeBatch();

		final GraphEntity createGraph = driver.getBatchResponseByRequestId(tmpResult.getRequestId());
		assertThat(createGraph, is(notNullValue()));
		assertThat(createGraph.getName(), is(this.graphName));
		assertThat(createGraph.getRequestId(), is(nullValue()));
	}

	@Test
	public void createGraphVertex() throws ArangoException {
		driver.startBatchMode();
		driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);

		final DocumentEntity<TestComplexEntity03> v1 = driver.graphCreateVertex(this.graphName, "from1-1",
			new TestComplexEntity03("v1-user", "desc1", 10), null);
		assertThat(v1, is(notNullValue()));
		assertThat(v1.getRequestId(), is(notNullValue()));

		final DocumentEntity<TestComplexEntity03> v2 = driver.graphCreateVertex(this.graphName, "to1-1",
			new TestComplexEntity03("v2-user", "desc2", 12), null);
		assertThat(v2, is(notNullValue()));
		assertThat(v2.getRequestId(), is(notNullValue()));

		driver.executeBatch();

		final DocumentEntity<TestComplexEntity03> v3 = driver.getBatchResponseByRequestId(v1.getRequestId());
		assertThat(v3, is(notNullValue()));
		assertThat(v3.getDocumentKey(), is(notNullValue()));
		assertThat(v3.getRequestId(), is(nullValue()));

		final DocumentEntity<TestComplexEntity03> v4 = driver.getBatchResponseByRequestId(v2.getRequestId());
		assertThat(v4, is(notNullValue()));
		assertThat(v4.getDocumentKey(), is(notNullValue()));
		assertThat(v4.getRequestId(), is(nullValue()));
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void createGraphEdge() throws ArangoException {

		driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);

		final DocumentEntity<TestComplexEntity03> v1 = driver.graphCreateVertex(this.graphName, "from1-1",
			new TestComplexEntity03("v1-user", "desc1", 10), null);

		final DocumentEntity<TestComplexEntity03> v2 = driver.graphCreateVertex(this.graphName, "to1-1",
			new TestComplexEntity03("v2-user", "desc2", 12), null);

		driver.startBatchMode();

		driver.graphCreateEdge(this.graphName, edgeCollectionName, null, v1.getDocumentHandle(), v2.getDocumentHandle(),
			null, null);

		driver.graphCreateEdge(this.graphName, edgeCollectionName, null, v1.getDocumentHandle(), v2.getDocumentHandle(),
			null, null);

		final EdgeEntity<?> e1 = driver.graphCreateEdge(this.graphName, edgeCollectionName, null,
			v1.getDocumentHandle(), v2.getDocumentHandle(), null, null);
		assertThat(e1, is(notNullValue()));
		assertThat(e1.getRequestId(), is(notNullValue()));

		driver.executeBatch();

		final EdgeEntity<?> e2 = driver.getBatchResponseByRequestId(e1.getRequestId());
		assertThat(e2, is(notNullValue()));
		assertThat(e2.getDocumentKey(), is(notNullValue()));
		assertThat(e2.getRequestId(), is(nullValue()));

		EdgeEntity<Map> graphGetEdge = driver.graphGetEdge(graphName, edgeCollectionName, e2.getDocumentKey(),
			Map.class, null, null);
		assertThat(graphGetEdge, is(notNullValue()));
		assertThat(graphGetEdge.getDocumentKey(), is(e2.getDocumentKey()));
	}

	@Test
	public void createGraphEdgeFail() throws ArangoException {

		driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);

		driver.startBatchMode();

		final DocumentEntity<TestComplexEntity03> v1 = driver.graphCreateVertex(this.graphName, "from1-1",
			new TestComplexEntity03("v1-user", "desc1", 10), null);

		final DocumentEntity<TestComplexEntity03> v2 = driver.graphCreateVertex(this.graphName, "to1-1",
			new TestComplexEntity03("v2-user", "desc2", 12), null);

		final EdgeEntity<?> e1 = driver.graphCreateEdge(this.graphName, edgeCollectionName, null,
			v1.getDocumentHandle(), v2.getDocumentHandle(), null, null);
		assertThat(e1, is(notNullValue()));
		assertThat(e1.getRequestId(), is(notNullValue()));

		driver.executeBatch();

		EdgeEntity<?> e2 = null;
		try {
			e2 = driver.getBatchResponseByRequestId(e1.getRequestId());
			fail("this should fail");
		} catch (Exception ex) {
		}
		assertThat(e2, is(nullValue()));

	}

}
