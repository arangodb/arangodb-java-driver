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

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * 
 */
@Ignore
public class BaseGraphTest extends BaseTest {

	public BaseGraphTest(ArangoConfigure configure, ArangoDriver driver) {
		super(configure, driver);
	}

	@Before
	public void _before() throws ArangoException {
		String deleteAllGrpahsAndTheirCollections = 
				"var db = require('internal').db;\n"
				+ "var graph = require('org/arangodb/general-graph');\n"
				+ "graph._list().forEach(function(g){\n"
				+ "  graph._drop(g, true)\n"
				+ "});";
		driver.executeScript(deleteAllGrpahsAndTheirCollections);
	}
	
	@After
	public void after() throws ArangoException {
		String deleteAllGraphsAndTheirCollections = 
				"var db = require('internal').db;\n"
				+ "var graph = require('org/arangodb/general-graph');\n"
				+ "graph._list().forEach(function(g){\n"
				+ "  graph._drop(g, true)\n"
				+ "});";
		driver.executeScript(deleteAllGraphsAndTheirCollections);
	}
}
