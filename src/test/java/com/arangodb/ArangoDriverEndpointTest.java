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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.arangodb.entity.BooleanResultEntity;
import com.arangodb.entity.Endpoint;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ArangoDriverEndpointTest extends BaseTest {

	private static final String DB = "db";
	private static final String MYDB = "mydb";
	private static final String MYDB1 = "mydb1";
	private static final String MYDB2 = "mydb2";
	private static final String[] DB_S = new String[] { DB, MYDB, MYDB1, MYDB2 };

	@Before
	public void _before() {
		for (final String db : DB_S) {
			try {
				driver.deleteDatabase(db);
			} catch (final ArangoException e) {
			}
			try {
				driver.createDatabase(db);
			} catch (final ArangoException e) {
			}
		}
	}

	@After
	public void _after() {
		for (final String db : DB_S) {
			try {
				driver.deleteDatabase(db);
			} catch (final ArangoException e) {
			}
		}
	}

	@Test
	public void test_create_endpoint() throws ArangoException {
		final BooleanResultEntity result = driver.createEndpoint("tcp://0.0.0.0:18529", DB);
		assertThat(result.getResult(), is(true));
	}

	@Test
	public void test_create_endpoint_dup() throws ArangoException {

		final BooleanResultEntity result1 = driver.createEndpoint("tcp://0.0.0.0:18529", DB);
		assertThat(result1.getResult(), is(true));

		final BooleanResultEntity result2 = driver.createEndpoint("tcp://0.0.0.0:18529", DB);
		assertThat(result2.getResult(), is(true));

	}

	@Test
	public void test_get_endpoints() throws ArangoException {

		final BooleanResultEntity result1 = driver.createEndpoint("tcp://0.0.0.0:18530", DB);
		assertThat(result1.getResult(), is(true));

		final BooleanResultEntity result2 = driver.createEndpoint("tcp://0.0.0.0:18531", MYDB1, MYDB2, MYDB);
		assertThat(result2.getResult(), is(true));

		final List<Endpoint> endpoints = driver.getEndpoints();
		// convert to Map
		final TreeMap<String, List<String>> endpointMap = new TreeMap<String, List<String>>();
		for (final Endpoint ep : endpoints) {
			endpointMap.put(ep.getEndpoint(), ep.getDatabases());
		}

		assertThat(endpointMap.get("tcp://0.0.0.0:18530"), is(Arrays.asList(DB)));
		assertThat(endpointMap.get("tcp://0.0.0.0:18531"), is(Arrays.asList(MYDB1, MYDB2, MYDB)));

	}

	@Test
	public void test_connect_new_endpoint() throws ArangoException {

		try {
			driver.createDatabase(MYDB2);
		} catch (final ArangoException e) {
		}

		final BooleanResultEntity result2 = driver.createEndpoint("tcp://0.0.0.0:18531", MYDB1, MYDB2, MYDB);
		assertThat(result2.getResult(), is(true));

		final ArangoConfigure configure = new ArangoConfigure();
		configure.getArangoHost().setPort(18531); // change port
		configure.init();
		try {
			final ArangoDriver driver = new ArangoDriver(configure, MYDB2);
			driver.getCollections();
		} finally {
			configure.shutdown();
		}

	}

	@Test
	public void test_delete() throws ArangoException {

		try {
			driver.createDatabase(MYDB2);
		} catch (final ArangoException e) {
		}

		final BooleanResultEntity result2 = driver.createEndpoint("tcp://0.0.0.0:18531", MYDB1, MYDB2, MYDB);
		assertThat(result2.getResult(), is(true));

		final BooleanResultEntity result3 = driver.deleteEndpoint("tcp://0.0.0.0:18531");
		assertThat(result3.getResult(), is(true));

		try {
			driver.deleteEndpoint("tcp://0.0.0.0:18531");
			fail();
		} catch (final ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorNumber(), is(1231));
		}

	}

}
