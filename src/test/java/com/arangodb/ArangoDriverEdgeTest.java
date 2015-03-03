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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.EdgeEntity;

/**
 * @author tamtam180 - kirscheless at gmail.com
 */
public class ArangoDriverEdgeTest extends BaseTest {

  public ArangoDriverEdgeTest(ArangoConfigure configure, ArangoDriver driver) {
    super(configure, driver);
  }

  final String collectionName = "unit_test_edge_collection_EdgeTest";
  final String collectionName2 = "unit_test_normal_collection_EdgeTest";

  @Before
  public void before() throws ArangoException {
    try {
      driver.deleteCollection(collectionName);
    } catch (ArangoException e) {
    }
    try {
      driver.deleteCollection(collectionName2);
    } catch (ArangoException e) {
    }
  }

  @After
  public void after() throws ArangoException {
    try {
      driver.deleteCollection(collectionName);
    } catch (ArangoException e) {
    }
    try {
      driver.deleteCollection(collectionName2);
    } catch (ArangoException e) {
    }
  }

  @Test
  public void test_create_normal() throws ArangoException {

    TestComplexEntity01 value = new TestComplexEntity01("user", "desc", 42);
    DocumentEntity<TestComplexEntity01> fromDoc = driver.createDocument(collectionName2, value, true, true);
    DocumentEntity<TestComplexEntity01> toDoc = driver.createDocument(collectionName2, value, true, true);
    
    EdgeEntity<TestComplexEntity01> doc = driver.createEdge(
        databaseName, 
        collectionName, 
        value, 
        fromDoc.getDocumentHandle(), 
        toDoc.getDocumentHandle(), 
        true, 
        true);

    assertThat(doc.getDocumentKey(), is(notNullValue()));
    assertThat(doc.getDocumentHandle(), is(collectionName + "/" + doc.getDocumentKey()));
    assertThat(doc.getDocumentRevision(), is(not(0L)));

  }

}
