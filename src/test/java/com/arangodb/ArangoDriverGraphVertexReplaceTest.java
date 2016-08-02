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
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.marker.VertexEntity;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * @author gschwab
 *
 */
public class ArangoDriverGraphVertexReplaceTest extends BaseGraphTest {

	private final String graphName = "UnitTestGraph";
	private final String collectionName = "UnitTestCollection";

	@Test
	public void test_vertex_replace() throws ArangoException {

		// create graph
		driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
		// create vertex collection
		driver.graphCreateVertexCollection(this.graphName, this.collectionName);
		// create vertex
		final VertexEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(this.graphName, this.collectionName,
			new TestComplexEntity01("Homer", "Simpson", 38), null);
		// check exists vertex
		final VertexEntity<TestComplexEntity01> vertex = driver.graphGetVertex(this.graphName, this.collectionName,
			v1.getDocumentKey(), TestComplexEntity01.class, null, null);
		assertThat(vertex.getCode(), is(200));

		// replace
		final DocumentEntity<TestComplexEntity02> updatedVertex = driver.graphReplaceVertex(this.graphName,
			this.collectionName, v1.getDocumentKey(), new TestComplexEntity02(1, 2, 3));
		assertThat(updatedVertex.getCode(), is(202));
		assertThat(updatedVertex.isError(), is(false));

		assertThat(updatedVertex.getDocumentHandle(), is(v1.getDocumentHandle()));
		assertThat(updatedVertex.getDocumentRevision(), is(not(v1.getDocumentRevision())));
		assertThat(updatedVertex.getDocumentRevision(), is(notNullValue()));
		assertThat(updatedVertex.getDocumentKey(), is(v1.getDocumentKey()));

		final DocumentEntity<TestComplexEntity02> updatedVertex2 = driver.graphGetVertex(this.graphName,
			this.collectionName, v1.getDocumentKey(), TestComplexEntity02.class);
		assertThat(updatedVertex2.getEntity().getX(), is(1));
		assertThat(updatedVertex2.getEntity().getY(), is(2));
		assertThat(updatedVertex2.getEntity().getZ(), is(3));

		// check count
		assertThat(driver.getCollectionCount(this.collectionName).getCount(), is(1L));

	}

