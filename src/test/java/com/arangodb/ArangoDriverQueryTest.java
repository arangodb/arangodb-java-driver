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

package com.arangodb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.arangodb.entity.DefaultEntity;
import com.arangodb.entity.QueriesResultEntity;
import com.arangodb.entity.QueryEntity;
import com.arangodb.entity.QueryTrackingPropertiesEntity;

/**
 * @author a-brandt
 * 
 */
public class ArangoDriverQueryTest extends BaseTest {

	@Test
	public void test_getQueryTrackingProperties() throws ArangoException {
		final QueryTrackingPropertiesEntity queryTrackingProperties = driver.getQueryTrackingProperties();
		assertEquals(200, queryTrackingProperties.getStatusCode());
		assertNotNull(queryTrackingProperties.getEnabled());
		assertNotNull(queryTrackingProperties.getTrackSlowQueries());
		assertNotNull(queryTrackingProperties.getSlowQueryThreshold());
		assertNotNull(queryTrackingProperties.getMaxSlowQueries());
		assertNotNull(queryTrackingProperties.getMaxQueryStringLength());
	}

	@Test
	public void test_setQueryTrackingProperties() throws ArangoException {
		final QueryTrackingPropertiesEntity properties1 = driver.getQueryTrackingProperties();

		final Long maxQueryStringLength = properties1.getMaxQueryStringLength() + 10;

		properties1.setMaxQueryStringLength(maxQueryStringLength);

		final QueryTrackingPropertiesEntity properties2 = driver.setQueryTrackingProperties(properties1);

		assertEquals(200, properties2.getStatusCode());
		assertNotNull(properties2.getEnabled());
		assertNotNull(properties2.getTrackSlowQueries());
		assertNotNull(properties2.getSlowQueryThreshold());
		assertNotNull(properties2.getMaxSlowQueries());
		assertNotNull(properties2.getMaxQueryStringLength());
		assertEquals(maxQueryStringLength, properties2.getMaxQueryStringLength());
	}

	@Test
	public void test_getCurrentlyRunningQueries() throws ArangoException, InterruptedException {

		final String queryString = "return sleep(3)";

		final Thread thread1 = new Thread(new RunnableThread(driver.getDefaultDatabase(), configure, queryString),
				"thread1");
		thread1.start();

		Thread.sleep(1000);

		final QueriesResultEntity currentlyRunningQueries = driver.getCurrentlyRunningQueries();

		// wait for thread
		thread1.join();

		// check result
		assertEquals(200, currentlyRunningQueries.getStatusCode());
		final List<QueryEntity> queries = currentlyRunningQueries.getQueries();
		assertEquals(1, queries.size());
	}

	@Test
	public void test_getCurrentlyRunningQueriesWithoutDatabase() throws ArangoException, InterruptedException {

		final String queryString = "return sleep(3)";

		// create job in _system database
		final Thread thread1 = new Thread(new RunnableThread(null, configure, queryString), "thread1");
		thread1.start();

		Thread.sleep(1000);

		// search in default database
		final QueriesResultEntity currentlyRunningQueries = driver.getCurrentlyRunningQueries();

		// wait for thread
		thread1.join();

		// check result
		assertEquals(200, currentlyRunningQueries.getStatusCode());
		final List<QueryEntity> queries = currentlyRunningQueries.getQueries();
		assertEquals(0, queries.size());
	}

	@Test
	public void test_getCurrentlyRunningQueriesWithoutDatabase2() throws ArangoException, InterruptedException {

		final String queryString = "return sleep(3)";

		// create job in _system database
		final Thread thread1 = new Thread(new RunnableThread(null, configure, queryString), "thread1");
		thread1.start();

		Thread.sleep(1000);

		// search in _system database
		final QueriesResultEntity currentlyRunningQueries = driver.getCurrentlyRunningQueries(null);

		// wait for thread
		thread1.join();

		// check result
		assertEquals(200, currentlyRunningQueries.getStatusCode());
		final List<QueryEntity> queries = currentlyRunningQueries.getQueries();
		assertEquals(1, queries.size());
	}

	@Test
	public void test_getSlowQueries() throws ArangoException, InterruptedException {
		// set SlowQueryThreshold to 2
		final QueryTrackingPropertiesEntity properties1 = driver.getQueryTrackingProperties();
		properties1.setSlowQueryThreshold(2L);
		driver.setQueryTrackingProperties(properties1);

		// create a slow query
		driver.executeDocumentQuery("return sleep(3)", new HashMap<String, Object>(), null, Map.class);

		final QueriesResultEntity currentlyRunningQueries = driver.getSlowQueries();

		// check result
		assertEquals(200, currentlyRunningQueries.getStatusCode());
		final List<QueryEntity> queries = currentlyRunningQueries.getQueries();
		assertTrue(queries.size() > 0);
	}

	@Test
	public void test_deleteSlowQueries() throws ArangoException, InterruptedException {
		driver.deleteSlowQueries();
		QueriesResultEntity currentlyRunningQueries = driver.getSlowQueries();

		// check result
		assertEquals(200, currentlyRunningQueries.getStatusCode());
		List<QueryEntity> queries = currentlyRunningQueries.getQueries();
		assertEquals(0, queries.size());

		// set SlowQueryThreshold to 2
		final QueryTrackingPropertiesEntity properties1 = driver.getQueryTrackingProperties();
		properties1.setSlowQueryThreshold(2L);
		driver.setQueryTrackingProperties(properties1);

		// create a slow query
		driver.executeDocumentQuery("return sleep(3)", new HashMap<String, Object>(), null, Map.class);

		currentlyRunningQueries = driver.getSlowQueries();

		// check result
		assertEquals(200, currentlyRunningQueries.getStatusCode());
		queries = currentlyRunningQueries.getQueries();
		assertEquals(1, queries.size());
	}

	@Test
	public void test_killQuery() throws ArangoException, InterruptedException {

		final String queryString = "return sleep(3)";

		// create job in _system database
		final Thread thread1 = new Thread(new RunnableThread(driver.getDefaultDatabase(), configure, queryString),
				"thread1");
		thread1.start();

		Thread.sleep(1000);

		int numKilled = 0;

		// search in default database
		final QueriesResultEntity currentlyRunningQueries = driver.getCurrentlyRunningQueries();
		// check result
		assertEquals(200, currentlyRunningQueries.getStatusCode());
		final List<QueryEntity> queries = currentlyRunningQueries.getQueries();
		if (queries.size() > 0) {
			for (final QueryEntity qe : queries) {
				try {
					final DefaultEntity killQuery = driver.killQuery(qe.getId());
					assertEquals(200, killQuery.getStatusCode());
					numKilled++;
				} catch (final ArangoException e) {

				}
			}
		}

		// wait for thread
		thread1.join();
		assertTrue(queries.size() > 0);
		assertEquals(numKilled, queries.size());
	}

	class RunnableThread implements Runnable {

		Thread runner;
		ArangoDriver driver;
		String queryString;

		public RunnableThread(final String database, final ArangoConfigure configure, final String queryString) {
			this.driver = new ArangoDriver(configure);
			this.driver.setDefaultDatabase(database);
			this.queryString = queryString;
		}

		public RunnableThread(final String threadName) {
			runner = new Thread(this, threadName);
			runner.start();
		}

		@Override
		public void run() {
			try {
				driver.executeDocumentQuery(queryString, new HashMap<String, Object>(), null, Map.class);
			} catch (final ArangoException e) {
				if (e.getErrorNumber() != ErrorNums.ERROR_QUERY_KILLED) {
					e.printStackTrace();
				}
			}

		}
	}
}
