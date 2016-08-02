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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.marker.VertexEntity;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * @author gschwab
 */
public class ArangoDriverGraphVertexUpdateTest extends BaseGraphTest {

	private final String graphName = "UnitTestGraph";
	private final String collectionName = "UnitTestCollection";

	@Test
	public void test_vertex_update_keep_null() throws ArangoException {

		// create graph
		driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
		// create vertex collection
		driver.graphCreateVertexCollection(this.graphName, this.collectionName);
		// create vertex
		final VertexEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(this.graphName, this.collectionName,
			new TestComplexEntity01("Homa", "Simpson", 83), null);
		// check exists vertex
		final VertexEntity<TestComplexEntity01> vertex = driver.graphGetVertex(this.graphName, this.collectionName,
			v1.getDocumentKey(), TestComplexEntity01.class, null, null);
		assertThat(vertex.getCode(), is(200));

		// update
		VertexEntity<TestComplexEntity01> updatedVertex = driver.graphUpdateVertex(this.graphName, this.collectionName,
			vertex.getDocumentKey(), new TestComplexEntity01("Homer", null, 38), true);
		assertThat(updatedVertex.getCode(), is(202));
		assertThat(updatedVertex.isError(), is(false));

		assertThat(updatedVertex.getDocumentHandle(), is(v1.getDocumentHandle()));
		assertThat(updatedVertex.getDocumentRevision(), is(not(v1.getDocumentRevision())));
		assertThat(updatedVertex.getDocumentRevision(), is(notNullValue()));
		assertThat(updatedVertex.getDocumentKey(), is(v1.getDocumentKey()));

		updatedVertex = driver.graphGetVertex(this.graphName, this.collectionName, vertex.getDocumentKey(),
			TestComplexEntity01.class);

		assertThat(updatedVertex.getEntity().getUser(), is("Homer"));
		assertThat(updatedVertex.getEntity().getDesc(), is("Simpson"));
		assertThat(updatedVertex.getEntity().getAge(), is(38));

		// check count
		assertThat(driver.getCollectionCount(this.collectionName).getCount(), is(1L));

	}

	@Test
	public void test_vertex_update_keep_null_false() throws ArangoException {

		// create graph
		driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
		// create vertex collection
		driver.graphCreateVertexCollection(this.graphName, this.collectionName);
		// create vertex
		final VertexEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(this.graphName, this.collectionName,
			new TestComplexEntity01("Homer", "A Sompsin", 83), null);
		// check exists vertex
		final VertexEntity<TestComplexEntity01> vertex = driver.graphGetVertex(this.graphName, this.collectionName,
			v1.getDocumentKey(), TestComplexEntity01.class, null, null);
		assertThat(vertex.getCode(), is(200));

		// update
		VertexEntity<TestComplexEntity01> updatedVertex = driver.graphUpdateVertex(this.graphName, this.collectionName,
			vertex.getDocumentKey(), new TestComplexEntity01("Homer", null, 38), false);
		assertThat(updatedVertex.getCode(), is(202));
		assertThat(updatedVertex.isError(), is(false));

		assertThat(updatedVertex.getDocumentHandle(), is(v1.getDocumentHandle()));
		assertThat(updatedVertex.getDocumentRevision(), is(not(v1.getDocumentRevision())));
		assertThat(updatedVertex.getDocumentRevision(), is(notNullValue()));
		assertThat(updatedVertex.getDocumentKey(), is(v1.getDocumentKey()));

		updatedVertex = driver.graphGetVertex(this.graphName, this.collectionName, vertex.getDocumentKey(),
			TestComplexEntity01.class);

		assertThat(updatedVertex.getEntity().getUser(), is("Homer"));
		assertThat(updatedVertex.getEntity().getDesc(), is(nullValue()));
		assertThat(updatedVertex.getEntity().getAge(), is(38));

		// check count
		assertThat(driver.getCollectionCount(this.collectionName).getCount(), is(1L));

	}

