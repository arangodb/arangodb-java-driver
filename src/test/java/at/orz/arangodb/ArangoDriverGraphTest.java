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

package at.orz.arangodb;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import at.orz.arangodb.entity.CollectionEntity;
import at.orz.arangodb.entity.EdgeDefinitionEntity;
import at.orz.arangodb.entity.GraphEntity;
import at.orz.arangodb.entity.GraphsEntity;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * 
 */
public class ArangoDriverGraphTest extends BaseGraphTest {

    public ArangoDriverGraphTest(ArangoConfigure configure, ArangoDriver driver) {
        super(configure, driver);
    }

    @Test
    public void test_get_graphs() throws ArangoException {
        GraphsEntity graphs = driver.getGraphs();
        assertThat(graphs.getGraphs().size(), is(0));
        driver.createGraph("UnitTestGraph1", true);
        driver.createGraph("UnitTestGraph2", true);
        driver.createGraph("UnitTestGraph3", true);
        graphs = driver.getGraphs();
        assertThat(graphs.getGraphs().size(), is(3));
    }

    @Test
    public void test_create_graph() throws ArangoException {

        String graphName = "unitTestGraph";

        List<EdgeDefinitionEntity> edgeDefinitions = new ArrayList<EdgeDefinitionEntity>();
        List<String> orphanCollections = new ArrayList<String>();

        // create
        GraphEntity graph = driver.createGraph(graphName, edgeDefinitions, orphanCollections, true);

        assertThat(graph.getCode(), is(201));
        assertThat(graph.getDocumentRevision(), is(not(0L)));
        assertThat(graph.getDocumentHandle(), is("_graphs/" + graphName));
        assertThat(graph.getName(), is(graphName));
        assertThat(graph.getOrphanCollections(), is(orphanCollections));

    }

    @Test
    public void test_create_graph2() throws ArangoException {

        String graphName = "unitTestGraph";

        List<EdgeDefinitionEntity> edgeDefinitions = this.createEdgeDefinitions(2, 0);

        List<String> orphanCollections = this.createOrphanCollections(2);

        // create
        GraphEntity graph = driver.createGraph(graphName, edgeDefinitions, orphanCollections, true);
        assertThat(graph.getCode(), is(201));
        assertThat(graph.getDocumentRevision(), is(not(0L)));
        assertThat(graph.getDocumentHandle(), is("_graphs/" + graphName));
        assertThat(graph.getName(), is(graphName));
        assertThat(graph.getOrphanCollections(), is(orphanCollections));
    }

