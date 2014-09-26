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
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import at.orz.arangodb.entity.CursorEntity;
import at.orz.arangodb.entity.Direction;
import at.orz.arangodb.entity.DocumentEntity;
import at.orz.arangodb.entity.EdgeEntity;
import at.orz.arangodb.entity.FilterCondition;
import at.orz.arangodb.entity.GraphEntity;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ArangoDriverGraphVertices2Test extends BaseGraphTest {

	public ArangoDriverGraphVertices2Test(ArangoConfigure configure, ArangoDriver driver) {
		super(configure, driver);
	}

	private Set<String> asSet(Collection<? extends DocumentEntity<?>> collection) {
		HashSet<String> set = new HashSet<String>();
		for (DocumentEntity<?> e : collection) {
			set.add(e.getDocumentKey());
		}
		return set;
	}
	private Set<String> asSet(DocumentEntity<?> ...entities) {
		HashSet<String> set = new HashSet<String>();
		for (DocumentEntity<?> e : entities) {
			set.add(e.getDocumentKey());
		}
		return set;
	}

	
	@Test
	public void test_vertices_filter_direction_any() throws ArangoException {

		// create graph
		GraphEntity g1 = driver.createGraph("g1","vcol1", "ecol1", null);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity02(1 ,10, 100), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity02(2 ,20, 100), null);
		DocumentEntity<TestComplexEntity01> v3 = driver.createVertex("g1", new TestComplexEntity02(3 ,30, 100), null);
		DocumentEntity<TestComplexEntity01> v4 = driver.createVertex("g1", new TestComplexEntity02(4 ,40, 200), null);
		DocumentEntity<TestComplexEntity01> v5 = driver.createVertex("g1", new TestComplexEntity02(5 ,50, 200), null);
		DocumentEntity<TestComplexEntity01> v6 = driver.createVertex("g1", new TestComplexEntity02(6 ,60, 400), null);
		DocumentEntity<TestComplexEntity01> v7 = driver.createVertex("g1", new TestComplexEntity02(7 ,70, 400), null);
		
		EdgeEntity<TestComplexEntity01> e1 = driver.createEdge("g1", "e1", v1.getDocumentKey(), v2.getDocumentKey(), new TestComplexEntity02(1000, 10000, 10000), "e1", null);
		EdgeEntity<TestComplexEntity01> e2 = driver.createEdge("g1", "e2", v2.getDocumentKey(), v3.getDocumentKey(), new TestComplexEntity02(2000, 20000, 10000), "e2", null);
		EdgeEntity<TestComplexEntity01> e3 = driver.createEdge("g1", "e3", v5.getDocumentKey(), v2.getDocumentKey(), new TestComplexEntity02(3000, 30000, 10000), "e3", null);
		EdgeEntity<TestComplexEntity01> e4 = driver.createEdge("g1", "e4", v2.getDocumentKey(), v6.getDocumentKey(), new TestComplexEntity02(4000, 40000, 20000), "e4", null);
		EdgeEntity<TestComplexEntity01> e5 = driver.createEdge("g1", "e5", v7.getDocumentKey(), v2.getDocumentKey(), new TestComplexEntity02(5000, 50000, 20000), "e5", null);
		EdgeEntity<TestComplexEntity01> e6 = driver.createEdge("g1", "e6", v3.getDocumentKey(), v4.getDocumentKey(), new TestComplexEntity02(6000, 60000, 40000), "e6", null);
		EdgeEntity<TestComplexEntity01> e7 = driver.createEdge("g1", "e7", v6.getDocumentKey(), v7.getDocumentKey(), new TestComplexEntity02(7000, 70000, 40000), "e7", null);
		
		Integer batchSize = null;
		Integer limit = null;
		Boolean count = true;
		String vertexKey = v2.getDocumentKey();
		CursorEntity<DocumentEntity<TestComplexEntity01>> cursor = driver.getVertices(
				"g1", vertexKey, TestComplexEntity01.class, batchSize, limit, count,
				Direction.ANY, null);
		assertThat(cursor.getCount(), is(5));
		
		assertThat(cursor.getCode(), is(201));
		assertThat(cursor.hasMore(), is(false));
		assertThat(cursor.getCursorId(), is(-1L));
		assertThat(cursor.isError(), is(false));
		
		assertThat(cursor.getResults().size(), is(5));
		assertThat(asSet(cursor.getResults()), is(asSet(v1, v3, v5, v6, v7)));
		
	}

	
	@Test
	public void test_vertices_filter_direction_in() throws ArangoException {

		// create graph
		GraphEntity g1 = driver.createGraph("g1","vcol1", "ecol1", null);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity02(1 ,10, 100), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity02(2 ,20, 100), null);
		DocumentEntity<TestComplexEntity01> v3 = driver.createVertex("g1", new TestComplexEntity02(3 ,30, 100), null);
		DocumentEntity<TestComplexEntity01> v4 = driver.createVertex("g1", new TestComplexEntity02(4 ,40, 200), null);
		DocumentEntity<TestComplexEntity01> v5 = driver.createVertex("g1", new TestComplexEntity02(5 ,50, 200), null);
		DocumentEntity<TestComplexEntity01> v6 = driver.createVertex("g1", new TestComplexEntity02(6 ,60, 400), null);
		DocumentEntity<TestComplexEntity01> v7 = driver.createVertex("g1", new TestComplexEntity02(7 ,70, 400), null);
		
		EdgeEntity<TestComplexEntity01> e1 = driver.createEdge("g1", "e1", v1.getDocumentKey(), v2.getDocumentKey(), new TestComplexEntity02(1000, 10000, 10000), "e1", null);
		EdgeEntity<TestComplexEntity01> e2 = driver.createEdge("g1", "e2", v2.getDocumentKey(), v3.getDocumentKey(), new TestComplexEntity02(2000, 20000, 10000), "e2", null);
		EdgeEntity<TestComplexEntity01> e3 = driver.createEdge("g1", "e3", v5.getDocumentKey(), v2.getDocumentKey(), new TestComplexEntity02(3000, 30000, 10000), "e3", null);
		EdgeEntity<TestComplexEntity01> e4 = driver.createEdge("g1", "e4", v2.getDocumentKey(), v6.getDocumentKey(), new TestComplexEntity02(4000, 40000, 20000), "e4", null);
		EdgeEntity<TestComplexEntity01> e5 = driver.createEdge("g1", "e5", v7.getDocumentKey(), v2.getDocumentKey(), new TestComplexEntity02(5000, 50000, 20000), "e5", null);
		EdgeEntity<TestComplexEntity01> e6 = driver.createEdge("g1", "e6", v3.getDocumentKey(), v4.getDocumentKey(), new TestComplexEntity02(6000, 60000, 40000), "e6", null);
		EdgeEntity<TestComplexEntity01> e7 = driver.createEdge("g1", "e7", v6.getDocumentKey(), v7.getDocumentKey(), new TestComplexEntity02(7000, 70000, 40000), "e7", null);
		
		Integer batchSize = null;
		Integer limit = null;
		Boolean count = true;
		String vertexKey = v2.getDocumentKey();
		CursorEntity<DocumentEntity<TestComplexEntity01>> cursor = driver.getVertices(
				"g1", vertexKey, TestComplexEntity01.class, batchSize, limit, count,
				Direction.IN, null);
		assertThat(cursor.getCount(), is(3));
		
		assertThat(cursor.getCode(), is(201));
		assertThat(cursor.hasMore(), is(false));
		assertThat(cursor.getCursorId(), is(-1L));
		assertThat(cursor.isError(), is(false));
		
		assertThat(cursor.getResults().size(), is(3));
		assertThat(asSet(cursor.getResults()), is(asSet(v1, v5, v7)));
		
	}


	@Test
	public void test_vertices_filter_direction_out() throws ArangoException {

		// create graph
		GraphEntity g1 = driver.createGraph("g1","vcol1", "ecol1", null);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity02(1 ,10, 100), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity02(2 ,20, 100), null);
		DocumentEntity<TestComplexEntity01> v3 = driver.createVertex("g1", new TestComplexEntity02(3 ,30, 100), null);
		DocumentEntity<TestComplexEntity01> v4 = driver.createVertex("g1", new TestComplexEntity02(4 ,40, 200), null);
		DocumentEntity<TestComplexEntity01> v5 = driver.createVertex("g1", new TestComplexEntity02(5 ,50, 200), null);
		DocumentEntity<TestComplexEntity01> v6 = driver.createVertex("g1", new TestComplexEntity02(6 ,60, 400), null);
		DocumentEntity<TestComplexEntity01> v7 = driver.createVertex("g1", new TestComplexEntity02(7 ,70, 400), null);
		
		EdgeEntity<TestComplexEntity01> e1 = driver.createEdge("g1", "e1", v1.getDocumentKey(), v2.getDocumentKey(), new TestComplexEntity02(1000, 10000, 10000), "e1", null);
		EdgeEntity<TestComplexEntity01> e2 = driver.createEdge("g1", "e2", v2.getDocumentKey(), v3.getDocumentKey(), new TestComplexEntity02(2000, 20000, 10000), "e2", null);
		EdgeEntity<TestComplexEntity01> e3 = driver.createEdge("g1", "e3", v5.getDocumentKey(), v2.getDocumentKey(), new TestComplexEntity02(3000, 30000, 10000), "e3", null);
		EdgeEntity<TestComplexEntity01> e4 = driver.createEdge("g1", "e4", v2.getDocumentKey(), v6.getDocumentKey(), new TestComplexEntity02(4000, 40000, 20000), "e4", null);
		EdgeEntity<TestComplexEntity01> e5 = driver.createEdge("g1", "e5", v7.getDocumentKey(), v2.getDocumentKey(), new TestComplexEntity02(5000, 50000, 20000), "e5", null);
		EdgeEntity<TestComplexEntity01> e6 = driver.createEdge("g1", "e6", v3.getDocumentKey(), v4.getDocumentKey(), new TestComplexEntity02(6000, 60000, 40000), "e6", null);
		EdgeEntity<TestComplexEntity01> e7 = driver.createEdge("g1", "e7", v6.getDocumentKey(), v7.getDocumentKey(), new TestComplexEntity02(7000, 70000, 40000), "e7", null);
		
		Integer batchSize = null;
		Integer limit = null;
		Boolean count = true;
		String vertexKey = v2.getDocumentKey();
		CursorEntity<DocumentEntity<TestComplexEntity01>> cursor = driver.getVertices(
				"g1", vertexKey, TestComplexEntity01.class, batchSize, limit, count,
				Direction.OUT, null);
		assertThat(cursor.getCount(), is(2));
		
		assertThat(cursor.getCode(), is(201));
		assertThat(cursor.hasMore(), is(false));
		assertThat(cursor.getCursorId(), is(-1L));
		assertThat(cursor.isError(), is(false));
		
		assertThat(cursor.getResults().size(), is(2));
		assertThat(asSet(cursor.getResults()), is(asSet(v3, v6)));
		
	}

	@Test
	public void test_vertices_filter_label_direction() throws ArangoException {

		// create graph
		GraphEntity g1 = driver.createGraph("g1","vcol1", "ecol1", null);
		DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity02(1 ,10, 100), null);
		DocumentEntity<TestComplexEntity01> v2 = driver.createVertex("g1", new TestComplexEntity02(2 ,20, 100), null);
		DocumentEntity<TestComplexEntity01> v3 = driver.createVertex("g1", new TestComplexEntity02(3 ,30, 100), null);
		DocumentEntity<TestComplexEntity01> v4 = driver.createVertex("g1", new TestComplexEntity02(4 ,40, 200), null);
		DocumentEntity<TestComplexEntity01> v5 = driver.createVertex("g1", new TestComplexEntity02(5 ,50, 200), null);
		DocumentEntity<TestComplexEntity01> v6 = driver.createVertex("g1", new TestComplexEntity02(6 ,60, 400), null);
		DocumentEntity<TestComplexEntity01> v7 = driver.createVertex("g1", new TestComplexEntity02(7 ,70, 400), null);
		
		EdgeEntity<TestComplexEntity01> e1 = driver.createEdge("g1", "e1", v1.getDocumentKey(), v2.getDocumentKey(), new TestComplexEntity02(1000, 10000, 10000), "e1", null);
		EdgeEntity<TestComplexEntity01> e2 = driver.createEdge("g1", "e2", v2.getDocumentKey(), v3.getDocumentKey(), new TestComplexEntity02(2000, 20000, 10000), "e2", null);
		EdgeEntity<TestComplexEntity01> e3 = driver.createEdge("g1", "e3", v5.getDocumentKey(), v2.getDocumentKey(), new TestComplexEntity02(3000, 30000, 10000), "e3", null);
		EdgeEntity<TestComplexEntity01> e4 = driver.createEdge("g1", "e4", v2.getDocumentKey(), v6.getDocumentKey(), new TestComplexEntity02(4000, 40000, 20000), "e4", null);
		EdgeEntity<TestComplexEntity01> e5 = driver.createEdge("g1", "e5", v7.getDocumentKey(), v2.getDocumentKey(), new TestComplexEntity02(5000, 50000, 20000), "e5", null);
		EdgeEntity<TestComplexEntity01> e6 = driver.createEdge("g1", "e6", v3.getDocumentKey(), v4.getDocumentKey(), new TestComplexEntity02(6000, 60000, 40000), "e6", null);
		EdgeEntity<TestComplexEntity01> e7 = driver.createEdge("g1", "e7", v6.getDocumentKey(), v7.getDocumentKey(), new TestComplexEntity02(7000, 70000, 40000), "e7", null);
		
		Integer batchSize = null;
		Integer limit = null;
		Boolean count = true;
		String vertexKey = v2.getDocumentKey();
		CursorEntity<DocumentEntity<TestComplexEntity01>> cursor = driver.getVertices(
				"g1", vertexKey, TestComplexEntity01.class, batchSize, limit, count,
				Direction.IN, Arrays.asList("e3", "e5"), new FilterCondition("x", 3000, "=="));
		// DIRECTION -> e1, e5, e7
		// LABEL -> e3, e5
		// FILTER -> x == 3000 -> e3
		// -> [e3]
		// -> v2 -> [e3] -> [v5]
		
		
		assertThat(cursor.getCount(), is(1));
		
		assertThat(cursor.getCode(), is(201));
		assertThat(cursor.hasMore(), is(false));
		assertThat(cursor.getCursorId(), is(-1L));
		assertThat(cursor.isError(), is(false));
		
		assertThat(cursor.getResults().size(), is(1));
		assertThat(asSet(cursor.getResults()), is(asSet(v5)));
		
	}

	
}
