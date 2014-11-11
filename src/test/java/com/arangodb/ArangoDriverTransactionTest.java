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

import com.arangodb.entity.*;
import com.arangodb.util.MapBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * 
 */
public class ArangoDriverTransactionTest extends BaseTest {

  public class ParamObject {
    private String a = "a";

    private String b = "b";

    private int i = 3;

    public String getA() {
      return a;
    }

    public void setA(String a) {
      this.a = a;
    }

    public String getB() {
      return b;
    }

    public void setB(String b) {
      this.b = b;
    }

    public int getI() {
      return i;
    }

    public void setI(int i) {
      this.i = i;
    }
  }

  public ArangoDriverTransactionTest(ArangoConfigure configure, ArangoDriver driver) {
    super(configure, driver);
  }

  @Before
  public void setup() throws ArangoException {
    TestComplexEntity01 value = new TestComplexEntity01("user-" + 9999, "desc:" + 9999, 9999);
    DocumentEntity<TestComplexEntity01> doc = driver.createDocument("someCollection", value, true, false);
  }

  @After
  public void teardown() throws ArangoException {
    try {
      driver.deleteCollection("someCollection");
    } catch (ArangoException e) {

    }
  }


  @Test
  public void test_Transaction() throws ArangoException {


    TransactionEntity transaction = driver.createTransaction("function (params) {return params;}");
    transaction.setParams(5);
    TransactionResultEntity result = driver.executeTransaction(transaction) ;

    assertThat(result.getResultAsDouble(), is(5.0));
    assertThat(result.getStatusCode(), is(200));
    assertThat(result.getCode(), is(200));
    assertThat(result.isError(), is(false));


    transaction = driver.createTransaction("function (params) {" +
      "var db = require('internal').db;" +
      "return db.someCollection.all().toArray()[0];" +
      "}");
    transaction.addReadCollection("someCollection");
    result = driver.executeTransaction(transaction);

    assertThat(result.getStatusCode(), is(200));
    assertThat(result.getCode(), is(200));
    assertThat(result.isError(), is(false));

    transaction = driver.createTransaction("function (params) {return params;}");
    transaction.setParams(5);
    result = driver.executeTransaction(transaction) ;

    assertThat(result.getResultAsInt(), is(5));
    assertThat(result.getStatusCode(), is(200));
    assertThat(result.getCode(), is(200));
    assertThat(result.isError(), is(false));

    transaction.setParams(true);
    result = driver.executeTransaction(transaction) ;

    assertThat(result.<Boolean>getResult(), is(true));
    assertThat(result.getStatusCode(), is(200));
    assertThat(result.getCode(), is(200));
    assertThat(result.isError(), is(false));

    transaction.setParams("Hans");
    result = driver.executeTransaction(transaction) ;

    assertThat(result.<String>getResult(), is("Hans"));
    assertThat(result.getStatusCode(), is(200));
    assertThat(result.getCode(), is(200));
    assertThat(result.isError(), is(false));

  }


}
