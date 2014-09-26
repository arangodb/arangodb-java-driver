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

import org.junit.Before;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class BaseGraphTest extends BaseTest {

	public BaseGraphTest(ArangoConfigure configure, ArangoDriver driver) {
		super(configure, driver);
	}

	@Before
	public void _before() throws ArangoException {

		String deleteAllCollectionAndGraphCode = 
				"var db = require(\"internal\").db; " +
				"var Graph = require('org/arangodb/graph').Graph;\n" +
				"Graph.getAll().forEach(function(g){\n" +
				"  new Graph(g._key).drop();\n" +
				"});\n" +
				"db._collections().forEach(function(col){\n" +
				"  var name = col.name();\n" +
				"  if (name.indexOf('_') != 0) col.drop();\n" +
				"});\n"
				;
		driver.executeScript(deleteAllCollectionAndGraphCode);

	}


}
