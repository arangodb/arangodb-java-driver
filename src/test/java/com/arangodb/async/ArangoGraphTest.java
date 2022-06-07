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

package com.arangodb.async;

import com.arangodb.entity.CollectionPropertiesEntity;
import com.arangodb.entity.EdgeDefinition;
import com.arangodb.entity.GraphEntity;
import com.arangodb.entity.ServerRole;
import com.arangodb.model.GraphCreateOptions;
import com.arangodb.model.VertexCollectionCreateOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author Mark Vollmary
 */
class ArangoGraphTest extends BaseTest {

    private static final String GRAPH_NAME = "db_collection_test";
    private static final String EDGE_COL_1 = "db_edge1_collection_test";
    private static final String EDGE_COL_2 = "db_edge2_collection_test";
    private static final String EDGE_COL_3 = "db_edge3_collection_test";
    private static final String VERTEX_COL_1 = "db_vertex1_collection_test";
    private static final String VERTEX_COL_2 = "db_vertex2_collection_test";
    private static final String VERTEX_COL_3 = "db_vertex3_collection_test";
    private static final String VERTEX_COL_4 = "db_vertex4_collection_test";
    private static final Integer REPLICATION_FACTOR = 2;
    private static final Integer NUMBER_OF_SHARDS = 2;

    @BeforeAll
    static void setup() throws InterruptedException, ExecutionException {
        if (db.graph(GRAPH_NAME).exists().get()) {
            db.graph(GRAPH_NAME).drop().get();
        }
        final Collection<EdgeDefinition> edgeDefinitions = new ArrayList<>();
        edgeDefinitions.add(new EdgeDefinition().collection(EDGE_COL_1).from(VERTEX_COL_1).to(VERTEX_COL_2));
        edgeDefinitions
                .add(new EdgeDefinition().collection(EDGE_COL_2).from(VERTEX_COL_2).to(VERTEX_COL_1, VERTEX_COL_3));
        final GraphCreateOptions options = new GraphCreateOptions();
        if (arangoDB.getRole().get() != ServerRole.SINGLE) {
            options.replicationFactor(REPLICATION_FACTOR).numberOfShards(NUMBER_OF_SHARDS);
        }
        db.createGraph(GRAPH_NAME, edgeDefinitions, options).get();
    }

    @AfterEach
    void teardown() throws InterruptedException, ExecutionException {
        for (final String collection : new String[]{EDGE_COL_1, EDGE_COL_2, VERTEX_COL_1, VERTEX_COL_2, VERTEX_COL_3,
                VERTEX_COL_4}) {
            final ArangoCollectionAsync c = db.collection(collection);
            if (c.exists().get()) {
                c.truncate().get();
            }
        }
    }

    @Test
    void create() throws InterruptedException, ExecutionException {
        try {
            final GraphEntity result = db.graph(GRAPH_NAME + "_1").create(null).get();
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(GRAPH_NAME + "_1");
        } finally {
            db.graph(GRAPH_NAME + "_1").drop().get();
        }
    }

