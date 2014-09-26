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
import at.orz.arangodb.entity.FilterCondition;
import at.orz.arangodb.entity.GraphEntity;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ArangoDriverGraphVertices1Test extends BaseGraphTest {

	public ArangoDriverGraphVertices1Test(ArangoConfigure configure, ArangoDriver driver) {
		super(configure, driver);
	}
	
	@Test
	public void test_vertices() throws ArangoException {

		// create graph
		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		for (int i = 1; i <= 100; i++) {
			DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("user" + i, "desc" + i, (i/10)+1), null);
			assertThat(v1.getCode(), is(202));
		}
		
		CursorResultSet<DocumentEntity<TestComplexEntity01>> rs = driver.getVerticesWithResultSet("g1", TestComplexEntity01.class);
		assertThat(rs.getTotalCount(), is(-1)); // server does not return it...

		int i = 0;
		while (rs.hasNext()) {
			DocumentEntity<TestComplexEntity01> doc = rs.next();
			assertThat(doc.getDocumentKey(), is(notNullValue()));
			assertThat(doc.getDocumentHandle(), is(notNullValue()));
			assertThat(doc.getDocumentRevision(), is(not(0L)));
			i++;
		}
		rs.close();
		
		assertThat(i, is(100));
	}

	@Test
	public void test_vertices_count() throws ArangoException {

		// create graph
		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		for (int i = 1; i <= 10; i++) {
			DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("user" + i, "desc" + i, (i/10)+1), null);
			assertThat(v1.getCode(), is(202));
		}
		
		CursorResultSet<DocumentEntity<TestComplexEntity01>> rs = driver.getVerticesWithResultSet("g1", TestComplexEntity01.class, null, null, true);
		assertThat(rs.getTotalCount(), is(10)); // server does not return it...

		int i = 0;
		while (rs.hasNext()) {
			DocumentEntity<TestComplexEntity01> doc = rs.next();
			assertThat(doc.getDocumentKey(), is(notNullValue()));
			assertThat(doc.getDocumentHandle(), is(notNullValue()));
			assertThat(doc.getDocumentRevision(), is(not(0L)));
			i++;
		}
		rs.close();
		
		assertThat(i, is(10));
	}

	@Test
	public void test_vertices_batch() throws ArangoException {

		// create graph
		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		for (int i = 1; i <= 10; i++) {
			DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("user" + i, "desc" + i, (i/10)+1), null);
			assertThat(v1.getCode(), is(202));
		}
		
		Integer batchSize = 5;
		Integer limit = null;
		Boolean count = true;
		CursorEntity<DocumentEntity<TestComplexEntity01>> cursor = driver.getVertices("g1", TestComplexEntity01.class, batchSize, limit, count);
		assertThat(cursor.getCount(), is(10));
		
		assertThat(cursor.getCode(), is(201));
		assertThat(cursor.hasMore(), is(true));
		assertThat(cursor.getCursorId(), is(not(-1L)));
		assertThat(cursor.isError(), is(false));
		
		assertThat(cursor.getResults().size(), is(5));
		assertThat(cursor.getResults().get(0).getEntity(), instanceOf(TestComplexEntity01.class));
		assertThat(cursor.getResults().get(0).getEntity().getUser(), is(notNullValue()));
		assertThat(cursor.getResults().get(0).getEntity().getDesc(), is(notNullValue()));
		assertThat(cursor.getResults().get(0).getEntity().getAge(),  is(not(0)));
		
	}

	@Test
	public void test_vertices_many_roundtrip_resultset() throws ArangoException {

		// create graph
		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		for (int i = 1; i <= 100; i++) {
			DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("user" + i, "desc" + i, (i/10)+1), null);
			assertThat(v1.getCode(), is(202));
		}
		
		Integer batchSize = 5;
		Integer limit = null;
		Boolean count = true;
		CursorResultSet<DocumentEntity<TestComplexEntity01>> rs = driver.getVerticesWithResultSet("g1", TestComplexEntity01.class, batchSize, limit, count);
		
		int i = 0;
		while (rs.hasNext()) {
			DocumentEntity<TestComplexEntity01> doc = rs.next();
			assertThat(doc.getDocumentKey(), is(notNullValue()));
			assertThat(doc.getDocumentHandle(), is(notNullValue()));
			assertThat(doc.getDocumentRevision(), is(not(0L)));
			i++;
		}
		assertThat(i, is(100));
		
	}

	@Test
	public void test_vertices_batch_limit() throws ArangoException {

		// create graph
		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		for (int i = 1; i <= 10; i++) {
			DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("user" + i, "desc" + i, (i/10)+1), null);
			assertThat(v1.getCode(), is(202));
		}
		
		Integer batchSize = 5;
		Integer limit = 6;
		Boolean count = true;
		CursorEntity<DocumentEntity<TestComplexEntity01>> cursor = driver.getVertices("g1", TestComplexEntity01.class, batchSize, limit, count);
		assertThat(cursor.getCount(), is(6));
		
		assertThat(cursor.getCode(), is(201));
		assertThat(cursor.hasMore(), is(true));
		assertThat(cursor.getCursorId(), is(not(-1L)));
		assertThat(cursor.isError(), is(false));
		
		assertThat(cursor.getResults().size(), is(5));
		assertThat(cursor.getResults().get(0).getEntity(), instanceOf(TestComplexEntity01.class));
		assertThat(cursor.getResults().get(0).getEntity().getUser(), is(notNullValue()));
		assertThat(cursor.getResults().get(0).getEntity().getDesc(), is(notNullValue()));
		assertThat(cursor.getResults().get(0).getEntity().getAge(),  is(not(0)));
		
		// next round trip
		CursorEntity<DocumentEntity<TestComplexEntity01>> cursor2 = driver.continueQuery(cursor.getCursorId(), DocumentEntity.class, TestComplexEntity01.class);
		assertThat(cursor2.getCode(), is(200));
		assertThat(cursor2.isError(), is(false));
		assertThat(cursor2.getCount(), is(6));
		assertThat(cursor2.hasMore(), is(false));
		
		assertThat(cursor2.getResults().size(), is(1));
		assertThat(cursor2.getResults().get(0).getEntity(), instanceOf(TestComplexEntity01.class));
		assertThat(cursor2.getResults().get(0).getEntity().getUser(), is(notNullValue()));
		assertThat(cursor2.getResults().get(0).getEntity().getDesc(), is(notNullValue()));
		assertThat(cursor2.getResults().get(0).getEntity().getAge(),  is(not(0)));
		
	}

	
	@Test
	public void test_vertices_filter_properties1() throws ArangoException {

		// create graph
		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		for (int i = 1; i <= 100; i++) {
			DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("user" + i, "desc" + i, (i/10)+1), null);
			assertThat(v1.getCode(), is(202));
		}
		
		Integer batchSize = 2;
		Integer limit = 20;
		Boolean count = true;
		CursorEntity<DocumentEntity<TestComplexEntity01>> cursor = driver.getVertices("g1", TestComplexEntity01.class, batchSize, limit, count, 
				new FilterCondition("age", 5, "=="));
		assertThat(cursor.getCount(), is(10));
		
		assertThat(cursor.getCode(), is(201));
		assertThat(cursor.hasMore(), is(true));
		assertThat(cursor.getCursorId(), is(not(-1L)));
		assertThat(cursor.isError(), is(false));
		
		assertThat(cursor.getResults().size(), is(2));
		
	}

	@Test
	public void test_vertices_filter_properties2() throws ArangoException {

		// create graph
		GraphEntity g1 = driver.createGraph("g1","v1", "e1", null);
		for (int i = 1; i <= 100; i++) {
			DocumentEntity<TestComplexEntity01> v1 = driver.createVertex("g1", new TestComplexEntity01("user" + i, "desc" + (i%10), (i/10)+1), null);
			assertThat(v1.getCode(), is(202));
		}
		
		Integer batchSize = 2;
		Integer limit = 20;
		Boolean count = true;
		CursorEntity<DocumentEntity<TestComplexEntity01>> cursor = driver.getVertices("g1", TestComplexEntity01.class, batchSize, limit, count, 
				new FilterCondition("age", 5, ">"), new FilterCondition("desc", "desc3", "=="));
		assertThat(cursor.getCount(), is(5));
		
		assertThat(cursor.getCode(), is(201));
		assertThat(cursor.hasMore(), is(true));
		assertThat(cursor.getCursorId(), is(not(-1L)));
		assertThat(cursor.isError(), is(false));
		
		assertThat(cursor.getResults().size(), is(2));
		
	}

}
