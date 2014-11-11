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
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.EdgeEntity;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ArangoDriverGraphEdgeGetTest extends BaseGraphTest {

  String graphName = "UnitTestGraph";
  String edgeCollectionName = "edge-1";

  public ArangoDriverGraphEdgeGetTest(ArangoConfigure configure, ArangoDriver driver) {
    super(configure, driver);
  }

  @Test
  public void test_get_edge() throws ArangoException {

    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    DocumentEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(
      this.graphName,
      "from1-1",
      new TestComplexEntity01("v1-user", "desc1", 10),
      null);
    DocumentEntity<TestComplexEntity01> v2 = driver.graphCreateVertex(this.graphName, "to1-1", new TestComplexEntity01(
        "v2-user", "desc2", 12), null);

    EdgeEntity<?> edge1 = driver.graphCreateEdge(
      this.graphName,
      this.edgeCollectionName,
      null,
      v1.getDocumentHandle(),
      v2.getDocumentHandle(),
      null,
      null);

    EdgeEntity<?> edge2 = driver.graphGetEdge(this.graphName, this.edgeCollectionName, edge1.getDocumentKey(), null);
    assertThat(edge2.getCode(), is(200));
    assertThat(edge2.isError(), is(false));

    assertThat(edge2.getDocumentHandle(), is(edge1.getDocumentHandle()));
    assertThat(edge2.getDocumentRevision(), is(edge1.getDocumentRevision()));
    assertThat(edge2.getDocumentKey(), is(edge1.getDocumentKey()));
    assertThat(edge2.getFromVertexHandle(), is(v1.getDocumentHandle()));
    assertThat(edge2.getToVertexHandle(), is(v2.getDocumentHandle()));

  }

  @Test
  public void test_get_edge2() throws ArangoException {

    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    DocumentEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(
      this.graphName,
      "from1-1",
      new TestComplexEntity01("v1-user", "desc1", 10),
      null);
    DocumentEntity<TestComplexEntity01> v2 = driver.graphCreateVertex(this.graphName, "to1-1", new TestComplexEntity01(
        "v2-user", "desc2", 12), null);

    EdgeEntity<?> edge1 = driver.graphCreateEdge(
      this.graphName,
      this.edgeCollectionName,
      null,
      v1.getDocumentHandle(),
      v2.getDocumentHandle(),
      new TestComplexEntity02(1, 2, 3),
      null);

    EdgeEntity<TestComplexEntity02> edge2 = driver.graphGetEdge(
      this.graphName,
      this.edgeCollectionName,
      edge1.getDocumentKey(),
      TestComplexEntity02.class);
    assertThat(edge2.getCode(), is(200));
    assertThat(edge2.isError(), is(false));

    assertThat(edge2.getDocumentHandle(), is(edge1.getDocumentHandle()));
    assertThat(edge2.getDocumentRevision(), is(edge1.getDocumentRevision()));
    assertThat(edge2.getDocumentKey(), is(edge1.getDocumentKey()));
    assertThat(edge2.getFromVertexHandle(), is(v1.getDocumentHandle()));
    assertThat(edge2.getToVertexHandle(), is(v2.getDocumentHandle()));

    assertThat(edge2.getEntity().getX(), is(1));
    assertThat(edge2.getEntity().getY(), is(2));
    assertThat(edge2.getEntity().getZ(), is(3));

  }

  @Test
  public void test_get_edge_graph_not_found() throws ArangoException {

    try {
      driver.graphGetEdge("foo", "bar", "v1", null);
      fail();
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(404));
      assertThat(e.getErrorNumber(), is(1924));
      assertThat(e.getErrorMessage(), startsWith("graph not found"));
    }

  }

  @Test
  public void test_get_edge_edge_not_found() throws ArangoException {

    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    DocumentEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(
      this.graphName,
      "from1-1",
      new TestComplexEntity01("v1-user", "desc1", 10),
      null);
    DocumentEntity<TestComplexEntity01> v2 = driver.graphCreateVertex(this.graphName, "to1-1", new TestComplexEntity01(
        "v2-user", "desc2", 12), null);

    EdgeEntity<?> edge1 = driver.graphCreateEdge(
      this.graphName,
      this.edgeCollectionName,
      null,
      v1.getDocumentHandle(),
      v2.getDocumentHandle(),
      null,
      null);

    try {
      EdgeEntity<?> edge2 = driver.graphGetEdge(this.graphName, this.edgeCollectionName, "xx", null);
      fail();
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(404));
      assertThat(e.getErrorNumber(), is(1202));
      assertThat(e.getErrorMessage(), startsWith("document not found"));
    }

  }

  @Test
  public void test_get_edge_rev_eq() throws ArangoException {

    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    DocumentEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(
      this.graphName,
      "from1-1",
      new TestComplexEntity01("v1-user", "desc1", 10),
      null);
    DocumentEntity<TestComplexEntity01> v2 = driver.graphCreateVertex(this.graphName, "to1-1", new TestComplexEntity01(
        "v2-user", "desc2", 12), null);

    EdgeEntity<?> edge1 = driver.graphCreateEdge(
      this.graphName,
      this.edgeCollectionName,
      null,
      v1.getDocumentHandle(),
      v2.getDocumentHandle(),
      new TestComplexEntity02(1, 2, 3),
      null);

    Long rev = edge1.getDocumentRevision();
    EdgeEntity<TestComplexEntity02> edge2 = driver.graphGetEdge(
      this.graphName,
      this.edgeCollectionName,
      edge1.getDocumentKey(),
      TestComplexEntity02.class,
      rev,
      null);
    assertThat(edge2.getCode(), is(200));
    assertThat(edge2.isError(), is(false));

    assertThat(edge2.getDocumentHandle(), is(edge1.getDocumentHandle()));
    assertThat(edge2.getDocumentRevision(), is(edge1.getDocumentRevision()));
    assertThat(edge2.getDocumentKey(), is(edge1.getDocumentKey()));
    assertThat(edge2.getFromVertexHandle(), is(v1.getDocumentHandle()));
    assertThat(edge2.getToVertexHandle(), is(v2.getDocumentHandle()));

    assertThat(edge2.getEntity().getX(), is(1));
    assertThat(edge2.getEntity().getY(), is(2));
    assertThat(edge2.getEntity().getZ(), is(3));

  }

  @Test
  public void test_get_edge_rev_ne() throws ArangoException {

    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    DocumentEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(
      this.graphName,
      "from1-1",
      new TestComplexEntity01("v1-user", "desc1", 10),
      null);
    DocumentEntity<TestComplexEntity01> v2 = driver.graphCreateVertex(this.graphName, "to1-1", new TestComplexEntity01(
        "v2-user", "desc2", 12), null);

    EdgeEntity<?> edge1 = driver.graphCreateEdge(
      this.graphName,
      this.edgeCollectionName,
      null,
      v1.getDocumentHandle(),
      v2.getDocumentHandle(),
      new TestComplexEntity02(1, 2, 3),
      null);

    try {
      Long rev = edge1.getDocumentRevision() + 1;
      EdgeEntity<TestComplexEntity02> edge2 = driver.graphGetEdge(
        this.graphName,
        this.edgeCollectionName,
        edge1.getDocumentKey(),
        TestComplexEntity02.class,
        rev,
        null);
      fail();
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(412));
      assertThat(e.getErrorNumber(), is(1903));
      assertThat(e.getErrorMessage(), is("wrong revision"));
    }

  }

  @Test
  public void test_get_edge_nomatch_eq() throws ArangoException {

    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    DocumentEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(
      this.graphName,
      "from1-1",
      new TestComplexEntity01("v1-user", "desc1", 10),
      null);
    DocumentEntity<TestComplexEntity01> v2 = driver.graphCreateVertex(this.graphName, "to1-1", new TestComplexEntity01(
        "v2-user", "desc2", 12), null);

    EdgeEntity<?> edge1 = driver.graphCreateEdge(
      this.graphName,
      this.edgeCollectionName,
      null,
      v1.getDocumentHandle(),
      v2.getDocumentHandle(),
      new TestComplexEntity02(1, 2, 3),
      null);

    Long rev = edge1.getDocumentRevision();
    EdgeEntity<TestComplexEntity02> edge2 = driver.graphGetEdge(
      this.graphName,
      this.edgeCollectionName,
      edge1.getDocumentKey(),
      TestComplexEntity02.class,
      null,
      rev);
    assertThat(edge2.getStatusCode(), is(304));
    assertThat(edge2.isNotModified(), is(true));
    assertThat(edge2.isError(), is(false));

  }

  @Test
  public void test_get_edge_nomatch_ne() throws ArangoException {

    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    DocumentEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(
      this.graphName,
      "from1-1",
      new TestComplexEntity01("v1-user", "desc1", 10),
      null);
    DocumentEntity<TestComplexEntity01> v2 = driver.graphCreateVertex(this.graphName, "to1-1", new TestComplexEntity01(
        "v2-user", "desc2", 12), null);

    EdgeEntity<?> edge1 = driver.graphCreateEdge(
      this.graphName,
      this.edgeCollectionName,
      null,
      v1.getDocumentHandle(),
      v2.getDocumentHandle(),
      new TestComplexEntity02(1, 2, 3),
      null);

    Long rev = edge1.getDocumentRevision() + 1;
    EdgeEntity<TestComplexEntity02> edge2 = driver.graphGetEdge(
      this.graphName,
      this.edgeCollectionName,
      edge1.getDocumentKey(),
      TestComplexEntity02.class,
      null,
      rev);

    assertThat(edge2.getCode(), is(200));
    assertThat(edge2.isError(), is(false));

    assertThat(edge2.getDocumentHandle(), is(edge1.getDocumentHandle()));
    assertThat(edge2.getDocumentRevision(), is(edge1.getDocumentRevision()));
    assertThat(edge2.getDocumentKey(), is(edge1.getDocumentKey()));
    assertThat(edge2.getFromVertexHandle(), is(v1.getDocumentHandle()));
    assertThat(edge2.getToVertexHandle(), is(v2.getDocumentHandle()));

    assertThat(edge2.getEntity().getX(), is(1));
    assertThat(edge2.getEntity().getY(), is(2));
    assertThat(edge2.getEntity().getZ(), is(3));

  }

  @Test
  public void test_get_edge_match_eq() throws ArangoException {

    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    DocumentEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(
      this.graphName,
      "from1-1",
      new TestComplexEntity01("v1-user", "desc1", 10),
      null);
    DocumentEntity<TestComplexEntity01> v2 = driver.graphCreateVertex(this.graphName, "to1-1", new TestComplexEntity01(
        "v2-user", "desc2", 12), null);

    EdgeEntity<?> edge1 = driver.graphCreateEdge(
      this.graphName,
      this.edgeCollectionName,
      null,
      v1.getDocumentHandle(),
      v2.getDocumentHandle(),
      new TestComplexEntity02(1, 2, 3),
      null);

    Long rev = edge1.getDocumentRevision();
    EdgeEntity<TestComplexEntity02> edge2 = driver.graphGetEdge(
      this.graphName,
      this.edgeCollectionName,
      edge1.getDocumentKey(),
      TestComplexEntity02.class,
      rev,
      null);
    assertThat(edge2.getCode(), is(200));
    assertThat(edge2.isError(), is(false));

    assertThat(edge2.getDocumentHandle(), is(edge1.getDocumentHandle()));
    assertThat(edge2.getDocumentRevision(), is(edge1.getDocumentRevision()));
    assertThat(edge2.getDocumentKey(), is(edge1.getDocumentKey()));
    assertThat(edge2.getFromVertexHandle(), is(v1.getDocumentHandle()));
    assertThat(edge2.getToVertexHandle(), is(v2.getDocumentHandle()));

    assertThat(edge2.getEntity().getX(), is(1));
    assertThat(edge2.getEntity().getY(), is(2));
    assertThat(edge2.getEntity().getZ(), is(3));

  }

  @Test
  public void test_get_edge_match_ne() throws ArangoException {

    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    DocumentEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(
      this.graphName,
      "from1-1",
      new TestComplexEntity01("v1-user", "desc1", 10),
      null);
    DocumentEntity<TestComplexEntity01> v2 = driver.graphCreateVertex(this.graphName, "to1-1", new TestComplexEntity01(
        "v2-user", "desc2", 12), null);

    EdgeEntity<?> edge1 = driver.graphCreateEdge(
      this.graphName,
      this.edgeCollectionName,
      null,
      v1.getDocumentHandle(),
      v2.getDocumentHandle(),
      new TestComplexEntity02(1, 2, 3),
      null);

    try {
      Long rev = edge1.getDocumentRevision() + 1;
      EdgeEntity<TestComplexEntity02> edge2 = driver.graphGetEdge(
        this.graphName,
        this.edgeCollectionName,
        edge1.getDocumentKey(),
        TestComplexEntity02.class,
        rev,
        null);
      fail();
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(412));
      assertThat(e.getErrorNumber(), is(1903));
      assertThat(e.getErrorMessage(), is("wrong revision"));
    }

  }

}
