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
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.EdgeDefinitionEntity;
import com.arangodb.entity.GraphEntity;
import com.arangodb.entity.GraphsEntity;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * @author gschwab
 * 
 */
public class ArangoDriverGraphTest extends BaseGraphTest {

  String graphName = "UnitTestGraph";
  String collectionName = "UnitTestCollection";

  public ArangoDriverGraphTest(ArangoConfigure configure, ArangoDriver driver) {
    super(configure, driver);
  }

  @Test
  public void test_getGraphs() throws ArangoException {
    GraphsEntity graphs = driver.getGraphs();
    assertThat(graphs.getGraphs().size(), is(0));
    driver.createGraph("UnitTestGraph1", true);
    driver.createGraph("UnitTestGraph2", true);
    driver.createGraph("UnitTestGraph3", true);
    graphs = driver.getGraphs();
    assertThat(graphs.getGraphs().size(), is(3));
    assertThat(graphs.getGraphs().get(0).getName(), startsWith("UnitTestGraph"));
    assertThat(graphs.getGraphs().get(1).getName(), startsWith("UnitTestGraph"));
    assertThat(graphs.getGraphs().get(2).getName(), startsWith("UnitTestGraph"));
  }

  @Test
  public void test_getGraphList() throws ArangoException {
    List<String> graphs = driver.getGraphList();
    assertThat(graphs.size(), is(0));
    String graphName1 = "UnitTestGraph1";
    String graphName2 = "UnitTestGraph2";
    String graphName3 = "UnitTestGraph3";
    driver.createGraph(graphName1, true);
    driver.createGraph(graphName2, true);
    driver.createGraph(graphName3, true);
    graphs = driver.getGraphList();
    assertThat(graphs.size(), is(3));
    assertThat(graphs.contains(graphName1), is(true));
    assertThat(graphs.contains(graphName2), is(true));
    assertThat(graphs.contains(graphName3), is(true));
    assertThat(graphs.contains("foo"), is(false));
  }

  @Test
  public void test_createGraph() throws ArangoException {

    List<EdgeDefinitionEntity> edgeDefinitions = new ArrayList<EdgeDefinitionEntity>();
    List<String> orphanCollections = new ArrayList<String>();

    // create
    GraphEntity graph = driver.createGraph(this.graphName, edgeDefinitions, orphanCollections, true);

    assertThat(graph.getCode(), is(201));
    assertThat(graph.getDocumentRevision(), is(not(0L)));
    assertThat(graph.getDocumentHandle(), is("_graphs/" + this.graphName));
    assertThat(graph.getName(), is(this.graphName));
    assertThat(graph.getOrphanCollections(), is(orphanCollections));

  }

  @Test
  public void test_createGraph_2() throws ArangoException {

    List<EdgeDefinitionEntity> edgeDefinitions = this.createEdgeDefinitions(2, 0);

    List<String> orphanCollections = this.createOrphanCollections(2);

    // create
    GraphEntity graph = driver.createGraph(this.graphName, edgeDefinitions, orphanCollections, true);
    assertThat(graph.getCode(), is(201));
    assertThat(graph.getDocumentRevision(), is(not(0L)));
    assertThat(graph.getDocumentHandle(), is("_graphs/" + this.graphName));
    assertThat(graph.getName(), is(this.graphName));
    assertThat(graph.getOrphanCollections(), is(orphanCollections));
  }

  @Test
  public void test_getGraph() throws ArangoException {
    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    GraphEntity graph = driver.getGraph(this.graphName);
    assertThat(graph.getEdgeDefinitionsEntity().getEdgeDefinitions().get(0).getClass().getName(), is(EdgeDefinitionEntity.class.getName()));
    assertThat(graph.getEdgeDefinitionsEntity().getEdgeDefinitions().get(0).getFrom().size(), is(3));
    assertThat(graph.getOrphanCollections().size(), is(2));
    assertThat(graph.getName(), is(this.graphName));
    assertThat(graph.getEdgeDefinitionsEntity().getSize(), is(2));
    assertThat(graph.getEdgeDefinitionsEntity().getEdgeDefinitions().get(0).getCollection().startsWith("edge"), is(true));

  }

