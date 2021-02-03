/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb;

import com.arangodb.entity.CollectionPropertiesEntity;
import com.arangodb.entity.EdgeDefinition;
import com.arangodb.entity.GraphEntity;
import com.arangodb.model.GraphCreateOptions;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.assumeTrue;

/**
 * @author Mark Vollmary
 */
@RunWith(Parameterized.class)
public class ArangoGraphTest extends BaseTest {

    private static final String GRAPH_NAME = "ArangoGraphTest_graph";

    private static final String VERTEX_COL_1 = "ArangoGraphTest_vertex_collection_1";
    private static final String VERTEX_COL_2 = "ArangoGraphTest_vertex_collection_2";
    private static final String VERTEX_COL_3 = "ArangoGraphTest_vertex_collection_3";
    private static final String VERTEX_COL_4 = "ArangoGraphTest_vertex_collection_4";

    private static final String EDGE_COL_1 = "ArangoGraphTest_edge_collection_1";
    private static final String EDGE_COL_2 = "ArangoGraphTest_edge_collection_2";
    private static final String EDGE_COL_3 = "ArangoGraphTest_edge_collection_3";

    private static final Integer REPLICATION_FACTOR = 2;
    private static final Integer NUMBER_OF_SHARDS = 2;

    private static final EdgeDefinition ed1 = new EdgeDefinition().collection(EDGE_COL_1).from(VERTEX_COL_1).to(VERTEX_COL_2);
    private static final EdgeDefinition ed2 = new EdgeDefinition().collection(EDGE_COL_2).from(VERTEX_COL_2).to(VERTEX_COL_1, VERTEX_COL_3);

    private final ArangoCollection vertexCollection1;
    private final ArangoCollection vertexCollection2;
    private final ArangoCollection vertexCollection3;
    private final ArangoCollection vertexCollection4;

    private final ArangoCollection edgeCollection1;
    private final ArangoCollection edgeCollection2;
    private final ArangoCollection edgeCollection3;

    private final ArangoGraph graph;

    private final ArangoVertexCollection vertices1;
    private final ArangoVertexCollection vertices2;
    private final ArangoVertexCollection vertices3;
    private final ArangoVertexCollection vertices4;

    private final ArangoEdgeCollection edges1;
    private final ArangoEdgeCollection edges2;
    private final ArangoEdgeCollection edges3;

    @BeforeClass
    public static void init() {
        final Collection<EdgeDefinition> edgeDefinitions = Arrays.asList(ed1, ed2);

        final GraphCreateOptions options = new GraphCreateOptions()
                .replicationFactor(REPLICATION_FACTOR)
                .numberOfShards(NUMBER_OF_SHARDS);

        BaseTest.initGraph(GRAPH_NAME, edgeDefinitions, options);
    }

    public ArangoGraphTest(final ArangoDB arangoDB) {
        super(arangoDB);

        vertexCollection1 = db.collection(VERTEX_COL_1);
        vertexCollection2 = db.collection(VERTEX_COL_2);
        vertexCollection3 = db.collection(VERTEX_COL_3);
        vertexCollection4 = db.collection(VERTEX_COL_4);

        edgeCollection1 = db.collection(EDGE_COL_1);
        edgeCollection2 = db.collection(EDGE_COL_2);
        edgeCollection3 = db.collection(EDGE_COL_3);

        graph = db.graph(GRAPH_NAME);

        vertices1 = graph.vertexCollection(VERTEX_COL_1);
        vertices2 = graph.vertexCollection(VERTEX_COL_2);
        vertices3 = graph.vertexCollection(VERTEX_COL_3);
        vertices4 = graph.vertexCollection(VERTEX_COL_4);

        edges1 = graph.edgeCollection(EDGE_COL_1);
        edges2 = graph.edgeCollection(EDGE_COL_2);
        edges3 = graph.edgeCollection(EDGE_COL_3);
    }

    @Test
    public void exists() {
        assertThat(graph.exists(), is(true));
        assertThat(db.graph(GRAPH_NAME + "no").exists(), is(false));
    }

    @Test
    public void createWithReplicationAndMinReplicationFactor() {
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isCluster());

