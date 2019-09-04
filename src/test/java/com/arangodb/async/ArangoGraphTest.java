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
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;

/**
 * @author Mark Vollmary
 */
public class ArangoGraphTest extends BaseTest {

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

    @BeforeClass
    public static void setup() throws InterruptedException, ExecutionException {
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

    @After
    public void teardown() throws InterruptedException, ExecutionException {
        for (final String collection : new String[]{EDGE_COL_1, EDGE_COL_2, VERTEX_COL_1, VERTEX_COL_2, VERTEX_COL_3,
                VERTEX_COL_4}) {
            final ArangoCollectionAsync c = db.collection(collection);
            if (c.exists().get()) {
                c.truncate().get();
            }
        }
    }

    @Test
    public void create() throws InterruptedException, ExecutionException {
        try {
            final GraphEntity result = db.graph(GRAPH_NAME + "_1").create(null).get();
            assertThat(result, is(notNullValue()));
            assertThat(result.getName(), is(GRAPH_NAME + "_1"));
        } finally {
            db.graph(GRAPH_NAME + "_1").drop().get();
        }
    }

    @Test
    public void createWithReplicationAndMinReplicationFactor() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isCluster());
        final Collection<EdgeDefinition> edgeDefinitions = new ArrayList<>();
        final GraphEntity graph = db.createGraph(GRAPH_NAME + "_1", edgeDefinitions, new GraphCreateOptions().isSmart(true).replicationFactor(2).minReplicationFactor(2)).get();
        assertThat(graph, is(notNullValue()));
        assertThat(graph.getName(), is(GRAPH_NAME + "_1"));
        assertThat(graph.getMinReplicationFactor(), is(2));
        assertThat(graph.getReplicationFactor(), is(2));
        db.graph(GRAPH_NAME + "_1").drop();
    }

    @Test
    public void getGraphs() throws InterruptedException, ExecutionException {
        final Collection<GraphEntity> graphs = db.getGraphs().get();
        assertThat(graphs, is(notNullValue()));
        assertThat(graphs.size(), greaterThanOrEqualTo(1));
    }

    @Test
    public void getInfo() throws InterruptedException, ExecutionException {
        final GraphEntity info = db.graph(GRAPH_NAME).getInfo().get();
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
            for (final String collection : new String[]{VERTEX_COL_1, VERTEX_COL_2}) {
                final CollectionPropertiesEntity properties = db.collection(collection).getProperties().get();
                assertThat(properties.getReplicationFactor(), is(REPLICATION_FACTOR));
                assertThat(properties.getNumberOfShards(), is(NUMBER_OF_SHARDS));
            }
            for (final String collection : new String[]{EDGE_COL_1, EDGE_COL_2}) {
                final CollectionPropertiesEntity properties = db.collection(collection).getProperties().get();
                assertThat(properties.getReplicationFactor(), is(REPLICATION_FACTOR));
            }
        }
    }

    @Test
    public void getVertexCollections() throws InterruptedException, ExecutionException {
        final Collection<String> vertexCollections = db.graph(GRAPH_NAME).getVertexCollections().get();
        assertThat(vertexCollections, is(notNullValue()));
        assertThat(vertexCollections.size(), is(3));
        assertThat(vertexCollections, hasItems(VERTEX_COL_1, VERTEX_COL_2, VERTEX_COL_3));
    }

    @Test
    public void addVertexCollection() throws InterruptedException, ExecutionException {
        final GraphEntity graph = db.graph(GRAPH_NAME).addVertexCollection(VERTEX_COL_4).get();
        assertThat(graph, is(notNullValue()));
        final Collection<String> vertexCollections = db.graph(GRAPH_NAME).getVertexCollections().get();
        assertThat(vertexCollections, hasItems(VERTEX_COL_1, VERTEX_COL_2, VERTEX_COL_3, VERTEX_COL_4));
        setup();
    }

    @Test
    public void getEdgeCollections() throws InterruptedException, ExecutionException {
        final Collection<String> edgeCollections = db.graph(GRAPH_NAME).getEdgeDefinitions().get();
        assertThat(edgeCollections, is(notNullValue()));
        assertThat(edgeCollections.size(), is(2));
        assertThat(edgeCollections, hasItems(EDGE_COL_1, EDGE_COL_2));
    }

    @Test
    public void addEdgeDefinition() throws InterruptedException, ExecutionException {
        final GraphEntity graph = db.graph(GRAPH_NAME)
                .addEdgeDefinition(new EdgeDefinition().collection(EDGE_COL_3).from(VERTEX_COL_1).to(VERTEX_COL_2))
                .get();
        assertThat(graph, is(notNullValue()));
        final Collection<EdgeDefinition> edgeDefinitions = graph.getEdgeDefinitions();
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
            final CollectionPropertiesEntity properties = db.collection(EDGE_COL_3).getProperties().get();
            assertThat(properties.getReplicationFactor(), is(REPLICATION_FACTOR));
            assertThat(properties.getNumberOfShards(), is(NUMBER_OF_SHARDS));
        }
        setup();
    }

    @Test
    public void replaceEdgeDefinition() throws InterruptedException, ExecutionException {
        final GraphEntity graph = db.graph(GRAPH_NAME)
                .replaceEdgeDefinition(new EdgeDefinition().collection(EDGE_COL_1).from(VERTEX_COL_3).to(VERTEX_COL_4))
                .get();
        final Collection<EdgeDefinition> edgeDefinitions = graph.getEdgeDefinitions();
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
        setup();
    }

    @Test
    public void removeEdgeDefinition() throws InterruptedException, ExecutionException {
        final GraphEntity graph = db.graph(GRAPH_NAME).removeEdgeDefinition(EDGE_COL_1).get();
        final Collection<EdgeDefinition> edgeDefinitions = graph.getEdgeDefinitions();
        assertThat(edgeDefinitions.size(), is(1));
        assertThat(edgeDefinitions.iterator().next().getCollection(), is(EDGE_COL_2));
        setup();
    }

    @Test
    public void smartGraph() throws InterruptedException, ExecutionException {
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
        assertThat(graph, is(notNullValue()));
        assertThat(graph.getIsSmart(), is(true));
        assertThat(graph.getSmartGraphAttribute(), is("test"));
        assertThat(graph.getNumberOfShards(), is(2));
        if (db.graph(GRAPH_NAME + "_smart").exists().get()) {
            db.graph(GRAPH_NAME + "_smart").drop().get();
        }
    }

    @Test
    public void drop() throws InterruptedException, ExecutionException {
        final String edgeCollection = "edge_drop";
        final String vertexCollection = "vertex_drop";
        final String graph = GRAPH_NAME + "_drop";
        final GraphEntity result = db.graph(graph).create(Collections
                .singleton(new EdgeDefinition().collection(edgeCollection).from(vertexCollection).to(vertexCollection)))
                .get();
        assertThat(result, is(notNullValue()));
        db.graph(graph).drop().get();
        assertThat(db.collection(edgeCollection).exists().get(), is(true));
        assertThat(db.collection(vertexCollection).exists().get(), is(true));
    }

    @Test
    public void dropPlusDropCollections() throws InterruptedException, ExecutionException {
        final String edgeCollection = "edge_dropC";
        final String vertexCollection = "vertex_dropC";
        final String graph = GRAPH_NAME + "_dropC";
        final GraphEntity result = db.graph(graph).create(Collections
                .singleton(new EdgeDefinition().collection(edgeCollection).from(vertexCollection).to(vertexCollection)))
                .get();
        assertThat(result, is(notNullValue()));
        db.graph(graph).drop(true).get();
        assertThat(db.collection(edgeCollection).exists().get(), is(false));
        assertThat(db.collection(vertexCollection).exists().get(), is(false));
    }
}
