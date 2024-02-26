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

import com.arangodb.entity.*;
import com.arangodb.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * @author Michele Rastelli
 */
class StreamTransactionGraphAsyncTest extends BaseJunit5 {

    private static final String GRAPH_NAME = "graph_stream_transaction_graph_test";
    private static final String EDGE_COLLECTION = "edge_collection_stream_transaction_graph_test";
    private static final String VERTEX_COLLECTION_1 = "vertex_collection_1_stream_transaction_graph_test";
    private static final String VERTEX_COLLECTION_2 = "vertex_collection_2_stream_transaction_graph_test";

    private static Stream<Arguments> asyncVertices() {
        return asyncDbsStream()
                .map(mapNamedPayload(db -> db.graph(GRAPH_NAME).vertexCollection(VERTEX_COLLECTION_1)))
                .map(Arguments::of);
    }

    private static Stream<Arguments> asyncEdges() {
        return asyncDbsStream()
                .map(mapNamedPayload(db -> db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION)))
                .map(Arguments::of);
    }

    @BeforeAll
    static void init() {
        initDB();
        initGraph(GRAPH_NAME, Collections.singletonList(new EdgeDefinition()
                .collection(EDGE_COLLECTION).from(VERTEX_COLLECTION_1).to(VERTEX_COLLECTION_2)
        ), null);
    }

    private BaseEdgeDocument createEdgeValue(String streamTransactionId, ArangoGraphAsync graph) throws ExecutionException, InterruptedException {
        ArangoVertexCollectionAsync vertexCollection1 = graph.vertexCollection(VERTEX_COLLECTION_1);
        ArangoVertexCollectionAsync vertexCollection2 = graph.vertexCollection(VERTEX_COLLECTION_2);
        VertexEntity v1 = vertexCollection1.insertVertex(new BaseDocument(),
                new VertexCreateOptions().streamTransactionId(streamTransactionId)).get();
        VertexEntity v2 = vertexCollection2.insertVertex(new BaseDocument(),
                new VertexCreateOptions().streamTransactionId(streamTransactionId)).get();
        BaseEdgeDocument value = new BaseEdgeDocument();
        value.setFrom(v1.getId());
        value.setTo(v2.getId());
        return value;
    }

    @ParameterizedTest
    @MethodSource("asyncVertices")
    void getVertex(ArangoVertexCollectionAsync vertexCollection1) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        ArangoDatabaseAsync db = vertexCollection1.graph().db();
        StreamTransactionEntity tx = db
                .beginStreamTransaction(new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)).get();

        // insert a vertex from outside the tx
        VertexEntity createdVertex = vertexCollection1.insertVertex(new BaseDocument()).get();

        // assert that the vertex is not found from within the tx
        assertThat(vertexCollection1.getVertex(createdVertex.getKey(), BaseDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())).get()).isNull();

        db.abortStreamTransaction(tx.getId()).get();
    }


    @ParameterizedTest
    @MethodSource("asyncVertices")
    void createVertex(ArangoVertexCollectionAsync vertexCollection1) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        ArangoDatabaseAsync db = vertexCollection1.graph().db();
        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)).get();

        // insert a vertex from within the tx
        VertexEntity createdVertex = vertexCollection1.insertVertex(new BaseDocument(),
                new VertexCreateOptions().streamTransactionId(tx.getId())).get();

        // assert that the vertex is not found from outside the tx
        assertThat(vertexCollection1.getVertex(createdVertex.getKey(), BaseDocument.class, null).get()).isNull();

        // assert that the vertex is found from within the tx
        assertThat(vertexCollection1.getVertex(createdVertex.getKey(), BaseDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())).get()).isNotNull();

        db.commitStreamTransaction(tx.getId()).get();

        // assert that the vertex is found after commit
        assertThat(vertexCollection1.getVertex(createdVertex.getKey(), BaseDocument.class, null).get()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("asyncVertices")
    void replaceVertex(ArangoVertexCollectionAsync vertexCollection1) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("test", "foo");

        VertexEntity createdVertex = vertexCollection1.insertVertex(doc, null).get();

        ArangoDatabaseAsync db = vertexCollection1.graph().db();
        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)).get();

        // replace vertex from within the tx
        doc.updateAttribute("test", "bar");
        vertexCollection1.replaceVertex(createdVertex.getKey(), doc,
                new VertexReplaceOptions().streamTransactionId(tx.getId())).get();

        // assert that the vertex has not been replaced from outside the tx
        assertThat(vertexCollection1.getVertex(createdVertex.getKey(), BaseDocument.class, null).get()
                .getProperties()).containsEntry("test", "foo");

        // assert that the vertex has been replaced from within the tx
        assertThat(vertexCollection1.getVertex(createdVertex.getKey(), BaseDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())).get().getProperties()).containsEntry("test"
                , "bar");

        db.commitStreamTransaction(tx.getId()).get();

        // assert that the vertex has been replaced after commit
        assertThat(vertexCollection1.getVertex(createdVertex.getKey(), BaseDocument.class, null).get()
                .getProperties()).containsEntry("test", "bar");
    }

    @ParameterizedTest
    @MethodSource("asyncVertices")
    void updateVertex(ArangoVertexCollectionAsync vertexCollection1) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("test", "foo");

        VertexEntity createdDoc = vertexCollection1.insertVertex(doc, null).get();

        ArangoDatabaseAsync db = vertexCollection1.graph().db();
        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)).get();

        // update vertex from within the tx
        doc.updateAttribute("test", "bar");
        vertexCollection1.updateVertex(createdDoc.getKey(), doc,
                new VertexUpdateOptions().streamTransactionId(tx.getId())).get();

        // assert that the vertex has not been updated from outside the tx
        assertThat(vertexCollection1.getVertex(createdDoc.getKey(), BaseDocument.class, null).get()
                .getProperties()).containsEntry("test", "foo");

        // assert that the vertex has been updated from within the tx
        assertThat(vertexCollection1.getVertex(createdDoc.getKey(), BaseDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())).get().getProperties()).containsEntry("test"
                , "bar");

        db.commitStreamTransaction(tx.getId()).get();

        // assert that the vertex has been updated after commit
        assertThat(vertexCollection1.getVertex(createdDoc.getKey(), BaseDocument.class, null).get()
                .getProperties()).containsEntry("test", "bar");
    }

    @ParameterizedTest
    @MethodSource("asyncVertices")
    void deleteVertex(ArangoVertexCollectionAsync vertexCollection1) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        VertexEntity createdDoc = vertexCollection1.insertVertex(new BaseDocument(), null).get();

        ArangoDatabaseAsync db = vertexCollection1.graph().db();
        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)).get();

        // delete vertex from within the tx
        vertexCollection1.deleteVertex(createdDoc.getKey(), new VertexDeleteOptions().streamTransactionId(tx.getId())).get();

        // assert that the vertex has not been deleted from outside the tx
        assertThat(vertexCollection1.getVertex(createdDoc.getKey(), BaseDocument.class, null).get()).isNotNull();

        // assert that the vertex has been deleted from within the tx
        assertThat(vertexCollection1.getVertex(createdDoc.getKey(), BaseDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())).get()).isNull();

        db.commitStreamTransaction(tx.getId()).get();

        // assert that the vertex has been deleted after commit
        assertThat(vertexCollection1.getVertex(createdDoc.getKey(), BaseDocument.class, null).get()).isNull();
    }


    @ParameterizedTest
    @MethodSource("asyncEdges")
    void getEdge(ArangoEdgeCollectionAsync edgeCollection) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        ArangoDatabaseAsync db = edgeCollection.graph().db();
        StreamTransactionEntity tx = db
                .beginStreamTransaction(new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)).get();

        // insert an edge from outside the tx
        EdgeEntity createdEdge = edgeCollection.insertEdge(createEdgeValue(null, edgeCollection.graph())).get();

        // assert that the edge is not found from within the tx
        assertThat(edgeCollection.getEdge(createdEdge.getKey(), BaseEdgeDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())).get()).isNull();

        db.abortStreamTransaction(tx.getId()).get();
    }


    @ParameterizedTest
    @MethodSource("asyncEdges")
    void createEdge(ArangoEdgeCollectionAsync edgeCollection) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        ArangoDatabaseAsync db = edgeCollection.graph().db();
        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)).get();

        // insert an edge from within the tx
        EdgeEntity createdEdge = edgeCollection.insertEdge(createEdgeValue(tx.getId(), edgeCollection.graph()),
                new EdgeCreateOptions().streamTransactionId(tx.getId())).get();

        // assert that the edge is not found from outside the tx
        assertThat(edgeCollection.getEdge(createdEdge.getKey(), BaseEdgeDocument.class, null).get()).isNull();

        // assert that the edge is found from within the tx
        assertThat(edgeCollection.getEdge(createdEdge.getKey(), BaseEdgeDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())).get()).isNotNull();

        db.commitStreamTransaction(tx.getId()).get();

        // assert that the edge is found after commit
        assertThat(edgeCollection.getEdge(createdEdge.getKey(), BaseEdgeDocument.class, null).get()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("asyncEdges")
    void replaceEdge(ArangoEdgeCollectionAsync edgeCollection) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        BaseEdgeDocument doc = createEdgeValue(null, edgeCollection.graph());
        doc.addAttribute("test", "foo");

        EdgeEntity createdEdge = edgeCollection.insertEdge(doc, null).get();

        ArangoDatabaseAsync db = edgeCollection.graph().db();
        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)).get();

        // replace edge from within the tx
        doc.updateAttribute("test", "bar");
        edgeCollection.replaceEdge(createdEdge.getKey(), doc,
                new EdgeReplaceOptions().streamTransactionId(tx.getId())).get();

        // assert that the edge has not been replaced from outside the tx
        assertThat(edgeCollection.getEdge(createdEdge.getKey(), BaseEdgeDocument.class, null).get()
                .getProperties()).containsEntry("test", "foo");

        // assert that the edge has been replaced from within the tx
        assertThat(edgeCollection.getEdge(createdEdge.getKey(), BaseEdgeDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())).get().getProperties()).containsEntry("test"
                , "bar");

        db.commitStreamTransaction(tx.getId()).get();

        // assert that the edge has been replaced after commit
        assertThat(edgeCollection.getEdge(createdEdge.getKey(), BaseEdgeDocument.class, null).get()
                .getProperties()).containsEntry("test", "bar");
    }

    @ParameterizedTest
    @MethodSource("asyncEdges")
    void updateEdge(ArangoEdgeCollectionAsync edgeCollection) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        BaseEdgeDocument doc = createEdgeValue(null, edgeCollection.graph());
        doc.addAttribute("test", "foo");

        EdgeEntity createdDoc = edgeCollection.insertEdge(doc, null).get();

        ArangoDatabaseAsync db = edgeCollection.graph().db();
        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)).get();

        // update edge from within the tx
        doc.updateAttribute("test", "bar");
        edgeCollection.updateEdge(createdDoc.getKey(), doc, new EdgeUpdateOptions().streamTransactionId(tx.getId())).get();

        // assert that the edge has not been updated from outside the tx
        assertThat(edgeCollection.getEdge(createdDoc.getKey(), BaseEdgeDocument.class, null).get()
                .getProperties()).containsEntry("test", "foo");

        // assert that the edge has been updated from within the tx
        assertThat(edgeCollection.getEdge(createdDoc.getKey(), BaseEdgeDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())).get().getProperties()).containsEntry("test"
                , "bar");

        db.commitStreamTransaction(tx.getId()).get();

        // assert that the edge has been updated after commit
        assertThat(edgeCollection.getEdge(createdDoc.getKey(), BaseEdgeDocument.class, null).get()
                .getProperties()).containsEntry("test", "bar");
    }

    @ParameterizedTest
    @MethodSource("asyncEdges")
    void deleteEdge(ArangoEdgeCollectionAsync edgeCollection) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        EdgeEntity createdDoc = edgeCollection.insertEdge(createEdgeValue(null, edgeCollection.graph()), null).get();

        ArangoDatabaseAsync db = edgeCollection.graph().db();
        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)).get();

        // delete edge from within the tx
        edgeCollection.deleteEdge(createdDoc.getKey(), new EdgeDeleteOptions().streamTransactionId(tx.getId())).get();

        // assert that the edge has not been deleted from outside the tx
        assertThat(edgeCollection.getEdge(createdDoc.getKey(), BaseEdgeDocument.class, null).get()).isNotNull();

        // assert that the edge has been deleted from within the tx
        assertThat(edgeCollection.getEdge(createdDoc.getKey(), BaseEdgeDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())).get()).isNull();

        db.commitStreamTransaction(tx.getId()).get();

        // assert that the edge has been deleted after commit
        assertThat(edgeCollection.getEdge(createdDoc.getKey(), BaseEdgeDocument.class, null).get()).isNull();
    }

}
