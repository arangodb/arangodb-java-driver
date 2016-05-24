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
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.arangodb.entity.DeletedEntity;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.marker.VertexEntity;
import com.arangodb.util.AqlQueryOptions;
import com.arangodb.util.GraphVerticesOptions;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * @author gschwab
 *
 */
public class ArangoDriverGraphVertexTest extends BaseGraphTest {

	private final static String GRAPH_NAME = "UnitTestGraph";
	private final static String COLLECTION_NAME = "UnitTestCollection";

	@Before
	public void setup() throws ArangoException {
		try {
			driver.createGraph(GRAPH_NAME, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
		} catch (final ArangoException e) {
		}
	}

	@Test
	public void test_create_vertex() throws ArangoException {

		final VertexEntity<TestComplexEntity01> vertex = driver.graphCreateVertex(GRAPH_NAME, "from1-1",
			new TestComplexEntity01("Homer", "Simpson", 38), true);

		assertThat(vertex.getDocumentHandle(), is(notNullValue()));
		assertThat(vertex.getDocumentRevision(), is(not(0L)));
		assertThat(vertex.getDocumentKey(), is(notNullValue()));
		assertThat(vertex.getEntity(), isA(TestComplexEntity01.class));
		final DocumentEntity<TestComplexEntity01> document = driver.getDocument(vertex.getDocumentHandle(),
			TestComplexEntity01.class);
		assertThat(document.getEntity().getUser(), is("Homer"));
		assertThat(document.getEntity().getDesc(), is("Simpson"));
		assertThat(document.getEntity().getAge(), is(38));
	}

	@Test
	public void test_create_vertex_with_document_attributes() throws ArangoException {

		final VertexEntity<TestComplexEntity03> vertex = driver.graphCreateVertex(GRAPH_NAME, "from1-1",
			new TestComplexEntity03("Homer", "Simpson", 38), true);

		assertThat(vertex.getDocumentHandle(), is(notNullValue()));
		assertThat(vertex.getDocumentRevision(), is(not(0L)));
		assertThat(vertex.getDocumentKey(), is(notNullValue()));
		assertThat(vertex.getEntity(), isA(TestComplexEntity03.class));

		assertThat(vertex.getEntity().getDocumentHandle(), is(notNullValue()));
		assertThat(vertex.getEntity().getDocumentKey(), is(notNullValue()));
		assertThat(vertex.getEntity().getDocumentRevision(), is(notNullValue()));

		final DocumentEntity<TestComplexEntity03> document = driver.getDocument(vertex.getDocumentHandle(),
			TestComplexEntity03.class);
		assertThat(document.getEntity().getUser(), is("Homer"));
		assertThat(document.getEntity().getDesc(), is("Simpson"));
		assertThat(document.getEntity().getAge(), is(38));

	}

	@Test
	public void test_create_vertex_error_graph() throws ArangoException {

		try {
			driver.graphCreateVertex("foo", "bar", new TestComplexEntity01("Homer", "Simpson", 38), true);
			fail();
		} catch (final ArangoException e) {
			assertThat(e.getCode(), greaterThan(300));
		}

	}

	@Test
	public void test_create_vertex_error_collection() throws ArangoException {

		try {
			driver.graphCreateVertex(GRAPH_NAME, "foo", new TestComplexEntity01("Homer", "Simpson", 38), true);
			fail();
		} catch (final ArangoException e) {
			assertThat(e.getCode(), greaterThan(300));
		}

	}

	@Test
	public void test_graphGetVertex() throws ArangoException {
		VertexEntity<TestComplexEntity01> vertex = driver.graphCreateVertex(GRAPH_NAME, "from1-1",
			new TestComplexEntity01("Homer", "Simpson", 38), true);
		try {
			vertex = driver.graphGetVertex(GRAPH_NAME, COLLECTION_NAME, vertex.getDocumentKey(),
				TestComplexEntity01.class, vertex.getDocumentRevision(), null);
		} catch (final ArangoException e) {
			assertThat(e.getCode(), greaterThan(300));
		}

	}

	// ***********************
	// *** Delete Vertex Tests
	// ***********************

	@Test
	public void test_delete_vertex() throws ArangoException {
		// create collection
		driver.graphCreateVertexCollection(GRAPH_NAME, COLLECTION_NAME);
		// create vertex
		final VertexEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(GRAPH_NAME, COLLECTION_NAME,
			new TestComplexEntity01("Homer", "Simpson", 38), true);

		// check exists vertex
		final VertexEntity<TestComplexEntity01> vertex = driver.graphGetVertex(GRAPH_NAME, COLLECTION_NAME,
			v1.getDocumentKey(), TestComplexEntity01.class);
		assertThat(vertex.getCode(), is(200));

		// delete
		final DeletedEntity deleted = driver.graphDeleteVertex(GRAPH_NAME, COLLECTION_NAME, v1.getDocumentKey(), true,
			null, null);
		assertThat(deleted.getCode(), is(200));
		assertThat(deleted.getDeleted(), is(true));

	}

	@Test
	public void test_delete_vertex_graph_not_found() throws ArangoException {

		try {
			driver.graphDeleteVertex("foo", "bar", "foobar", true, null, null);
			fail();
		} catch (final ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorNumber(), is(1924));
		}

	}