    @Test
    public void test_get_graph() throws ArangoException {
        String graphName = "UnitTestGraph";
        driver.createGraph(graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
        GraphEntity graph = driver.getGraph(graphName);
        assertThat(graph.getOrphanCollections().size(), is(2));
        assertThat(graph.getName(), is(graphName));
        assertThat(graph.getEdgeDefinitions().size(), is(2));
        assertThat(graph.getEdgeDefinitions().get(0).getCollection().startsWith("edge"), is(true));

    }

    @Test
    public void test_delete_graph_keep_Collections() throws ArangoException {
        GraphsEntity graphs = driver.getGraphs();
        assertThat(graphs.getGraphs().size(), is(0));
        String graphName = "UnitTestGraph";
        driver.createGraph(graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
        graphs = driver.getGraphs();
        assertThat(graphs.getGraphs().size(), is(1));
        assertThat(driver.getGraph(graphName).getName(), is(graphName));
        driver.deleteGraph(graphName);
        graphs = driver.getGraphs();
        assertThat(graphs.getGraphs().size(), is(0));
        assertThat("number of collections", driver.getCollections(true).getCollections().size(), greaterThan(0));
    }

    @Test
    public void test_delete_graph_delete_Collections() throws ArangoException {
        GraphsEntity graphs = driver.getGraphs();
        assertThat(graphs.getGraphs().size(), is(0));
        String graphName = "UnitTestGraph";
        driver.createGraph(graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
        graphs = driver.getGraphs();
        assertThat(graphs.getGraphs().size(), is(1));
        assertThat(driver.getGraph(graphName).getName(), is(graphName));
        driver.deleteGraph(graphName, true);
        graphs = driver.getGraphs();
        assertThat(graphs.getGraphs().size(), is(0));
        assertThat("number of collections", driver.getCollections(true).getCollections().size(), is(0));
    }

    @Test
    public void test_delete_graph_not_found() throws ArangoException {
        try {
            driver.deleteGraph("foo");

        } catch (ArangoException e) {
            assertThat(e.getErrorMessage(), is("graph not found"));
        }
    }

    @Test
    public void test_get_vertex_Collections() throws ArangoException {
        String graphName = "UnitTestGraph";
        driver.createGraph(graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
        List<String> collections = driver.graphGetVertexCollections(graphName);
        assertThat(collections.size(), is(14));
    }

    @Test
    public void test_get_vertex_Collections_not_found() throws ArangoException {
        try {
            driver.graphGetVertexCollections("foo");
            fail();
        } catch (ArangoException e) {
            assertThat(e.getCode(), is(404));
            assertThat(e.getErrorMessage(), is("graph not found"));
        }
    }

    @Test
    public void test_create_new_vertex_collection() throws ArangoException {
        String graphName = "UnitTestGraph";
        String collectionName = "UnitTestCollection";
        driver.createGraph(graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
        GraphEntity graph = driver.graphCreateVertexCollection(graphName, collectionName);
        assertThat(driver.graphGetVertexCollections(graphName).contains(collectionName), is(true));
        assertThat(graph.getName(), is(graphName));
    }

    @Test
    public void test_create_new_vertex_collection_error() throws ArangoException {
        try {
            driver.graphCreateVertexCollection("foo", "UnitTestCollection");
        } catch (ArangoException e) {
            assertThat(e.getCode(), is(404));
        }
    }

    @Test
    public void test_delete_vertex_collection_keep_collection() throws ArangoException {
        String graphName = "UnitTestGraph";
        String collectionName = "UnitTestCollection";
        driver.createGraph(graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
        driver.graphCreateVertexCollection(graphName, collectionName);
        assertThat(driver.graphGetVertexCollections(graphName).contains(collectionName), is(true));
        driver.graphDeleteVertexCollection(graphName, collectionName, false);
        assertThat(driver.graphGetVertexCollections(graphName).contains(collectionName), is(false));
        assertThat(driver.getCollection(collectionName).getClass().getName(), is(CollectionEntity.class.getName()));

    }

    @Test
    public void test_delete_vertex_collection_drop_collection() throws ArangoException {
        String graphName = "UnitTestGraph";
        String collectionName = "UnitTestCollection";
        driver.createGraph(graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
        driver.graphCreateVertexCollection(graphName, collectionName);
        assertThat(driver.graphGetVertexCollections(graphName).contains(collectionName), is(true));
        driver.graphDeleteVertexCollection(graphName, collectionName, true);
        assertThat(driver.graphGetVertexCollections(graphName).contains(collectionName), is(false));
        try {
            driver.getCollection(collectionName);
            fail();
        } catch (ArangoException e) {
            assertThat(e.getCode(), greaterThan(300));
        }

    }

    @Test
    public void test_delete_vertex_collection_drop_collection_fail() throws ArangoException {
        String graphName1 = "UnitTestGraph1";
        String graphName2 = "UnitTestGraph2";
        String collectionName = "UnitTestCollection";
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
        String graphName = "UnitTestGraph";
        driver.createGraph(graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
        try {
            driver.graphDeleteVertexCollection(graphName, "bar", true);
            fail();
        } catch (ArangoException e) {
            assertThat(e.getCode(), is(404));
        }
    }

    @Test
    public void test_get_edge_Collections() throws ArangoException {
        String graphName = "UnitTestGraph";
        driver.createGraph(graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
        List<String> collections = driver.graphGetEdgeCollections(graphName);
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
        String graphName = "UnitTestGraph";
        String edgeCollectionName = "UnitTestEdgeCollection";
        String fromCollectionName = "UnitTestFromCollection";
        String toCollectionName = "UnitTestToCollection";
        driver.createGraph(graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
        EdgeDefinitionEntity edgeDefinition = new EdgeDefinitionEntity();
        edgeDefinition.setCollection(edgeCollectionName);
        List<String> from = new ArrayList<String>();
        from.add(fromCollectionName);
        edgeDefinition.setFrom(from);
        List<String> to = new ArrayList<String>();
        to.add(toCollectionName);
        edgeDefinition.setTo(to);
        driver.graphCreateEdgeDefinition(graphName, edgeDefinition);
        assertThat(driver.graphGetEdgeCollections(graphName).contains(edgeCollectionName), is(true));
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
        String graphName = "UnitTestGraph";
        String edgeCollectionName = "UnitTestEdgeCollection";
        String fromCollectionName1 = "UnitTestFromCollection1";
        String fromCollectionName2 = "UnitTestFromCollection2";
        String toCollectionName1 = "UnitTestToCollection1";
        String toCollectionName2 = "UnitTestToCollection2";
        driver.createGraph(graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
        EdgeDefinitionEntity edgeDefinition1 = new EdgeDefinitionEntity();
        edgeDefinition1.setCollection(edgeCollectionName);
        List<String> from1 = new ArrayList<String>();
        from1.add(fromCollectionName1);
        edgeDefinition1.setFrom(from1);
        List<String> to1 = new ArrayList<String>();
        to1.add(toCollectionName1);
        edgeDefinition1.setTo(to1);
        driver.graphCreateEdgeDefinition(graphName, edgeDefinition1);
        assertThat(driver.graphGetEdgeCollections(graphName).contains(edgeCollectionName), is(true));
        assertThat(driver.graphGetVertexCollections(graphName).contains(fromCollectionName1), is(true));
        assertThat(driver.graphGetVertexCollections(graphName).contains(toCollectionName1), is(true));
        EdgeDefinitionEntity edgeDefinition2 = new EdgeDefinitionEntity();
        edgeDefinition2.setCollection(edgeCollectionName);
        List<String> from2 = new ArrayList<String>();
        from2.add(fromCollectionName2);
        edgeDefinition2.setFrom(from2);
        List<String> to2 = new ArrayList<String>();
        to2.add(toCollectionName2);
        edgeDefinition2.setTo(to2);
        GraphEntity graph = driver.graphReplaceEdgeDefinition(graphName, edgeCollectionName, edgeDefinition2);
        List<EdgeDefinitionEntity> edgeDefinitions = graph.getEdgeDefinitions();
        for (EdgeDefinitionEntity edgeDef : edgeDefinitions) {
            List<String> f = edgeDef.getFrom();
            assertThat(f.contains(from1), is(false));
            List<String> t = edgeDef.getTo();
            assertThat(t.contains(to1), is(false));
        }
        assertThat(driver.graphGetEdgeCollections(graphName).contains(edgeCollectionName), is(true));
        assertThat(driver.graphGetVertexCollections(graphName).contains(fromCollectionName1), is(true));
        assertThat(driver.graphGetVertexCollections(graphName).contains(toCollectionName1), is(true));
        assertThat(driver.graphGetVertexCollections(graphName).contains(fromCollectionName2), is(true));
        assertThat(driver.graphGetVertexCollections(graphName).contains(toCollectionName2), is(true));
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
        List<EdgeDefinitionEntity> edgeDefinitions1 = graph1.getEdgeDefinitions();
        for (EdgeDefinitionEntity edgeDef : edgeDefinitions1) {
            List<String> f = edgeDef.getFrom();
            assertThat(f.contains(from1), is(false));
            List<String> t = edgeDef.getTo();
            assertThat(t.contains(to1), is(false));
        }
        GraphEntity graph2 = driver.graphReplaceEdgeDefinition(graphName1, edgeCollectionName, edgeDefinition2);
        List<EdgeDefinitionEntity> edgeDefinitions2 = graph2.getEdgeDefinitions();
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
        String graphName = "UnitTestGraph";
        driver.createGraph(graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
        driver.graphDeleteEdgeDefinition(graphName, "edge-1", false);
        assertThat(driver.graphGetEdgeCollections(graphName).contains("edge-1"), is(false));
        assertThat(driver.graphGetEdgeCollections(graphName).contains("edge-2"), is(true));

    }

    @Test
    public void test_delete_edge_definition_drop_collections() throws ArangoException {
        String graphName = "UnitTestGraph";
        driver.createGraph(graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
        GraphEntity blub = driver.graphDeleteEdgeDefinition(graphName, "edge-1", true);
        assertThat(driver.graphGetEdgeCollections(graphName).contains("edge-1"), is(false));
        assertThat(driver.graphGetEdgeCollections(graphName).contains("edge-2"), is(true));
        try {
            CollectionEntity col = driver.getCollection("edge-1");
            fail();
        } catch (ArangoException e) {
            assertThat(e.getCode(), greaterThan(300));
        }

    }

    /*
     * @Test public void test_drop_Graph() throws ArangoException { String
     * graphName = "unitTestGraph";
     * 
     * // create GraphEntity entity1 = driver.createGraph( graphName, new
     * ArrayList<EdgeDefinitionEntity>(), new ArrayList<String>(), true );
     * 
     * assertThat(entity1.getCode(), is(201));
     * assertThat(entity1.getDocumentRevision(), is(not(0L)));
     * assertThat(entity1.getDocumentHandle(), is("_graphs/" + graphName));
     * 
     * }
     * 
     * 
     * 
     * 
     * 
     * @Test
     * 
     * @Ignore public void test_create_graph_202() throws ArangoException {
     * 
     * // in 1.4.0 manual // 202 is returned if the graph was created
     * successfully and waitForSync // was false.
     * 
     * GraphEntity entity = driver.createGraph("g1", "vcol1", "ecol1", false);
     * assertThat(entity.getCode(), is(202));
     * assertThat(entity.getDocumentRevision(), is(not(0L)));
     * assertThat(entity.getDocumentHandle(), is("_graphs/g1"));
     * assertThat(entity.getDocumentKey(), is("g1")); //
     * assertThat(entity.getVertices(), is("vcol1")); //
     * assertThat(entity.getEdges(), is("ecol1"));
     * 
     * }
     * 
     * @Test
     * 
     * @Ignore public void test_create_graph_dup() throws ArangoException {
     * 
     * GraphEntity entity = driver.createGraph("g1", "vcol1", "ecol1", true);
     * assertThat(entity.getCode(), is(201));
     * assertThat(entity.getDocumentRevision(), is(not(0L)));
     * assertThat(entity.getDocumentHandle(), is("_graphs/g1"));
     * assertThat(entity.getDocumentKey(), is("g1")); //
     * assertThat(entity.getVertices(), is("vcol1")); //
     * assertThat(entity.getEdges(), is("ecol1"));
     * 
     * try { driver.createGraph("g1", "vcol1", "ecol1", false); fail(); } catch
     * (ArangoException e) { assertThat(e.getCode(), is(400));
     * assertThat(e.getErrorNumber(), is(1902)); // graph with the name //
     * already exists }
     * 
     * }
     * 
     * // TODO: errorNum: 1902 : "found graph but has different <name>"
     * 
     * @Test
     * 
     * @Ignore public void get_graphs() throws ArangoException {
     * 
     * driver.createGraph("g1", "v1", "e1", null); driver.createGraph("g2",
     * "v2", "e2", null); driver.createGraph("g3", "v3", "e3", null);
     * 
     * GraphsEntity graphs = driver.getGraphs(); assertThat(graphs.isError(),
     * is(false)); assertThat(graphs.getCode(), is(200));
     * assertThat(graphs.getGraphs().size(), is(3));
     * 
     * Collections.sort(graphs.getGraphs(), new Comparator<GraphEntity>() {
     * public int compare(GraphEntity o1, GraphEntity o2) { return
     * o1.getDocumentKey().compareTo(o2.getDocumentKey()); } });
     * 
     * GraphEntity g = graphs.getGraphs().get(0);
     * assertThat(g.getDocumentRevision(), is(not(0L)));
     * assertThat(g.getDocumentHandle(), is("_graphs/g1"));
     * assertThat(g.getDocumentKey(), is("g1")); // assertThat(g.getVertices(),
     * is("v1")); // assertThat(g.getEdges(), is("e1"));
     * 
     * g = graphs.getGraphs().get(1); assertThat(g.getDocumentRevision(),
     * is(not(0L))); assertThat(g.getDocumentHandle(), is("_graphs/g2"));
     * assertThat(g.getDocumentKey(), is("g2")); // assertThat(g.getVertices(),
     * is("v2")); // assertThat(g.getEdges(), is("e2"));
     * 
     * g = graphs.getGraphs().get(2); assertThat(g.getDocumentRevision(),
     * is(not(0L))); assertThat(g.getDocumentHandle(), is("_graphs/g3"));
     * assertThat(g.getDocumentKey(), is("g3")); // assertThat(g.getVertices(),
     * is("v3")); // assertThat(g.getEdges(), is("e3"));
     * 
     * }
     * 
     * @Test
     * 
     * @Ignore public void test_get_graphOLD() throws ArangoException {
     * 
     * driver.createGraph("g1", "v1", "e1", null); GraphEntity g1 =
     * driver.getGraph("g1"); assertThat(g1.getDocumentRevision(), is(not(0L)));
     * assertThat(g1.getDocumentHandle(), is("_graphs/g1"));
     * assertThat(g1.getDocumentKey(), is("g1")); //
     * assertThat(g1.getVertices(), is("v1")); // assertThat(g1.getEdges(),
     * is("e1"));
     * 
     * }
     * 
     * @Test public void test_get_graph_404() throws ArangoException {
     * 
     * try { driver.getGraph("g1"); fail(); } catch (ArangoException e) {
     * assertThat(e.getCode(), is(404)); assertThat(e.getErrorNumber(),
     * is(1901)); }
     * 
     * }
     * 
     * @Test public void test_get_graph_none_match_eq() throws ArangoException {
     * 
     * GraphEntity g = driver.createGraph("g1", "v1", "e1", null); GraphEntity
     * g1 = driver.getGraph("g1", g.getDocumentRevision(), null);
     * assertThat(g1.getStatusCode(), is(304)); assertThat(g1.isNotModified(),
     * is(true));
     * 
     * }
     * 
     * @Test public void test_get_graph_none_match_ne() throws ArangoException {
     * 
     * GraphEntity g = driver.createGraph("g1", "v1", "e1", null);
     * 
     * GraphEntity g1 = driver.getGraph("g1", g.getDocumentRevision() + 1,
     * null); assertThat(g1.isNotModified(), is(false));
     * assertThat(g1.getDocumentRevision(), is(not(0L)));
     * assertThat(g1.getDocumentHandle(), is("_graphs/g1"));
     * assertThat(g1.getDocumentKey(), is("g1")); //
     * assertThat(g1.getVertices(), is("v1")); // assertThat(g1.getEdges(),
     * is("e1"));
     * 
     * }
     * 
     * @Test public void test_get_graph_match_eq() throws ArangoException {
     * 
     * GraphEntity g = driver.createGraph("g1", "v1", "e1", null);
     * 
     * GraphEntity g1 = driver.getGraph("g1", null, g.getDocumentRevision());
     * assertThat(g1.isNotModified(), is(false));
     * assertThat(g1.getDocumentRevision(), is(not(0L)));
     * assertThat(g1.getDocumentHandle(), is("_graphs/g1"));
     * assertThat(g1.getDocumentKey(), is("g1")); //
     * assertThat(g1.getVertices(), is("v1")); // assertThat(g1.getEdges(),
     * is("e1"));
     * 
     * }
     * 
     * @Test public void test_get_graph_match_ne() throws ArangoException {
     * 
     * GraphEntity g = driver.createGraph("g1", "v1", "e1", null);
     * 
     * try { driver.getGraph("g1", null, g.getDocumentRevision() + 1); fail(); }
     * catch (ArangoException e) { assertThat(e.getCode(), is(412));
     * assertThat(e.getErrorNumber(), is(1901)); // wrong revision }
     * 
     * }
     * 
     * @Test public void test_delete_graph() throws ArangoException {
     * 
     * GraphEntity g1 = driver.createGraph("g1", "v1", "e1", false);
     * assertThat(g1.getCode(), is(201));
     * 
     * DeletedEntity del = driver.deleteGraph("g1"); assertThat(del.getCode(),
     * is(200)); assertThat(del.getDeleted(), is(true));
     * 
     * }
     * 
     * @Test public void test_delete_graph_404() throws ArangoException {
     * 
     * try { driver.deleteGraph("g1"); fail(); } catch (ArangoException e) {
     * assertThat(e.getCode(), is(404)); assertThat(e.getErrorNumber(),
     * is(1901)); }
     * 
     * }
     * 
     * @Test public void test_delete_graph_ifmatch_ok() throws ArangoException {
     * 
     * GraphEntity g1 = driver.createGraph("g1", "v1", "e1", false);
     * assertThat(g1.getCode(), is(201));
     * 
     * DeletedEntity del = driver.deleteGraph("g1", g1.getDocumentRevision());
     * assertThat(del.getCode(), is(200)); assertThat(del.getDeleted(),
     * is(true));
     * 
     * }
     * 
     * @Test public void test_delete_graph_ifmatch_ng_412() throws
     * ArangoException {
     * 
     * GraphEntity g1 = driver.createGraph("g1", "v1", "e1", false);
     * assertThat(g1.getCode(), is(201));
     * 
     * try { driver.deleteGraph("g1", 10L); fail(); } catch (ArangoException e)
     * { assertThat(e.getCode(), is(412)); assertThat(e.getErrorNumber(),
     * is(1901)); // wrong revision assertThat(e.getMessage(),
     * is("[1901]wrong revision")); }
     * 
     * }
     */
    private List<EdgeDefinitionEntity> createEdgeDefinitions(int count, int offset) {
        List<EdgeDefinitionEntity> edgeDefinitions = new ArrayList<EdgeDefinitionEntity>();
        for (int i = 1 + offset; i <= count + offset; i++) {
            EdgeDefinitionEntity edgeDefinition = new EdgeDefinitionEntity();
            edgeDefinition.setCollection("edge-" + i);
            List<String> from = new ArrayList<String>();
            from.add("from" + i + "-1");
            from.add("from" + i + "-2");
            from.add("from" + i + "-3");
            edgeDefinition.setFrom(from);
            List<String> to = new ArrayList<String>();
            to.add("to" + i + "-1");
            to.add("to" + i + "-2");
            to.add("to" + i + "-3");
            edgeDefinition.setTo(to);
            edgeDefinitions.add(edgeDefinition);
        }
        return edgeDefinitions;
    }

    private List<String> createOrphanCollections(int count) {
        List<String> orphanCollections = new ArrayList<String>();
        for (int i = 1; i <= count; i++) {
            orphanCollections.add("orphan" + i);
        }
        return orphanCollections;
    }

}
