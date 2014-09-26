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
public class ArangoDriverGraphVertexUpdateTest extends BaseGraphTest {

	public ArangoDriverGraphVertexUpdateTest(ArangoConfigure configure, ArangoDriver driver) {
		super(configure, driver);
	}
	
	@Test
	public void test_vertex_update_keeknull() throws ArangoException {

		// create graph
		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		// create vertex
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("xxx", "yyy", 10), null);
		// check exists vertex
		DocumentEntity<TestComplexEntity01> vertex = driver.getVertex("g1", v1.getDocumentKey(), TestComplexEntity01.class, 
				null, null, null);
		assertThat(vertex.getCode(), is(200));
		
		// update
		DocumentEntity<TestComplexEntity01> updatedVertex = driver.updateVertex("g1", vertex.getDocumentKey(), new TestComplexEntity01("zzz", null, 99), true);
		assertThat(updatedVertex.getCode(), is(202));
		assertThat(updatedVertex.isError(), is(false));
		
		assertThat(updatedVertex.getDocumentHandle(), is(v1.getDocumentHandle()));
		assertThat(updatedVertex.getDocumentRevision(), is(not(v1.getDocumentRevision())));
		assertThat(updatedVertex.getDocumentRevision(), is(not(0L)));
		assertThat(updatedVertex.getDocumentKey(), is(v1.getDocumentKey()));
		
		assertThat(updatedVertex.getEntity().getUser(), is("zzz"));
		assertThat(updatedVertex.getEntity().getDesc(), is("yyy"));
		assertThat(updatedVertex.getEntity().getAge(), is(99));
		