	@Test
	public void test_vertex_replace_graph_not_found() throws ArangoException {

		try {
			driver.graphReplaceVertex(this.graphName, this.collectionName, "key1", new TestComplexEntity02(1, 2, 3));
			fail();
		} catch (final ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorMessage(), is("graph not found"));
		}

	}

	@Test
	public void test_vertex_replace_vertex_not_found() throws ArangoException {

		// create graph
		driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
		// create vertex collection
		driver.graphCreateVertexCollection(this.graphName, this.collectionName);
		// replace
		try {
			driver.graphReplaceVertex(this.graphName, this.collectionName, "key1", new TestComplexEntity02(1, 2, 3));
			fail();
		} catch (final ArangoException e) {
			assertThat(e.getCode(), is(404));
		}

	}

	@Test
	public void test_vertex_replace_rev_eq() throws ArangoException {

		// create graph
		driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
		// create vertex collection
		driver.graphCreateVertexCollection(this.graphName, this.collectionName);
		// create vertex
		final VertexEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(this.graphName, this.collectionName,
			new TestComplexEntity01("Homer", "Simpson", 38), null);
		// check exists vertex
		final VertexEntity<TestComplexEntity01> vertex = driver.graphGetVertex(this.graphName, this.collectionName,
			v1.getDocumentKey(), TestComplexEntity01.class, null, null);
		assertThat(vertex.getCode(), is(200));

		// replace
		final VertexEntity<TestComplexEntity02> updatedVertex = driver.graphReplaceVertex(this.graphName,
			this.collectionName, v1.getDocumentKey(), new TestComplexEntity02(1, 2, 3));
		assertThat(updatedVertex.getCode(), is(202));
		assertThat(updatedVertex.isError(), is(false));

		assertThat(updatedVertex.getDocumentHandle(), is(v1.getDocumentHandle()));
		assertThat(updatedVertex.getDocumentRevision(), is(not(v1.getDocumentRevision())));
		assertThat(updatedVertex.getDocumentRevision(), is(notNullValue()));
		assertThat(updatedVertex.getDocumentKey(), is(v1.getDocumentKey()));

		final VertexEntity<TestComplexEntity02> updatedVertex2 = driver.graphGetVertex(this.graphName,
			this.collectionName, v1.getDocumentKey(), TestComplexEntity02.class);
		assertThat(updatedVertex2.getEntity().getX(), is(1));
		assertThat(updatedVertex2.getEntity().getY(), is(2));
		assertThat(updatedVertex2.getEntity().getZ(), is(3));

		// check count
		assertThat(driver.getCollectionCount(this.collectionName).getCount(), is(1L));

	}

	@Test
	public void test_vertex_replace_rev_ne() throws ArangoException {

		// create graph
		driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
		// create vertex collection
		driver.graphCreateVertexCollection(this.graphName, this.collectionName);
		// create vertex
		final VertexEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(this.graphName, this.collectionName,
			new TestComplexEntity01("Homer", "Simpson", 38), null);
		// check exists vertex
		final VertexEntity<TestComplexEntity01> vertex = driver.graphGetVertex(this.graphName, this.collectionName,
			v1.getDocumentKey(), TestComplexEntity01.class, null, null);
		assertThat(vertex.getCode(), is(200));

		// replace
		try {
			final String rev = "1";
			driver.graphReplaceVertex(this.graphName, this.collectionName, v1.getDocumentKey(),
				new TestComplexEntity02(1, 2, 3), true, rev, null);
			fail();
		} catch (final ArangoException e) {
			assertThat(e.getCode(), is(412));
			assertThat(e.getErrorNumber(), is(1903));
			assertThat(e.getErrorMessage(), is("wrong revision"));
		}

	}

	@Test
	public void test_vertex_replace_ifmatch_eq() throws ArangoException {

		// create graph
		driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
		// create vertex collection
		driver.graphCreateVertexCollection(this.graphName, this.collectionName);
		// create vertex
		final VertexEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(this.graphName, this.collectionName,
			new TestComplexEntity01("Homer", "Simpson", 38), null);
		// check exists vertex
		final VertexEntity<TestComplexEntity01> vertex = driver.graphGetVertex(this.graphName, this.collectionName,
			v1.getDocumentKey(), TestComplexEntity01.class, null, null);
		assertThat(vertex.getCode(), is(200));

		// replace
		final String rev = vertex.getDocumentRevision();
		final VertexEntity<TestComplexEntity02> updatedVertex = driver.graphReplaceVertex(this.graphName,
			this.collectionName, v1.getDocumentKey(), new TestComplexEntity02(1, 2, 3), true, rev, null);
		assertThat(updatedVertex.getCode(), is(200));
		assertThat(updatedVertex.isError(), is(false));

		assertThat(updatedVertex.getDocumentHandle(), is(v1.getDocumentHandle()));
		assertThat(updatedVertex.getDocumentRevision(), is(not(v1.getDocumentRevision())));
		assertThat(updatedVertex.getDocumentRevision(), is(notNullValue()));
		assertThat(updatedVertex.getDocumentKey(), is(v1.getDocumentKey()));

		final DocumentEntity<TestComplexEntity02> updatedVertex2 = driver.graphGetVertex(this.graphName,
			this.collectionName, v1.getDocumentKey(), TestComplexEntity02.class);
		assertThat(updatedVertex2.getEntity().getX(), is(1));
		assertThat(updatedVertex2.getEntity().getY(), is(2));
		assertThat(updatedVertex2.getEntity().getZ(), is(3));

		// check count
		assertThat(driver.getCollectionCount(this.collectionName).getCount(), is(1L));

	}

	@Test
	public void test_vertex_replace_ifmatch_ne() throws ArangoException {

		// create graph
		driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
		// create vertex collection
		driver.graphCreateVertexCollection(this.graphName, this.collectionName);
		// create vertex
		final VertexEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(this.graphName, this.collectionName,
			new TestComplexEntity01("xxx", "yyy", 10), null);
		// check exists vertex
		final VertexEntity<TestComplexEntity01> vertex = driver.graphGetVertex(this.graphName, this.collectionName,
			v1.getDocumentKey(), TestComplexEntity01.class, null, null);
		assertThat(vertex.getCode(), is(200));

		// replace
		try {
			final String rev = "1";
			driver.graphReplaceVertex(this.graphName, this.collectionName, v1.getDocumentKey(),
				new TestComplexEntity02(1, 2, 3), true, rev, null);
			fail();
		} catch (final ArangoException e) {
			assertThat(e.getCode(), is(412));
			assertThat(e.getErrorNumber(), is(1903));
			assertThat(e.getErrorMessage(), is("wrong revision"));
		}

	}
}
