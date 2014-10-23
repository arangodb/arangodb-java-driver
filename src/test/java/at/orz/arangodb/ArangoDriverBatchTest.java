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

import at.orz.arangodb.entity.AqlFunctionsEntity;
import at.orz.arangodb.entity.DefaultEntity;
import at.orz.arangodb.entity.DocumentEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ArangoDriverBatchTest extends BaseTest {

	public ArangoDriverBatchTest(ArangoConfigure configure, ArangoDriver driver) {
		super(configure, driver);
	}

	private static Logger logger = LoggerFactory.getLogger(ArangoDriverCollectionTest.class);


	@Before
	public void before() throws ArangoException {
    logger.debug("----------");
  }

	@After
	public void after() {
		logger.debug("----------");
	}

	@Test
	public void test_BatchMode() throws ArangoException {

    driver.startBatchMode();
    driver.startBatchMode();
    DefaultEntity res = driver.createAqlFunction(
      "someNamespace::testCode", "function (celsius) { return celsius * 2.8 + 32; }"
    );
    assertNull(res);
    res = driver.createAqlFunction(
      "someNamespace::testC&&&&&&&&&&de", "function (celsius) { return celsius * 2.8 + 32; }"
    );
    assertNull(res);
    res = driver.createAqlFunction(
      "anotherNamespace::testCode", "function (celsius) { return celsius * 2.8 + 32; }"
    );
    assertNull(res);
    res = driver.createAqlFunction(
      "anotherNamespace::testCode2", "function (celsius) { return celsius * 2.8 + 32; }"
    );
    for (int i = 0; i < 100; i++) {
      TestComplexEntity01 value = new TestComplexEntity01("user-" + i, "data:" + i, i);
      driver.createDocument("blub", value, true, false);
    }

    driver.executeSimpleRangeWithDocument("_users", "an attrib", "a", "b", false, 1, 1, DocumentEntity.class);

    assertNull(res);
    AqlFunctionsEntity r = driver.getAqlFunctions(null);
    assertNull(r);
    r = driver.getAqlFunctions("someNamespace");
    res = driver.deleteAqlFunction("someNamespace::testCode", false);
    assertNull(res);
    res = driver.deleteAqlFunction("anotherNamespace", true);
    assertNull(res);
    AqlFunctionsEntity c = driver.getAqlFunctions("someNamespace");
    assertNull(c);
    //driver.executeBatch();
    driver.executeBatch();
  }

}

