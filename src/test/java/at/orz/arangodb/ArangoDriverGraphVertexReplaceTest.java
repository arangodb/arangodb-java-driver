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

package at.orz.arangodb;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import at.orz.arangodb.entity.DocumentEntity;
import at.orz.arangodb.entity.GraphEntity;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ArangoDriverGraphVertexReplaceTest extends BaseGraphTest {

	public ArangoDriverGraphVertexReplaceTest(ArangoConfigure configure, ArangoDriver driver) {
		super(configure, driver);
	}
	
	@Test
	public void test_vertex_replace() throws ArangoException {

		// create graph
		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		// create vertex
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("xxx", "yyy", 10), null);
		// check exists vertex
		DocumentEntity<TestComplexEntity01> vertex = driver.getVertex("g1", v1.getDocumentKey(), TestComplexEntity01.class, 
				null, null, null);
		assertThat(vertex.getCode(), is(200));
		
		// replace
		DocumentEntity<TestComplexEntity02> updatedVertex = driver.replaceVertex("g1", v1.getDocumentKey(), new TestComplexEntity02(1,2,3));
		assertThat(updatedVertex.getCode(), is(202));
		assertThat(updatedVertex.isError(), is(false));
		
		assertThat(updatedVertex.getDocumentHandle(), is(v1.getDocumentHandle()));
		assertThat(updatedVertex.getDocumentRevision(), is(not(v1.getDocumentRevision())));
		assertThat(updatedVertex.getDocumentRevision(), is(not(0L)));
		assertThat(updatedVertex.getDocumentKey(), is(v1.getDocumentKey()));
		
		assertThat(updatedVertex.getEntity().getX(), is(1));
		assertThat(updatedVertex.getEntity().getY(), is(2));
		assertThat(updatedVertex.getEntity().getZ(), is(3));
		
		// check count
		assertThat(driver.getCollectionCount("v1").getCount(), is(1L));
		
	}

	@Test
	public void test_vertex_replace_graph_not_found() throws ArangoException {
		
		try {
			DocumentEntity<TestComplexEntity02> updatedVertex = driver.replaceVertex("g1", "key1", new TestComplexEntity02(1,2,3));
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorNumber(), is(1901));
			assertThat(e.getErrorMessage(), startsWith("no graph named"));
		}
		
	}

	@Test
	public void test_vertex_replace_vertex_not_found() throws ArangoException {

		// create graph
		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		// replace
		try {
			DocumentEntity<TestComplexEntity02> updatedVertex = driver.replaceVertex("g1", "key1", new TestComplexEntity02(1,2,3));
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorNumber(), is(1905));
			assertThat(e.getErrorMessage(), startsWith("no vertex found for"));
		}
		
	}
	
	@Test
	public void test_vertex_replace_rev_eq() throws ArangoException {

		// create graph
		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		// create vertex
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("xxx", "yyy", 10), null);
		// check exists vertex
		DocumentEntity<TestComplexEntity01> vertex = driver.getVertex("g1", v1.getDocumentKey(), TestComplexEntity01.class, 
				null, null, null);
		assertThat(vertex.getCode(), is(200));
		
		
		// replace
		Long rev = vertex.getDocumentRevision();
		DocumentEntity<TestComplexEntity02> updatedVertex = driver.replaceVertex("g1", v1.getDocumentKey(), new TestComplexEntity02(1,2,3), true,
				rev, null);
		assertThat(updatedVertex.getCode(), is(201));
		assertThat(updatedVertex.isError(), is(false));
		
		assertThat(updatedVertex.getDocumentHandle(), is(v1.getDocumentHandle()));
		assertThat(updatedVertex.getDocumentRevision(), is(not(v1.getDocumentRevision())));
		assertThat(updatedVertex.getDocumentRevision(), is(not(0L)));
		assertThat(updatedVertex.getDocumentKey(), is(v1.getDocumentKey()));
		
		assertThat(updatedVertex.getEntity().getX(), is(1));
		assertThat(updatedVertex.getEntity().getY(), is(2));
		assertThat(updatedVertex.getEntity().getZ(), is(3));
		
		// check count
		assertThat(driver.getCollectionCount("v1").getCount(), is(1L));
		
	}

	
	@Test
	public void test_vertex_replace_rev_ne() throws ArangoException {

		// create graph
		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		// create vertex
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("xxx", "yyy", 10), null);
		// check exists vertex
		DocumentEntity<TestComplexEntity01> vertex = driver.getVertex("g1", v1.getDocumentKey(), TestComplexEntity01.class, 
				null, null, null);
		assertThat(vertex.getCode(), is(200));
		
		
		// replace
		try {
			Long rev = 1L;
			DocumentEntity<TestComplexEntity02> updatedVertex = driver.replaceVertex("g1", v1.getDocumentKey(), new TestComplexEntity02(1,2,3), true,
					rev, null);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(412));
			assertThat(e.getErrorNumber(), is(1903));
			assertThat(e.getErrorMessage(), is("wrong revision"));
		}
		
	}


	@Test
	public void test_vertex_replace_ifmatch_eq() throws ArangoException {

		// create graph
		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		// create vertex
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("xxx", "yyy", 10), null);
		// check exists vertex
		DocumentEntity<TestComplexEntity01> vertex = driver.getVertex("g1", v1.getDocumentKey(), TestComplexEntity01.class, 
				null, null, null);
		assertThat(vertex.getCode(), is(200));
		
		
		// replace
		Long rev = vertex.getDocumentRevision();
		DocumentEntity<TestComplexEntity02> updatedVertex = driver.replaceVertex("g1", v1.getDocumentKey(), new TestComplexEntity02(1,2,3), true,
				null, rev);
		assertThat(updatedVertex.getCode(), is(201));
		assertThat(updatedVertex.isError(), is(false));
		
		assertThat(updatedVertex.getDocumentHandle(), is(v1.getDocumentHandle()));
		assertThat(updatedVertex.getDocumentRevision(), is(not(v1.getDocumentRevision())));
		assertThat(updatedVertex.getDocumentRevision(), is(not(0L)));
		assertThat(updatedVertex.getDocumentKey(), is(v1.getDocumentKey()));
		
		assertThat(updatedVertex.getEntity().getX(), is(1));
		assertThat(updatedVertex.getEntity().getY(), is(2));
		assertThat(updatedVertex.getEntity().getZ(), is(3));
		
		// check count
		assertThat(driver.getCollectionCount("v1").getCount(), is(1L));
		
	}

	@Test
	public void test_vertex_replace_ifmatch_ne() throws ArangoException {

		// create graph
		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		// create vertex
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("xxx", "yyy", 10), null);
		// check exists vertex
		DocumentEntity<TestComplexEntity01> vertex = driver.getVertex("g1", v1.getDocumentKey(), TestComplexEntity01.class, 
				null, null, null);
		assertThat(vertex.getCode(), is(200));
		
		
		// replace
		try {
			Long rev = 1L;
			DocumentEntity<TestComplexEntity02> updatedVertex = driver.replaceVertex("g1", v1.getDocumentKey(), new TestComplexEntity02(1,2,3), true,
					null, rev);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(412));
			assertThat(e.getErrorNumber(), is(1903));
			assertThat(e.getErrorMessage(), is("wrong revision"));
		}
		
	}
}