	@Test
	public void test_vertex_update_graph_not_found() throws ArangoException {

		// update
		try {
			driver.graphUpdateVertex(this.graphName, this.collectionName, "xx",
				new TestComplexEntity01("zzz", null, 99), true);
			fail();
		} catch (final ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorMessage(), startsWith("graph not found"));
		}

	}

	@Test
	public void test_vertex_update_collection_not_found() throws ArangoException {

		// create graph
		driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);

		// update
		try {
			driver.graphUpdateVertex(this.graphName, this.collectionName, "xx",
				new TestComplexEntity01("zzz", null, 99), true);
			fail();
		} catch (final ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorMessage(), startsWith("collection not found"));
		}

	}

	@Test
	public void test_vertex_update_rev_eq() throws ArangoException {

		// create graph
		driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
		// create vertex collection
		driver.graphCreateVertexCollection(this.graphName, this.collectionName);
		// create vertex
		final VertexEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(this.graphName, this.collectionName,
			new TestComplexEntity01("xxx", "yyy", 83), null);
		// check exists vertex
		final VertexEntity<TestComplexEntity01> vertex = driver.graphGetVertex(this.graphName, this.collectionName,
			v1.getDocumentKey(), TestComplexEntity01.class, null, null);
		assertThat(vertex.getCode(), is(200));

		// update
		final String rev = vertex.getDocumentRevision();
		DocumentEntity<TestComplexEntity01> updatedVertex = driver.graphUpdateVertex(this.graphName,
			this.collectionName, vertex.getDocumentKey(), new TestComplexEntity01("zzz", null, 99), null, true, rev,
			null);
		assertThat(updatedVertex.getCode(), is(200));
		assertThat(updatedVertex.isError(), is(false));

		assertThat(updatedVertex.getDocumentHandle(), is(v1.getDocumentHandle()));
		assertThat(updatedVertex.getDocumentRevision(), is(not(v1.getDocumentRevision())));
		assertThat(updatedVertex.getDocumentRevision(), is(notNullValue()));
		assertThat(updatedVertex.getDocumentKey(), is(v1.getDocumentKey()));

		updatedVertex = driver.graphGetVertex(this.graphName, this.collectionName, vertex.getDocumentKey(),
			TestComplexEntity01.class);

		assertThat(updatedVertex.getEntity().getUser(), is("zzz"));
		assertThat(updatedVertex.getEntity().getDesc(), is("yyy"));
		assertThat(updatedVertex.getEntity().getAge(), is(99));

		// check count
		assertThat(driver.getCollectionCount(this.collectionName).getCount(), is(1L));

	}

	@Test
	public void test_vertex_update_rev_ne() throws ArangoException {

		// create graph
		driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
		// create vertex collection
		driver.graphCreateVertexCollection(this.graphName, this.collectionName);
		// create vertex
		final VertexEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(this.graphName, this.collectionName,
			new TestComplexEntity01("xxx", "yyy", 83), null);
		// check exists vertex
		final VertexEntity<TestComplexEntity01> vertex = driver.graphGetVertex(this.graphName, this.collectionName,
			v1.getDocumentKey(), TestComplexEntity01.class, null, null);
		assertThat(vertex.getCode(), is(200));

		// update
		try {
			final String rev = vertex.getDocumentRevision() + 1;
			driver.graphUpdateVertex(this.graphName, this.collectionName, vertex.getDocumentKey(),
				new TestComplexEntity01("zzz", null, 99), null, true, rev, null);
			fail();
		} catch (final ArangoException e) {
			assertThat(e.getCode(), is(412));
			assertThat(e.getErrorNumber(), is(1903));
			assertThat(e.getErrorMessage(), is("wrong revision"));
		}

	}

	@Test
	public void test_vertex_update_ifmatch_eq() throws ArangoException {

		// create graph
		driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
		// create vertex collection
		driver.graphCreateVertexCollection(this.graphName, this.collectionName);
		// create vertex
		final VertexEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(this.graphName, this.collectionName,
			new TestComplexEntity01("xxx", "yyy", 83), null);
		// check exists vertex
		final VertexEntity<TestComplexEntity01> vertex = driver.graphGetVertex(this.graphName, this.collectionName,
			v1.getDocumentKey(), TestComplexEntity01.class, null, null);
		assertThat(vertex.getCode(), is(200));

		// update
		final String rev = vertex.getDocumentRevision();
		VertexEntity<TestComplexEntity01> updatedVertex = driver.graphUpdateVertex(this.graphName, this.collectionName,
			vertex.getDocumentKey(), new TestComplexEntity01("zzz", null, 99), null, true, rev, null);
		assertThat(updatedVertex.getCode(), is(200));
		assertThat(updatedVertex.isError(), is(false));

		assertThat(updatedVertex.getDocumentHandle(), is(v1.getDocumentHandle()));
		assertThat(updatedVertex.getDocumentRevision(), is(not(v1.getDocumentRevision())));
		assertThat(updatedVertex.getDocumentRevision(), is(notNullValue()));
		assertThat(updatedVertex.getDocumentKey(), is(v1.getDocumentKey()));

		updatedVertex = driver.graphGetVertex(this.graphName, this.collectionName, vertex.getDocumentKey(),
			TestComplexEntity01.class);

		assertThat(updatedVertex.getEntity().getUser(), is("zzz"));
		assertThat(updatedVertex.getEntity().getDesc(), is("yyy"));
		assertThat(updatedVertex.getEntity().getAge(), is(99));

		// check count
		assertThat(driver.getCollectionCount(this.collectionName).getCount(), is(1L));

	}

	@Test
	public void test_vertex_update_ifmatch_ne() throws ArangoException {

		// create graph
		driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
		// create vertex collection
		driver.graphCreateVertexCollection(this.graphName, this.collectionName);
		// create vertex
		final VertexEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(this.graphName, this.collectionName,
			new TestComplexEntity01("Homer", "A Sompsin", 83), null);
		// check exists vertex
		final VertexEntity<TestComplexEntity01> vertex = driver.graphGetVertex(this.graphName, this.collectionName,
			v1.getDocumentKey(), TestComplexEntity01.class, null, null);
		assertThat(vertex.getCode(), is(200));

		// update
		try {
			final String rev = vertex.getDocumentRevision() + 1;
			driver.graphUpdateVertex(this.graphName, this.collectionName, vertex.getDocumentKey(),
				new TestComplexEntity01("zzz", null, 99), null, true, rev, null);
			fail();
		} catch (final ArangoException e) {
			assertThat(e.getCode(), is(412));
			assertThat(e.getErrorNumber(), is(1903));
			assertThat(e.getErrorMessage(), is("wrong revision"));
		}

	}

}
