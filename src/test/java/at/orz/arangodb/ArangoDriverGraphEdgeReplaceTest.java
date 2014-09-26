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
import at.orz.arangodb.entity.EdgeEntity;
import at.orz.arangodb.entity.GraphEntity;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ArangoDriverGraphEdgeReplaceTest extends BaseGraphTest {

	public ArangoDriverGraphEdgeReplaceTest(ArangoConfigure configure, ArangoDriver driver) {
		super(configure, driver);
	}
	
	@Test
	public void test_replace_edge() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		DocumentEntity<TestComplexEntity01> v3 = driver.createVertex("g1", new TestComplexEntity01("v3-user", "desc3", 14), null);
		DocumentEntity<TestComplexEntity01> v4 = driver.createVertex("g1", new TestComplexEntity01("v4-user", "desc4", 20), null);
		
		EdgeEntity<?> edge = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1, 2, 3), "mylabel", null);
		assertThat(edge.getCode(), is(202));
		
		EdgeEntity<TestComplexEntity01> replacedEdge = driver.replaceEdge("g1", edge.getDocumentKey(), new TestComplexEntity01("xx", "yy", 20));
		assertThat(replacedEdge.getCode(), is(202));
		assertThat(replacedEdge.isError(), is(false));
		assertThat(replacedEdge.getDocumentKey(), is(edge.getDocumentKey()));
		assertThat(replacedEdge.getDocumentRevision(), is(not(edge.getDocumentRevision())));
		assertThat(replacedEdge.getDocumentHandle(), is(edge.getDocumentHandle()));
		assertThat(replacedEdge.getFromVertexHandle(), is(v1.getDocumentHandle()));
		assertThat(replacedEdge.getToVertexHandle(), is(v2.getDocumentHandle()));
		assertThat(replacedEdge.getEdgeLabel(), is("mylabel"));
		
		assertThat(replacedEdge.getEntity(), instanceOf(TestComplexEntity01.class));
		assertThat(replacedEdge.getEntity().getUser(), is("xx"));
		assertThat(replacedEdge.getEntity().getDesc(), is("yy"));
		assertThat(replacedEdge.getEntity().getAge(), is(20));
		
	}

	@Test
	public void test_replace_edge_null() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		DocumentEntity<TestComplexEntity01> v3 = driver.createVertex("g1", new TestComplexEntity01("v3-user", "desc3", 14), null);
		DocumentEntity<TestComplexEntity01> v4 = driver.createVertex("g1", new TestComplexEntity01("v4-user", "desc4", 20), null);
		
		EdgeEntity<?> edge = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1, 2, 3), "mylabel", null);
		assertThat(edge.getCode(), is(202));
		
		EdgeEntity<?> replacedEdge = driver.replaceEdge("g1", edge.getDocumentKey(), null);
		assertThat(replacedEdge.getCode(), is(202));
		assertThat(replacedEdge.isError(), is(false));
		assertThat(replacedEdge.getDocumentKey(), is(edge.getDocumentKey()));
		assertThat(replacedEdge.getDocumentRevision(), is(not(edge.getDocumentRevision())));
		assertThat(replacedEdge.getDocumentHandle(), is(edge.getDocumentHandle()));
		assertThat(replacedEdge.getFromVertexHandle(), is(v1.getDocumentHandle()));
		assertThat(replacedEdge.getToVertexHandle(), is(v2.getDocumentHandle()));
		assertThat(replacedEdge.getEdgeLabel(), is("mylabel"));
		assertThat(replacedEdge.getEntity(), is(nullValue()));
		
	}

	@Test
	public void test_replace_edge_waitForSync() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		DocumentEntity<TestComplexEntity01> v3 = driver.createVertex("g1", new TestComplexEntity01("v3-user", "desc3", 14), null);
		DocumentEntity<TestComplexEntity01> v4 = driver.createVertex("g1", new TestComplexEntity01("v4-user", "desc4", 20), null);
		
		EdgeEntity<?> edge = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1, 2, 3), "mylabel", null);
		assertThat(edge.getCode(), is(202));
		
		EdgeEntity<?> replacedEdge = driver.replaceEdge("g1", edge.getDocumentKey(), null, false, null, null);
		assertThat(replacedEdge.getCode(), is(201));
		assertThat(replacedEdge.isError(), is(false));
		assertThat(replacedEdge.getDocumentKey(), is(edge.getDocumentKey()));
		assertThat(replacedEdge.getDocumentRevision(), is(not(edge.getDocumentRevision())));
		assertThat(replacedEdge.getDocumentHandle(), is(edge.getDocumentHandle()));
		assertThat(replacedEdge.getFromVertexHandle(), is(v1.getDocumentHandle()));
		assertThat(replacedEdge.getToVertexHandle(), is(v2.getDocumentHandle()));
		assertThat(replacedEdge.getEdgeLabel(), is("mylabel"));
		assertThat(replacedEdge.getEntity(), is(nullValue()));
		
	}


	@Test
	public void test_replace_edge_rev_eq() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		DocumentEntity<TestComplexEntity01> v3 = driver.createVertex("g1", new TestComplexEntity01("v3-user", "desc3", 14), null);
		DocumentEntity<TestComplexEntity01> v4 = driver.createVertex("g1", new TestComplexEntity01("v4-user", "desc4", 20), null);
		
		EdgeEntity<?> edge = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1, 2, 3), "mylabel", null);
		assertThat(edge.getCode(), is(202));
		
		Long rev = edge.getDocumentRevision();
		EdgeEntity<TestComplexEntity01> replacedEdge = driver.replaceEdge("g1", edge.getDocumentKey(), new TestComplexEntity01("xx", "yy", 20), null, rev, null);
		assertThat(replacedEdge.getCode(), is(202));
		assertThat(replacedEdge.isError(), is(false));
		assertThat(replacedEdge.getDocumentKey(), is(edge.getDocumentKey()));
		assertThat(replacedEdge.getDocumentRevision(), is(not(edge.getDocumentRevision())));
		assertThat(replacedEdge.getDocumentHandle(), is(edge.getDocumentHandle()));
		assertThat(replacedEdge.getFromVertexHandle(), is(v1.getDocumentHandle()));
		assertThat(replacedEdge.getToVertexHandle(), is(v2.getDocumentHandle()));
		assertThat(replacedEdge.getEdgeLabel(), is("mylabel"));
		
		assertThat(replacedEdge.getEntity(), instanceOf(TestComplexEntity01.class));
		assertThat(replacedEdge.getEntity().getUser(), is("xx"));
		assertThat(replacedEdge.getEntity().getDesc(), is("yy"));
		assertThat(replacedEdge.getEntity().getAge(), is(20));
		
	}

	@Test
	public void test_replace_edge_rev_ne() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		DocumentEntity<TestComplexEntity01> v3 = driver.createVertex("g1", new TestComplexEntity01("v3-user", "desc3", 14), null);
		DocumentEntity<TestComplexEntity01> v4 = driver.createVertex("g1", new TestComplexEntity01("v4-user", "desc4", 20), null);
		
		EdgeEntity<?> edge = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1, 2, 3), "mylabel", null);
		assertThat(edge.getCode(), is(202));
		
		try {
			Long rev = edge.getDocumentRevision() + 1;
			EdgeEntity<TestComplexEntity01> replacedEdge = driver.replaceEdge("g1", edge.getDocumentKey(), new TestComplexEntity01("xx", "yy", 20), null, rev, null);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(412));
			assertThat(e.getErrorNumber(), is(1906));
			assertThat(e.getErrorMessage(), is("wrong revision"));
		}
		
	}

	@Test
	public void test_replace_edge_match_eq() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		DocumentEntity<TestComplexEntity01> v3 = driver.createVertex("g1", new TestComplexEntity01("v3-user", "desc3", 14), null);
		DocumentEntity<TestComplexEntity01> v4 = driver.createVertex("g1", new TestComplexEntity01("v4-user", "desc4", 20), null);
		
		EdgeEntity<?> edge = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1, 2, 3), "mylabel", null);
		assertThat(edge.getCode(), is(202));
		
		Long rev = edge.getDocumentRevision();
		EdgeEntity<TestComplexEntity01> replacedEdge = driver.replaceEdge("g1", edge.getDocumentKey(), new TestComplexEntity01("xx", "yy", 20), null, null, rev);
		assertThat(replacedEdge.getCode(), is(202));
		assertThat(replacedEdge.isError(), is(false));
		assertThat(replacedEdge.getDocumentKey(), is(edge.getDocumentKey()));
		assertThat(replacedEdge.getDocumentRevision(), is(not(edge.getDocumentRevision())));
		assertThat(replacedEdge.getDocumentHandle(), is(edge.getDocumentHandle()));
		assertThat(replacedEdge.getFromVertexHandle(), is(v1.getDocumentHandle()));
		assertThat(replacedEdge.getToVertexHandle(), is(v2.getDocumentHandle()));
		assertThat(replacedEdge.getEdgeLabel(), is("mylabel"));
		
		assertThat(replacedEdge.getEntity(), instanceOf(TestComplexEntity01.class));
		assertThat(replacedEdge.getEntity().getUser(), is("xx"));
		assertThat(replacedEdge.getEntity().getDesc(), is("yy"));
		assertThat(replacedEdge.getEntity().getAge(), is(20));
		
	}

	@Test
	public void test_replace_edge_match_ne() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		DocumentEntity<TestComplexEntity01> v3 = driver.createVertex("g1", new TestComplexEntity01("v3-user", "desc3", 14), null);
		DocumentEntity<TestComplexEntity01> v4 = driver.createVertex("g1", new TestComplexEntity01("v4-user", "desc4", 20), null);
		
		EdgeEntity<?> edge = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1, 2, 3), "mylabel", null);
		assertThat(edge.getCode(), is(202));
		
		try {
			Long rev = edge.getDocumentRevision() + 1;
			EdgeEntity<TestComplexEntity01> replacedEdge = driver.replaceEdge("g1", edge.getDocumentKey(), new TestComplexEntity01("xx", "yy", 20), null, null, rev);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(412));
			assertThat(e.getErrorNumber(), is(1906));
			assertThat(e.getErrorMessage(), is("wrong revision"));
		}
		
	}


	
}