        final Collection<EdgeDefinition> edgeDefinitions = new ArrayList<>();
        final GraphEntity graph = db.createGraph(GRAPH_NAME + "_1", edgeDefinitions, new GraphCreateOptions().isSmart(true).replicationFactor(2).minReplicationFactor(2));
        assertThat(graph, is(notNullValue()));
        assertThat(graph.getName(), is(GRAPH_NAME + "_1"));
        assertThat(graph.getMinReplicationFactor(), is(2));
        assertThat(graph.getReplicationFactor(), is(2));
        db.graph(GRAPH_NAME + "_1").drop();
    }

    @Test
    public void getGraphs() {
        final Collection<GraphEntity> graphs = db.getGraphs();
        assertThat(graphs.stream().anyMatch(it -> it.getName().equals(GRAPH_NAME)), is(true));
    }

    @Test
    public void getInfo() {
        final GraphEntity info = graph.getInfo();
        assertThat(info, is(notNullValue()));
        assertThat(info.getName(), is(GRAPH_NAME));
        assertThat(info.getEdgeDefinitions().size(), is(2));
        final Iterator<EdgeDefinition> iterator = info.getEdgeDefinitions().iterator();
        final EdgeDefinition e1 = iterator.next();
        assertThat(e1.getCollection(), is(EDGE_COL_1));
        assertThat(e1.getFrom(), hasItem(VERTEX_COL_1));
        assertThat(e1.getTo(), hasItem(VERTEX_COL_2));
        final EdgeDefinition e2 = iterator.next();
        assertThat(e2.getCollection(), is(EDGE_COL_2));
        assertThat(e2.getFrom(), hasItem(VERTEX_COL_2));
        assertThat(e2.getTo(), hasItems(VERTEX_COL_1, VERTEX_COL_3));
        assertThat(info.getOrphanCollections(), is(empty()));

        if (isCluster()) {
            for (final String collection : new String[]{EDGE_COL_1, EDGE_COL_2, VERTEX_COL_1, VERTEX_COL_2}) {
                final CollectionPropertiesEntity properties = db.collection(collection).getProperties();
                assertThat(properties.getReplicationFactor(), is(REPLICATION_FACTOR));
                assertThat(properties.getNumberOfShards(), is(NUMBER_OF_SHARDS));
            }
        }
    }

    @Test
    public void getVertexCollections() {
        final Collection<String> vertexCollections = graph.getVertexCollections();
        assertThat(vertexCollections, is(notNullValue()));
        assertThat(vertexCollections.size(), is(3));
        assertThat(vertexCollections, hasItems(VERTEX_COL_1, VERTEX_COL_2, VERTEX_COL_3));
    }

    @Test
    public void addVertexCollection() {
        final GraphEntity g = graph.addVertexCollection(VERTEX_COL_4);
        assertThat(g, is(notNullValue()));
        final Collection<String> vertexCollections = graph.getVertexCollections();
        assertThat(vertexCollections, hasItems(VERTEX_COL_1, VERTEX_COL_2, VERTEX_COL_3, VERTEX_COL_4));

        // revert
        graph.vertexCollection(VERTEX_COL_4).drop();
    }

    @Test
    public void getEdgeCollections() {
        final Collection<String> edgeCollections = graph.getEdgeDefinitions();
        assertThat(edgeCollections, is(notNullValue()));
        assertThat(edgeCollections.size(), is(2));
        assertThat(edgeCollections, hasItems(EDGE_COL_1, EDGE_COL_2));
    }

    @Test
    public void addEdgeDefinition() {
        EdgeDefinition ed = new EdgeDefinition().collection(EDGE_COL_3).from(VERTEX_COL_1).to(VERTEX_COL_2);
        final GraphEntity g = graph.addEdgeDefinition(ed);
        assertThat(g, is(notNullValue()));
        final Collection<EdgeDefinition> edgeDefinitions = g.getEdgeDefinitions();
        assertThat(edgeDefinitions.size(), is(3));
        int count = 0;
        for (final EdgeDefinition e : edgeDefinitions) {
            if (e.getCollection().equals(EDGE_COL_3)) {
                count++;
            }
        }
        assertThat(count, is(1));
        for (final EdgeDefinition e : edgeDefinitions) {
            if (e.getCollection().equals(EDGE_COL_3)) {
                assertThat(e.getFrom(), hasItem(VERTEX_COL_1));
                assertThat(e.getTo(), hasItem(VERTEX_COL_2));
            }
        }
        if (isCluster()) {
            final CollectionPropertiesEntity properties = db.collection(EDGE_COL_3).getProperties();
            assertThat(properties.getReplicationFactor(), is(REPLICATION_FACTOR));
            assertThat(properties.getNumberOfShards(), is(NUMBER_OF_SHARDS));
        }

        // revert
        graph.removeEdgeDefinition(EDGE_COL_3);
    }

    @Test
    public void replaceEdgeDefinition() {
        final GraphEntity g = graph
                .replaceEdgeDefinition(new EdgeDefinition().collection(EDGE_COL_1).from(VERTEX_COL_3).to(VERTEX_COL_4));
        final Collection<EdgeDefinition> edgeDefinitions = g.getEdgeDefinitions();
        assertThat(edgeDefinitions.size(), is(2));
        int count = 0;
        for (final EdgeDefinition e : edgeDefinitions) {
            if (e.getCollection().equals(EDGE_COL_1)) {
                count++;
            }
        }
        assertThat(count, is(1));
        for (final EdgeDefinition e : edgeDefinitions) {
            if (e.getCollection().equals(EDGE_COL_1)) {
                assertThat(e.getFrom(), hasItem(VERTEX_COL_3));
                assertThat(e.getTo(), hasItem(VERTEX_COL_4));
            }
        }

        // revert
        graph.removeEdgeDefinition(EDGE_COL_1);
        graph.vertexCollection(VERTEX_COL_4).drop();
        graph.addEdgeDefinition(ed1);
    }

    @Test
    public void removeEdgeDefinition() {
        final GraphEntity g = graph.removeEdgeDefinition(EDGE_COL_1);
        final Collection<EdgeDefinition> edgeDefinitions = g.getEdgeDefinitions();
        assertThat(edgeDefinitions.size(), is(1));
        assertThat(edgeDefinitions.iterator().next().getCollection(), is(EDGE_COL_2));

        //revert
        graph.addEdgeDefinition(ed1);
    }

    @Test
    public void smartGraph() {
        assumeTrue(isEnterprise());
        assumeTrue(isCluster());

        final Collection<EdgeDefinition> edgeDefinitions = new ArrayList<>();
        edgeDefinitions.add(new EdgeDefinition().collection("smartGraph-edge-" + rnd()).from("smartGraph-vertex-" + rnd()).to("smartGraph-vertex-" + rnd()));

        String graphId = GRAPH_NAME + rnd();
        final GraphEntity g = db.createGraph(graphId, edgeDefinitions, new GraphCreateOptions().isSmart(true).smartGraphAttribute("test").numberOfShards(2));

        assertThat(g, is(notNullValue()));
        assertThat(g.getIsSmart(), is(true));
        assertThat(g.getSmartGraphAttribute(), is("test"));
        assertThat(g.getNumberOfShards(), is(2));
    }

    @Test
    public void disjointSmartGraph() {
        assumeTrue(isEnterprise());
        assumeTrue(isCluster());
        assumeTrue((isAtLeastVersion(3, 7)));

        final Collection<EdgeDefinition> edgeDefinitions = new ArrayList<>();
        edgeDefinitions.add(new EdgeDefinition().collection("smartGraph-edge-" + rnd()).from("smartGraph-vertex-" + rnd()).to("smartGraph-vertex-" + rnd()));

        String graphId = GRAPH_NAME + rnd();
        final GraphEntity g = db.createGraph(graphId, edgeDefinitions, new GraphCreateOptions()
                .isSmart(true).isDisjoint(true).smartGraphAttribute("test").numberOfShards(2));

        assertThat(g, is(notNullValue()));
        assertThat(g.getIsSmart(), is(true));
        assertThat(g.getIsDisjoint(), is(true));
        assertThat(g.getSmartGraphAttribute(), is("test"));
        assertThat(g.getNumberOfShards(), is(2));
    }

    @Test
    public void drop() {
        final String edgeCollection = "edge_" + rnd();
        final String vertexCollection = "vertex_" + rnd();
        final String graphId = GRAPH_NAME + rnd();
        final GraphEntity result = db.graph(graphId).create(Collections
                .singleton(new EdgeDefinition().collection(edgeCollection).from(vertexCollection).to(vertexCollection)));
        assertThat(result, is(notNullValue()));
        db.graph(graphId).drop();
        assertThat(db.collection(edgeCollection).exists(), is(true));
        assertThat(db.collection(vertexCollection).exists(), is(true));
    }

    @Test
    public void dropPlusDropCollections() {
        final String edgeCollection = "edge_dropC" + rnd();
        final String vertexCollection = "vertex_dropC" + rnd();
        final String graphId = GRAPH_NAME + "_dropC" + rnd();
        final GraphEntity result = db.graph(graphId).create(Collections
                .singleton(new EdgeDefinition().collection(edgeCollection).from(vertexCollection).to(vertexCollection)));
        assertThat(result, is(notNullValue()));
        db.graph(graphId).drop(true);
        assertThat(db.collection(edgeCollection).exists(), is(false));
        assertThat(db.collection(vertexCollection).exists(), is(false));
    }

}
