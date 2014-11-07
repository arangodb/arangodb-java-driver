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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.EdgeEntity;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * @author gschwab
 *
 */
public class ArangoDriverGraphEdgeCreateTest extends BaseGraphTest {

  String graphName = "UnitTestGraph";
  String edgeCollectionName = "edge-1";

  public ArangoDriverGraphEdgeCreateTest(ArangoConfigure configure, ArangoDriver driver) {
    super(configure, driver);
  }

  @Test
  public void test_create_edge_no_key() throws ArangoException {

    String edgeCollectionName = "edge-1";

    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    DocumentEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(
      this.graphName,
      "from1-1",
      new TestComplexEntity01("v1-user", "desc1", 10),
      null);
    DocumentEntity<TestComplexEntity01> v2 = driver.graphCreateVertex(this.graphName, "to1-1", new TestComplexEntity01(
        "v2-user", "desc2", 12), null);

    EdgeEntity<?> edge = driver.graphCreateEdge(
      this.graphName,
      edgeCollectionName,
      null,
      v1.getDocumentHandle(),
      v2.getDocumentHandle(),
      null,
      null);

    assertThat(edge.getCode(), is(202));
    assertThat(edge.isError(), is(false));

    assertThat(edge.getDocumentHandle(), is(notNullValue()));
    assertThat(edge.getDocumentRevision(), is(not(0L)));
    assertThat(edge.getDocumentKey(), is(notNullValue()));
    edge = driver.graphGetEdge(this.graphName, this.edgeCollectionName, edge.getDocumentKey(), null);
    assertThat(edge.getFromVertexHandle(), is(v1.getDocumentHandle()));
    assertThat(edge.getToVertexHandle(), is(v2.getDocumentHandle()));

  }

  @Test
  public void test_create_edge_key() throws ArangoException {

    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    DocumentEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(
      this.graphName,
      "from1-1",
      new TestComplexEntity01("v1-user", "desc1", 10),
      null);
    DocumentEntity<TestComplexEntity01> v2 = driver.graphCreateVertex(this.graphName, "to1-1", new TestComplexEntity01(
        "v2-user", "desc2", 12), null);

    EdgeEntity<?> edge = driver.graphCreateEdge(
      this.graphName,
      edgeCollectionName,
      "e1",
      v1.getDocumentHandle(),
      v2.getDocumentHandle(),
      null,
      null);

    assertThat(edge.getCode(), is(202));
    assertThat(edge.isError(), is(false));

    assertThat(edge.getDocumentHandle(), is(notNullValue()));
    assertThat(edge.getDocumentRevision(), is(not(0L)));
    assertThat(edge.getDocumentKey(), is("e1"));
    edge = driver.graphGetEdge(this.graphName, this.edgeCollectionName, edge.getDocumentKey(), null);
    assertThat(edge.getFromVertexHandle(), is(v1.getDocumentHandle()));
    assertThat(edge.getToVertexHandle(), is(v2.getDocumentHandle()));

  }

  @Test
  public void test_create_edge_optional_value() throws ArangoException {

    String edgeCollectionName = "edge-1";
    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    DocumentEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(
      this.graphName,
      "from1-1",
      new TestComplexEntity01("v1-user", "desc1", 10),
      null);
    DocumentEntity<TestComplexEntity01> v2 = driver.graphCreateVertex(this.graphName, "to1-1", new TestComplexEntity01(
        "v2-user", "desc2", 12), null);

    EdgeEntity<TestComplexEntity02> edge = driver.graphCreateEdge(
      this.graphName,
      edgeCollectionName,
      "e1",
      v1.getDocumentHandle(),
      v2.getDocumentHandle(),
      new TestComplexEntity02(1, 2, 3),
      null);

    assertThat(edge.getCode(), is(202));
    assertThat(edge.isError(), is(false));

    assertThat(edge.getDocumentHandle(), is(notNullValue()));
    assertThat(edge.getDocumentRevision(), is(not(0L)));
    assertThat(edge.getDocumentKey(), is("e1"));
    edge = driver.graphGetEdge(
      this.graphName,
      this.edgeCollectionName,
      edge.getDocumentKey(),
      TestComplexEntity02.class);
    assertThat(edge.getFromVertexHandle(), is(v1.getDocumentHandle()));
    assertThat(edge.getToVertexHandle(), is(v2.getDocumentHandle()));

    assertThat(edge.getEntity(), instanceOf(TestComplexEntity02.class));
    assertThat(edge.getEntity().getX(), is(1));
    assertThat(edge.getEntity().getY(), is(2));
    assertThat(edge.getEntity().getZ(), is(3));

  }

