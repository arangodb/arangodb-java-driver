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

import com.arangodb.entity.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ArangoDriverAsyncTest extends BaseTest {

  public ArangoDriverAsyncTest(ArangoConfigure configure, ArangoDriver driver) {
    super(configure, driver);
  }

  @Before
  public void before() throws ArangoException {
    for (String col: new String[]{"blub"}) {
      try {
        driver.deleteCollection(col);
      } catch (ArangoException e) {
      }
      try {
        driver.stopAsyncMode();
      } catch (ArangoException e) {

      }
      AqlFunctionsEntity res = driver.getAqlFunctions(null);
      Iterator<String> it = res.getAqlFunctions().keySet().iterator();
      while(it.hasNext()) {
        driver.deleteAqlFunction(it.next(), false);
      }
    }
  }

  @After
  public void after() {
  }

  @Test
  public void test_StartCancelExecuteAsyncMode() throws ArangoException {

    driver.startAsyncMode(false);
    String msg = "";
    try {
      driver.startAsyncMode(false);
    } catch (ArangoException e) {
      msg = e.getErrorMessage();
    }
    assertThat(msg , is("Arango driver already set to asynchronous mode."));

    driver.stopAsyncMode();
    msg = "";
    try {
      driver.stopAsyncMode();
    } catch (ArangoException e) {
      msg = e.getErrorMessage();
    }
    assertThat(msg , is("Arango driver already set to synchronous mode."));

  }



  @Test
  public void test_execAsyncMode() throws ArangoException {

    driver.startAsyncMode(false);

    driver.createAqlFunction(
      "someNamespace::testCode", "function (celsius) { return celsius * 2.8 + 32; }"
    );

    assertNotNull(driver.getLastJobId());

    driver.createAqlFunction(
      "someNamespace::testC&&&&&&&&&&de", "function (celsius) { return celsius * 2.8 + 32; }"
    );

    assertThat(driver.getJobIds().size(), is(2));

    driver.getAqlFunctions(null);

    assertThat(driver.getJobIds().size(), is(3));

    for (int i = 0; i < 10; i++) {
      TestComplexEntity01 value = new TestComplexEntity01("user-" + i, "data:" + i, i);
      driver.createDocument("blub", value, true, false);
      assertThat(driver.getJobIds().size(), is(4 +i));
    }

    driver.stopAsyncMode();

    assertThat(driver.getJobIds().size(), is(13));

  }

  @Test
  public void test_execFireAndForgetMode() throws ArangoException {

    driver.startAsyncMode(true);

    assertThat(driver.getJobIds().size(), is(0));

    driver.createAqlFunction(
      "someNamespace::testCode", "function (celsius) { return celsius * 2.8 + 32; }"
    );

    assertThat(driver.getJobIds().size(), is(0));

    driver.createAqlFunction(
      "someNamespace::testC&&&&&&&&&&de", "function (celsius) { return celsius * 2.8 + 32; }"
    );

    assertThat(driver.getJobIds().size(), is(0));

    driver.stopAsyncMode();


  }


  @Test
  public void test_GetJobsMode() throws ArangoException {

    driver.deleteAllJobs();

    driver.startAsyncMode(false);

    driver.createAqlFunction(
      "someNamespace::testCode", "function (celsius) { return celsius * 2.8 + 32; }"
    );

    assertNotNull(driver.getLastJobId());

    driver.createAqlFunction(
      "someNamespace::testC&&&&&&&&&&de", "function (celsius) { return celsius * 2.8 + 32; }"
    );

    assertThat(driver.getJobIds().size(), is(2));

    driver.getAqlFunctions(null);

    assertThat(driver.getJobIds().size(), is(3));

    for (int i = 0; i < 10; i++) {
      TestComplexEntity01 value = new TestComplexEntity01("user-" + i, "data:" + i, i);
      driver.createDocument("blub", value, true, false);
      assertThat(driver.getJobIds().size(), is(4 +i));
    }

    driver.stopAsyncMode();
    List<String> jobs = driver.getJobs(JobsEntity.JobState.DONE, 10);
    jobs = driver.getJobs(JobsEntity.JobState.PENDING, 10);

    jobs = driver.getJobs(JobsEntity.JobState.DONE);


    driver.startAsyncMode(false);
    for (int i = 0; i < 100; i++) {
      TestComplexEntity01 value = new TestComplexEntity01("user-" + i, "data:" + i, i);
      driver.createDocument("blub", value, true, false);
    }
    driver.stopAsyncMode();
    driver.deleteExpiredJobs((int) (System.currentTimeMillis() / 2000L));
    jobs = driver.getJobs(JobsEntity.JobState.PENDING);

  }


  @Test
  public void test_GetJobsResult() throws ArangoException {

    driver.deleteAllJobs();

    driver.startAsyncMode(false);

    driver.createAqlFunction(
      "someNamespace::testCode", "function (celsius) { return celsius * 2.8 + 32; }"
    );

    String id1 = driver.getLastJobId();

    driver.createAqlFunction(
      "someNamespace::testC&&&&&&&&&&de", "function (celsius) { return celsius * 2.8 + 32; }"
    );

    String id2 = driver.getLastJobId();

    driver.getAqlFunctions(null);

    String id3 = driver.getLastJobId();

    List<String> ids = new ArrayList<String>();
    for (int i = 0; i < 10; i++) {
      TestComplexEntity01 value = new TestComplexEntity01("user-" + i, "data:" + i, i);
      driver.createDocument("blub", value, true, false);
      ids.add(driver.getLastJobId());
    }

    driver.stopAsyncMode();
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    DefaultEntity de = driver.getJobResult(id1);
    assertThat(de.getStatusCode() , is(201));

    DefaultEntity de2 = driver.getJobResult(id2);

    try {
      de2 = driver.getJobResult(id2);
    } catch (ArangoException e) {
      assertTrue(e.getErrorMessage().equals("No result for JobId."));
    }

    AqlFunctionsEntity functions = driver.getJobResult(id3);
    assertThat(functions.getStatusCode() , is(200));


    DocumentEntity<TestComplexEntity01>  resultComplex;

    for (String id : ids) {
      resultComplex  = driver.getJobResult(id);
      assertThat(resultComplex.getStatusCode() , is(202));
    }

  }


}


