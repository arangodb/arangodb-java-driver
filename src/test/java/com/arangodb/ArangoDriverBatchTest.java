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

package com.arangodb;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.entity.*;

import java.util.Iterator;
import java.util.List;

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



  @Before
  public void before() throws ArangoException {
    for (String col: new String[]{"blub"}) {
      try {
        driver.deleteCollection(col);
      } catch (ArangoException e) {
      }
    }
  }

  @After
  public void after() {
  }

  @Test
  public void test_StartCancelExecuteBatchMode() throws ArangoException {

    driver.startBatchMode();
    String msg = "";
    try {
      driver.startBatchMode();
    } catch (ArangoException e) {
      msg = e.getErrorMessage();
    }
    assertThat(msg , is("BatchMode is already active."));

    driver.cancelBatchMode();
    msg = "";
    try {
      driver.cancelBatchMode();
    } catch (ArangoException e) {
      msg = e.getErrorMessage();
    }
    assertThat(msg , is("BatchMode is not active."));

    msg = "";
    try {
      driver.executeBatch();
    } catch (ArangoException e) {
      msg = e.getErrorMessage();
    }
    assertThat(msg , is("BatchMode is not active."));


  }



  @Test
  public void test_execBatchMode() throws ArangoException {

    driver.startBatchMode();

    BaseEntity res = driver.createAqlFunction(
      "someNamespace::testCode", "function (celsius) { return celsius * 2.8 + 32; }"
    );

    assertThat(res.getStatusCode(), is(206));
    assertThat(res.getRequestId() , is("request1"));

    res = driver.createAqlFunction(
      "someNamespace::testC&&&&&&&&&&de", "function (celsius) { return celsius * 2.8 + 32; }"
    );

    assertThat(res.getStatusCode(), is(206));
    assertThat(res.getRequestId(), is("request2"));

    res = driver.getAqlFunctions(null);
    assertThat(res.getStatusCode(), is(206));
    assertThat(res.getRequestId(), is("request3"));

    for (int i = 0; i < 10; i++) {
      TestComplexEntity01 value = new TestComplexEntity01("user-" + i, "data:" + i, i);
      res = driver.createDocument("blub", value, true, false);

      assertThat(res.getStatusCode(), is(206));
      assertThat(res.getRequestId(), is("request" + (4 + i)));
    }

    List<String> r = driver.getDocuments("blub");

    DefaultEntity result = driver.executeBatch();
    DefaultEntity created = driver.getBatchResponseByRequestId("request1");
    assertThat(created.getStatusCode() , is(201));
    AqlFunctionsEntity functions =  driver.getBatchResponseByRequestId("request3");
    assertThat(functions.getStatusCode() , is(200));
    assertThat(String.valueOf(functions.getAqlFunctions().keySet().toArray()[0]) , is("someNamespace::testCode"));
    for (int i = 0; i < 10; i++) {
      DocumentEntity<TestComplexEntity01>  resultComplex =  driver.getBatchResponseByRequestId("request" + (4+i));
      assertThat(resultComplex.getStatusCode() , is(202));
    }

    List<String> documents =  driver.getBatchResponseByRequestId("request14");
    assertThat(documents.size(), is(10));

  }

}


