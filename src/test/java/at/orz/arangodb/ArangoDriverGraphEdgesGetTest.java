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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.callback.Callback;

import org.junit.Test;

import at.orz.arangodb.entity.CursorEntity;
import at.orz.arangodb.entity.Direction;
import at.orz.arangodb.entity.DocumentEntity;
import at.orz.arangodb.entity.EdgeEntity;
import at.orz.arangodb.entity.FilterCondition;
import at.orz.arangodb.entity.GraphEntity;
import at.orz.arangodb.util.TestUtils;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ArangoDriverGraphEdgesGetTest extends BaseGraphTest {

	public ArangoDriverGraphEdgesGetTest(ArangoConfigure configure, ArangoDriver driver) {
		super(configure, driver);
	}
	
	private Set<String> asSet(Collection<? extends EdgeEntity<?>> collection) {
		HashSet<String> set = new HashSet<String>();
		for (EdgeEntity<?> e : collection) {
			set.add(e.getDocumentKey());
		}
		return set;
	}
	private Set<String> asSet(Object... objects) {
		HashSet<String> set = new HashSet<String>();
		for (Object o : objects) {
			set.add(o.toString());
		}
		return set;
	}
	
	@Test
	public void test_get_edges() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		DocumentEntity<TestComplexEntity01> v3 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc3", 12), null);
		DocumentEntity<TestComplexEntity01> v4 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc4", 12), null);
		
		EdgeEntity<TestComplexEntity02> e1 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,20,100), "e1", null);
		EdgeEntity<TestComplexEntity02> e2 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,20,200), "e2", null);
		EdgeEntity<TestComplexEntity02> e3 = driver.createEdge("g1", null, v2.getDocumentHandle(), v3.getDocumentHandle(), new TestComplexEntity02(1,30,300), "e3", null);
		EdgeEntity<TestComplexEntity02> e4 = driver.createEdge("g1", null, v4.getDocumentHandle(), v3.getDocumentHandle(), new TestComplexEntity02(1,30,400), "e4", null);

		HashMap<String, EdgeEntity<TestComplexEntity02>> map = new HashMap<String, EdgeEntity<TestComplexEntity02>>();
		map.put(e1.getDocumentKey(), e1);
		map.put(e2.getDocumentKey(), e2);
		map.put(e3.getDocumentKey(), e3);
		map.put(e4.getDocumentKey(), e4);

		
		
		CursorEntity<EdgeEntity<TestComplexEntity02>> cursor = driver.getEdges("g1", TestComplexEntity02.class);
		assertThat(cursor.getCount(), is(-1));
		assertThat(cursor.getCode(), is(201));
		assertThat(cursor.isError(), is(false));
		assertThat(cursor.hasMore(), is(false));
		assertThat(cursor.getCursorId(), is(-1L));
		
		assertThat(cursor.getResults().size(), is(4));
		
		EdgeEntity<TestComplexEntity02> e0 = map.get(cursor.getResults().get(0).getDocumentKey());
		assertThat(cursor.getResults().get(0).getDocumentHandle(), is(e0.getDocumentHandle()));
		assertThat(cursor.getResults().get(0).getDocumentRevision(), is(e0.getDocumentRevision()));
		assertThat(cursor.getResults().get(0).getDocumentKey(), is(e0.getDocumentKey()));
		assertThat(cursor.getResults().get(0).getFromVertexHandle(), is(e0.getFromVertexHandle()));
		assertThat(cursor.getResults().get(0).getToVertexHandle(), is(e0.getToVertexHandle()));
		assertThat(cursor.getResults().get(0).getEdgeLabel(), is(e0.getEdgeLabel()));
		assertThat(cursor.getResults().get(0).getEntity().getX(), is(e0.getEntity().getX()));
		assertThat(cursor.getResults().get(0).getEntity().getY(), is(e0.getEntity().getY()));
		assertThat(cursor.getResults().get(0).getEntity().getZ(), is(e0.getEntity().getZ()));
		
	}

	@Test
	public void test_get_edges_batchszie_limit_count() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		DocumentEntity<TestComplexEntity01> v3 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc3", 12), null);
		DocumentEntity<TestComplexEntity01> v4 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc4", 12), null);
		
		EdgeEntity<?> e1 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,20,100), "e1", null);
		EdgeEntity<?> e2 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,20,200), "e2", null);
		EdgeEntity<?> e3 = driver.createEdge("g1", null, v2.getDocumentHandle(), v3.getDocumentHandle(), new TestComplexEntity02(1,30,300), "e3", null);
		EdgeEntity<?> e4 = driver.createEdge("g1", null, v4.getDocumentHandle(), v3.getDocumentHandle(), new TestComplexEntity02(1,30,400), "e4", null);
		
		Integer batchSize = 2;
		Integer limit = 3;
		Boolean count = true;
		CursorEntity<EdgeEntity<TestComplexEntity02>> cursor = driver.getEdges(
				"g1", TestComplexEntity02.class, 
				batchSize, limit, count);
		
		assertThat(cursor.getCount(), is(3));
		assertThat(cursor.getCode(), is(201));
		assertThat(cursor.isError(), is(false));
		assertThat(cursor.hasMore(), is(true));
		assertThat(cursor.getCursorId(), is(not(-1L)));
		
		assertThat(cursor.getResults().size(), is(2));
		
	}

	
	@Test
	public void test_get_edges_filter_label() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		DocumentEntity<TestComplexEntity01> v3 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc3", 12), null);
		DocumentEntity<TestComplexEntity01> v4 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc4", 12), null);
		
		EdgeEntity<?> e1 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,20,100), "e1", null);
		EdgeEntity<?> e2 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,20,200), "e2", null);
		EdgeEntity<?> e3 = driver.createEdge("g1", null, v2.getDocumentHandle(), v3.getDocumentHandle(), new TestComplexEntity02(1,30,300), "e3", null);
		EdgeEntity<?> e4 = driver.createEdge("g1", null, v4.getDocumentHandle(), v3.getDocumentHandle(), new TestComplexEntity02(1,30,400), "e4", null);
		
		CursorEntity<EdgeEntity<TestComplexEntity02>> cursor = driver.getEdges(
				"g1", TestComplexEntity02.class, 
				null, null, true, 
				Arrays.asList("e1", "e2", "e3"));
		
		assertThat(cursor.getCount(), is(3));
		assertThat(cursor.getCode(), is(201));
		assertThat(cursor.isError(), is(false));
		assertThat(cursor.hasMore(), is(false));
		assertThat(cursor.getCursorId(), is(-1L));
		
		assertThat(cursor.getResults().size(), is(3));
		
	}

	
	@Test
	public void test_get_edges_filter_by_condition() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		DocumentEntity<TestComplexEntity01> v3 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc3", 12), null);
		DocumentEntity<TestComplexEntity01> v4 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc4", 12), null);
		
		EdgeEntity<?> e1 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,20,100), "e1", null);
		EdgeEntity<?> e2 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,20,200), "e2", null);
		EdgeEntity<?> e3 = driver.createEdge("g1", null, v2.getDocumentHandle(), v3.getDocumentHandle(), new TestComplexEntity02(1,30,300), "e3", null);
		EdgeEntity<?> e4 = driver.createEdge("g1", null, v4.getDocumentHandle(), v3.getDocumentHandle(), new TestComplexEntity02(1,30,400), "e4", null);
		
		CursorEntity<EdgeEntity<TestComplexEntity02>> cursor = driver.getEdges(
				"g1", TestComplexEntity02.class, 
				null, null, true, 
				null, new FilterCondition("y", 20, "=="), new FilterCondition("z", 200, "=="));
		// FilterCondition is AND condition.
		
		assertThat(cursor.getCount(), is(1));
		assertThat(cursor.getCode(), is(201));
		assertThat(cursor.isError(), is(false));
		assertThat(cursor.hasMore(), is(false));
		assertThat(cursor.getCursorId(), is(-1L));
		
		assertThat(cursor.getResults().size(), is(1));
		
	}

	@Test
	public void test_get_edges_filter_by_label_condition() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		DocumentEntity<TestComplexEntity01> v3 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc3", 12), null);
		DocumentEntity<TestComplexEntity01> v4 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc4", 12), null);
		
		EdgeEntity<?> e1 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,20,100), "e1", null);
		EdgeEntity<?> e2 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,20,200), "e2", null);
		EdgeEntity<?> e3 = driver.createEdge("g1", null, v2.getDocumentHandle(), v3.getDocumentHandle(), new TestComplexEntity02(1,30,300), "e3", null);
		EdgeEntity<?> e4 = driver.createEdge("g1", null, v4.getDocumentHandle(), v3.getDocumentHandle(), new TestComplexEntity02(1,30,400), "e4", null);
		
		CursorEntity<EdgeEntity<TestComplexEntity02>> cursor = driver.getEdges(
				"g1", TestComplexEntity02.class, 
				null, null, true, 
				Arrays.asList("e1", "e2", "e4", "e99"), new FilterCondition("y", 20, "=="), new FilterCondition("z", 200, "=="));
		// And condition: labels and filterCondition
		// (e1 OR e2 OR e4 OR e99) AND (y == 20) AND (z == 200)
		
		assertThat(cursor.getCount(), is(1));
		assertThat(cursor.getCode(), is(201));
		assertThat(cursor.isError(), is(false));
		assertThat(cursor.hasMore(), is(false));
		assertThat(cursor.getCursorId(), is(-1L));
		
		assertThat(cursor.getResults().size(), is(1));
		
	}


	@Test
	public void test_get_edges_filter_by_label_condition2() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		DocumentEntity<TestComplexEntity01> v3 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc3", 12), null);
		DocumentEntity<TestComplexEntity01> v4 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc4", 12), null);
		
		EdgeEntity<?> e1 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,20,100), "e1", null);
		EdgeEntity<?> e2 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,20,200), "e2", null);
		EdgeEntity<?> e3 = driver.createEdge("g1", null, v2.getDocumentHandle(), v3.getDocumentHandle(), new TestComplexEntity02(1,30,300), "e3", null);
		EdgeEntity<?> e4 = driver.createEdge("g1", null, v4.getDocumentHandle(), v3.getDocumentHandle(), new TestComplexEntity02(1,30,400), "e4", null);
		
		CursorEntity<EdgeEntity<TestComplexEntity02>> cursor = driver.getEdges(
				"g1", TestComplexEntity02.class, 
				null, null, true, 
				Arrays.asList("e1", "e4"), new FilterCondition("y", 20, "=="), new FilterCondition("z", 200, "=="));
		// And condition: labels and filterCondition
		// (e1 OR e4) AND (y == 20) AND (z == 200)
		
		assertThat(cursor.getCount(), is(0));
		assertThat(cursor.getCode(), is(201));
		assertThat(cursor.isError(), is(false));
		assertThat(cursor.hasMore(), is(false));
		assertThat(cursor.getCursorId(), is(-1L));
		
		assertThat(cursor.getResults().size(), is(0));
		
	}

	@Test
	public void test_get_edges_batchSize_server_limit_1000() throws ArangoException {

		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		
		for (int i = 0; i < 1010; i++) {
			EdgeEntity<?> e1 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,20,100), "e1", null);
		}
		
		Integer batchSize = null;
		Integer limit = null;
		Boolean count = true;
		CursorEntity<EdgeEntity<TestComplexEntity02>> cursor = driver.getEdges(
				"g1", TestComplexEntity02.class, 
				batchSize, limit, count);
		
		assertThat(cursor.getCount(), is(1010));
		assertThat(cursor.getCode(), is(201));
		assertThat(cursor.isError(), is(false));
		assertThat(cursor.hasMore(), is(true));
		assertThat(cursor.getCursorId(), is(not(-1L)));
		
		assertThat(cursor.getResults().size(), is(1000));
		
	}

	@Test
	public void test_get_edges_graph_not_found() throws ArangoException {

		try {
			CursorEntity<EdgeEntity<TestComplexEntity02>> cursor = driver.getEdges("g1", TestComplexEntity02.class);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorNumber(), is(1901));
			assertThat(e.getErrorMessage(), startsWith("no graph named"));
		}

	}


	@Test
	public void test_get_edges_result_set() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		DocumentEntity<TestComplexEntity01> v3 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc3", 12), null);
		DocumentEntity<TestComplexEntity01> v4 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc4", 12), null);
		
		EdgeEntity<TestComplexEntity02> e1 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,20,100), "e1", null);
		EdgeEntity<TestComplexEntity02> e2 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,20,200), "e2", null);
		EdgeEntity<TestComplexEntity02> e3 = driver.createEdge("g1", null, v2.getDocumentHandle(), v3.getDocumentHandle(), new TestComplexEntity02(1,30,300), "e3", null);
		EdgeEntity<TestComplexEntity02> e4 = driver.createEdge("g1", null, v4.getDocumentHandle(), v3.getDocumentHandle(), new TestComplexEntity02(1,30,400), "e4", null);

		HashMap<String, EdgeEntity<TestComplexEntity02>> map = new HashMap<String, EdgeEntity<TestComplexEntity02>>();
		map.put(e1.getDocumentKey(), e1);
		map.put(e2.getDocumentKey(), e2);
		map.put(e3.getDocumentKey(), e3);
		map.put(e4.getDocumentKey(), e4);
		
		CursorResultSet<EdgeEntity<TestComplexEntity02>> rs = driver.getEdgesWithResultSet("g1", TestComplexEntity02.class, 
				2, 10, true);
		assertThat(rs.getTotalCount(), is(4)); // count = true
		
		while (rs.hasNext()) {
			
			EdgeEntity<TestComplexEntity02> e = rs.next();
			EdgeEntity<TestComplexEntity02> e0 = map.get(e.getDocumentKey());
			
			assertThat(e.getDocumentHandle(), is(e0.getDocumentHandle()));
			assertThat(e.getDocumentRevision(), is(e0.getDocumentRevision()));
			assertThat(e.getDocumentKey(), is(e0.getDocumentKey()));
			assertThat(e.getFromVertexHandle(), is(e0.getFromVertexHandle()));
			assertThat(e.getToVertexHandle(), is(e0.getToVertexHandle()));
			assertThat(e.getEdgeLabel(), is(e0.getEdgeLabel()));
			assertThat(e.getEntity().getX(), is(e0.getEntity().getX()));
			assertThat(e.getEntity().getY(), is(e0.getEntity().getY()));
			assertThat(e.getEntity().getZ(), is(e0.getEntity().getZ()));

		}
		
	}

	// ----- with vertex

	@Test
	public void test_get_edges_v() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		DocumentEntity<TestComplexEntity01> v3 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc3", 12), null);
		DocumentEntity<TestComplexEntity01> v4 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc4", 12), null);
		
		EdgeEntity<TestComplexEntity02> e1 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,20,100), "e1", null);
		EdgeEntity<TestComplexEntity02> e2 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,20,200), "e2", null);
		EdgeEntity<TestComplexEntity02> e3 = driver.createEdge("g1", null, v2.getDocumentHandle(), v3.getDocumentHandle(), new TestComplexEntity02(1,30,300), "e3", null);
		EdgeEntity<TestComplexEntity02> e4 = driver.createEdge("g1", null, v4.getDocumentHandle(), v3.getDocumentHandle(), new TestComplexEntity02(1,30,400), "e4", null);

		HashMap<String, EdgeEntity<TestComplexEntity02>> map = new HashMap<String, EdgeEntity<TestComplexEntity02>>();
		map.put(e1.getDocumentKey(), e1);
		map.put(e2.getDocumentKey(), e2);
		map.put(e3.getDocumentKey(), e3);
		map.put(e4.getDocumentKey(), e4);

		
		
		CursorEntity<EdgeEntity<TestComplexEntity02>> cursor = driver.getEdges("g1", v2.getDocumentKey(), TestComplexEntity02.class);
		assertThat(cursor.getCount(), is(-1));
		assertThat(cursor.getCode(), is(201));
		assertThat(cursor.isError(), is(false));
		assertThat(cursor.hasMore(), is(false));
		assertThat(cursor.getCursorId(), is(-1L));
		
		assertThat(cursor.getResults().size(), is(3));
		
		EdgeEntity<TestComplexEntity02> e0 = map.get(cursor.getResults().get(0).getDocumentKey());
		assertThat(cursor.getResults().get(0).getDocumentHandle(), is(e0.getDocumentHandle()));
		assertThat(cursor.getResults().get(0).getDocumentRevision(), is(e0.getDocumentRevision()));
		assertThat(cursor.getResults().get(0).getDocumentKey(), is(e0.getDocumentKey()));
		assertThat(cursor.getResults().get(0).getFromVertexHandle(), is(e0.getFromVertexHandle()));
		assertThat(cursor.getResults().get(0).getToVertexHandle(), is(e0.getToVertexHandle()));
		assertThat(cursor.getResults().get(0).getEdgeLabel(), is(e0.getEdgeLabel()));
		assertThat(cursor.getResults().get(0).getEntity().getX(), is(e0.getEntity().getX()));
		assertThat(cursor.getResults().get(0).getEntity().getY(), is(e0.getEntity().getY()));
		assertThat(cursor.getResults().get(0).getEntity().getZ(), is(e0.getEntity().getZ()));
		
		Set<String> act = new HashSet<String>(Arrays.asList(cursor.getResults().get(0).getEdgeLabel(), cursor.getResults().get(1).getEdgeLabel(), cursor.getResults().get(2).getEdgeLabel()));
		Set<String> ans = new HashSet<String>(Arrays.asList("e1", "e2", "e3"));
		assertThat(act, is(ans));
		
	}

	
	@Test
	public void test_get_edges_v_batchSize_limit_count() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		DocumentEntity<TestComplexEntity01> v3 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc3", 12), null);
		DocumentEntity<TestComplexEntity01> v4 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc4", 12), null);
		
		EdgeEntity<TestComplexEntity02> e1 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,20,100), "e1", null);
		EdgeEntity<TestComplexEntity02> e2 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,20,200), "e2", null);
		EdgeEntity<TestComplexEntity02> e3 = driver.createEdge("g1", null, v2.getDocumentHandle(), v3.getDocumentHandle(), new TestComplexEntity02(1,30,300), "e3", null);
		EdgeEntity<TestComplexEntity02> e4 = driver.createEdge("g1", null, v4.getDocumentHandle(), v3.getDocumentHandle(), new TestComplexEntity02(1,30,400), "e4", null);

		HashMap<String, EdgeEntity<TestComplexEntity02>> map = new HashMap<String, EdgeEntity<TestComplexEntity02>>();
		map.put(e1.getDocumentKey(), e1);
		map.put(e2.getDocumentKey(), e2);
		map.put(e3.getDocumentKey(), e3);
		map.put(e4.getDocumentKey(), e4);

		
		
		CursorEntity<EdgeEntity<TestComplexEntity02>> cursor = driver.getEdges("g1", v2.getDocumentKey(), TestComplexEntity02.class,
				1, 2, true);
		assertThat(cursor.getCount(), is(2));
		assertThat(cursor.getCode(), is(201));
		assertThat(cursor.isError(), is(false));
		assertThat(cursor.hasMore(), is(true));
		assertThat(cursor.getCursorId(), is(not(-1L)));
		
		assertThat(cursor.getResults().size(), is(1));
		
		EdgeEntity<TestComplexEntity02> e0 = map.get(cursor.getResults().get(0).getDocumentKey());
		assertThat(cursor.getResults().get(0).getDocumentHandle(), is(e0.getDocumentHandle()));
		assertThat(cursor.getResults().get(0).getDocumentRevision(), is(e0.getDocumentRevision()));
		assertThat(cursor.getResults().get(0).getDocumentKey(), is(e0.getDocumentKey()));
		assertThat(cursor.getResults().get(0).getFromVertexHandle(), is(e0.getFromVertexHandle()));
		assertThat(cursor.getResults().get(0).getToVertexHandle(), is(e0.getToVertexHandle()));
		assertThat(cursor.getResults().get(0).getEdgeLabel(), is(e0.getEdgeLabel()));
		assertThat(cursor.getResults().get(0).getEntity().getX(), is(e0.getEntity().getX()));
		assertThat(cursor.getResults().get(0).getEntity().getY(), is(e0.getEntity().getY()));
		assertThat(cursor.getResults().get(0).getEntity().getZ(), is(e0.getEntity().getZ()));
		
	}


	@Test
	public void test_get_edges_v_direction_in() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		DocumentEntity<TestComplexEntity01> v3 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc3", 12), null);
		DocumentEntity<TestComplexEntity01> v4 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc4", 12), null);
		
		EdgeEntity<TestComplexEntity02> e1 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,20,100), "e1", null);
		EdgeEntity<TestComplexEntity02> e2 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,20,200), "e2", null);
		EdgeEntity<TestComplexEntity02> e3 = driver.createEdge("g1", null, v2.getDocumentHandle(), v3.getDocumentHandle(), new TestComplexEntity02(1,30,300), "e3", null);
		EdgeEntity<TestComplexEntity02> e4 = driver.createEdge("g1", null, v4.getDocumentHandle(), v3.getDocumentHandle(), new TestComplexEntity02(1,30,400), "e4", null);
		
		// V2[IN]
		CursorEntity<EdgeEntity<TestComplexEntity02>> cursor = driver.getEdges(
				"g1", v2.getDocumentKey(), TestComplexEntity02.class,
				null, null, true,
				Direction.IN, null);
		assertThat(cursor.getCount(), is(2));
		assertThat(cursor.getCode(), is(201));
		assertThat(cursor.isError(), is(false));
		assertThat(cursor.hasMore(), is(false));
		assertThat(cursor.getCursorId(), is(-1L));

		
		assertThat(cursor.getResults().size(), is(2));
		assertThat(asSet(cursor.getResults()), is(asSet(e1.getDocumentKey(), e2.getDocumentKey())));
		
	}

	@Test
	public void test_get_edges_v_direction_out() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		DocumentEntity<TestComplexEntity01> v3 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc3", 12), null);
		DocumentEntity<TestComplexEntity01> v4 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc4", 12), null);
		
		EdgeEntity<TestComplexEntity02> e1 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,20,100), "e1", null);
		EdgeEntity<TestComplexEntity02> e2 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,20,200), "e2", null);
		EdgeEntity<TestComplexEntity02> e3 = driver.createEdge("g1", null, v2.getDocumentHandle(), v3.getDocumentHandle(), new TestComplexEntity02(1,30,300), "e3", null);
		EdgeEntity<TestComplexEntity02> e4 = driver.createEdge("g1", null, v4.getDocumentHandle(), v3.getDocumentHandle(), new TestComplexEntity02(1,30,400), "e4", null);

		// V2[OUT]
		CursorEntity<EdgeEntity<TestComplexEntity02>> cursor = driver.getEdges(
				"g1", v2.getDocumentKey(), TestComplexEntity02.class,
				null, null, true,
				Direction.OUT, null);
		
		assertThat(cursor.getCount(), is(1));
		assertThat(cursor.getCode(), is(201));
		assertThat(cursor.isError(), is(false));
		assertThat(cursor.hasMore(), is(false));
		assertThat(cursor.getCursorId(), is(-1L));
		
		assertThat(cursor.getResults().size(), is(1));
		assertThat(asSet(cursor.getResults()), is(asSet(e3.getDocumentKey())));
		
	}

	@Test
	public void test_get_edges_v_direction_any() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		DocumentEntity<TestComplexEntity01> v3 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc3", 12), null);
		DocumentEntity<TestComplexEntity01> v4 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc4", 12), null);
		
		EdgeEntity<TestComplexEntity02> e1 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,20,100), "e1", null);
		EdgeEntity<TestComplexEntity02> e2 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,20,200), "e2", null);
		EdgeEntity<TestComplexEntity02> e3 = driver.createEdge("g1", null, v2.getDocumentHandle(), v3.getDocumentHandle(), new TestComplexEntity02(1,30,300), "e3", null);
		EdgeEntity<TestComplexEntity02> e4 = driver.createEdge("g1", null, v4.getDocumentHandle(), v3.getDocumentHandle(), new TestComplexEntity02(1,30,400), "e4", null);

		// V2[ANY]
		CursorEntity<EdgeEntity<TestComplexEntity02>> cursor = driver.getEdges(
				"g1", v2.getDocumentKey(), TestComplexEntity02.class,
				null, null, true,
				Direction.ANY, null);
		
		assertThat(cursor.getCount(), is(3));
		assertThat(cursor.getCode(), is(201));
		assertThat(cursor.isError(), is(false));
		assertThat(cursor.hasMore(), is(false));
		assertThat(cursor.getCursorId(), is(-1L));
		
		assertThat(cursor.getResults().size(), is(3));
		assertThat(asSet(cursor.getResults()), is(asSet(e1.getDocumentKey(), e2.getDocumentKey(), e3.getDocumentKey())));
		
	}

	@Test
	public void test_get_edges_v_direction_in_filter() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		DocumentEntity<TestComplexEntity01> v3 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc3", 12), null);
		DocumentEntity<TestComplexEntity01> v4 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc4", 12), null);
		
		EdgeEntity<TestComplexEntity02> e1 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,20,100), "e1", null);
		EdgeEntity<TestComplexEntity02> e2 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,20,200), "e2", null);
		EdgeEntity<TestComplexEntity02> e3 = driver.createEdge("g1", null, v2.getDocumentHandle(), v3.getDocumentHandle(), new TestComplexEntity02(1,30,300), "e3", null);
		EdgeEntity<TestComplexEntity02> e4 = driver.createEdge("g1", null, v4.getDocumentHandle(), v3.getDocumentHandle(), new TestComplexEntity02(1,30,400), "e4", null);
		EdgeEntity<TestComplexEntity02> e5 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,40,500), "e5", null);
		EdgeEntity<TestComplexEntity02> e6 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,40,600), "e6", null);
		EdgeEntity<TestComplexEntity02> e7 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,40,700), "e7", null);
		EdgeEntity<TestComplexEntity02> e8 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,40,800), "e8", null);
		
		// V2[IN]
		CursorEntity<EdgeEntity<TestComplexEntity02>> cursor = driver.getEdges(
				"g1", v2.getDocumentKey(), TestComplexEntity02.class,
				null, null, true,
				Direction.IN, Arrays.asList("e1", "e5", "e6", "e8"), new FilterCondition("z", 600, ">="), new FilterCondition("z", 800, "<"));
		// IN: e[1,2,5,6,7,8]
		// LABEL: e[1,5,6,8]
		// FILTER: z>=600  ->e[6,7,8]
		// FILTER: z<800  ->e[6,7]
		// -> e6
		
		assertThat(cursor.getCount(), is(1));
		assertThat(cursor.getCode(), is(201));
		assertThat(cursor.isError(), is(false));
		assertThat(cursor.hasMore(), is(false));
		assertThat(cursor.getCursorId(), is(-1L));

		assertThat(cursor.getResults().size(), is(1));
		assertThat(asSet(cursor.getResults()), is(asSet(e6.getDocumentKey())));
		
	}

	
	@Test
	public void test_get_edges_v_direction_in_filter_rs() throws ArangoException {
		
		GraphEntity g1 = driver.createGraph("g1", "vcol1", "ecol1", true);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("v1-user", "desc1", 10), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc2", 12), null);
		DocumentEntity<TestComplexEntity01> v3 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc3", 12), null);
		DocumentEntity<TestComplexEntity01> v4 = driver.createVertex("g1", new TestComplexEntity01("v2-user", "desc4", 12), null);
		
		EdgeEntity<TestComplexEntity02> e1 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,20,100), "e1", null);
		EdgeEntity<TestComplexEntity02> e2 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,20,200), "e2", null);
		EdgeEntity<TestComplexEntity02> e3 = driver.createEdge("g1", null, v2.getDocumentHandle(), v3.getDocumentHandle(), new TestComplexEntity02(1,30,300), "e3", null);
		EdgeEntity<TestComplexEntity02> e4 = driver.createEdge("g1", null, v4.getDocumentHandle(), v3.getDocumentHandle(), new TestComplexEntity02(1,30,400), "e4", null);
		EdgeEntity<TestComplexEntity02> e5 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,40,500), "e5", null);
		EdgeEntity<TestComplexEntity02> e6 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,40,600), "e6", null);
		EdgeEntity<TestComplexEntity02> e7 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,40,700), "e7", null);
		EdgeEntity<TestComplexEntity02> e8 = driver.createEdge("g1", null, v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1,40,800), "e8", null);
		
		// V2[IN]
		CursorResultSet<EdgeEntity<TestComplexEntity02>> rs = driver.getEdgesWithResultSet(
				"g1", v2.getDocumentKey(), TestComplexEntity02.class,
				null, null, true,
				Direction.IN, Arrays.asList("e1", "e5", "e6", "e8"), new FilterCondition("z", 600, ">="), new FilterCondition("z", 800, "<"));
		// IN: e[1,2,5,6,7,8]
		// LABEL: e[1,5,6,8]
		// FILTER: z>=600  ->e[6,7,8]
		// FILTER: z<800  ->e[6,7]
		// -> e6
		
		assertThat(rs.getTotalCount(), is(1));
		
		EdgeEntity<TestComplexEntity02> edge = rs.next();

		assertThat(edge.getDocumentHandle(), is(e6.getDocumentHandle()));
		assertThat(edge.getDocumentRevision(), is(e6.getDocumentRevision()));
		assertThat(edge.getDocumentKey(), is(e6.getDocumentKey()));
		assertThat(edge.getFromVertexHandle(), is(e6.getFromVertexHandle()));
		assertThat(edge.getToVertexHandle(), is(e6.getToVertexHandle()));
		assertThat(edge.getEdgeLabel(), is(e6.getEdgeLabel()));
		assertThat(edge.getEntity().getX(), is(e6.getEntity().getX()));
		assertThat(edge.getEntity().getY(), is(e6.getEntity().getY()));
		assertThat(edge.getEntity().getZ(), is(e6.getEntity().getZ()));

		
	}

	
}
