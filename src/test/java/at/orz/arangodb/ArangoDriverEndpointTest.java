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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

import at.orz.arangodb.entity.BooleanResultEntity;
import at.orz.arangodb.entity.Endpoint;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ArangoDriverEndpointTest extends BaseTest {

	public ArangoDriverEndpointTest(ArangoConfigure configure, ArangoDriver driver) {
		super(configure, driver);
	}

	@Test
	public void test_create_endpoint() throws ArangoException {
		BooleanResultEntity result = driver.createEndpoint("tcp://0.0.0.0:18529", "db");
		assertThat(result.getResult(), is(true));
	}

	@Test
	public void test_create_endpoint_dup() throws ArangoException {

		BooleanResultEntity result1 = driver.createEndpoint("tcp://0.0.0.0:18529", "db");
		assertThat(result1.getResult(), is(true));

		BooleanResultEntity result2 = driver.createEndpoint("tcp://0.0.0.0:18529", "db");
		assertThat(result2.getResult(), is(true));

	}
	
	@Test
	public void test_get_endpoints() throws ArangoException {

		BooleanResultEntity result1 = driver.createEndpoint("tcp://0.0.0.0:18530", "db");
		assertThat(result1.getResult(), is(true));

		BooleanResultEntity result2 = driver.createEndpoint("tcp://0.0.0.0:18531", "mydb1", "mydb2", "mydb");
		assertThat(result2.getResult(), is(true));
		
		List<Endpoint> endpoints = driver.getEndpoints();
		// convert to Map
		TreeMap<String, List<String>> endpointMap = new TreeMap<String, List<String>>();
		for (Endpoint ep: endpoints) {
			endpointMap.put(ep.getEndpoint(), ep.getDatabases());
		}
		
		assertThat(endpointMap.get("tcp://0.0.0.0:18530"), is(Arrays.asList("db")));
		assertThat(endpointMap.get("tcp://0.0.0.0:18531"), is(Arrays.asList("mydb1", "mydb2", "mydb")));

	}

	@Test
	public void test_connect_new_endpoint() throws ArangoException {
		
		try {
			driver.createDatabase("mydb2");
		} catch(ArangoException e) {}
		
		BooleanResultEntity result2 = driver.createEndpoint("tcp://0.0.0.0:18531", "mydb1", "mydb2", "mydb");
		assertThat(result2.getResult(), is(true));
		
		ArangoConfigure configure = new ArangoConfigure();
		configure.setPort(18531); // change port
		configure.init();
		try {
			ArangoDriver driver = new ArangoDriver(configure, "mydb2");
			driver.getCollections();
		} finally {
			configure.shutdown();
		}

	}

	@Test
	public void test_delete() throws ArangoException {
		
		try {
			driver.createDatabase("mydb2");
		} catch(ArangoException e) {}
		
		BooleanResultEntity result2 = driver.createEndpoint("tcp://0.0.0.0:18531", "mydb1", "mydb2", "mydb");
		assertThat(result2.getResult(), is(true));

		BooleanResultEntity result3 = driver.deleteEndpoint("tcp://0.0.0.0:18531");
		assertThat(result3.getResult(), is(true));

		try {
			driver.deleteEndpoint("tcp://0.0.0.0:18531");
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorNumber(), is(1231));
		}

	}

}
