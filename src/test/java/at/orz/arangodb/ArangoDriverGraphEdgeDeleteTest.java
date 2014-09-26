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
public class ArangoDriverGraphEdgeDeleteTest extends BaseGraphTest {

	public ArangoDriverGraphEdgeDeleteTest(ArangoConfigure configure, ArangoDriver driver) {
		super(configure, driver);
	}
	
	@Test
	public void test_delete_edge() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		
		EdgeEntity<?> edge = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), null, null, null);
		assertThat(edge.getCode(), is(202));
		
		DeletedEntity deleted = driver.deleteEdge("g1", edge.getDocumentKey());
		assertThat(deleted.getCode(), is(202));
		assertThat(deleted.isError(), is(false));
		assertThat(deleted.getDeleted(), is(true));
		
	}
	
	@Test
	public void test_delete_edge_no_graph() throws ArangoException {
		try {
			driver.deleteEdge("g1", "hoge");
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorNumber(), is(1901));
			assertThat(e.getErrorMessage(), startsWith("no graph named"));
		}
		
	}

	@Test
	public void test_delete_edge_no_edge() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		
		try {
			driver.deleteEdge("g1", "1");
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorNumber(), is(1906));
			assertThat(e.getErrorMessage(), startsWith("no edge found for"));
		}
		
	}

	@Test
	public void test_delete_edge_waitForSync() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		
		EdgeEntity<?> edge = driver.createEdge("g1", null, v1.getDocumentHandle(), v1.getDocumentHandle(), null, null, false);
		assertThat(edge.getCode(), is(201));
		assertThat(edge.isError(), is(false));
		
		DeletedEntity deleted = driver.deleteEdge("g1", edge.getDocumentKey(), false);
		assertThat(deleted.getCode(), is(200));
		assertThat(deleted.getDeleted(), is(true));
		
	}


	@Test
	public void test_delete_edge_rev_eq() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		
		EdgeEntity<?> edge = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), null, null, null);
		assertThat(edge.getCode(), is(202));
		
		Long rev = edge.getDocumentRevision();
		DeletedEntity deleted = driver.deleteEdge("g1", edge.getDocumentKey(), null, rev, null);
		assertThat(deleted.getCode(), is(202));
		assertThat(deleted.isError(), is(false));
		assertThat(deleted.getDeleted(), is(true));
		
	}

	@Test
	public void test_delete_edge_rev_ne() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		
		EdgeEntity<?> edge = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), null, null, null);
		assertThat(edge.getCode(), is(202));
		
		try {
			Long rev = edge.getDocumentRevision() + 1;
			DeletedEntity deleted = driver.deleteEdge("g1", edge.getDocumentKey(), null, rev, null);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(412));
			assertThat(e.getErrorNumber(), is(1906));
			assertThat(e.getErrorMessage(), is("wrong revision"));
		}
		
	}

	@Test
	public void test_delete_edge_match_eq() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		
		EdgeEntity<?> edge = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), null, null, null);
		assertThat(edge.getCode(), is(202));
		
		Long rev = edge.getDocumentRevision();
		DeletedEntity deleted = driver.deleteEdge("g1", edge.getDocumentKey(), null, null, rev);
		assertThat(deleted.getCode(), is(202));
		assertThat(deleted.isError(), is(false));
		assertThat(deleted.getDeleted(), is(true));
		
	}

	@Test
	public void test_delete_edge_match_ne() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		
		EdgeEntity<?> edge = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), null, null, null);
		assertThat(edge.getCode(), is(202));
		
		try {
			Long rev = edge.getDocumentRevision() + 1;
			DeletedEntity deleted = driver.deleteEdge("g1", edge.getDocumentKey(), null, null, rev);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(412));
			assertThat(e.getErrorNumber(), is(1906));
			assertThat(e.getErrorMessage(), is("wrong revision"));
		}
		
	}

}
