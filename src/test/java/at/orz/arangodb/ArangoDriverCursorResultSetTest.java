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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import at.orz.arangodb.ArangoException;
import at.orz.arangodb.CursorResultSet;
import at.orz.arangodb.util.MapBuilder;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ArangoDriverCursorResultSetTest extends BaseTest {

	public ArangoDriverCursorResultSetTest(ArangoConfigure configure, ArangoDriver driver) {
		super(configure, driver);
	}

	
	@Before
	public void setup() throws ArangoException {

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

	}
	
	@Test
	public void test1() throws ArangoException {
		
		//String query = "SELECT t FROM unit_test_query_test t WHERE t.age >= @age@ order by t.age";
		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();
		
		// 全件とれる範囲
		CursorResultSet<TestComplexEntity01> rs = driver.executeQueryWithResultSet(
				query, bindVars, TestComplexEntity01.class, true, 20);
		
		int count = 0;
		for (TestComplexEntity01 obj: rs) {
			assertThat(obj.getAge(), is(90+count));
			count++;
		}
		assertThat(count, is(10));
		
	}

	@Test
	public void test2() throws ArangoException {
		
		//String query = "SELECT t FROM unit_test_query_test t WHERE t.age >= @age@ order by t.age";
		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();
		
		CursorResultSet<TestComplexEntity01> rs = driver.executeQueryWithResultSet(
				query, bindVars, TestComplexEntity01.class, true, 10);
		
		int count = 0;
		for (TestComplexEntity01 obj: rs) {
			assertThat(obj.getAge(), is(90+count));
			count++;
		}
		assertThat(count, is(10));
		
	}

	@Test
	public void test3() throws ArangoException {
		
		//String query = "SELECT t FROM unit_test_query_test t WHERE t.age >= @age@ order by t.age";
		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();
		
		CursorResultSet<TestComplexEntity01> rs = driver.executeQueryWithResultSet(
				query, bindVars, TestComplexEntity01.class, true, 5);
		
		int count = 0;
		for (TestComplexEntity01 obj: rs) {
			assertThat(obj.getAge(), is(90+count));
			count++;
		}
		assertThat(count, is(10));
		
	}

	@Test
	public void test4() throws ArangoException {
		
		//String query = "SELECT t FROM unit_test_query_test t WHERE t.age >= @age@ order by t.age";
		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();
		
		CursorResultSet<TestComplexEntity01> rs = driver.executeQueryWithResultSet(
				query, bindVars, TestComplexEntity01.class, true, 3);
		
		int count = 0;
		for (TestComplexEntity01 obj: rs) {
			assertThat(obj.getAge(), is(90+count));
			count++;
		}
		assertThat(count, is(10));
		
	}
	
	@Test
	public void test5() throws ArangoException {
		
		//String query = "SELECT t FROM unit_test_query_test t WHERE t.age >= @age@ order by t.age";
		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();
		
		CursorResultSet<TestComplexEntity01> rs = driver.executeQueryWithResultSet(
				query, bindVars, TestComplexEntity01.class, true, 1);
		
		int count = 0;
		for (TestComplexEntity01 obj: rs) {
			assertThat(obj.getAge(), is(90+count));
			count++;
		}
		assertThat(count, is(10));
		
	}

	/**
	 * Iterableを使わないバージョン。
	 * @throws ArangoException
	 */
	@Test
	public void test6() throws ArangoException {
		
		//String query = "SELECT t FROM unit_test_query_test t WHERE t.age >= @age@ order by t.age";
		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();
		
		CursorResultSet<TestComplexEntity01> rs = driver.executeQueryWithResultSet(
				query, bindVars, TestComplexEntity01.class, true, 2);
		
		int count = 0;
		while (rs.hasNext()) {
			TestComplexEntity01 obj = rs.next();
			assertThat(obj.getAge(), is(90+count));
			count++;
		}
		rs.close();
		assertThat(count, is(10));
		
	}

	/**
	 * Iterableを使わないバージョン。
	 * 途中で終了。
	 * @throws ArangoException
	 */
	@Test
	public void test7() throws ArangoException {
		
		//String query = "SELECT t FROM unit_test_query_test t WHERE t.age >= @age@ order by t.age";
		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();
		
		CursorResultSet<TestComplexEntity01> rs = driver.executeQueryWithResultSet(
				query, bindVars, TestComplexEntity01.class, true, 2);
		
		int count = 0;
		while (rs.hasNext()) {
			TestComplexEntity01 obj = rs.next();
			assertThat(obj.getAge(), is(90+count));
			count++;
			if (count == 5) {
				rs.close();
				break;
			}
		}
		assertThat(count, is(5));
		
	}


}
