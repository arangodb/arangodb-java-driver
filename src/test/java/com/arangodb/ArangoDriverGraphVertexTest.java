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
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.arangodb.entity.DeletedEntity;
import com.arangodb.entity.DocumentEntity;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * @author gschwab
 *
 */
public class ArangoDriverGraphVertexTest extends BaseGraphTest {

  String graphName = "UnitTestGraph";
  String collectionName = "UnitTestCollection";

  public ArangoDriverGraphVertexTest(ArangoConfigure configure, ArangoDriver driver) {
    super(configure, driver);
  }

  @Test
  public void test_create_vertex() throws ArangoException {

    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);

    DocumentEntity<TestComplexEntity01> vertex = driver.graphCreateVertex(
      this.graphName,
      "from1-1",
      new TestComplexEntity01("Homer", "Simpson", 38),
      true);
    assertThat(vertex.getDocumentHandle(), is(notNullValue()));
    assertThat(vertex.getDocumentRevision(), is(not(0L)));
    assertThat(vertex.getDocumentKey(), is(notNullValue()));
    assertThat(vertex.getEntity(), isA(TestComplexEntity01.class));
    DocumentEntity<TestComplexEntity01> document = driver.getDocument(
      vertex.getDocumentHandle(),
      TestComplexEntity01.class);
    assertThat(document.getEntity().getUser(), is("Homer"));
    assertThat(document.getEntity().getDesc(), is("Simpson"));
    assertThat(document.getEntity().getAge(), is(38));

  }

  @Test
  public void test_create_vertex_error_graph() throws ArangoException {

    try {
      driver.graphCreateVertex("foo", "bar", new TestComplexEntity01("Homer", "Simpson", 38), true);
      fail();
    } catch (ArangoException e) {
      assertThat(e.getCode(), greaterThan(300));
    }

  }

  @Test
  public void test_create_vertex_error_collection() throws ArangoException {

    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);

    try {
      DocumentEntity<TestComplexEntity01> vertex = driver.graphCreateVertex(
        this.graphName,
        "foo",
        new TestComplexEntity01("Homer", "Simpson", 38),
        true);
      fail();
    } catch (ArangoException e) {
      assertThat(e.getCode(), greaterThan(300));
    }

  }

  @Test
  public void test_graphGetVertex() throws ArangoException {
    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    DocumentEntity<TestComplexEntity01> vertex = driver.graphCreateVertex(
      this.graphName,
      "from1-1",
      new TestComplexEntity01("Homer", "Simpson", 38),
      true);
    try {
      vertex = driver.graphGetVertex(
        graphName,
        collectionName,
        vertex.getDocumentKey(),
        TestComplexEntity01.class,
        vertex.getDocumentRevision(),
        null);
    } catch (ArangoException e) {
      assertThat(e.getCode(), greaterThan(300));
    }

  }

  // ***********************
  // *** Delete Vertex Tests
  // ***********************

  @Test
  public void test_delete_vertex() throws ArangoException {

    // create graph
    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    // create collection
    driver.graphCreateVertexCollection(this.graphName, this.collectionName);
    // create vertex
    DocumentEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(
      this.graphName,
      this.collectionName,
      new TestComplexEntity01("Homer", "Simpson", 38),
      true);

    // check exists vertex
    DocumentEntity<TestComplexEntity01> vertex = driver.graphGetVertex(
      this.graphName,
      this.collectionName,
      v1.getDocumentKey(),
      TestComplexEntity01.class);
    assertThat(vertex.getCode(), is(200));

    // delete
    DeletedEntity deleted = driver.graphDeleteVertex(
      this.graphName,
      this.collectionName,
      v1.getDocumentKey(),
      true,
      null,
      null);
    assertThat(deleted.getCode(), is(200));
    assertThat(deleted.getDeleted(), is(true));

  }

  @Test
  public void test_delete_vertex_graph_not_found() throws ArangoException {

    try {
      driver.graphDeleteVertex("foo", "bar", "foobar", true, null, null);
      fail();
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(404));
      assertThat(e.getErrorNumber(), is(1924));
    }

  }

  @Test
  public void test_delete_vertex_not_found() throws ArangoException {

    // create graph
    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    driver.graphCreateVertexCollection(this.graphName, this.collectionName);

    try {
      driver.graphDeleteVertex(this.graphName, this.collectionName, "foo", true, null, null);
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(404));
    }

  }

  @Test
  public void test_delete_vertex_rev_eq() throws ArangoException {

    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    driver.graphCreateVertexCollection(this.graphName, this.collectionName);
    DocumentEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(
      this.graphName,
      this.collectionName,
      new TestComplexEntity01("Hoemr", "Simpson", 38),
      null);
    DocumentEntity<TestComplexEntity01> vertex = driver.graphGetVertex(
      this.graphName,
      this.collectionName,
      v1.getDocumentKey(),
      TestComplexEntity01.class,
      null,
      null);
    assertThat(vertex.getCode(), is(200));

    // delete
    DeletedEntity deleted = driver.graphDeleteVertex(
      this.graphName,
      this.collectionName,
      v1.getDocumentKey(),
      null,
      v1.getDocumentRevision(),
      null);
    assertThat(deleted.getCode(), is(202));
    assertThat(deleted.getDeleted(), is(true));

  }

  @Test
  public void test_delete_vertex_rev_ng() throws ArangoException {

    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    driver.graphCreateVertexCollection(this.graphName, this.collectionName);
    DocumentEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(
      this.graphName,
      this.collectionName,
      new TestComplexEntity01("Homer", "Simspin", 38),
      null);
    DocumentEntity<TestComplexEntity01> vertex = driver.graphGetVertex(
      this.graphName,
      this.collectionName,
      v1.getDocumentKey(),
      TestComplexEntity01.class,
      null,
      null);
    assertThat(vertex.getCode(), is(200));

    // delete
    try {
      driver.graphDeleteVertex(
        this.graphName,
        this.collectionName,
        v1.getDocumentKey(),
        null,
        v1.getDocumentRevision() + 1,
        null);
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(412));
      assertThat(e.getErrorNumber(), is(1903));
      assertThat(e.getErrorMessage(), is("wrong revision"));
    }

  }

  @Test
  public void test_delete_vertex_match_eq() throws ArangoException {

    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    driver.graphCreateVertexCollection(this.graphName, this.collectionName);
    DocumentEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(
      this.graphName,
      this.collectionName,
      new TestComplexEntity01("Homer", "Simpson", 38),
      null);
    DocumentEntity<TestComplexEntity01> vertex = driver.graphGetVertex(
      this.graphName,
      this.collectionName,
      v1.getDocumentKey(),
      TestComplexEntity01.class,
      null,
      null);
    assertThat(vertex.getCode(), is(200));

    // delete
    DeletedEntity deleted = driver.graphDeleteVertex(
      this.graphName,
      this.collectionName,
      v1.getDocumentKey(),
      null,
      v1.getDocumentRevision(),
      null);
    assertThat(deleted.getCode(), is(202));
    assertThat(deleted.getDeleted(), is(true));

  }

  @Test
  public void test_delete_vertex_match_ng() throws ArangoException {

    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    driver.graphCreateVertexCollection(this.graphName, this.collectionName);
    DocumentEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(
      this.graphName,
      this.collectionName,
      new TestComplexEntity01("Homer", "Simpson", 38),
      null);
    DocumentEntity<TestComplexEntity01> vertex = driver.graphGetVertex(
      this.graphName,
      this.collectionName,
      v1.getDocumentKey(),
      TestComplexEntity01.class,
      null,
      null);
    assertThat(vertex.getCode(), is(200));

    // delete
    try {
      driver.graphDeleteVertex(
        this.graphName,
        this.collectionName,
        v1.getDocumentKey(),
        null,
        null,
        v1.getDocumentRevision() + 1);
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(412));
      assertThat(e.getErrorNumber(), is(1903));
      assertThat(e.getErrorMessage(), is("wrong revision"));
    }

  }
}
