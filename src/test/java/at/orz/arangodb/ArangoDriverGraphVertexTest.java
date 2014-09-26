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

import at.orz.arangodb.entity.DeletedEntity;
import at.orz.arangodb.entity.DocumentEntity;
import at.orz.arangodb.entity.GraphEntity;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ArangoDriverGraphVertexTest extends BaseGraphTest {

	public ArangoDriverGraphVertexTest(ArangoConfigure configure, ArangoDriver driver) {
		super(configure, driver);
	}

	@Test
	public void test_create_vertex() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		
		DocumentEntity<TestComplexEntity01> vertex = driver.createVertex("g1", new TestComplexEntity01("xxx", "yyy", 10), null);
		assertThat(vertex.getDocumentHandle(), is(notNullValue()));
		assertThat(vertex.getDocumentRevision(), is(not(0L)));
		assertThat(vertex.getDocumentKey(), is(notNullValue()));
		assertThat(vertex.getEntity(), isA(TestComplexEntity01.class));
		assertThat(vertex.getEntity().getUser(), is("xxx"));
		assertThat(vertex.getEntity().getDesc(), is("yyy"));
		assertThat(vertex.getEntity().getAge(), is(10));
		
	}

	// TODO: create with _key
	// TODO: create with _key and duplication error
	
	@Test
	public void test_get_vertex() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("xxx", "yyy", 10), null);
		
		DocumentEntity<TestComplexEntity01> vertex = driver.getVertex("g1", v1.getDocumentKey(), TestComplexEntity01.class);
		assertThat(vertex.getCode(), is(200));
		assertThat(vertex.isError(), is(false));
		assertThat(vertex.getDocumentHandle(), is(notNullValue()));
		assertThat(vertex.getDocumentRevision(), is(not(0L)));
		assertThat(vertex.getDocumentKey(), is(notNullValue()));
		assertThat(vertex.getEntity(), isA(TestComplexEntity01.class));
		assertThat(vertex.getEntity().getUser(), is("xxx"));
		assertThat(vertex.getEntity().getDesc(), is("yyy"));
		assertThat(vertex.getEntity().getAge(), is(10));
		
	}

	@Test
	public void test_get_vertex_rev_eq() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("xxx", "yyy", 10), null);
		
		DocumentEntity<TestComplexEntity01> vertex = driver.getVertex("g1", v1.getDocumentKey(), TestComplexEntity01.class, 
				v1.getDocumentRevision(), null, null);
		assertThat(vertex.getCode(), is(200));
		assertThat(vertex.isError(), is(false));
		assertThat(vertex.getDocumentHandle(), is(notNullValue()));
		assertThat(vertex.getDocumentRevision(), is(not(0L)));
		assertThat(vertex.getDocumentKey(), is(notNullValue()));
		assertThat(vertex.getEntity(), isA(TestComplexEntity01.class));
		assertThat(vertex.getEntity().getUser(), is("xxx"));
		assertThat(vertex.getEntity().getDesc(), is("yyy"));
		assertThat(vertex.getEntity().getAge(), is(10));
		
	}

	@Test
	public void test_get_vertex_rev_ne() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("xxx", "yyy", 10), null);
		
		try {
			driver.getVertex("g1", v1.getDocumentKey(), TestComplexEntity01.class, 
					v1.getDocumentRevision() -1, null, null);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(412));
			assertThat(e.getErrorNumber(), is(1903)); // wrong revision
		}
		
	}

	@Test
	public void test_get_vertex_none_match_eq() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("xxx", "yyy", 10), null);
		
		DocumentEntity<TestComplexEntity01> vertex = driver.getVertex("g1", v1.getDocumentKey(), TestComplexEntity01.class, 
				null, v1.getDocumentRevision(), null);
		
		assertThat(vertex.getStatusCode(), is(304));
		assertThat(vertex.isNotModified(), is(true));
		
	}

	@Test
	public void test_get_vertex_none_match_ne() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("xxx", "yyy", 10), null);
		
		DocumentEntity<TestComplexEntity01> vertex = driver.getVertex("g1", v1.getDocumentKey(), TestComplexEntity01.class, 
				null, v1.getDocumentRevision() + 1, null);
		
		assertThat(vertex.getCode(), is(200));
		assertThat(vertex.isError(), is(false));
		assertThat(vertex.getDocumentHandle(), is(notNullValue()));
		assertThat(vertex.getDocumentRevision(), is(not(0L)));
		assertThat(vertex.getDocumentKey(), is(notNullValue()));
		assertThat(vertex.getEntity(), isA(TestComplexEntity01.class));
		assertThat(vertex.getEntity().getUser(), is("xxx"));
		assertThat(vertex.getEntity().getDesc(), is("yyy"));
		assertThat(vertex.getEntity().getAge(), is(10));
		
	}

	
	@Test
	public void test_get_vertex_match_eq() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("xxx", "yyy", 10), null);
		
		DocumentEntity<TestComplexEntity01> vertex = driver.getVertex("g1", v1.getDocumentKey(), TestComplexEntity01.class, 
				null, null, v1.getDocumentRevision());
		
		assertThat(vertex.getCode(), is(200));
		assertThat(vertex.isError(), is(false));
		assertThat(vertex.getDocumentHandle(), is(notNullValue()));
		assertThat(vertex.getDocumentRevision(), is(not(0L)));
		assertThat(vertex.getDocumentKey(), is(notNullValue()));
		assertThat(vertex.getEntity(), isA(TestComplexEntity01.class));
		assertThat(vertex.getEntity().getUser(), is("xxx"));
		assertThat(vertex.getEntity().getDesc(), is("yyy"));
		assertThat(vertex.getEntity().getAge(), is(10));
		
	}

	@Test
	public void test_get_vertex_match_ne() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("xxx", "yyy", 10), null);
		
		try {
			driver.getVertex("g1", v1.getDocumentKey(), TestComplexEntity01.class, 
				null, null, v1.getDocumentRevision() + 1);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(412));
			assertThat(e.getErrorNumber(), is(1903));
		}
		
	}

	@Test
	public void test_get_vertex_graph_not_found() throws ArangoException {

		try {
			driver.getVertex("g1", "gkey1", TestComplexEntity01.class);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorNumber(), is(1901));
		}
		
	}

	@Test
	public void test_get_vertex_not_found() throws ArangoException {

		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);

		try {
			driver.createVertex("g2", new TestComplexEntity01("xxx", "yyy", 10), null);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorNumber(), is(1901));
		}
		
	}
	
	@Test
	public void test_delete_vertex() throws ArangoException {

		// create graph
		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		// create vertex
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("xxx", "yyy", 10), null);
		// check exists vertex
		DocumentEntity<TestComplexEntity01> vertex = driver.getVertex("g1", v1.getDocumentKey(), TestComplexEntity01.class, 
				null, null, null);
		assertThat(vertex.getCode(), is(200));

		// delete
		DeletedEntity deleted = driver.deleteVertex("g1", v1.getDocumentKey(), true, null, null);
		assertThat(deleted.getCode(), is(200));
		assertThat(deleted.getDeleted(), is(true));

	}

	@Test
	public void test_delete_vertex_graph_not_found() throws ArangoException {

		try {
			DeletedEntity deleted = driver.deleteVertex("g2", "key", true, null, null);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorNumber(), is(1901));
			assertThat(e.getErrorMessage(), startsWith("no graph named"));
		}

	}

	@Test
	public void test_delete_vertex_not_found() throws ArangoException {

		// create graph
		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);

		try {
			DeletedEntity deleted = driver.deleteVertex("g1", "key", true, null, null);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorNumber(), is(1903));
			assertThat(e.getErrorMessage(), startsWith("no vertex found for"));
		}

	}

	@Test
	public void test_delete_vertex_rev_eq() throws ArangoException {

		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("xxx", "yyy", 10), null);
		DocumentEntity<TestComplexEntity01> vertex = driver.getVertex("g1", v1.getDocumentKey(), TestComplexEntity01.class, 
				null, null, null);
		assertThat(vertex.getCode(), is(200));

		// delete
		DeletedEntity deleted = driver.deleteVertex("g1", v1.getDocumentKey(), null, v1.getDocumentRevision(), null);
		assertThat(deleted.getCode(), is(202));
		assertThat(deleted.getDeleted(), is(true));

	}

	@Test
	public void test_delete_vertex_rev_ng() throws ArangoException {

		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("xxx", "yyy", 10), null);
		DocumentEntity<TestComplexEntity01> vertex = driver.getVertex("g1", v1.getDocumentKey(), TestComplexEntity01.class, 
				null, null, null);
		assertThat(vertex.getCode(), is(200));

		// delete
		try {
			driver.deleteVertex("g1", v1.getDocumentKey(), null, v1.getDocumentRevision() + 1, null);
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(412));
			assertThat(e.getErrorNumber(), is(1903));
			assertThat(e.getErrorMessage(), is("wrong revision"));
		}

	}

	@Test
	public void test_delete_vertex_match_eq() throws ArangoException {

		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("xxx", "yyy", 10), null);
		DocumentEntity<TestComplexEntity01> vertex = driver.getVertex("g1", v1.getDocumentKey(), TestComplexEntity01.class, 
				null, null, null);
		assertThat(vertex.getCode(), is(200));

		// delete
		DeletedEntity deleted = driver.deleteVertex("g1", v1.getDocumentKey(), null, null, v1.getDocumentRevision());
		assertThat(deleted.getCode(), is(202));
		assertThat(deleted.getDeleted(), is(true));

	}

	@Test
	public void test_delete_vertex_match_ng() throws ArangoException {

		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("xxx", "yyy", 10), null);
		DocumentEntity<TestComplexEntity01> vertex = driver.getVertex("g1", v1.getDocumentKey(), TestComplexEntity01.class, 
				null, null, null);
		assertThat(vertex.getCode(), is(200));

		// delete
		try {
			driver.deleteVertex("g1", v1.getDocumentKey(), null, null, v1.getDocumentRevision() + 1);
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(412));
			assertThat(e.getErrorNumber(), is(1903));
			assertThat(e.getErrorMessage(), is("wrong revision"));
		}

	}

	
	@Test
	public void test_vertex_update() throws ArangoException {

		// create graph
		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		// create vertex
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("xxx", "yyy", 10), null);
		// check exists vertex
		DocumentEntity<TestComplexEntity01> vertex = driver.getVertex("g1", v1.getDocumentKey(), TestComplexEntity01.class, 
				null, null, null);
		assertThat(vertex.getCode(), is(200));
		
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

	
	
}