		// check count
		//assertThat(driver.getCollectionCount("v1").getCount(), is(1L));
		
	}

	@Test
	public void test_vertex_update_keeknull_false() throws ArangoException {

		// create graph
		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		// create vertex
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("xxx", "yyy", 10), null);
		// check exists vertex
		DocumentEntity<TestComplexEntity01> vertex = driver.getVertex("g1", v1.getDocumentKey(), TestComplexEntity01.class, 
				null, null, null);
		assertThat(vertex.getCode(), is(200));
		
		// update
		DocumentEntity<TestComplexEntity01> updatedVertex = driver.updateVertex("g1", vertex.getDocumentKey(), new TestComplexEntity01("zzz", null, 99), false);
		assertThat(updatedVertex.getCode(), is(202));
		assertThat(updatedVertex.isError(), is(false));
		
		assertThat(updatedVertex.getDocumentHandle(), is(v1.getDocumentHandle()));
		assertThat(updatedVertex.getDocumentRevision(), is(not(v1.getDocumentRevision())));
		assertThat(updatedVertex.getDocumentRevision(), is(not(0L)));
		assertThat(updatedVertex.getDocumentKey(), is(v1.getDocumentKey()));
		
		assertThat(updatedVertex.getEntity().getUser(), is("zzz"));
		assertThat(updatedVertex.getEntity().getDesc(), is(nullValue()));
		assertThat(updatedVertex.getEntity().getAge(), is(99));
		
		// check count
		assertThat(driver.getCollectionCount("v1").getCount(), is(1L));
		
	}

	@Test
	public void test_vertex_update_graph_not_found() throws ArangoException {

		// update
		try {
			driver.updateVertex("g1", "xx", new TestComplexEntity01("zzz", null, 99), true);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorNumber(), is(1901));
			assertThat(e.getErrorMessage(), startsWith("no graph named"));
		}
		
	}

	@Test
	public void test_vertex_update_vertex_not_found() throws ArangoException {

		// create graph
		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);

		// update
		try {
			driver.updateVertex("g1", "xx", new TestComplexEntity01("zzz", null, 99), true);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorNumber(), is(1905));
			assertThat(e.getErrorMessage(), startsWith("no vertex found for:"));
		}
		
	}

	@Test
	public void test_vertex_update_rev_eq() throws ArangoException {

		// create graph
		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		// create vertex
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("xxx", "yyy", 10), null);
		// check exists vertex
		DocumentEntity<TestComplexEntity01> vertex = driver.getVertex("g1", v1.getDocumentKey(), TestComplexEntity01.class, 
				null, null, null);
		assertThat(vertex.getCode(), is(200));
		
		// update
		Long rev = vertex.getDocumentRevision();
		DocumentEntity<TestComplexEntity01> updatedVertex = driver.updateVertex("g1", vertex.getDocumentKey(), new TestComplexEntity01("zzz", null, 99), null, 
				true, rev, null);
		assertThat(updatedVertex.getCode(), is(201));
		assertThat(updatedVertex.isError(), is(false));
		
		assertThat(updatedVertex.getDocumentHandle(), is(v1.getDocumentHandle()));
		assertThat(updatedVertex.getDocumentRevision(), is(not(v1.getDocumentRevision())));
		assertThat(updatedVertex.getDocumentRevision(), is(not(0L)));
		assertThat(updatedVertex.getDocumentKey(), is(v1.getDocumentKey()));
		
		assertThat(updatedVertex.getEntity().getUser(), is("zzz"));
		assertThat(updatedVertex.getEntity().getDesc(), is("yyy"));
		assertThat(updatedVertex.getEntity().getAge(), is(99));
		
		// check count
		assertThat(driver.getCollectionCount("v1").getCount(), is(1L));
		
	}

	@Test
	public void test_vertex_update_rev_ne() throws ArangoException {

		// create graph
		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		// create vertex
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("xxx", "yyy", 10), null);
		// check exists vertex
		DocumentEntity<TestComplexEntity01> vertex = driver.getVertex("g1", v1.getDocumentKey(), TestComplexEntity01.class, 
				null, null, null);
		assertThat(vertex.getCode(), is(200));
		
		// update
		try {
			Long rev = vertex.getDocumentRevision() + 1;
			DocumentEntity<TestComplexEntity01> updatedVertex = driver.updateVertex("g1", vertex.getDocumentKey(), new TestComplexEntity01("zzz", null, 99), null, 
					true, rev, null);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(412));
			assertThat(e.getErrorNumber(), is(1903));
			assertThat(e.getErrorMessage(), is("wrong revision"));
		}
		
	}

	@Test
	public void test_vertex_update_ifmatch_eq() throws ArangoException {

		// create graph
		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		// create vertex
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("xxx", "yyy", 10), null);
		// check exists vertex
		DocumentEntity<TestComplexEntity01> vertex = driver.getVertex("g1", v1.getDocumentKey(), TestComplexEntity01.class, 
				null, null, null);
		assertThat(vertex.getCode(), is(200));
		
		// update
		Long rev = vertex.getDocumentRevision();
		DocumentEntity<TestComplexEntity01> updatedVertex = driver.updateVertex("g1", vertex.getDocumentKey(), new TestComplexEntity01("zzz", null, 99), null, 
				true, null, rev);
		assertThat(updatedVertex.getCode(), is(201));
		assertThat(updatedVertex.isError(), is(false));
		
		assertThat(updatedVertex.getDocumentHandle(), is(v1.getDocumentHandle()));
		assertThat(updatedVertex.getDocumentRevision(), is(not(v1.getDocumentRevision())));
		assertThat(updatedVertex.getDocumentRevision(), is(not(0L)));
		assertThat(updatedVertex.getDocumentKey(), is(v1.getDocumentKey()));
		
		assertThat(updatedVertex.getEntity().getUser(), is("zzz"));
		assertThat(updatedVertex.getEntity().getDesc(), is("yyy"));
		assertThat(updatedVertex.getEntity().getAge(), is(99));
		
		// check count
		assertThat(driver.getCollectionCount("v1").getCount(), is(1L));
		
	}

	@Test
	public void test_vertex_update_ifmatch_ne() throws ArangoException {

		// create graph
		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		// create vertex
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("xxx", "yyy", 10), null);
		// check exists vertex
		DocumentEntity<TestComplexEntity01> vertex = driver.getVertex("g1", v1.getDocumentKey(), TestComplexEntity01.class, 
				null, null, null);
		assertThat(vertex.getCode(), is(200));
		
		// update
		try {
			Long rev = vertex.getDocumentRevision() + 1;
			DocumentEntity<TestComplexEntity01> updatedVertex = driver.updateVertex("g1", vertex.getDocumentKey(), new TestComplexEntity01("zzz", null, 99), null, 
					true, null, rev);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(412));
			assertThat(e.getErrorNumber(), is(1903));
			assertThat(e.getErrorMessage(), is("wrong revision"));
		}
		
	}


}
