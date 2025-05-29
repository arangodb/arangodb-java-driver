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
import com.arangodb.entity.ReplicationFactor;
import com.arangodb.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
class ArangoGraphAsyncTest extends BaseJunit5 {

    private static final String GRAPH_NAME = "ArangoGraphTest_graph";

    private static final String VERTEX_COL_1 = rndName();
    private static final String VERTEX_COL_2 = rndName();
    private static final String VERTEX_COL_3 = rndName();
    private static final String VERTEX_COL_4 = rndName();
    private static final String VERTEX_COL_5 = rndName();

    private static final String EDGE_COL_1 = rndName();
    private static final String EDGE_COL_2 = rndName();
    private static final String EDGE_COL_3 = rndName();

    private static final Integer REPLICATION_FACTOR = 2;
    private static final Integer NUMBER_OF_SHARDS = 2;

    private static final EdgeDefinition ed1 =
            new EdgeDefinition().collection(EDGE_COL_1).from(VERTEX_COL_1).to(VERTEX_COL_5);
    private static final EdgeDefinition ed2 =
            new EdgeDefinition().collection(EDGE_COL_2).from(VERTEX_COL_2).to(VERTEX_COL_1, VERTEX_COL_3);

    private static Stream<Arguments> asyncGraphs() {
        return asyncDbsStream()
                .map(mapNamedPayload(db -> db.graph(GRAPH_NAME)))
                .map(Arguments::of);
    }

    @BeforeAll
    static void init() {
        final Collection<EdgeDefinition> edgeDefinitions = Arrays.asList(ed1, ed2);

        final GraphCreateOptions options = new GraphCreateOptions()
                .replicationFactor(REPLICATION_FACTOR)
                .numberOfShards(NUMBER_OF_SHARDS);

        initGraph(GRAPH_NAME, edgeDefinitions, options);
    }


