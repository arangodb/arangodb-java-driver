/*
 * Copyright (C) 2012 tamtam180
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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import at.orz.arangodb.ArangoException;
import at.orz.arangodb.entity.CursorEntity;
import at.orz.arangodb.entity.DefaultEntity;
import at.orz.arangodb.entity.ExplainEntity;
import at.orz.arangodb.util.MapBuilder;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ArangoDriverCursorTest extends BaseTest {
	
	public ArangoDriverCursorTest(ArangoConfigure configure, ArangoDriver driver) {
		super(configure, driver);
	}

	@Test
	public void test_validateQuery() throws ArangoException {
		
		CursorEntity<?> entity = driver.validateQuery(
				//"SELECT t FROM unit_test_cursor t WHERE t.name == @name@ && t.age >= @age@"
				"FOR t IN unit_test_cursor FILTER t.name == @name && t.age >= @age RETURN t"
				);
		
		assertThat(entity.getCode(), is(200));
		assertThat(entity.getBindVars().size(), is(2));
		assertThat(entity.getBindVars().get(0), is("name"));
		assertThat(entity.getBindVars().get(1), is("age"));
		
	}

	@Test
	public void test_validateQuery_400_1() throws ArangoException {
		
		// =じゃなくて==じゃないとダメ。文法間違いエラー
		CursorEntity<?> entity = driver.validateQuery(
				//"SELECT t FROM unit_test_cursor t WHERE t.name = @name@"
				"FOR t IN unit_test_cursor FILTER t.name = @name@"
				);
		
		assertThat(entity.getCode(), is(400));
		assertThat(entity.getErrorNumber(), is(1501));
		
	}

	@Test
	@Ignore
	public void test_validateQuery_400_2() throws ArangoException {
	}

	@Test
	public void test_explainQuery() throws ArangoException {

		// create collection
		String collectionName = "unit_test_query_test";
		try {
			driver.createCollection(collectionName);
		} catch (ArangoException e) {}
		
		// TODO no collection
		// TODO bind null
		ExplainEntity entity = driver.explainQuery(
				"FOR t IN unit_test_query_test FILTER t.name == 'aaa' && t.age >= 999 RETURN t",
				null
				);
		
		// {"id":1,"loopLevel":1,"type":"for","resultVariable":"t","expression":{"type":"collection","value":"unit_test_query_test","extra":{"accessType":"all"}}},
		// {"id":2,"loopLevel":1,"type":"filter","expression":{"type":"expression","value":"t.name == \"aaa\" && t.age >= 999"}},
		// {"id":3,"loopLevel":1,"type":"return","expression":{"type":"reference","value":"t"}}
		assertThat(entity.getCode(), is(200));
		assertThat(entity.getPlan().size(), is(3));
		
		assertThat(entity.getPlan().get(0).getId(), is(1L));
		assertThat(entity.getPlan().get(0).getLoopLevel(), is(1));
		assertThat(entity.getPlan().get(0).getType(), is("for"));
		assertThat(entity.getPlan().get(0).getResultVariable(), is("t"));
		assertThat(entity.getPlan().get(0).getExpression().getType(), is("collection"));
		assertThat(entity.getPlan().get(0).getExpression().getValue(), is("unit_test_query_test"));
		assertThat(entity.getPlan().get(0).getExpression().getExtra().size(), is(1));
		assertThat((String)entity.getPlan().get(0).getExpression().getExtra().get("accessType"), is("all"));

		assertThat(entity.getPlan().get(1).getId(), is(2L));
		assertThat(entity.getPlan().get(1).getLoopLevel(), is(1));
		assertThat(entity.getPlan().get(1).getType(), is("filter"));
		assertThat(entity.getPlan().get(1).getExpression().getType(), is("expression"));
		assertThat(entity.getPlan().get(1).getExpression().getValue(), is("t.name == \"aaa\" && t.age >= 999"));
		assertThat(entity.getPlan().get(1).getExpression().getExtra(), is(nullValue()));

		assertThat(entity.getPlan().get(2).getId(), is(3L));
		assertThat(entity.getPlan().get(2).getLoopLevel(), is(1));
		assertThat(entity.getPlan().get(2).getType(), is("return"));
		assertThat(entity.getPlan().get(2).getExpression().getType(), is("reference"));
		assertThat(entity.getPlan().get(2).getExpression().getValue(), is("t"));
		assertThat(entity.getPlan().get(2).getExpression().getExtra(), is(nullValue()));

	}

	@Test
	public void test_explainQuery_with_bindvars() throws ArangoException {

		// create collection
		String collectionName = "unit_test_query_test";
		try {
			driver.createCollection(collectionName);
		} catch (ArangoException e) {}
		
		ExplainEntity entity = driver.explainQuery(
				"FOR t IN unit_test_query_test FILTER t.name == @name && t.age >= @age RETURN t",
				new MapBuilder().put("name", "aaa").put("age", 999).get()
				);
		
		assertThat(entity.getCode(), is(200));
		assertThat(entity.getPlan().size(), is(3));
		
		assertThat(entity.getPlan().get(0).getId(), is(1L));
		assertThat(entity.getPlan().get(0).getLoopLevel(), is(1));
		assertThat(entity.getPlan().get(0).getType(), is("for"));
		assertThat(entity.getPlan().get(0).getResultVariable(), is("t"));
		assertThat(entity.getPlan().get(0).getExpression().getType(), is("collection"));
		assertThat(entity.getPlan().get(0).getExpression().getValue(), is("unit_test_query_test"));
		assertThat(entity.getPlan().get(0).getExpression().getExtra().size(), is(1));
		assertThat((String)entity.getPlan().get(0).getExpression().getExtra().get("accessType"), is("all"));

		assertThat(entity.getPlan().get(1).getId(), is(2L));
		assertThat(entity.getPlan().get(1).getLoopLevel(), is(1));
		assertThat(entity.getPlan().get(1).getType(), is("filter"));
		assertThat(entity.getPlan().get(1).getExpression().getType(), is("expression"));
		assertThat(entity.getPlan().get(1).getExpression().getValue(), is("t.name == \"aaa\" && t.age >= 999"));
		assertThat(entity.getPlan().get(1).getExpression().getExtra(), is(nullValue()));

		assertThat(entity.getPlan().get(2).getId(), is(3L));
		assertThat(entity.getPlan().get(2).getLoopLevel(), is(1));
		assertThat(entity.getPlan().get(2).getType(), is("return"));
		assertThat(entity.getPlan().get(2).getExpression().getType(), is("reference"));
		assertThat(entity.getPlan().get(2).getExpression().getValue(), is("t"));
		assertThat(entity.getPlan().get(2).getExpression().getExtra(), is(nullValue()));

	}

	@Test
	public void test_explainQuery_404() throws ArangoException {

		String collectionName = "unit_test_query_test";
		try {
			driver.deleteCollection(collectionName);
		} catch (ArangoException e) {}
		
		try {
			driver.explainQuery(
					"FOR t IN unit_test_query_test FILTER t.name == 'aaa' && t.age >= 999 RETURN t",
					null
					);
			fail("not raise error.");
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorNumber(), is(1203));
			assertThat(e.getMessage(), containsString("collection not found"));
		}
	}

	@Test
	public void test_explainQuery_no_bindvar() throws ArangoException {

		String collectionName = "unit_test_query_test";
		try {
			driver.createCollection(collectionName);
		} catch (ArangoException e) {}
		
		try {
			driver.explainQuery(
					"FOR t IN unit_test_query_test FILTER t.name == @name && t.age >= @age RETURN t",
					null
					);
			fail("not raise error.");
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(400));
			assertThat(e.getErrorNumber(), is(1551));
			assertThat(e.getMessage(), containsString("no value specified for declared bind parameter"));
		}
	}

	@Test
	public void test_explainQuery_invalid_query() throws ArangoException {

		String collectionName = "unit_test_query_test";
		try {
			driver.createCollection(collectionName);
		} catch (ArangoException e) {}
		
		try {
			driver.explainQuery(
					"FOR t IN unit_test_query_test FILTER t.name ===== 'xx' && t.age >= 99 RETURN t",
					null
					);
			fail("not raise error.");
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(400));
			assertThat(e.getErrorNumber(), is(1501));
			assertThat(e.getMessage(), containsString("syntax error"));
		}
	}

	@Test
	public void test_executeQuery() throws ArangoException {
		
		// Collectionを作る
		String collectionName = "unit_test_query_test";
		try {
			driver.createCollection(collectionName);
		} catch (ArangoException e) {}
		driver.truncateCollection(collectionName);
		
		// テストデータを作る
		for (int i = 0; i < 100; i++) {
			TestComplexEntity01 value = new TestComplexEntity01(
					"user_" + (i % 10), 
					"desc" + (i % 10), 
					i);
			driver.createDocument(collectionName, value, null, null);
		}
		
		//String query = "SELECT t FROM unit_test_query_test t WHERE t.age >= @age@";
		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();
		
		// 全件とれる範囲
		{
			CursorEntity<TestComplexEntity01> result = driver.<TestComplexEntity01>executeQuery(
					query, bindVars, TestComplexEntity01.class, true, 20);
			assertThat(result.size(), is(10));
			assertThat(result.getCount(), is(10));
			assertThat(result.hasMore(), is(false));
		}
		
	}

	@Test
	public void test_executeQuery_2() throws ArangoException {
		
		// Collectionを作る
		String collectionName = "unit_test_query_test";
		try {
			driver.createCollection(collectionName);
		} catch (ArangoException e) {}
		driver.truncateCollection(collectionName);
		
		// テストデータを作る
		for (int i = 0; i < 100; i++) {
			TestComplexEntity01 value = new TestComplexEntity01(
					"user_" + (i % 10), 
					"desc" + (i % 10), 
					i);
			driver.createDocument(collectionName, value, null, null);
		}
		
		//String query = "SELECT t FROM unit_test_query_test t WHERE t.age >= @age@";
		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();
		
		// ちまちまとる範囲
		long cursorId;
		{
			CursorEntity<TestComplexEntity01> result = driver.executeQuery(
					query, bindVars, TestComplexEntity01.class, true, 3);
			assertThat(result.size(), is(3));
			assertThat(result.getCount(), is(10));
			assertThat(result.hasMore(), is(true));
			assertThat(result.getCursorId(), is(not(-1L)));
			assertThat(result.getCursorId(), is(not(0L)));
			
			cursorId = result.getCursorId();
		}
		
		// 次のRoundTrip
		{
			CursorEntity<TestComplexEntity01> result = driver.continueQuery(
					cursorId, TestComplexEntity01.class);
			assertThat(result.size(), is(3));
			assertThat(result.getCount(), is(10));
			assertThat(result.hasMore(), is(true));
		}

		// 次のRoundTrip
		{
			CursorEntity<TestComplexEntity01> result = driver.continueQuery(
					cursorId, TestComplexEntity01.class);
			assertThat(result.size(), is(3));
			assertThat(result.getCount(), is(10));
			assertThat(result.hasMore(), is(true));
		}

		// 次のRoundTrip
		{
			CursorEntity<TestComplexEntity01> result = driver.continueQuery(
					cursorId, TestComplexEntity01.class);
			assertThat(result.size(), is(1));
			assertThat(result.getCount(), is(10));
			assertThat(result.hasMore(), is(false));
		}
		
		// 削除
		{
			DefaultEntity result = driver.finishQuery(cursorId);
		}
		
	}

	
}
