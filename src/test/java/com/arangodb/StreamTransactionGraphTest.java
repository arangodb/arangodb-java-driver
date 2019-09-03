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

import com.arangodb.ArangoDB.Builder;
import com.arangodb.entity.*;
import com.arangodb.model.GraphDocumentReadOptions;
import com.arangodb.model.StreamTransactionOptions;
import com.arangodb.model.VertexCreateOptions;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;

/**
 * @author Michele Rastelli
 */
@RunWith(Parameterized.class)
public class StreamTransactionGraphTest extends BaseTest {

    private static final String GRAPH_NAME = "graph_stream_transaction_graph_test";
    private static final String EDGE_COLLECTION = "edge_collection_stream_transaction_graph_test";
    private static final String VERTEX_COLLECTION_1 = "vertex_collection_1_stream_transaction_graph_test";
    private static final String VERTEX_COLLECTION_2 = "vertex_collection_2_stream_transaction_graph_test";

    private ArangoGraph graph;
    private ArangoVertexCollection vertexCollection1;
    private ArangoVertexCollection vertexCollection2;
    private ArangoEdgeCollection edgeCollection;

    public StreamTransactionGraphTest(final Builder builder) {
        super(builder);

        graph = db.graph(GRAPH_NAME);

        if (graph.exists())
            graph.drop();

        graph.create(Collections.singletonList(new EdgeDefinition().collection(EDGE_COLLECTION).from(VERTEX_COLLECTION_1).to(VERTEX_COLLECTION_2)));

        vertexCollection1 = graph.vertexCollection(VERTEX_COLLECTION_1);
        vertexCollection2 = graph.vertexCollection(VERTEX_COLLECTION_2);
        edgeCollection = graph.edgeCollection(EDGE_COLLECTION);
    }

    @After
    public void teardown() {
        if (graph.exists())
            graph.drop();
        if (db.collection(EDGE_COLLECTION).exists())
            db.collection(EDGE_COLLECTION).drop();
        if (db.collection(VERTEX_COLLECTION_1).exists())
            db.collection(VERTEX_COLLECTION_1).drop();
        if (db.collection(VERTEX_COLLECTION_2).exists())
            db.collection(VERTEX_COLLECTION_2).drop();
    }

    private BaseEdgeDocument createEdgeValue(String streamTransactionId) {
        final VertexEntity v1 = vertexCollection1.insertVertex(new BaseDocument(), null);
        final VertexEntity v2 = vertexCollection2.insertVertex(new BaseDocument(), null);
        final BaseEdgeDocument value = new BaseEdgeDocument();
        value.setFrom(v1.getId());
        value.setTo(v2.getId());
        return value;
    }

    @Test
    public void createVertex() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION));

        // insert a vertex from within the tx
        VertexEntity createdVertex = vertexCollection1.insertVertex(new BaseDocument(), new VertexCreateOptions().streamTransactionId(tx.getId()));

        // assert that the vertex is not found from outside the tx
        assertThat(vertexCollection1.getVertex(createdVertex.getKey(), BaseDocument.class, null), is(nullValue()));

        // assert that the vertex is found from within the tx
        assertThat(vertexCollection1.getVertex(createdVertex.getKey(), BaseDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())), is(notNullValue()));

        db.commitStreamTransaction(tx.getId());

        // assert that the vertex is found after commit
        assertThat(vertexCollection1.getVertex(createdVertex.getKey(), BaseDocument.class, null), is(notNullValue()));
    }

}