    @ParameterizedTest
    @MethodSource("asyncGraphs")
    void exists(ArangoGraphAsync graph) throws ExecutionException, InterruptedException {
        assertThat(graph.exists().get()).isTrue();
        assertThat(graph.db().graph(GRAPH_NAME + "no").exists().get()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void createWithReplicationAndWriteConcern(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isCluster());

        final Collection<EdgeDefinition> edgeDefinitions = new ArrayList<>();
        final GraphEntity graph = db.createGraph(GRAPH_NAME + "_1", edgeDefinitions,
                new GraphCreateOptions().isSmart(true).replicationFactor(2).writeConcern(2)).get();
        assertThat(graph).isNotNull();
        assertThat(graph.getName()).isEqualTo(GRAPH_NAME + "_1");
        assertThat(graph.getWriteConcern()).isEqualTo(2);
        assertThat(graph.getReplicationFactor().get()).isEqualTo(2);
        db.graph(GRAPH_NAME + "_1").drop().get();
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void getGraphs(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final Collection<GraphEntity> graphs = db.getGraphs().get();
        assertThat(graphs.stream().anyMatch(it -> it.getName().equals(GRAPH_NAME))).isTrue();
    }

    @ParameterizedTest
    @MethodSource("asyncGraphs")
    void getInfo(ArangoGraphAsync graph) throws ExecutionException, InterruptedException {
        final GraphEntity info = graph.getInfo().get();
        assertThat(info).isNotNull();
        assertThat(info.getName()).isEqualTo(GRAPH_NAME);
        assertThat(info.getEdgeDefinitions()).hasSize(2);

        assertThat(info.getEdgeDefinitions())
                .anySatisfy(e1 -> {
                    assertThat(e1.getCollection()).isEqualTo(EDGE_COL_1);
                    assertThat(e1.getFrom()).contains(VERTEX_COL_1);
                    assertThat(e1.getTo()).contains(VERTEX_COL_5);
                })
                .anySatisfy(e2 -> {
                    assertThat(e2.getCollection()).isEqualTo(EDGE_COL_2);
                    assertThat(e2.getFrom()).contains(VERTEX_COL_2);
                    assertThat(e2.getTo()).contains(VERTEX_COL_1, VERTEX_COL_3);
                });

        assertThat(info.getOrphanCollections()).isEmpty();

        if (isCluster()) {
            for (final String collection : new String[]{EDGE_COL_1, EDGE_COL_2, VERTEX_COL_1, VERTEX_COL_2, VERTEX_COL_5}) {
                final CollectionPropertiesEntity properties = graph.db().collection(collection).getProperties().get();
                assertThat(properties.getReplicationFactor().get()).isEqualTo(REPLICATION_FACTOR);
                assertThat(properties.getNumberOfShards()).isEqualTo(NUMBER_OF_SHARDS);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("asyncGraphs")
    void getVertexCollections(ArangoGraphAsync graph) throws ExecutionException, InterruptedException {
        final Collection<String> vertexCollections = graph.getVertexCollections().get();
        assertThat(vertexCollections)
                .hasSize(4)
                .contains(VERTEX_COL_1, VERTEX_COL_2, VERTEX_COL_3, VERTEX_COL_5);
    }

    @ParameterizedTest
    @MethodSource("asyncGraphs")
    void addVertexCollection(ArangoGraphAsync graph) throws ExecutionException, InterruptedException {
        final GraphEntity g = graph.addVertexCollection(VERTEX_COL_4).get();
        assertThat(g).isNotNull();
        final Collection<String> vertexCollections = graph.getVertexCollections().get();
        assertThat(vertexCollections).contains(VERTEX_COL_1, VERTEX_COL_2, VERTEX_COL_3, VERTEX_COL_4, VERTEX_COL_5);

        // revert
        graph.vertexCollection(VERTEX_COL_4).remove().get();
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void addSatelliteVertexCollection(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        String v1Name = "vertex-" + rnd();

        ArangoGraphAsync g = db.graph(GRAPH_NAME + rnd());
        g.create(null, new GraphCreateOptions().isSmart(true).smartGraphAttribute("test")).get();
        g.addVertexCollection(v1Name, new VertexCollectionCreateOptions().satellites(v1Name)).get();

        Collection<String> vertexCollections = g.getVertexCollections().get();
        assertThat(vertexCollections).contains(v1Name);
        assertThat(db.collection(v1Name).getProperties().get().getReplicationFactor()).isEqualTo(ReplicationFactor.ofSatellite());

        // revert
        g.drop().get();
    }

    @ParameterizedTest
    @MethodSource("asyncGraphs")
    void getEdgeCollections(ArangoGraphAsync graph) throws ExecutionException, InterruptedException {
        final Collection<String> edgeCollections = graph.getEdgeDefinitions().get();
        assertThat(edgeCollections)
                .hasSize(2)
                .contains(EDGE_COL_1, EDGE_COL_2);
    }

    @ParameterizedTest
    @MethodSource("asyncGraphs")
    void addEdgeDefinition(ArangoGraphAsync graph) throws ExecutionException, InterruptedException {
        EdgeDefinition ed = new EdgeDefinition().collection(EDGE_COL_3).from(VERTEX_COL_1).to(VERTEX_COL_2);
        final GraphEntity g = graph.addEdgeDefinition(ed).get();
        assertThat(g).isNotNull();
        final Collection<EdgeDefinition> edgeDefinitions = g.getEdgeDefinitions();
        assertThat(edgeDefinitions).hasSize(3);
        int count = 0;
        for (final EdgeDefinition e : edgeDefinitions) {
            if (e.getCollection().equals(EDGE_COL_3)) {
                count++;
            }
        }
        assertThat(count).isEqualTo(1);
        for (final EdgeDefinition e : edgeDefinitions) {
            if (e.getCollection().equals(EDGE_COL_3)) {
                assertThat(e.getFrom()).contains(VERTEX_COL_1);
                assertThat(e.getTo()).contains(VERTEX_COL_2);
            }
        }
        if (isCluster()) {
            final CollectionPropertiesEntity properties = graph.db().collection(EDGE_COL_3).getProperties().get();
            assertThat(properties.getReplicationFactor().get()).isEqualTo(REPLICATION_FACTOR);
            assertThat(properties.getNumberOfShards()).isEqualTo(NUMBER_OF_SHARDS);
        }

        // revert
        graph.edgeCollection(EDGE_COL_3).remove().get();
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void addSatelliteEdgeDefinition(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        String eName = "edge-" + rnd();
        String v1Name = "vertex-" + rnd();
        String v2Name = "vertex-" + rnd();
        EdgeDefinition ed = new EdgeDefinition().collection(eName).from(v1Name).to(v2Name).satellites(v1Name);

        ArangoGraphAsync g = db.graph(GRAPH_NAME + rnd());
        g.create(null, new GraphCreateOptions().isSmart(true).smartGraphAttribute("test")).get();
        g.addEdgeDefinition(ed).get();
        final GraphEntity ge = g.getInfo().get();
        assertThat(ge).isNotNull();
        final Collection<EdgeDefinition> edgeDefinitions = ge.getEdgeDefinitions();
        assertThat(edgeDefinitions).hasSize(1);
        EdgeDefinition e = edgeDefinitions.iterator().next();
        assertThat(e.getCollection()).isEqualTo(eName);
        assertThat(e.getFrom()).contains(v1Name);
        assertThat(e.getTo()).contains(v2Name);

        assertThat(db.collection(v1Name).getProperties().get().getReplicationFactor()).isEqualTo(ReplicationFactor.ofSatellite());

        // revert
        g.drop().get();
    }

    @ParameterizedTest
    @MethodSource("asyncGraphs")
    void replaceEdgeDefinition(ArangoGraphAsync graph) throws ExecutionException, InterruptedException {
        final GraphEntity g = graph
                .replaceEdgeDefinition(new EdgeDefinition().collection(EDGE_COL_1).from(VERTEX_COL_3).to(VERTEX_COL_4)).get();
        final Collection<EdgeDefinition> edgeDefinitions = g.getEdgeDefinitions();
        assertThat(edgeDefinitions).hasSize(2);
        int count = 0;
        for (final EdgeDefinition e : edgeDefinitions) {
            if (e.getCollection().equals(EDGE_COL_1)) {
                count++;
            }
        }
        assertThat(count).isEqualTo(1);
        for (final EdgeDefinition e : edgeDefinitions) {
            if (e.getCollection().equals(EDGE_COL_1)) {
                assertThat(e.getFrom()).contains(VERTEX_COL_3);
                assertThat(e.getTo()).contains(VERTEX_COL_4);
            }
        }
        assertThat(graph.db().collection(VERTEX_COL_1).exists().get()).isTrue();

        // revert
        graph.edgeCollection(EDGE_COL_1).remove().get();
        graph.vertexCollection(VERTEX_COL_4).remove().get();
        graph.addEdgeDefinition(ed1).get();
    }

    @ParameterizedTest
    @MethodSource("asyncGraphs")
    @Disabled
        // FIXME: with dropCollections=true the vertex collections remain in the graph as orphan and not dropped
    void replaceEdgeDefinitionDropCollections(ArangoGraphAsync graph) throws ExecutionException, InterruptedException {
        final GraphEntity g = graph
                .replaceEdgeDefinition(new EdgeDefinition().collection(EDGE_COL_1).from(VERTEX_COL_3).to(VERTEX_COL_4),
                        new ReplaceEdgeDefinitionOptions().waitForSync(true).dropCollections(true)).get();
        final Collection<EdgeDefinition> edgeDefinitions = g.getEdgeDefinitions();
        assertThat(edgeDefinitions).hasSize(2);
        int count = 0;
        for (final EdgeDefinition e : edgeDefinitions) {
            if (e.getCollection().equals(EDGE_COL_1)) {
                count++;
            }
        }
        assertThat(count).isEqualTo(1);
        for (final EdgeDefinition e : edgeDefinitions) {
            if (e.getCollection().equals(EDGE_COL_1)) {
                assertThat(e.getFrom()).contains(VERTEX_COL_3);
                assertThat(e.getTo()).contains(VERTEX_COL_4);
            }
        }
        assertThat(graph.db().collection(VERTEX_COL_5).exists().get()).isFalse();

        // revert
        graph.edgeCollection(EDGE_COL_1).remove().get();
        graph.vertexCollection(VERTEX_COL_4).remove().get();
        graph.addEdgeDefinition(ed1).get();
    }

    @ParameterizedTest
    @MethodSource("asyncGraphs")
    void removeEdgeDefinition(ArangoGraphAsync graph) throws ExecutionException, InterruptedException {
        graph.edgeCollection(EDGE_COL_1).remove().get();
        Collection<String> edgeDefinitions = graph.getEdgeDefinitions().get();
        assertThat(edgeDefinitions).hasSize(1);
        assertThat(edgeDefinitions.iterator().next()).isEqualTo(EDGE_COL_2);
        assertThat(graph.db().collection(EDGE_COL_1).exists().get()).isTrue();

        //revert
        graph.addEdgeDefinition(ed1).get();
    }

    @ParameterizedTest
    @MethodSource("asyncGraphs")
    void removeEdgeDefinitionDropCollections(ArangoGraphAsync graph) throws ExecutionException, InterruptedException {
        graph.edgeCollection(EDGE_COL_1).remove(new EdgeCollectionRemoveOptions()
                .dropCollections(true)
                .waitForSync(true)).get();
        Collection<String> edgeDefinitions = graph.getEdgeDefinitions().get();
        assertThat(edgeDefinitions).hasSize(1);
        assertThat(edgeDefinitions.iterator().next()).isEqualTo(EDGE_COL_2);
        assertThat(graph.db().collection(EDGE_COL_1).exists().get()).isFalse();

        //revert
        graph.addEdgeDefinition(ed1).get();
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void smartGraph(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final Collection<EdgeDefinition> edgeDefinitions = new ArrayList<>();
        edgeDefinitions.add(new EdgeDefinition().collection("smartGraph-edge-" + rnd()).from("smartGraph-vertex-" + rnd()).to("smartGraph-vertex-" + rnd()));

        String graphId = GRAPH_NAME + rnd();
        final GraphEntity g = db.createGraph(graphId, edgeDefinitions,
                new GraphCreateOptions().isSmart(true).smartGraphAttribute("test").numberOfShards(2)).get();

        assertThat(g).isNotNull();
        assertThat(g.getIsSmart()).isTrue();
        assertThat(g.getSmartGraphAttribute()).isEqualTo("test");
        assertThat(g.getNumberOfShards()).isEqualTo(2);
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void hybridSmartGraph(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final Collection<EdgeDefinition> edgeDefinitions = new ArrayList<>();
        String eName = "hybridSmartGraph-edge-" + rnd();
        String v1Name = "hybridSmartGraph-vertex-" + rnd();
        String v2Name = "hybridSmartGraph-vertex-" + rnd();
        edgeDefinitions.add(new EdgeDefinition().collection(eName).from(v1Name).to(v2Name));

        String graphId = GRAPH_NAME + rnd();
        final GraphEntity g = db.createGraph(graphId, edgeDefinitions, new GraphCreateOptions()
                .satellites(eName, v1Name)
                .isSmart(true).smartGraphAttribute("test").replicationFactor(2).numberOfShards(2)).get();

        assertThat(g).isNotNull();
        assertThat(g.getIsSmart()).isTrue();
        assertThat(g.getSmartGraphAttribute()).isEqualTo("test");
        assertThat(g.getNumberOfShards()).isEqualTo(2);

        assertThat(db.collection(eName).getProperties().get().getReplicationFactor()).isEqualTo(ReplicationFactor.ofSatellite());
        assertThat(db.collection(v1Name).getProperties().get().getReplicationFactor()).isEqualTo(ReplicationFactor.ofSatellite());
        assertThat(db.collection(v2Name).getProperties().get().getReplicationFactor().get()).isEqualTo(2);
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void disjointSmartGraph(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final Collection<EdgeDefinition> edgeDefinitions = new ArrayList<>();
        edgeDefinitions.add(new EdgeDefinition().collection("smartGraph-edge-" + rnd()).from("smartGraph-vertex-" + rnd()).to("smartGraph-vertex-" + rnd()));

        String graphId = GRAPH_NAME + rnd();
        final GraphEntity g = db.createGraph(graphId, edgeDefinitions, new GraphCreateOptions()
                .isSmart(true).isDisjoint(true).smartGraphAttribute("test").numberOfShards(2)).get();

        assertThat(g).isNotNull();
        assertThat(g.getIsSmart()).isTrue();
        assertThat(g.getIsDisjoint()).isTrue();
        assertThat(g.getSmartGraphAttribute()).isEqualTo("test");
        assertThat(g.getNumberOfShards()).isEqualTo(2);
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void hybridDisjointSmartGraph(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final Collection<EdgeDefinition> edgeDefinitions = new ArrayList<>();
        String eName = "hybridDisjointSmartGraph-edge-" + rnd();
        String v1Name = "hybridDisjointSmartGraph-vertex-" + rnd();
        String v2Name = "hybridDisjointSmartGraph-vertex-" + rnd();
        edgeDefinitions.add(new EdgeDefinition().collection(eName).from(v1Name).to(v2Name));

        String graphId = GRAPH_NAME + rnd();
        final GraphEntity g = db.createGraph(graphId, edgeDefinitions, new GraphCreateOptions()
                .satellites(v1Name)
                .isSmart(true).isDisjoint(true).smartGraphAttribute("test").replicationFactor(2).numberOfShards(2)).get();

        assertThat(g).isNotNull();
        assertThat(g.getIsSmart()).isTrue();
        assertThat(g.getIsDisjoint()).isTrue();
        assertThat(g.getSmartGraphAttribute()).isEqualTo("test");
        assertThat(g.getNumberOfShards()).isEqualTo(2);

        assertThat(db.collection(v1Name).getProperties().get().getReplicationFactor()).isEqualTo(ReplicationFactor.ofSatellite());
        assertThat(db.collection(v2Name).getProperties().get().getReplicationFactor().get()).isEqualTo(2);
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void enterpriseGraph(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final Collection<EdgeDefinition> edgeDefinitions = new ArrayList<>();
        edgeDefinitions.add(new EdgeDefinition().collection("enterpriseGraph-edge-" + rnd()).from("enterpriseGraph-vertex-" + rnd()).to("enterpriseGraph-vertex-" + rnd()));

        String graphId = GRAPH_NAME + rnd();
        final GraphEntity g = db.createGraph(graphId, edgeDefinitions, new GraphCreateOptions().isSmart(true).numberOfShards(2)).get();

        assertThat(g).isNotNull();
        assertThat(g.getSmartGraphAttribute()).isNull();
        assertThat(g.getNumberOfShards()).isEqualTo(2);
        assertThat(g.getIsSmart()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void drop(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final String edgeCollection = "edge_" + rnd();
        final String vertexCollection = "vertex_" + rnd();
        final String graphId = GRAPH_NAME + rnd();
        final GraphEntity result = db.graph(graphId).create(Collections
                .singleton(new EdgeDefinition().collection(edgeCollection).from(vertexCollection).to(vertexCollection))).get();
        assertThat(result).isNotNull();
        db.graph(graphId).drop();
        assertThat(db.collection(edgeCollection).exists().get()).isTrue();
        assertThat(db.collection(vertexCollection).exists().get()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void dropPlusDropCollections(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final String edgeCollection = "edge_dropC" + rnd();
        final String vertexCollection = "vertex_dropC" + rnd();
        final String graphId = GRAPH_NAME + "_dropC" + rnd();
        final GraphEntity result = db.graph(graphId).create(Collections
                .singleton(new EdgeDefinition().collection(edgeCollection).from(vertexCollection).to(vertexCollection))).get();
        assertThat(result).isNotNull();
        db.graph(graphId).drop(true).get();
        assertThat(db.collection(edgeCollection).exists().get()).isFalse();
        assertThat(db.collection(vertexCollection).exists().get()).isFalse();
    }

}