  @Test
  public void test_deleteGraph_keep_collections() throws ArangoException {
    GraphsEntity graphs = driver.getGraphs();
    assertThat(graphs.getGraphs().size(), is(0));
    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    graphs = driver.getGraphs();
    assertThat(graphs.getGraphs().size(), is(1));
    assertThat(driver.getGraph(this.graphName).getName(), is(this.graphName));
    driver.deleteGraph(this.graphName);
    graphs = driver.getGraphs();
    assertThat(graphs.getGraphs().size(), is(0));
    assertThat("number of collections", driver.getCollections(true).getCollections().size(), greaterThan(0));
  }

  @Test
  public void test_deleteGraph_delete_collections() throws ArangoException {
    GraphsEntity graphs = driver.getGraphs();
    assertThat(graphs.getGraphs().size(), is(0));
    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    graphs = driver.getGraphs();
    assertThat(graphs.getGraphs().size(), is(1));
    assertThat(driver.getGraph(this.graphName).getName(), is(this.graphName));
    driver.deleteGraph(this.graphName, true);
    graphs = driver.getGraphs();
    assertThat(graphs.getGraphs().size(), is(0));
    assertThat("number of collections", driver.getCollections(true).getCollections().size(), is(0));
  }

  @Test
  public void test_deleteGraph_not_found() throws ArangoException {
    try {
      driver.deleteGraph("foo");

    } catch (ArangoException e) {
      assertThat(e.getErrorMessage(), is("graph not found"));
    }
  }

  @Test
  public void test_getVertexCollections() throws ArangoException {
    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    List<String> collections = driver.graphGetVertexCollections(this.graphName);
    assertThat(collections.size(), is(14));
  }

