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
import com.arangodb.model.*;
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

    private final ArangoGraph graph;
    private final ArangoVertexCollection vertexCollection1;
    private final ArangoVertexCollection vertexCollection2;
    private final ArangoEdgeCollection edgeCollection;

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
        final VertexEntity v1 = vertexCollection1.insertVertex(new BaseDocument(), new VertexCreateOptions().streamTransactionId(streamTransactionId));
        final VertexEntity v2 = vertexCollection2.insertVertex(new BaseDocument(), new VertexCreateOptions().streamTransactionId(streamTransactionId));
        final BaseEdgeDocument value = new BaseEdgeDocument();
        value.setFrom(v1.getId());
        value.setTo(v2.getId());
        return value;
    }

    @Test
    public void getVertex() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db
                .beginStreamTransaction(new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION));

        // insert a vertex from outside the tx
        VertexEntity createdVertex = vertexCollection1.insertVertex(new BaseDocument());

        // assert that the vertex is not found from within the tx
        assertThat(vertexCollection1.getVertex(createdVertex.getKey(), BaseDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())), is(nullValue()));

        db.abortStreamTransaction(tx.getId());
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

    @Test
    public void replaceVertex() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        BaseDocument doc = new BaseDocument();
        doc.addAttribute("test", "foo");

        VertexEntity createdVertex = vertexCollection1.insertVertex(doc, null);

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
                .getProperties().get("test"), is("foo"));

        // assert that the vertex has been replaced from within the tx
        assertThat(vertexCollection1.getVertex(createdVertex.getKey(), BaseDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())).getProperties().get("test"), is("bar"));

        db.commitStreamTransaction(tx.getId());

        // assert that the vertex has been replaced after commit
        assertThat(vertexCollection1.getVertex(createdVertex.getKey(), BaseDocument.class, null)
                .getProperties().get("test"), is("bar"));
    }

    @Test
    public void updateVertex() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        BaseDocument doc = new BaseDocument();
        doc.addAttribute("test", "foo");

        VertexEntity createdDoc = vertexCollection1.insertVertex(doc, null);

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
                .getProperties().get("test"), is("foo"));

        // assert that the vertex has been updated from within the tx
        assertThat(vertexCollection1.getVertex(createdDoc.getKey(), BaseDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())).getProperties().get("test"), is("bar"));

        db.commitStreamTransaction(tx.getId());

        // assert that the vertex has been updated after commit
        assertThat(vertexCollection1.getVertex(createdDoc.getKey(), BaseDocument.class, null)
                .getProperties().get("test"), is("bar"));
    }

    @Test
    public void deleteVertex() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        VertexEntity createdDoc = vertexCollection1.insertVertex(new BaseDocument(), null);

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION));

        // delete vertex from within the tx
        vertexCollection1.deleteVertex(createdDoc.getKey(), new VertexDeleteOptions().streamTransactionId(tx.getId()));

        // assert that the vertex has not been deleted from outside the tx
        assertThat(vertexCollection1.getVertex(createdDoc.getKey(), BaseDocument.class, null), is(notNullValue()));

        // assert that the vertex has been deleted from within the tx
        assertThat(vertexCollection1.getVertex(createdDoc.getKey(), BaseDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())), is(nullValue()));

        db.commitStreamTransaction(tx.getId());

        // assert that the vertex has been deleted after commit
        assertThat(vertexCollection1.getVertex(createdDoc.getKey(), BaseDocument.class, null),
                is(nullValue()));
    }


    @Test
    public void getEdge() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db
                .beginStreamTransaction(new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION));

        // insert a edge from outside the tx
        EdgeEntity createdEdge = edgeCollection.insertEdge(createEdgeValue(null));

        // assert that the edge is not found from within the tx
        assertThat(edgeCollection.getEdge(createdEdge.getKey(), BaseEdgeDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())), is(nullValue()));

        db.abortStreamTransaction(tx.getId());
    }


    @Test
    public void createEdge() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION));

        // insert a edge from within the tx
        EdgeEntity createdEdge = edgeCollection.insertEdge(createEdgeValue(tx.getId()), new EdgeCreateOptions().streamTransactionId(tx.getId()));

        // assert that the edge is not found from outside the tx
        assertThat(edgeCollection.getEdge(createdEdge.getKey(), BaseEdgeDocument.class, null), is(nullValue()));

        // assert that the edge is found from within the tx
        assertThat(edgeCollection.getEdge(createdEdge.getKey(), BaseEdgeDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())), is(notNullValue()));

        db.commitStreamTransaction(tx.getId());

        // assert that the edge is found after commit
        assertThat(edgeCollection.getEdge(createdEdge.getKey(), BaseEdgeDocument.class, null), is(notNullValue()));
    }

    @Test
    public void replaceEdge() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        BaseEdgeDocument doc = createEdgeValue(null);
        doc.addAttribute("test", "foo");

        EdgeEntity createdEdge = edgeCollection.insertEdge(doc, null);

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
                .getProperties().get("test"), is("foo"));

        // assert that the edge has been replaced from within the tx
        assertThat(edgeCollection.getEdge(createdEdge.getKey(), BaseEdgeDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())).getProperties().get("test"), is("bar"));

        db.commitStreamTransaction(tx.getId());

        // assert that the edge has been replaced after commit
        assertThat(edgeCollection.getEdge(createdEdge.getKey(), BaseEdgeDocument.class, null)
                .getProperties().get("test"), is("bar"));
    }

    @Test
    public void updateEdge() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        BaseEdgeDocument doc = createEdgeValue(null);
        doc.addAttribute("test", "foo");

        EdgeEntity createdDoc = edgeCollection.insertEdge(doc, null);

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
                .getProperties().get("test"), is("foo"));

        // assert that the edge has been updated from within the tx
        assertThat(edgeCollection.getEdge(createdDoc.getKey(), BaseEdgeDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())).getProperties().get("test"), is("bar"));

        db.commitStreamTransaction(tx.getId());

        // assert that the edge has been updated after commit
        assertThat(edgeCollection.getEdge(createdDoc.getKey(), BaseEdgeDocument.class, null)
                .getProperties().get("test"), is("bar"));
    }

    @Test
    public void deleteEdge() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        EdgeEntity createdDoc = edgeCollection.insertEdge(createEdgeValue(null), null);

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION));

        // delete edge from within the tx
        edgeCollection.deleteEdge(createdDoc.getKey(), new EdgeDeleteOptions().streamTransactionId(tx.getId()));

        // assert that the edge has not been deleted from outside the tx
        assertThat(edgeCollection.getEdge(createdDoc.getKey(), BaseEdgeDocument.class, null), is(notNullValue()));

        // assert that the edge has been deleted from within the tx
        assertThat(edgeCollection.getEdge(createdDoc.getKey(), BaseEdgeDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())), is(nullValue()));

        db.commitStreamTransaction(tx.getId());

        // assert that the edge has been deleted after commit
        assertThat(edgeCollection.getEdge(createdDoc.getKey(), BaseEdgeDocument.class, null),
                is(nullValue()));
    }

}