	@Test
	public void test_delete_vertex_not_found() throws ArangoException {

		driver.graphCreateVertexCollection(GRAPH_NAME, COLLECTION_NAME);

		try {
			driver.graphDeleteVertex(GRAPH_NAME, COLLECTION_NAME, "foo", true, null, null);
		} catch (final ArangoException e) {
			assertThat(e.getCode(), is(404));
		}

	}

	@Test
	public void test_delete_vertex_rev_eq() throws ArangoException {

		driver.graphCreateVertexCollection(GRAPH_NAME, COLLECTION_NAME);

		final VertexEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(GRAPH_NAME, COLLECTION_NAME,
			new TestComplexEntity01("Hoemr", "Simpson", 38), null);
		final VertexEntity<TestComplexEntity01> vertex = driver.graphGetVertex(GRAPH_NAME, COLLECTION_NAME,
			v1.getDocumentKey(), TestComplexEntity01.class, null, null);
		assertThat(vertex.getCode(), is(200));

		// delete
		final DeletedEntity deleted = driver.graphDeleteVertex(GRAPH_NAME, COLLECTION_NAME, v1.getDocumentKey(), null,
			v1.getDocumentRevision(), null);
		assertThat(deleted.getCode(), is(202));
		assertThat(deleted.getDeleted(), is(true));

	}

	@Test
	public void test_delete_vertex_rev_ng() throws ArangoException {

		driver.graphCreateVertexCollection(GRAPH_NAME, COLLECTION_NAME);

		final VertexEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(GRAPH_NAME, COLLECTION_NAME,
			new TestComplexEntity01("Homer", "Simspin", 38), null);
		final VertexEntity<TestComplexEntity01> vertex = driver.graphGetVertex(GRAPH_NAME, COLLECTION_NAME,
			v1.getDocumentKey(), TestComplexEntity01.class, null, null);
		assertThat(vertex.getCode(), is(200));

		// delete
		try {
			driver.graphDeleteVertex(GRAPH_NAME, COLLECTION_NAME, v1.getDocumentKey(), null,
				v1.getDocumentRevision() + 1, null);
		} catch (final ArangoException e) {
			assertThat(e.getCode(), is(412));
			assertThat(e.getErrorNumber(), is(1903));
			assertThat(e.getErrorMessage(), is("wrong revision"));
		}

	}

	@Test
	public void test_delete_vertex_match_eq() throws ArangoException {
		driver.graphCreateVertexCollection(GRAPH_NAME, COLLECTION_NAME);

		final VertexEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(GRAPH_NAME, COLLECTION_NAME,
			new TestComplexEntity01("Homer", "Simpson", 38), null);
		final VertexEntity<TestComplexEntity01> vertex = driver.graphGetVertex(GRAPH_NAME, COLLECTION_NAME,
			v1.getDocumentKey(), TestComplexEntity01.class, null, null);
		assertThat(vertex.getCode(), is(200));

		// delete
		final DeletedEntity deleted = driver.graphDeleteVertex(GRAPH_NAME, COLLECTION_NAME, v1.getDocumentKey(), null,
			v1.getDocumentRevision(), null);
		assertThat(deleted.getCode(), is(202));
		assertThat(deleted.getDeleted(), is(true));

	}