    @Test
    void createWithReplicationAndWriteConcern() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isCluster());
        final Collection<EdgeDefinition> edgeDefinitions = new ArrayList<>();
        final GraphEntity graph = db.createGraph(GRAPH_NAME + "_1", edgeDefinitions, new GraphCreateOptions().isSmart(true).replicationFactor(2).writeConcern(2)).get();
        assertThat(graph).isNotNull();
        assertThat(graph.getName()).isEqualTo(GRAPH_NAME + "_1");
        assertThat(graph.getWriteConcern()).isEqualTo(2);
        assertThat(graph.getReplicationFactor()).isEqualTo(2);
        db.graph(GRAPH_NAME + "_1").drop();
    }

    @Test
    void getGraphs() throws InterruptedException, ExecutionException {
        final Collection<GraphEntity> graphs = db.getGraphs().get();
        assertThat(graphs).isNotNull();
        assertThat(graphs).isNotEmpty();
    }

    @Test
    void getInfo() throws InterruptedException, ExecutionException {
        final GraphEntity info = db.graph(GRAPH_NAME).getInfo().get();
        assertThat(info).isNotNull();
        assertThat(info.getName()).isEqualTo(GRAPH_NAME);
        assertThat(info.getEdgeDefinitions()).hasSize(2);
        final Iterator<EdgeDefinition> iterator = info.getEdgeDefinitions().iterator();
        final EdgeDefinition e1 = iterator.next();
        assertThat(e1.getCollection()).isEqualTo(EDGE_COL_1);
        assertThat(e1.getFrom()).contains(VERTEX_COL_1);
        assertThat(e1.getTo()).contains(VERTEX_COL_2);
        final EdgeDefinition e2 = iterator.next();
        assertThat(e2.getCollection()).isEqualTo(EDGE_COL_2);
        assertThat(e2.getFrom()).contains(VERTEX_COL_2);
        assertThat(e2.getTo()).contains(VERTEX_COL_1, VERTEX_COL_3);
        assertThat(info.getOrphanCollections()).isEmpty();

        if (isCluster()) {
            for (final String collection : new String[]{VERTEX_COL_1, VERTEX_COL_2}) {
                final CollectionPropertiesEntity properties = db.collection(collection).getProperties().get();
                assertThat(properties.getReplicationFactor()).isEqualTo(REPLICATION_FACTOR);
                assertThat(properties.getNumberOfShards()).isEqualTo(NUMBER_OF_SHARDS);
            }
            for (final String collection : new String[]{EDGE_COL_1, EDGE_COL_2}) {
                final CollectionPropertiesEntity properties = db.collection(collection).getProperties().get();
                assertThat(properties.getReplicationFactor()).isEqualTo(REPLICATION_FACTOR);
            }
        }
    }

    @Test
    void getVertexCollections() throws InterruptedException, ExecutionException {
        final Collection<String> vertexCollections = db.graph(GRAPH_NAME).getVertexCollections().get();
        assertThat(vertexCollections).isNotNull();
        assertThat(vertexCollections).hasSize(3);
        assertThat(vertexCollections).contains(VERTEX_COL_1, VERTEX_COL_2, VERTEX_COL_3);
    }

    @Test
    void addVertexCollection() throws InterruptedException, ExecutionException {
        final GraphEntity graph = db.graph(GRAPH_NAME).addVertexCollection(VERTEX_COL_4).get();
        assertThat(graph).isNotNull();
        final Collection<String> vertexCollections = db.graph(GRAPH_NAME).getVertexCollections().get();
        assertThat(vertexCollections).contains(VERTEX_COL_1, VERTEX_COL_2, VERTEX_COL_3, VERTEX_COL_4);
        setup();
    }

    @Test
    void addSatelliteVertexCollection() throws ExecutionException, InterruptedException {
        assumeTrue(isCluster());
        assumeTrue(isEnterprise());
        assumeTrue(isAtLeastVersion(3, 9));

        String v1Name = "vertex-" + rnd();

        ArangoGraphAsync g = db.graph(GRAPH_NAME + rnd());
        g.createGraph(Collections.emptyList(), new GraphCreateOptions().isSmart(true).smartGraphAttribute("test")).get();
        g.addVertexCollection(v1Name, new VertexCollectionCreateOptions().satellites(v1Name)).get();

        Collection<String> vertexCollections = g.getVertexCollections().get();
        assertThat(vertexCollections).contains(v1Name);
        assertThat(db.collection(v1Name).getProperties().get().getSatellite()).isTrue();

        // revert
        g.drop().get();
    }

    @Test
    void getEdgeCollections() throws InterruptedException, ExecutionException {
        final Collection<String> edgeCollections = db.graph(GRAPH_NAME).getEdgeDefinitions().get();
        assertThat(edgeCollections).isNotNull();
        assertThat(edgeCollections).hasSize(2);
        assertThat(edgeCollections).contains(EDGE_COL_1, EDGE_COL_2);
    }

    @Test
    void addEdgeDefinition() throws InterruptedException, ExecutionException {
        final GraphEntity graph = db.graph(GRAPH_NAME)
                .addEdgeDefinition(new EdgeDefinition().collection(EDGE_COL_3).from(VERTEX_COL_1).to(VERTEX_COL_2))
                .get();
        assertThat(graph).isNotNull();
        final Collection<EdgeDefinition> edgeDefinitions = graph.getEdgeDefinitions();
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
            final CollectionPropertiesEntity properties = db.collection(EDGE_COL_3).getProperties().get();
            assertThat(properties.getReplicationFactor()).isEqualTo(REPLICATION_FACTOR);
            assertThat(properties.getNumberOfShards()).isEqualTo(NUMBER_OF_SHARDS);
        }
        setup();
    }

    @Test
    void addSatelliteEdgeDefinition() throws ExecutionException, InterruptedException {
        assumeTrue(isCluster());
        assumeTrue(isEnterprise());
        assumeTrue(isAtLeastVersion(3, 9));

        String eName = "edge-" + rnd();
        String v1Name = "vertex-" + rnd();
        String v2Name = "vertex-" + rnd();
        EdgeDefinition ed = new EdgeDefinition().collection(eName).from(v1Name).to(v2Name).satellites(v1Name);

        ArangoGraphAsync g = db.graph(GRAPH_NAME + rnd());
        g.createGraph(Collections.emptyList(), new GraphCreateOptions().isSmart(true).smartGraphAttribute("test")).get();
        g.addEdgeDefinition(ed).get();
        final GraphEntity ge = g.getInfo().get();
        assertThat(ge).isNotNull();
        final Collection<EdgeDefinition> edgeDefinitions = ge.getEdgeDefinitions();
        assertThat(edgeDefinitions).hasSize(1);
        EdgeDefinition e = edgeDefinitions.iterator().next();
        assertThat(e.getCollection()).isEqualTo(eName);
        assertThat(e.getFrom()).contains(v1Name);
        assertThat(e.getTo()).contains(v2Name);

        assertThat(db.collection(v1Name).getProperties().get().getSatellite()).isTrue();

        // revert
        g.drop().get();
    }

    @Test
    void replaceEdgeDefinition() throws InterruptedException, ExecutionException {
        final GraphEntity graph = db.graph(GRAPH_NAME)
                .replaceEdgeDefinition(new EdgeDefinition().collection(EDGE_COL_1).from(VERTEX_COL_3).to(VERTEX_COL_4))
                .get();
        final Collection<EdgeDefinition> edgeDefinitions = graph.getEdgeDefinitions();
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
        setup();
    }

    @Test
    void removeEdgeDefinition() throws InterruptedException, ExecutionException {
        final GraphEntity graph = db.graph(GRAPH_NAME).removeEdgeDefinition(EDGE_COL_1).get();
        final Collection<EdgeDefinition> edgeDefinitions = graph.getEdgeDefinitions();
        assertThat(edgeDefinitions).hasSize(1);
        assertThat(edgeDefinitions.iterator().next().getCollection()).isEqualTo(EDGE_COL_2);
        setup();
    }

    @Test
    void smartGraph() throws InterruptedException, ExecutionException {
        assumeTrue(isCluster());
        assumeTrue(isEnterprise());
        for (final String collection : new String[]{EDGE_COL_1, EDGE_COL_2, VERTEX_COL_1, VERTEX_COL_2,
                VERTEX_COL_3, VERTEX_COL_4}) {
            if (db.collection(collection).exists().get()) {
                db.collection(collection).drop().get();
            }
        }
        final Collection<EdgeDefinition> edgeDefinitions = new ArrayList<>();
        edgeDefinitions.add(new EdgeDefinition().collection(EDGE_COL_1).from(VERTEX_COL_1).to(VERTEX_COL_2));
        edgeDefinitions
                .add(new EdgeDefinition().collection(EDGE_COL_2).from(VERTEX_COL_2).to(VERTEX_COL_1, VERTEX_COL_3));
        final GraphEntity graph = db.createGraph(GRAPH_NAME + "_smart", edgeDefinitions,
                        new GraphCreateOptions().isSmart(true).smartGraphAttribute("test").replicationFactor(REPLICATION_FACTOR)
                                .numberOfShards(NUMBER_OF_SHARDS))
                .get();
        assertThat(graph).isNotNull();
        assertThat(graph.getIsSmart()).isTrue();
        assertThat(graph.getSmartGraphAttribute()).isEqualTo("test");
        assertThat(graph.getNumberOfShards()).isEqualTo(2);
        if (db.graph(GRAPH_NAME + "_smart").exists().get()) {
            db.graph(GRAPH_NAME + "_smart").drop().get();
        }
    }

    @Test
    void hybridSmartGraph() throws ExecutionException, InterruptedException {
        assumeTrue(isEnterprise());
        assumeTrue(isCluster());
        assumeTrue((isAtLeastVersion(3, 9)));

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

        assertThat(db.collection(eName).getProperties().get().getSatellite()).isTrue();
        assertThat(db.collection(v1Name).getProperties().get().getSatellite()).isTrue();
        assertThat(db.collection(v2Name).getProperties().get().getReplicationFactor()).isEqualTo(2);
    }

    @Test
    void hybridDisjointSmartGraph() throws ExecutionException, InterruptedException {
        assumeTrue(isEnterprise());
        assumeTrue(isCluster());
        assumeTrue((isAtLeastVersion(3, 9)));

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

        assertThat(db.collection(v1Name).getProperties().get().getSatellite()).isTrue();
        assertThat(db.collection(v2Name).getProperties().get().getReplicationFactor()).isEqualTo(2);
    }

    @Test
    void drop() throws InterruptedException, ExecutionException {
        final String edgeCollection = "edge_drop";
        final String vertexCollection = "vertex_drop";
        final String graph = GRAPH_NAME + "_drop";
        final GraphEntity result = db.graph(graph).create(Collections
                        .singleton(new EdgeDefinition().collection(edgeCollection).from(vertexCollection).to(vertexCollection)))
                .get();
        assertThat(result).isNotNull();
        db.graph(graph).drop().get();
        assertThat(db.collection(edgeCollection).exists().get()).isTrue();
        assertThat(db.collection(vertexCollection).exists().get()).isTrue();
    }

    @Test
    void dropPlusDropCollections() throws InterruptedException, ExecutionException {
        final String edgeCollection = "edge_dropC";
        final String vertexCollection = "vertex_dropC";
        final String graph = GRAPH_NAME + "_dropC";
        final GraphEntity result = db.graph(graph).create(Collections
                        .singleton(new EdgeDefinition().collection(edgeCollection).from(vertexCollection).to(vertexCollection)))
                .get();
        assertThat(result).isNotNull();
        db.graph(graph).drop(true).get();
        assertThat(db.collection(edgeCollection).exists().get()).isFalse();
        assertThat(db.collection(vertexCollection).exists().get()).isFalse();
    }
}