  @Test
  public void test_create_edge_label() throws ArangoException {

    String edgeCollectionName = "edge-1";
    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);

    DocumentEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(
      this.graphName,
      "from1-1",
      new TestComplexEntity01("v1-user", "desc1", 10),
      null);
    DocumentEntity<TestComplexEntity01> v2 = driver.graphCreateVertex(this.graphName, "to1-1", new TestComplexEntity01(
        "v2-user", "desc2", 12), null);

    EdgeEntity<TestComplexEntity02> edge = driver.graphCreateEdge(
      this.graphName,
      edgeCollectionName,
      "e1",
      v1.getDocumentHandle(),
      v2.getDocumentHandle(),
      new TestComplexEntity02(1, 2, 3),
      null);

    assertThat(edge.getCode(), is(202));
    assertThat(edge.isError(), is(false));

    assertThat(edge.getDocumentHandle(), is(notNullValue()));
    assertThat(edge.getDocumentRevision(), is(not(0L)));
    assertThat(edge.getDocumentKey(), is("e1"));
    edge = driver.graphGetEdge(
      this.graphName,
      this.edgeCollectionName,
      edge.getDocumentKey(),
      TestComplexEntity02.class);
    assertThat(edge.getFromVertexHandle(), is(v1.getDocumentHandle()));
    assertThat(edge.getToVertexHandle(), is(v2.getDocumentHandle()));

    assertThat(edge.getEntity(), instanceOf(TestComplexEntity02.class));
    assertThat(edge.getEntity().getX(), is(1));
    assertThat(edge.getEntity().getY(), is(2));
    assertThat(edge.getEntity().getZ(), is(3));

  }

  @Test
  public void test_create_edge_no_graph() throws ArangoException {
    try {
      driver.graphCreateEdge(this.graphName, "edge-1", null, "1", "2", null, null);
      fail();
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(404));
      assertThat(e.getErrorMessage(), startsWith("graph not found"));
    }

  }

  @Test
  public void test_create_edge_no_edge() throws ArangoException {

    String edgeCollectionName = "edge-1";
    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    DocumentEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(
      this.graphName,
      "from1-1",
      new TestComplexEntity01("v1-user", "desc1", 10),
      null);
    DocumentEntity<TestComplexEntity01> v2 = driver.graphCreateVertex(this.graphName, "to1-1", new TestComplexEntity01(
        "v2-user", "desc2", 12), null);

    try {
      driver.graphCreateEdge(this.graphName, edgeCollectionName, null, v1.getDocumentHandle(), "vol1/1", null, null);
      fail();
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(400));
      assertThat(e.getErrorMessage(), startsWith("invalid edge"));
    }

  }

  @Test
  public void test_create_edge_waitForSync() throws ArangoException {

    String edgeCollectionName = "edge-1";
    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    DocumentEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(
      this.graphName,
      "from1-1",
      new TestComplexEntity01("v1-user", "desc1", 10),
      null);
    DocumentEntity<TestComplexEntity01> v2 = driver.graphCreateVertex(this.graphName, "to1-1", new TestComplexEntity01(
        "v2-user", "desc2", 12), null);

    EdgeEntity<?> edge = driver.graphCreateEdge(
      this.graphName,
      edgeCollectionName,
      null,
      v1.getDocumentHandle(),
      v2.getDocumentHandle(),
      null,
      false);

    assertThat(edge.getCode(), is(202));
    assertThat(edge.isError(), is(false));

    assertThat(edge.getDocumentHandle(), is(notNullValue()));
    assertThat(edge.getDocumentRevision(), is(not(0L)));
    assertThat(edge.getDocumentKey(), is(notNullValue()));
    edge = driver.graphGetEdge(this.graphName, this.edgeCollectionName, edge.getDocumentKey(), null);
    assertThat(edge.getFromVertexHandle(), is(v1.getDocumentHandle()));
    assertThat(edge.getToVertexHandle(), is(v2.getDocumentHandle()));

  }

}
