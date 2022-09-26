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
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * @author Michele Rastelli
 */
class StreamTransactionGraphTest extends BaseJunit5 {

    private static final String GRAPH_NAME = "graph_stream_transaction_graph_test";
    private static final String EDGE_COLLECTION = "edge_collection_stream_transaction_graph_test";
    private static final String VERTEX_COLLECTION_1 = "vertex_collection_1_stream_transaction_graph_test";
    private static final String VERTEX_COLLECTION_2 = "vertex_collection_2_stream_transaction_graph_test";

    private static Stream<Arguments> vertices() {
        return dbsStream()
                .map(db -> db.graph(GRAPH_NAME).vertexCollection(VERTEX_COLLECTION_1))
                .map(Arguments::of);
    }

    private static Stream<Arguments> edges() {
        return dbsStream()
                .map(db -> db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION))
                .map(Arguments::of);
    }

    @BeforeAll
    static void init() {
        initDB();
        initGraph(GRAPH_NAME, Collections.singletonList(new EdgeDefinition()
                .collection(EDGE_COLLECTION).from(VERTEX_COLLECTION_1).to(VERTEX_COLLECTION_2)
        ), null);
    }

    private BaseEdgeDocument createEdgeValue(String streamTransactionId, ArangoGraph graph) {
        ArangoVertexCollection vertexCollection1 = graph.vertexCollection(VERTEX_COLLECTION_1);
        ArangoVertexCollection vertexCollection2 = graph.vertexCollection(VERTEX_COLLECTION_2);
        VertexEntity v1 = vertexCollection1.insertVertex(new BaseDocument(), new VertexCreateOptions().streamTransactionId(streamTransactionId));
        VertexEntity v2 = vertexCollection2.insertVertex(new BaseDocument(), new VertexCreateOptions().streamTransactionId(streamTransactionId));
        BaseEdgeDocument value = new BaseEdgeDocument();
        value.setFrom(v1.getId());
        value.setTo(v2.getId());
        return value;
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("vertices")
    void getVertex(ArangoVertexCollection vertexCollection1) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        ArangoDatabase db = vertexCollection1.graph().db();
        StreamTransactionEntity tx = db
                .beginStreamTransaction(new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION));

        // insert a vertex from outside the tx
        VertexEntity createdVertex = vertexCollection1.insertVertex(new BaseDocument());

        // assert that the vertex is not found from within the tx
        assertThat(vertexCollection1.getVertex(createdVertex.getKey(), BaseDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId()))).isNull();

        db.abortStreamTransaction(tx.getId());
    }


    @ParameterizedTest(name = "{index}")
    @MethodSource("vertices")
    void createVertex(ArangoVertexCollection vertexCollection1) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        ArangoDatabase db = vertexCollection1.graph().db();
        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION));

        // insert a vertex from within the tx
        VertexEntity createdVertex = vertexCollection1.insertVertex(new BaseDocument(), new VertexCreateOptions().streamTransactionId(tx.getId()));

        // assert that the vertex is not found from outside the tx
        assertThat(vertexCollection1.getVertex(createdVertex.getKey(), BaseDocument.class, null)).isNull();

        // assert that the vertex is found from within the tx
        assertThat(vertexCollection1.getVertex(createdVertex.getKey(), BaseDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId()))).isNotNull();

        db.commitStreamTransaction(tx.getId());

        // assert that the vertex is found after commit
        assertThat(vertexCollection1.getVertex(createdVertex.getKey(), BaseDocument.class, null)).isNotNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("vertices")
    void replaceVertex(ArangoVertexCollection vertexCollection1) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        BaseDocument doc = new BaseDocument();
        doc.addAttribute("test", "foo");

        VertexEntity createdVertex = vertexCollection1.insertVertex(doc, null);

        ArangoDatabase db = vertexCollection1.graph().db();
        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION));

        // replace vertex from within the tx
        doc.getProperties().clear();
        doc.addAttribute("test", "bar");
        vertexCollection1.replaceVertex(createdVertex.getKey(), doc,
                new VertexReplaceOptions().streamTransactionId(tx.getId()));

        // assert that the vertex has not been replaced from outside the tx
        assertThat(vertexCollection1.getVertex(createdVertex.getKey(), BaseDocument.class, null)
                .getProperties()).containsEntry("test", "foo");

        // assert that the vertex has been replaced from within the tx
        assertThat(vertexCollection1.getVertex(createdVertex.getKey(), BaseDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())).getProperties()).containsEntry("test", "bar");

        db.commitStreamTransaction(tx.getId());

        // assert that the vertex has been replaced after commit
        assertThat(vertexCollection1.getVertex(createdVertex.getKey(), BaseDocument.class, null)
                .getProperties()).containsEntry("test", "bar");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("vertices")
    void updateVertex(ArangoVertexCollection vertexCollection1) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        BaseDocument doc = new BaseDocument();
        doc.addAttribute("test", "foo");

        VertexEntity createdDoc = vertexCollection1.insertVertex(doc, null);

        ArangoDatabase db = vertexCollection1.graph().db();
        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION));

        // update vertex from within the tx
        doc.getProperties().clear();
        doc.addAttribute("test", "bar");
        vertexCollection1.updateVertex(createdDoc.getKey(), doc, new VertexUpdateOptions().streamTransactionId(tx.getId()));

        // assert that the vertex has not been updated from outside the tx
        assertThat(vertexCollection1.getVertex(createdDoc.getKey(), BaseDocument.class, null)
                .getProperties()).containsEntry("test", "foo");

        // assert that the vertex has been updated from within the tx
        assertThat(vertexCollection1.getVertex(createdDoc.getKey(), BaseDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())).getProperties()).containsEntry("test", "bar");

        db.commitStreamTransaction(tx.getId());

        // assert that the vertex has been updated after commit
        assertThat(vertexCollection1.getVertex(createdDoc.getKey(), BaseDocument.class, null)
                .getProperties()).containsEntry("test", "bar");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("vertices")
    void deleteVertex(ArangoVertexCollection vertexCollection1) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        VertexEntity createdDoc = vertexCollection1.insertVertex(new BaseDocument(), null);

        ArangoDatabase db = vertexCollection1.graph().db();
        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION));

        // delete vertex from within the tx
        vertexCollection1.deleteVertex(createdDoc.getKey(), new VertexDeleteOptions().streamTransactionId(tx.getId()));

        // assert that the vertex has not been deleted from outside the tx
        assertThat(vertexCollection1.getVertex(createdDoc.getKey(), BaseDocument.class, null)).isNotNull();

        // assert that the vertex has been deleted from within the tx
        assertThat(vertexCollection1.getVertex(createdDoc.getKey(), BaseDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId()))).isNull();

        db.commitStreamTransaction(tx.getId());

        // assert that the vertex has been deleted after commit
        assertThat(vertexCollection1.getVertex(createdDoc.getKey(), BaseDocument.class, null)).isNull();
    }


    @ParameterizedTest(name = "{index}")
    @MethodSource("edges")
    void getEdge(ArangoEdgeCollection edgeCollection) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        ArangoDatabase db = edgeCollection.graph().db();
        StreamTransactionEntity tx = db
                .beginStreamTransaction(new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION));

        // insert an edge from outside the tx
        EdgeEntity createdEdge = edgeCollection.insertEdge(createEdgeValue(null, edgeCollection.graph()));

        // assert that the edge is not found from within the tx
        assertThat(edgeCollection.getEdge(createdEdge.getKey(), BaseEdgeDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId()))).isNull();

        db.abortStreamTransaction(tx.getId());
    }


    @ParameterizedTest(name = "{index}")
    @MethodSource("edges")
    void createEdge(ArangoEdgeCollection edgeCollection) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        ArangoDatabase db = edgeCollection.graph().db();
        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION));

        // insert an edge from within the tx
        EdgeEntity createdEdge = edgeCollection.insertEdge(createEdgeValue(tx.getId(), edgeCollection.graph()), new EdgeCreateOptions().streamTransactionId(tx.getId()));

        // assert that the edge is not found from outside the tx
        assertThat(edgeCollection.getEdge(createdEdge.getKey(), BaseEdgeDocument.class, null)).isNull();

        // assert that the edge is found from within the tx
        assertThat(edgeCollection.getEdge(createdEdge.getKey(), BaseEdgeDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId()))).isNotNull();

        db.commitStreamTransaction(tx.getId());

        // assert that the edge is found after commit
        assertThat(edgeCollection.getEdge(createdEdge.getKey(), BaseEdgeDocument.class, null)).isNotNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("edges")
    void replaceEdge(ArangoEdgeCollection edgeCollection) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        BaseEdgeDocument doc = createEdgeValue(null, edgeCollection.graph());
        doc.addAttribute("test", "foo");

        EdgeEntity createdEdge = edgeCollection.insertEdge(doc, null);

        ArangoDatabase db = edgeCollection.graph().db();
        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION));

        // replace edge from within the tx
        doc.getProperties().clear();
        doc.addAttribute("test", "bar");
        edgeCollection.replaceEdge(createdEdge.getKey(), doc,
                new EdgeReplaceOptions().streamTransactionId(tx.getId()));

        // assert that the edge has not been replaced from outside the tx
        assertThat(edgeCollection.getEdge(createdEdge.getKey(), BaseEdgeDocument.class, null)
                .getProperties()).containsEntry("test", "foo");

        // assert that the edge has been replaced from within the tx
        assertThat(edgeCollection.getEdge(createdEdge.getKey(), BaseEdgeDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())).getProperties()).containsEntry("test", "bar");

        db.commitStreamTransaction(tx.getId());

        // assert that the edge has been replaced after commit
        assertThat(edgeCollection.getEdge(createdEdge.getKey(), BaseEdgeDocument.class, null)
                .getProperties()).containsEntry("test", "bar");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("edges")
    void updateEdge(ArangoEdgeCollection edgeCollection) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        BaseEdgeDocument doc = createEdgeValue(null, edgeCollection.graph());
        doc.addAttribute("test", "foo");

        EdgeEntity createdDoc = edgeCollection.insertEdge(doc, null);

        ArangoDatabase db = edgeCollection.graph().db();
        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION));

        // update edge from within the tx
        doc.getProperties().clear();
        doc.addAttribute("test", "bar");
        edgeCollection.updateEdge(createdDoc.getKey(), doc, new EdgeUpdateOptions().streamTransactionId(tx.getId()));

        // assert that the edge has not been updated from outside the tx
        assertThat(edgeCollection.getEdge(createdDoc.getKey(), BaseEdgeDocument.class, null)
                .getProperties()).containsEntry("test", "foo");

        // assert that the edge has been updated from within the tx
        assertThat(edgeCollection.getEdge(createdDoc.getKey(), BaseEdgeDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())).getProperties()).containsEntry("test", "bar");

        db.commitStreamTransaction(tx.getId());

        // assert that the edge has been updated after commit
        assertThat(edgeCollection.getEdge(createdDoc.getKey(), BaseEdgeDocument.class, null)
                .getProperties()).containsEntry("test", "bar");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("edges")
    void deleteEdge(ArangoEdgeCollection edgeCollection) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        EdgeEntity createdDoc = edgeCollection.insertEdge(createEdgeValue(null, edgeCollection.graph()), null);

        ArangoDatabase db = edgeCollection.graph().db();
        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION));

        // delete edge from within the tx
        edgeCollection.deleteEdge(createdDoc.getKey(), new EdgeDeleteOptions().streamTransactionId(tx.getId()));

        // assert that the edge has not been deleted from outside the tx
        assertThat(edgeCollection.getEdge(createdDoc.getKey(), BaseEdgeDocument.class, null)).isNotNull();

        // assert that the edge has been deleted from within the tx
        assertThat(edgeCollection.getEdge(createdDoc.getKey(), BaseEdgeDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId()))).isNull();

        db.commitStreamTransaction(tx.getId());

        // assert that the edge has been deleted after commit
        assertThat(edgeCollection.getEdge(createdDoc.getKey(), BaseEdgeDocument.class, null)).isNull();
    }

}
