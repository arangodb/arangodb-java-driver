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
import at.orz.arangodb.entity.EdgeEntity;
import at.orz.arangodb.entity.GraphEntity;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ArangoDriverGraphEdgeGetTest extends BaseGraphTest {

	public ArangoDriverGraphEdgeGetTest(ArangoConfigure configure, ArangoDriver driver) {
		super(configure, driver);
	}
	
	@Test
	public void test_get_edge() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		
		EdgeEntity<?> edge1 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), null, null, null);
		
		EdgeEntity<?> edge2 = driver.getEdge("g1", edge1.getDocumentKey(), null);
		assertThat(edge2.getCode(), is(200));
		assertThat(edge2.isError(), is(false));
		
		assertThat(edge2.getEdgeLabel(), is(nullValue()));
		assertThat(edge2.getDocumentHandle(), is(edge1.getDocumentHandle()));
		assertThat(edge2.getDocumentRevision(), is(edge1.getDocumentRevision()));
		assertThat(edge2.getDocumentKey(), is(edge1.getDocumentKey()));
		assertThat(edge2.getFromVertexHandle(), is(v1.getDocumentHandle()));
		assertThat(edge2.getToVertexHandle(), is(v2.getDocumentHandle()));
		
	}

	@Test
	public void test_get_edge2() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		
		EdgeEntity<?> edge1 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1, 2, 3), "mylabel", null);
		
		EdgeEntity<TestComplexEntity02> edge2 = driver.getEdge("g1", edge1.getDocumentKey(), TestComplexEntity02.class);
		assertThat(edge2.getCode(), is(200));
		assertThat(edge2.isError(), is(false));
		
		assertThat(edge2.getEdgeLabel(), is("mylabel"));
		assertThat(edge2.getDocumentHandle(), is(edge1.getDocumentHandle()));
		assertThat(edge2.getDocumentRevision(), is(edge1.getDocumentRevision()));
		assertThat(edge2.getDocumentKey(), is(edge1.getDocumentKey()));
		assertThat(edge2.getFromVertexHandle(), is(v1.getDocumentHandle()));
		assertThat(edge2.getToVertexHandle(), is(v2.getDocumentHandle()));
		
		assertThat(edge2.getEntity().getX(), is(1));
		assertThat(edge2.getEntity().getY(), is(2));
		assertThat(edge2.getEntity().getZ(), is(3));
		
	}


	@Test
	public void test_get_edge_graph_not_found() throws ArangoException {
		
		try {
			driver.getEdge("g1", "v1", null);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorNumber(), is(1901));
			assertThat(e.getErrorMessage(), startsWith("no graph named"));
		}
		
	}

	@Test
	public void test_get_edge_edge_not_found() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		
		EdgeEntity<?> edge1 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), null, null, null);
		
		try {
			EdgeEntity<?> edge2 = driver.getEdge("g1", "xx", null);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorNumber(), is(1906));
			assertThat(e.getErrorMessage(), startsWith("no edge found for"));
		}
		
	}

	@Test
	public void test_get_edge_rev_eq() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		
		EdgeEntity<?> edge1 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1, 2, 3), "mylabel", null);
		
		Long rev = edge1.getDocumentRevision();
		EdgeEntity<TestComplexEntity02> edge2 = driver.getEdge("g1", edge1.getDocumentKey(), TestComplexEntity02.class, 
				rev, null, null);
		assertThat(edge2.getCode(), is(200));
		assertThat(edge2.isError(), is(false));
		
		assertThat(edge2.getEdgeLabel(), is("mylabel"));
		assertThat(edge2.getDocumentHandle(), is(edge1.getDocumentHandle()));
		assertThat(edge2.getDocumentRevision(), is(edge1.getDocumentRevision()));
		assertThat(edge2.getDocumentKey(), is(edge1.getDocumentKey()));
		assertThat(edge2.getFromVertexHandle(), is(v1.getDocumentHandle()));
		assertThat(edge2.getToVertexHandle(), is(v2.getDocumentHandle()));
		
		assertThat(edge2.getEntity().getX(), is(1));
		assertThat(edge2.getEntity().getY(), is(2));
		assertThat(edge2.getEntity().getZ(), is(3));
		
	}

	@Test
	public void test_get_edge_rev_ne() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		
		EdgeEntity<?> edge1 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1, 2, 3), "mylabel", null);
		
		try {
			Long rev = edge1.getDocumentRevision() + 1;
			EdgeEntity<TestComplexEntity02> edge2 = driver.getEdge("g1", edge1.getDocumentKey(), TestComplexEntity02.class, 
					rev, null, null);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(412));
			assertThat(e.getErrorNumber(), is(1906));
			assertThat(e.getErrorMessage(), is("wrong revision"));
		}
		
	}

	@Test
	public void test_get_edge_nomatch_eq() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		
		EdgeEntity<?> edge1 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1, 2, 3), "mylabel", null);
		
		Long rev = edge1.getDocumentRevision();
		EdgeEntity<TestComplexEntity02> edge2 = driver.getEdge("g1", edge1.getDocumentKey(), TestComplexEntity02.class, 
				null, rev, null);
		assertThat(edge2.getStatusCode(), is(304));
		assertThat(edge2.isNotModified(), is(true));
		assertThat(edge2.isError(), is(false));
		
	}

	@Test
	public void test_get_edge_nomatch_ne() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		
		EdgeEntity<?> edge1 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1, 2, 3), "mylabel", null);
		
		Long rev = edge1.getDocumentRevision() + 1;
		EdgeEntity<TestComplexEntity02> edge2 = driver.getEdge("g1", edge1.getDocumentKey(), TestComplexEntity02.class, 
				null, rev, null);

		assertThat(edge2.getCode(), is(200));
		assertThat(edge2.isError(), is(false));

		assertThat(edge2.getEdgeLabel(), is("mylabel"));
		assertThat(edge2.getDocumentHandle(), is(edge1.getDocumentHandle()));
		assertThat(edge2.getDocumentRevision(), is(edge1.getDocumentRevision()));
		assertThat(edge2.getDocumentKey(), is(edge1.getDocumentKey()));
		assertThat(edge2.getFromVertexHandle(), is(v1.getDocumentHandle()));
		assertThat(edge2.getToVertexHandle(), is(v2.getDocumentHandle()));
		
		assertThat(edge2.getEntity().getX(), is(1));
		assertThat(edge2.getEntity().getY(), is(2));
		assertThat(edge2.getEntity().getZ(), is(3));

	}


	@Test
	public void test_get_edge_match_eq() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		
		EdgeEntity<?> edge1 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1, 2, 3), "mylabel", null);
		
		Long rev = edge1.getDocumentRevision();
		EdgeEntity<TestComplexEntity02> edge2 = driver.getEdge("g1", edge1.getDocumentKey(), TestComplexEntity02.class, 
				null, null, rev);
		assertThat(edge2.getCode(), is(200));
		assertThat(edge2.isError(), is(false));
		
		assertThat(edge2.getEdgeLabel(), is("mylabel"));
		assertThat(edge2.getDocumentHandle(), is(edge1.getDocumentHandle()));
		assertThat(edge2.getDocumentRevision(), is(edge1.getDocumentRevision()));
		assertThat(edge2.getDocumentKey(), is(edge1.getDocumentKey()));
		assertThat(edge2.getFromVertexHandle(), is(v1.getDocumentHandle()));
		assertThat(edge2.getToVertexHandle(), is(v2.getDocumentHandle()));
		
		assertThat(edge2.getEntity().getX(), is(1));
		assertThat(edge2.getEntity().getY(), is(2));
		assertThat(edge2.getEntity().getZ(), is(3));
		
	}

	@Test
	public void test_get_edge_match_ne() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		
		EdgeEntity<?> edge1 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1, 2, 3), "mylabel", null);
		
		try {
			Long rev = edge1.getDocumentRevision() + 1;
			EdgeEntity<TestComplexEntity02> edge2 = driver.getEdge("g1", edge1.getDocumentKey(), TestComplexEntity02.class, 
					null, null, rev);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(412));
			assertThat(e.getErrorNumber(), is(1906));
			assertThat(e.getErrorMessage(), is("wrong revision"));
		}
		
	}

	
}
