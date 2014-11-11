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

import com.arangodb.entity.DeletedEntity;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.EdgeEntity;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * @author gschwab
 *
 */
public class ArangoDriverGraphEdgeDeleteTest extends BaseGraphTest {

  String graphName = "UnitTestGraph";
  String edgeCollectionName = "edge-1";

  public ArangoDriverGraphEdgeDeleteTest(ArangoConfigure configure, ArangoDriver driver) {
    super(configure, driver);
  }

  @Test
  public void test_delete_edge() throws ArangoException {

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
      this.edgeCollectionName,
      null,
      v1.getDocumentHandle(),
      v2.getDocumentHandle(),
      null,
      null);
    assertThat(edge.getCode(), is(202));

    DeletedEntity deleted = driver.graphDeleteEdge(this.graphName, this.edgeCollectionName, edge.getDocumentKey());
    assertThat(deleted.getCode(), is(202));
    assertThat(deleted.isError(), is(false));
    assertThat(deleted.getDeleted(), is(true));

  }

  @Test
  public void test_delete_edge_no_graph() throws ArangoException {
    try {
      driver.graphDeleteEdge("foo", "bar", null);
      fail();
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(404));
      assertThat(e.getErrorNumber(), is(1924));
      assertThat(e.getErrorMessage(), startsWith("graph not found"));
    }

  }

  @Test
  public void test_delete_edge_no_edge_collection() throws ArangoException {

    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    driver.graphCreateVertex(this.graphName, "from1-1", new TestComplexEntity01("v1-user", "desc1", 10), null);
    driver.graphCreateVertex(this.graphName, "to1-1", new TestComplexEntity01("v2-user", "desc2", 12), null);

    try {
      driver.graphDeleteEdge(this.graphName, "1", "2");
      fail();
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(404));
      assertThat(e.getErrorNumber(), is(1203));
      assertThat(e.getErrorMessage(), startsWith("collection not found"));
    }

  }

  @Test
  public void test_delete_edge_waitForSync() throws ArangoException {

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
      this.edgeCollectionName,
      null,
      v1.getDocumentHandle(),
      v2.getDocumentHandle(),
      null,
      false);
    assertThat(edge.getCode(), is(202));
    assertThat(edge.isError(), is(false));

    DeletedEntity deleted = driver.graphDeleteEdge(
      this.graphName,
      this.edgeCollectionName,
      edge.getDocumentKey(),
      false);
    assertThat(deleted.getCode(), is(202));
    assertThat(deleted.getDeleted(), is(true));

  }

  @Test
  public void test_delete_edge_rev_eq() throws ArangoException {

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
      this.edgeCollectionName,
      null,
      v1.getDocumentHandle(),
      v2.getDocumentHandle(),
      null,
      null);
    assertThat(edge.getCode(), is(202));

    Long rev = edge.getDocumentRevision();
    DeletedEntity deleted = driver.graphDeleteEdge(
      this.graphName,
      this.edgeCollectionName,
      edge.getDocumentKey(),
      null,
      rev,
      null);
    assertThat(deleted.getCode(), is(202));
    assertThat(deleted.isError(), is(false));
    assertThat(deleted.getDeleted(), is(true));

  }

  @Test
  public void test_delete_edge_rev_ne() throws ArangoException {

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
      this.edgeCollectionName,
      null,
      v1.getDocumentHandle(),
      v2.getDocumentHandle(),
      null,
      null);
    assertThat(edge.getCode(), is(202));

    try {
      Long rev = edge.getDocumentRevision() + 1;
      driver.graphDeleteEdge(this.graphName, this.edgeCollectionName, edge.getDocumentKey(), null, rev, null);
      fail();
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(412));
      assertThat(e.getErrorNumber(), is(1903));
      assertThat(e.getErrorMessage(), is("wrong revision"));
    }

  }

  @Test
  public void test_delete_edge_match_eq() throws ArangoException {

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
      this.edgeCollectionName,
      null,
      v1.getDocumentHandle(),
      v2.getDocumentHandle(),
      null,
      null);
    assertThat(edge.getCode(), is(202));

    Long rev = edge.getDocumentRevision();
    DeletedEntity deleted = driver.graphDeleteEdge(
      this.graphName,
      this.edgeCollectionName,
      edge.getDocumentKey(),
      null,
      rev,
      null);
    assertThat(deleted.getCode(), is(202));
    assertThat(deleted.isError(), is(false));
    assertThat(deleted.getDeleted(), is(true));

  }

  @Test
  public void test_delete_edge_match_ne() throws ArangoException {

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
      this.edgeCollectionName,
      null,
      v1.getDocumentHandle(),
      v2.getDocumentHandle(),
      null,
      null);
    assertThat(edge.getCode(), is(202));

    try {
      Long rev = edge.getDocumentRevision() + 1;
      driver.graphDeleteEdge(this.graphName, this.edgeCollectionName, edge.getDocumentKey(), null, rev, null);
      fail();
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(412));
      assertThat(e.getErrorNumber(), is(1903));
      assertThat(e.getErrorMessage(), is("wrong revision"));
    }

  }

}
