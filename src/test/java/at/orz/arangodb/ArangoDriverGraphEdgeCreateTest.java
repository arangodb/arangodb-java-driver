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

import at.orz.arangodb.entity.CursorEntity;
import at.orz.arangodb.entity.DocumentEntity;
import at.orz.arangodb.entity.EdgeEntity;
import at.orz.arangodb.entity.FilterCondition;
import at.orz.arangodb.entity.GraphEntity;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ArangoDriverGraphEdgeCreateTest extends BaseGraphTest {

	public ArangoDriverGraphEdgeCreateTest(ArangoConfigure configure, ArangoDriver driver) {
		super(configure, driver);
	}
	
	@Test
	public void test_create_edge_nokey() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		
		EdgeEntity<?> edge = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), null, null, null);
		
		assertThat(edge.getCode(), is(202));
		assertThat(edge.isError(), is(false));
		
		assertThat(edge.getEdgeLabel(), is(nullValue()));
		assertThat(edge.getDocumentHandle(), is(notNullValue()));
		assertThat(edge.getDocumentRevision(), is(not(0L)));
		assertThat(edge.getDocumentKey(), is(notNullValue()));
		assertThat(edge.getFromVertexHandle(), is(v1.getDocumentHandle()));
		assertThat(edge.getToVertexHandle(), is(v2.getDocumentHandle()));
		
	}

	
	@Test
	public void test_create_edge_key() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		
		EdgeEntity<?> edge = driver.createEdge("g1", "e1", v1.getDocumentHandle(), v2.getDocumentHandle(), null, null, null);
		
		assertThat(edge.getCode(), is(202));
		assertThat(edge.isError(), is(false));
		
		assertThat(edge.getEdgeLabel(), is(nullValue()));
		assertThat(edge.getDocumentHandle(), is(notNullValue()));
		assertThat(edge.getDocumentRevision(), is(not(0L)));
		assertThat(edge.getDocumentKey(), is("e1"));
		assertThat(edge.getFromVertexHandle(), is(v1.getDocumentHandle()));
		assertThat(edge.getToVertexHandle(), is(v2.getDocumentHandle()));
		
	}

	@Test
	public void test_create_edge_optional_value() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		
		EdgeEntity<TestComplexEntity02> edge = driver.createEdge("g1", "e1", v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1, 2, 3), null, null);
		
		assertThat(edge.getCode(), is(202));
		assertThat(edge.isError(), is(false));
		
		assertThat(edge.getEdgeLabel(), is(nullValue()));
		assertThat(edge.getDocumentHandle(), is(notNullValue()));
		assertThat(edge.getDocumentRevision(), is(not(0L)));
		assertThat(edge.getDocumentKey(), is("e1"));
		assertThat(edge.getFromVertexHandle(), is(v1.getDocumentHandle()));
		assertThat(edge.getToVertexHandle(), is(v2.getDocumentHandle()));
		
		assertThat(edge.getEntity(), instanceOf(TestComplexEntity02.class));
		assertThat(edge.getEntity().getX(), is(1));
		assertThat(edge.getEntity().getY(), is(2));
		assertThat(edge.getEntity().getZ(), is(3));
		
	}

	@Test
	public void test_create_edge_label() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		
		EdgeEntity<TestComplexEntity02> edge = driver.createEdge("g1", "e1", v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1, 2, 3), "mylabel", null);
		
		assertThat(edge.getCode(), is(202));
		assertThat(edge.isError(), is(false));
		
		assertThat(edge.getEdgeLabel(), is("mylabel"));
		assertThat(edge.getDocumentHandle(), is(notNullValue()));
		assertThat(edge.getDocumentRevision(), is(not(0L)));
		assertThat(edge.getDocumentKey(), is("e1"));
		assertThat(edge.getFromVertexHandle(), is(v1.getDocumentHandle()));
		assertThat(edge.getToVertexHandle(), is(v2.getDocumentHandle()));
		
		assertThat(edge.getEntity(), instanceOf(TestComplexEntity02.class));
		assertThat(edge.getEntity().getX(), is(1));
		assertThat(edge.getEntity().getY(), is(2));
		assertThat(edge.getEntity().getZ(), is(3));
		
	}

	
	@Test
	public void test_create_edge_no_graph() throws ArangoException {
		try {
			driver.createEdge("g1", null, "1", "2", null, null, null);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorNumber(), is(1901));
			assertThat(e.getErrorMessage(), startsWith("no graph named"));
		}
		
	}

	@Test
	public void test_create_edge_no_edge() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		
		try {
			driver.createEdge("g1", null, v1.getDocumentHandle(), "vol1/1", null, null, null);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(400));
			assertThat(e.getErrorNumber(), is(1907));
			assertThat(e.getErrorMessage(), startsWith("TypeError: Cannot read property"));
		}
		
	}

	@Test
	public void test_create_edge_waitForSync() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		
		EdgeEntity<?> edge = driver.createEdge("g1", null, v1.getDocumentHandle(), v1.getDocumentHandle(), null, null, false);
		
		assertThat(edge.getCode(), is(201));
		assertThat(edge.isError(), is(false));
		
		assertThat(edge.getEdgeLabel(), is(nullValue()));
		assertThat(edge.getDocumentHandle(), is(notNullValue()));
		assertThat(edge.getDocumentRevision(), is(not(0L)));
		assertThat(edge.getDocumentKey(), is(notNullValue()));
		assertThat(edge.getFromVertexHandle(), is(v1.getDocumentHandle()));
		assertThat(edge.getToVertexHandle(), is(v1.getDocumentHandle()));
		
	}

	
}