	@Test
	public void test_delete_vertex_match_ng() throws ArangoException {
		driver.graphCreateVertexCollection(GRAPH_NAME, COLLECTION_NAME);

		final VertexEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(GRAPH_NAME, COLLECTION_NAME,
			new TestComplexEntity01("Homer", "Simpson", 38), null);
		final VertexEntity<TestComplexEntity01> vertex = driver.graphGetVertex(GRAPH_NAME, COLLECTION_NAME,
			v1.getDocumentKey(), TestComplexEntity01.class, null, null);
		assertThat(vertex.getCode(), is(200));

		// delete
		try {
			driver.graphDeleteVertex(GRAPH_NAME, COLLECTION_NAME, v1.getDocumentKey(), null, null,
				v1.getDocumentRevision() + 1);
		} catch (final ArangoException e) {
			assertThat(e.getCode(), is(412));
			assertThat(e.getErrorNumber(), is(1903));
			assertThat(e.getErrorMessage(), is("wrong revision"));
		}
	}

	@Test
	public void graphGetVertexCursorTest() throws ArangoException {

		final TestComplexEntity01 v1 = new TestComplexEntity01("Homer", "A Simpson", 38);
		final TestComplexEntity01 v2 = new TestComplexEntity01("Marge", "A Simpson", 36);
		final TestComplexEntity01 v3 = new TestComplexEntity01("Bart", "A Simpson", 10);
		final TestComplexEntity01 v4 = new TestComplexEntity01("Remoh", "Homer's twin", 38);

		final VertexEntity<TestComplexEntity01> vertex1 = driver.graphCreateVertex(GRAPH_NAME, "from1-1", v1, true);
		final VertexEntity<TestComplexEntity01> vertex2 = driver.graphCreateVertex(GRAPH_NAME, "to1-1", v2, true);
		final VertexEntity<TestComplexEntity01> vertex3 = driver.graphCreateVertex(GRAPH_NAME, "to1-1", v3, true);
		final VertexEntity<TestComplexEntity01> vertex4 = driver.graphCreateVertex(GRAPH_NAME, "from1-1", v4, true);

		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex1.getDocumentHandle(), vertex2.getDocumentHandle(),
			new TestComplexEntity02(1, 2, 3), null);

		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex1.getDocumentHandle(), vertex3.getDocumentHandle(),
			new TestComplexEntity02(4, 5, 6), null);

		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex4.getDocumentHandle(), vertex2.getDocumentHandle(),
			new TestComplexEntity02(7, 8, 9), null);

		driver.graphCreateEdge(GRAPH_NAME, "edge-1", null, vertex4.getDocumentHandle(), vertex3.getDocumentHandle(),
			new TestComplexEntity02(10, 11, 12), null);

		// setCount = true
		final AqlQueryOptions aqlQueryOptions = driver.getDefaultAqlQueryOptions().setCount(true);

		VertexCursor<TestComplexEntity01> vertexCursor = driver.graphGetVertexCursor(GRAPH_NAME,
			TestComplexEntity01.class, null, null, aqlQueryOptions);
		assertEquals(4, vertexCursor.getCount());
		assertEquals(201, vertexCursor.getCode());

		vertexCursor = driver.graphGetVertexCursor(GRAPH_NAME, TestComplexEntity01.class,
			new TestComplexEntity01("Homer", null, null), null, aqlQueryOptions);
		assertEquals(1, vertexCursor.getCount());
		assertEquals(201, vertexCursor.getCode());

		final GraphVerticesOptions graphVerticesOptions = new GraphVerticesOptions();
		graphVerticesOptions.setDirection(Direction.INBOUND);

		vertexCursor = driver.graphGetVertexCursor(GRAPH_NAME, TestComplexEntity01.class, null, graphVerticesOptions,
			aqlQueryOptions);
		assertEquals(2, vertexCursor.getCount());
		assertEquals(201, vertexCursor.getCode());

		final List<String> vertexCollectionRestriction = new ArrayList<String>();
		vertexCollectionRestriction.add("from1-1");
		graphVerticesOptions.setVertexCollectionRestriction(vertexCollectionRestriction);

		vertexCursor = driver.graphGetVertexCursor(GRAPH_NAME, TestComplexEntity01.class, null, graphVerticesOptions,
			aqlQueryOptions);
		assertEquals(0, vertexCursor.getCount());
		assertEquals(201, vertexCursor.getCode());

	}

	@Test
	public void test_replace_vertex_with_document_attributes() throws ArangoException {

		final VertexEntity<TestComplexEntity03> vertex = driver.graphCreateVertex(GRAPH_NAME, "from1-1",
			new TestComplexEntity03("Homer", "Simpson", 38), true);

		assertThat(vertex.getDocumentHandle(), is(notNullValue()));
		assertThat(vertex.getDocumentRevision(), is(not(0L)));
		assertThat(vertex.getDocumentKey(), is(notNullValue()));
		final TestComplexEntity03 en1 = vertex.getEntity();
		assertThat(en1, isA(TestComplexEntity03.class));

		assertThat(en1.getDocumentHandle(), is(notNullValue()));
		assertThat(en1.getDocumentKey(), is(notNullValue()));
		assertThat(en1.getDocumentRevision(), is(notNullValue()));
		final Long rev = en1.getDocumentRevision();

		en1.setUser("Tim");

		final DocumentEntity<TestComplexEntity03> document2 = driver.graphReplaceVertex(GRAPH_NAME, "from1-1",
			en1.getDocumentKey(), en1);
		assertThat(document2.getEntity().getUser(), is("Tim"));
		assertThat(document2.getEntity().getDesc(), is("Simpson"));
		assertThat(document2.getEntity().getAge(), is(38));
		final TestComplexEntity03 en2 = document2.getEntity();
		assertThat(en2.getDocumentHandle(), is(notNullValue()));
		assertThat(en2.getDocumentKey(), is(notNullValue()));
		assertThat(en2.getDocumentRevision(), is(notNullValue()));
		assertThat(en2.getDocumentRevision(), is(not(rev)));
		final Long rev2 = en2.getDocumentRevision();

		final DocumentEntity<TestComplexEntity03> document = driver.getDocument(vertex.getDocumentHandle(),
			TestComplexEntity03.class);
		assertThat(document.getEntity().getUser(), is("Tim"));
		assertThat(document.getEntity().getDesc(), is("Simpson"));
		assertThat(document.getEntity().getAge(), is(38));
		assertThat(document.getDocumentRevision(), is(rev2));
	}

	@Test
	public void test_update_vertex_with_document_attributes() throws ArangoException {

		final VertexEntity<TestComplexEntity03> vertex = driver.graphCreateVertex(GRAPH_NAME, "from1-1",
			new TestComplexEntity03("Homer", "Simpson", 38), true);

		assertThat(vertex.getDocumentHandle(), is(notNullValue()));
		assertThat(vertex.getDocumentRevision(), is(not(0L)));
		assertThat(vertex.getDocumentKey(), is(notNullValue()));
		final Long rev = vertex.getDocumentRevision();

		final TestComplexEntity03 en1 = new TestComplexEntity03("Tim", null, null);

		final DocumentEntity<TestComplexEntity03> document2 = driver.graphUpdateVertex(GRAPH_NAME, "from1-1",
			vertex.getDocumentKey(), en1, true);
		assertThat(document2.getEntity().getUser(), is("Tim"));
		final TestComplexEntity03 en2 = document2.getEntity();
		assertThat(en2.getDocumentHandle(), is(notNullValue()));
		assertThat(en2.getDocumentKey(), is(notNullValue()));
		assertThat(en2.getDocumentRevision(), is(notNullValue()));
		assertThat(en2.getDocumentRevision(), is(not(rev)));
		final Long rev2 = en2.getDocumentRevision();

		final DocumentEntity<TestComplexEntity03> document = driver.getDocument(vertex.getDocumentHandle(),
			TestComplexEntity03.class);
		assertThat(document.getEntity().getUser(), is("Tim"));
		assertThat(document.getEntity().getDesc(), is("Simpson"));
		assertThat(document.getEntity().getAge(), is(38));
		assertThat(document.getDocumentRevision(), is(rev2));
	}
}