  @Test
  public void test_getVertexCollections_not_found() throws ArangoException {
    try {
      driver.graphGetVertexCollections("foo");
      fail();
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(404));
      assertThat(e.getErrorMessage(), is("graph not found"));
    }
  }

  @Test
  public void test_createVertexCollection() throws ArangoException {
    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    GraphEntity graph = driver.graphCreateVertexCollection(this.graphName, collectionName);
    assertThat(driver.graphGetVertexCollections(this.graphName).contains(collectionName), is(true));
    assertThat(graph.getName(), is(this.graphName));
  }

  @Test
  public void test_create_newVertexCollection_error() throws ArangoException {
    try {
      driver.graphCreateVertexCollection("foo", "UnitTestCollection");
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(404));
    }
  }

  @Test
  public void test_deleteVertexCollection_keep_collection() throws ArangoException {

    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    driver.graphCreateVertexCollection(this.graphName, collectionName);
    assertThat(driver.graphGetVertexCollections(this.graphName).contains(collectionName), is(true));
    driver.graphDeleteVertexCollection(this.graphName, collectionName, false);
    assertThat(driver.graphGetVertexCollections(this.graphName).contains(collectionName), is(false));
    assertThat(driver.getCollection(collectionName).getClass().getName(), is(CollectionEntity.class.getName()));

  }

  @Test
  public void test_deleteVertexCollection_drop_collection() throws ArangoException {
    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    driver.graphCreateVertexCollection(this.graphName, collectionName);
    assertThat(driver.graphGetVertexCollections(this.graphName).contains(collectionName), is(true));
    driver.graphDeleteVertexCollection(this.graphName, collectionName, true);
    assertThat(driver.graphGetVertexCollections(this.graphName).contains(collectionName), is(false));
    try {
      driver.getCollection(collectionName);
      fail();
    } catch (ArangoException e) {
      assertThat(e.getCode(), greaterThan(300));
    }

  }

  @Test
  public void test_deleteVertexCollection_drop_collection_fail() throws ArangoException {
    String graphName1 = "UnitTestGraph1";
    String graphName2 = "UnitTestGraph2";
    driver.createGraph(graphName1, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    driver.createGraph(graphName2, this.createEdgeDefinitions(2, 2), this.createOrphanCollections(2), true);
    driver.graphCreateVertexCollection(graphName1, collectionName);
    assertThat(driver.graphGetVertexCollections(graphName1).contains(collectionName), is(true));
    driver.graphCreateVertexCollection(graphName2, collectionName);
    assertThat(driver.graphGetVertexCollections(graphName2).contains(collectionName), is(true));
    driver.graphDeleteVertexCollection(graphName1, collectionName, true);
    assertThat(driver.graphGetVertexCollections(graphName1).contains(collectionName), is(false));
    assertThat(driver.graphGetVertexCollections(graphName2).contains(collectionName), is(true));
    assertThat(driver.getCollection(collectionName).getClass().getName(), is(CollectionEntity.class.getName()));
  }

  @Test
  public void test_delete_vertex_collection_error1() throws ArangoException {
    try {
      driver.graphDeleteVertexCollection("foo", "bar", true);
      fail();
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(404));
    }
  }

  @Test
  public void test_delete_vertex_collection_error2() throws ArangoException {
    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    try {
      driver.graphDeleteVertexCollection(this.graphName, "bar", true);
      fail();
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(404));
    }
  }

  @Test
  public void test_get_edge_Collections() throws ArangoException {
    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    List<String> collections = driver.graphGetEdgeCollections(this.graphName);
    assertThat(collections.size(), is(2));
  }

  @Test
  public void test_get_edge_Collections_not_found() throws ArangoException {
    try {
      driver.graphGetEdgeCollections("foo");
      fail();
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(404));
      assertThat(e.getErrorMessage(), is("graph not found"));
    }
  }

  @Test
  public void test_create_edge_definition() throws ArangoException {
    String edgeCollectionName = "UnitTestEdgeCollection";
    String fromCollectionName = "UnitTestFromCollection";
    String toCollectionName = "UnitTestToCollection";
    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    EdgeDefinitionEntity edgeDefinition = new EdgeDefinitionEntity();
    edgeDefinition.setCollection(edgeCollectionName);
    List<String> from = new ArrayList<String>();
    from.add(fromCollectionName);
    edgeDefinition.setFrom(from);
    List<String> to = new ArrayList<String>();
    to.add(toCollectionName);
    edgeDefinition.setTo(to);
    driver.graphCreateEdgeDefinition(this.graphName, edgeDefinition);
    assertThat(driver.graphGetEdgeCollections(this.graphName).contains(edgeCollectionName), is(true));
  }

  @Test
  public void test_create_edge_definition_2_graphs() throws ArangoException {
    String graphName1 = "UnitTestGraph1";
    String graphName2 = "UnitTestGraph2";
    String edgeCollectionName = "UnitTestEdgeCollection";
    String fromCollectionName = "UnitTestFromCollection";
    String toCollectionName = "UnitTestToCollection";
    driver.createGraph(graphName1, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    driver.createGraph(graphName2, this.createEdgeDefinitions(2, 2), this.createOrphanCollections(2), true);
    EdgeDefinitionEntity edgeDefinition = new EdgeDefinitionEntity();
    edgeDefinition.setCollection(edgeCollectionName);
    List<String> from = new ArrayList<String>();
    from.add(fromCollectionName);
    edgeDefinition.setFrom(from);
    List<String> to = new ArrayList<String>();
    to.add(toCollectionName);
    edgeDefinition.setTo(to);
    driver.graphCreateEdgeDefinition(graphName1, edgeDefinition);
    driver.graphCreateEdgeDefinition(graphName2, edgeDefinition);
    assertThat(driver.graphGetEdgeCollections(graphName1).contains(edgeCollectionName), is(true));
    assertThat(driver.graphGetEdgeCollections(graphName2).contains(edgeCollectionName), is(true));
  }

  @Test
  public void test_replace_edge_definition() throws ArangoException {
    String edgeCollectionName = "UnitTestEdgeCollection";
    String fromCollectionName1 = "UnitTestFromCollection1";
    String fromCollectionName2 = "UnitTestFromCollection2";
    String toCollectionName1 = "UnitTestToCollection1";
    String toCollectionName2 = "UnitTestToCollection2";
    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    EdgeDefinitionEntity edgeDefinition1 = new EdgeDefinitionEntity();
    edgeDefinition1.setCollection(edgeCollectionName);
    List<String> from1 = new ArrayList<String>();
    from1.add(fromCollectionName1);
    edgeDefinition1.setFrom(from1);
    List<String> to1 = new ArrayList<String>();
    to1.add(toCollectionName1);
    edgeDefinition1.setTo(to1);
    driver.graphCreateEdgeDefinition(this.graphName, edgeDefinition1);
    assertThat(driver.graphGetEdgeCollections(this.graphName).contains(edgeCollectionName), is(true));
    assertThat(driver.graphGetVertexCollections(this.graphName).contains(fromCollectionName1), is(true));
    assertThat(driver.graphGetVertexCollections(this.graphName).contains(toCollectionName1), is(true));
    EdgeDefinitionEntity edgeDefinition2 = new EdgeDefinitionEntity();
    edgeDefinition2.setCollection(edgeCollectionName);
    List<String> from2 = new ArrayList<String>();
    from2.add(fromCollectionName2);
    edgeDefinition2.setFrom(from2);
    List<String> to2 = new ArrayList<String>();
    to2.add(toCollectionName2);
    edgeDefinition2.setTo(to2);
    GraphEntity graph = driver.graphReplaceEdgeDefinition(this.graphName, edgeCollectionName, edgeDefinition2);
    List<EdgeDefinitionEntity> edgeDefinitions = graph.getEdgeDefinitionsEntity().getEdgeDefinitions();
    for (EdgeDefinitionEntity edgeDef : edgeDefinitions) {
      List<String> f = edgeDef.getFrom();
      assertThat(f.contains(from1), is(false));
      List<String> t = edgeDef.getTo();
      assertThat(t.contains(to1), is(false));
    }
    assertThat(driver.graphGetEdgeCollections(this.graphName).contains(edgeCollectionName), is(true));
    assertThat(driver.graphGetVertexCollections(this.graphName).contains(fromCollectionName1), is(true));
    assertThat(driver.graphGetVertexCollections(this.graphName).contains(toCollectionName1), is(true));
    assertThat(driver.graphGetVertexCollections(this.graphName).contains(fromCollectionName2), is(true));
    assertThat(driver.graphGetVertexCollections(this.graphName).contains(toCollectionName2), is(true));
  }

  @Test
  public void test_replace_edge_definition_2_graphs() throws ArangoException {
    String graphName1 = "UnitTestGraph1";
    String graphName2 = "UnitTestGraph2";
    String edgeCollectionName = "UnitTestEdgeCollection";
    String fromCollectionName1 = "UnitTestFromCollection1";
    String fromCollectionName2 = "UnitTestFromCollection2";
    String toCollectionName1 = "UnitTestToCollection1";
    String toCollectionName2 = "UnitTestToCollection2";
    driver.createGraph(graphName1, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    driver.createGraph(graphName2, this.createEdgeDefinitions(2, 2), this.createOrphanCollections(2), true);
    EdgeDefinitionEntity edgeDefinition1 = new EdgeDefinitionEntity();
    edgeDefinition1.setCollection(edgeCollectionName);
    List<String> from1 = new ArrayList<String>();
    from1.add(fromCollectionName1);
    edgeDefinition1.setFrom(from1);
    List<String> to1 = new ArrayList<String>();
    to1.add(toCollectionName1);
    edgeDefinition1.setTo(to1);
    driver.graphCreateEdgeDefinition(graphName1, edgeDefinition1);
    driver.graphCreateEdgeDefinition(graphName2, edgeDefinition1);
    assertThat(driver.graphGetEdgeCollections(graphName1).contains(edgeCollectionName), is(true));
    assertThat(driver.graphGetVertexCollections(graphName1).contains(fromCollectionName1), is(true));
    assertThat(driver.graphGetVertexCollections(graphName1).contains(toCollectionName1), is(true));
    assertThat(driver.graphGetEdgeCollections(graphName2).contains(edgeCollectionName), is(true));
    assertThat(driver.graphGetVertexCollections(graphName2).contains(fromCollectionName1), is(true));
    assertThat(driver.graphGetVertexCollections(graphName2).contains(toCollectionName1), is(true));
    EdgeDefinitionEntity edgeDefinition2 = new EdgeDefinitionEntity();
    edgeDefinition2.setCollection(edgeCollectionName);
    List<String> from2 = new ArrayList<String>();
    from2.add(fromCollectionName2);
    edgeDefinition2.setFrom(from2);
    List<String> to2 = new ArrayList<String>();
    to2.add(toCollectionName2);
    edgeDefinition2.setTo(to2);
    GraphEntity graph1 = driver.graphReplaceEdgeDefinition(graphName1, edgeCollectionName, edgeDefinition2);
    List<EdgeDefinitionEntity> edgeDefinitions1 = graph1.getEdgeDefinitionsEntity().getEdgeDefinitions();
    for (EdgeDefinitionEntity edgeDef : edgeDefinitions1) {
      List<String> f = edgeDef.getFrom();
      assertThat(f.contains(from1), is(false));
      List<String> t = edgeDef.getTo();
      assertThat(t.contains(to1), is(false));
    }
    GraphEntity graph2 = driver.graphReplaceEdgeDefinition(graphName1, edgeCollectionName, edgeDefinition2);
    List<EdgeDefinitionEntity> edgeDefinitions2 = graph2.getEdgeDefinitionsEntity().getEdgeDefinitions();
    for (EdgeDefinitionEntity edgeDef : edgeDefinitions2) {
      List<String> f = edgeDef.getFrom();
      assertThat(f.contains(from1), is(false));
      List<String> t = edgeDef.getTo();
      assertThat(t.contains(to1), is(false));
    }
    assertThat(driver.graphGetEdgeCollections(graphName1).contains(edgeCollectionName), is(true));
    assertThat(driver.graphGetVertexCollections(graphName1).contains(fromCollectionName1), is(true));
    assertThat(driver.graphGetVertexCollections(graphName1).contains(toCollectionName1), is(true));
    assertThat(driver.graphGetVertexCollections(graphName1).contains(fromCollectionName2), is(true));
    assertThat(driver.graphGetVertexCollections(graphName1).contains(toCollectionName2), is(true));
    assertThat(driver.graphGetEdgeCollections(graphName2).contains(edgeCollectionName), is(true));
    assertThat(driver.graphGetVertexCollections(graphName2).contains(fromCollectionName1), is(true));
    assertThat(driver.graphGetVertexCollections(graphName2).contains(toCollectionName1), is(true));
    assertThat(driver.graphGetVertexCollections(graphName2).contains(fromCollectionName2), is(true));
    assertThat(driver.graphGetVertexCollections(graphName2).contains(toCollectionName2), is(true));
  }

  @Test
  public void test_delete_edge_definition_keep_collections() throws ArangoException {
    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    driver.graphDeleteEdgeDefinition(this.graphName, "edge-1", false);
    assertThat(driver.graphGetEdgeCollections(this.graphName).contains("edge-1"), is(false));
    assertThat(driver.graphGetEdgeCollections(this.graphName).contains("edge-2"), is(true));

  }

  @Test
  public void test_delete_edge_definition_drop_collections() throws ArangoException {
    driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
    driver.graphDeleteEdgeDefinition(this.graphName, "edge-1", true);
    assertThat(driver.graphGetEdgeCollections(this.graphName).contains("edge-1"), is(false));
    assertThat(driver.graphGetEdgeCollections(this.graphName).contains("edge-2"), is(true));
    try {
      driver.getCollection("edge-1");
      fail();
    } catch (ArangoException e) {
      assertThat(e.getCode(), greaterThan(300));
    }

  }

}
