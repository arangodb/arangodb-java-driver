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


import com.arangodb.entity.*;
import com.arangodb.model.*;
import org.junit.After;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;

/**
 * @author Michele Rastelli
 */
public class StreamTransactionGraphTest extends BaseTest {

    private static final String GRAPH_NAME = "graph_stream_transaction_graph_test";
    private static final String EDGE_COLLECTION = "edge_collection_stream_transaction_graph_test";
    private static final String VERTEX_COLLECTION_1 = "vertex_collection_1_stream_transaction_graph_test";
    private static final String VERTEX_COLLECTION_2 = "vertex_collection_2_stream_transaction_graph_test";

    private final ArangoGraphAsync graph;
    private ArangoVertexCollectionAsync vertexCollection1;
    private ArangoVertexCollectionAsync vertexCollection2;
    private ArangoEdgeCollectionAsync edgeCollection;

    public StreamTransactionGraphTest() throws ExecutionException, InterruptedException {

        graph = db.graph(GRAPH_NAME);

        if (graph.exists().get())
            graph.drop().get();

        graph.create(Collections.singletonList(new EdgeDefinition().collection(EDGE_COLLECTION).from(VERTEX_COLLECTION_1).to(VERTEX_COLLECTION_2))).get();

        vertexCollection1 = graph.vertexCollection(VERTEX_COLLECTION_1);
        vertexCollection2 = graph.vertexCollection(VERTEX_COLLECTION_2);
        edgeCollection = graph.edgeCollection(EDGE_COLLECTION);
    }

    @After
    public void teardown() throws ExecutionException, InterruptedException {
        if (graph.exists().get())
            graph.drop().get();
        if (db.collection(EDGE_COLLECTION).exists().get())
            db.collection(EDGE_COLLECTION).drop().get();
        if (db.collection(VERTEX_COLLECTION_1).exists().get())
            db.collection(VERTEX_COLLECTION_1).drop().get();
        if (db.collection(VERTEX_COLLECTION_2).exists().get())
            db.collection(VERTEX_COLLECTION_2).drop().get();
    }

    private BaseEdgeDocument createEdgeValue(String streamTransactionId) throws ExecutionException, InterruptedException {
        final VertexEntity v1 = vertexCollection1.insertVertex(new BaseDocument(), new VertexCreateOptions().streamTransactionId(streamTransactionId)).get();
        final VertexEntity v2 = vertexCollection2.insertVertex(new BaseDocument(), new VertexCreateOptions().streamTransactionId(streamTransactionId)).get();
        final BaseEdgeDocument value = new BaseEdgeDocument();
        value.setFrom(v1.getId());
        value.setTo(v2.getId());
        return value;
    }

    @Test
    public void getVertex() throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db
                .beginStreamTransaction(new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)).get();

        // insert a vertex from outside the tx
        VertexEntity createdVertex = vertexCollection1.insertVertex(new BaseDocument()).get();

        // assert that the vertex is not found from within the tx
        assertThat(vertexCollection1.getVertex(createdVertex.getKey(), BaseDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())).get(), is(nullValue()));

        db.abortStreamTransaction(tx.getId()).get();
    }


    @Test
    public void createVertex() throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)).get();

        // insert a vertex from within the tx
        VertexEntity createdVertex = vertexCollection1.insertVertex(new BaseDocument(), new VertexCreateOptions().streamTransactionId(tx.getId())).get();

        // assert that the vertex is not found from outside the tx
        assertThat(vertexCollection1.getVertex(createdVertex.getKey(), BaseDocument.class, null).get(), is(nullValue()));

        // assert that the vertex is found from within the tx
        assertThat(vertexCollection1.getVertex(createdVertex.getKey(), BaseDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())).get(), is(notNullValue()));

        db.commitStreamTransaction(tx.getId()).get();

        // assert that the vertex is found after commit
        assertThat(vertexCollection1.getVertex(createdVertex.getKey(), BaseDocument.class, null).get(), is(notNullValue()));
    }

    @Test
    public void replaceVertex() throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        BaseDocument doc = new BaseDocument();
        doc.addAttribute("test", "foo");

        VertexEntity createdVertex = vertexCollection1.insertVertex(doc, null).get();

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)).get();

        // replace vertex from within the tx
        doc.getProperties().clear();
        doc.addAttribute("test", "bar");
        vertexCollection1.replaceVertex(createdVertex.getKey(), doc,
                new VertexReplaceOptions().streamTransactionId(tx.getId()));

        // assert that the vertex has not been replaced from outside the tx
        assertThat(vertexCollection1.getVertex(createdVertex.getKey(), BaseDocument.class, null).get()
                .getProperties().get("test"), is("foo"));

        // assert that the vertex has been replaced from within the tx
        assertThat(vertexCollection1.getVertex(createdVertex.getKey(), BaseDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())).get().getProperties().get("test"), is("bar"));

        db.commitStreamTransaction(tx.getId()).get();

        // assert that the vertex has been replaced after commit
        assertThat(vertexCollection1.getVertex(createdVertex.getKey(), BaseDocument.class, null).get()
                .getProperties().get("test"), is("bar"));
    }

    @Test
    public void updateVertex() throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        BaseDocument doc = new BaseDocument();
        doc.addAttribute("test", "foo");

        VertexEntity createdDoc = vertexCollection1.insertVertex(doc, null).get();

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)).get();

        // update vertex from within the tx
        doc.getProperties().clear();
        doc.addAttribute("test", "bar");
        vertexCollection1.updateVertex(createdDoc.getKey(), doc, new VertexUpdateOptions().streamTransactionId(tx.getId()));

        // assert that the vertex has not been updated from outside the tx
        assertThat(vertexCollection1.getVertex(createdDoc.getKey(), BaseDocument.class, null).get()
                .getProperties().get("test"), is("foo"));

        // assert that the vertex has been updated from within the tx
        assertThat(vertexCollection1.getVertex(createdDoc.getKey(), BaseDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())).get().getProperties().get("test"), is("bar"));

        db.commitStreamTransaction(tx.getId()).get();

        // assert that the vertex has been updated after commit
        assertThat(vertexCollection1.getVertex(createdDoc.getKey(), BaseDocument.class, null).get()
                .getProperties().get("test"), is("bar"));
    }

    @Test
    public void deleteVertex() throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        VertexEntity createdDoc = vertexCollection1.insertVertex(new BaseDocument(), null).get();

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)).get();

        // delete vertex from within the tx
        vertexCollection1.deleteVertex(createdDoc.getKey(), new VertexDeleteOptions().streamTransactionId(tx.getId()));

        // assert that the vertex has not been deleted from outside the tx
        assertThat(vertexCollection1.getVertex(createdDoc.getKey(), BaseDocument.class, null).get(), is(notNullValue()));

        // assert that the vertex has been deleted from within the tx
        assertThat(vertexCollection1.getVertex(createdDoc.getKey(), BaseDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())).get(), is(nullValue()));

        db.commitStreamTransaction(tx.getId()).get();

        // assert that the vertex has been deleted after commit
        assertThat(vertexCollection1.getVertex(createdDoc.getKey(), BaseDocument.class, null).get(),
                is(nullValue()));
    }


    @Test
    public void getEdge() throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db
                .beginStreamTransaction(new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)).get();

        // insert a edge from outside the tx
        EdgeEntity createdEdge = edgeCollection.insertEdge(createEdgeValue(null)).get();

        // assert that the edge is not found from within the tx
        assertThat(edgeCollection.getEdge(createdEdge.getKey(), BaseEdgeDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())).get(), is(nullValue()));

        db.abortStreamTransaction(tx.getId()).get();
    }


    @Test
    public void createEdge() throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)).get();

        // insert a edge from within the tx
        EdgeEntity createdEdge = edgeCollection.insertEdge(createEdgeValue(tx.getId()), new EdgeCreateOptions().streamTransactionId(tx.getId())).get();

        // assert that the edge is not found from outside the tx
        assertThat(edgeCollection.getEdge(createdEdge.getKey(), BaseEdgeDocument.class, null).get(), is(nullValue()));

        // assert that the edge is found from within the tx
        assertThat(edgeCollection.getEdge(createdEdge.getKey(), BaseEdgeDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())).get(), is(notNullValue()));

        db.commitStreamTransaction(tx.getId()).get();

        // assert that the edge is found after commit
        assertThat(edgeCollection.getEdge(createdEdge.getKey(), BaseEdgeDocument.class, null).get(), is(notNullValue()));
    }

    @Test
    public void replaceEdge() throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        BaseEdgeDocument doc = createEdgeValue(null);
        doc.addAttribute("test", "foo");

        EdgeEntity createdEdge = edgeCollection.insertEdge(doc, null).get();

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)).get();

        // replace edge from within the tx
        doc.getProperties().clear();
        doc.addAttribute("test", "bar");
        edgeCollection.replaceEdge(createdEdge.getKey(), doc,
                new EdgeReplaceOptions().streamTransactionId(tx.getId()));

        // assert that the edge has not been replaced from outside the tx
        assertThat(edgeCollection.getEdge(createdEdge.getKey(), BaseEdgeDocument.class, null).get()
                .getProperties().get("test"), is("foo"));

        // assert that the edge has been replaced from within the tx
        assertThat(edgeCollection.getEdge(createdEdge.getKey(), BaseEdgeDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())).get().getProperties().get("test"), is("bar"));

        db.commitStreamTransaction(tx.getId()).get();

        // assert that the edge has been replaced after commit
        assertThat(edgeCollection.getEdge(createdEdge.getKey(), BaseEdgeDocument.class, null).get()
                .getProperties().get("test"), is("bar"));
    }

    @Test
    public void updateEdge() throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        BaseEdgeDocument doc = createEdgeValue(null);
        doc.addAttribute("test", "foo");

        EdgeEntity createdDoc = edgeCollection.insertEdge(doc, null).get();

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)).get();

        // update edge from within the tx
        doc.getProperties().clear();
        doc.addAttribute("test", "bar");
        edgeCollection.updateEdge(createdDoc.getKey(), doc, new EdgeUpdateOptions().streamTransactionId(tx.getId()));

        // assert that the edge has not been updated from outside the tx
        assertThat(edgeCollection.getEdge(createdDoc.getKey(), BaseEdgeDocument.class, null).get()
                .getProperties().get("test"), is("foo"));

        // assert that the edge has been updated from within the tx
        assertThat(edgeCollection.getEdge(createdDoc.getKey(), BaseEdgeDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())).get().getProperties().get("test"), is("bar"));

        db.commitStreamTransaction(tx.getId()).get();

        // assert that the edge has been updated after commit
        assertThat(edgeCollection.getEdge(createdDoc.getKey(), BaseEdgeDocument.class, null).get()
                .getProperties().get("test"), is("bar"));
    }

    @Test
    public void deleteEdge() throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        EdgeEntity createdDoc = edgeCollection.insertEdge(createEdgeValue(null), null).get();

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions()
                        .readCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)
                        .writeCollections(VERTEX_COLLECTION_1, VERTEX_COLLECTION_2, EDGE_COLLECTION)).get();

        // delete edge from within the tx
        edgeCollection.deleteEdge(createdDoc.getKey(), new EdgeDeleteOptions().streamTransactionId(tx.getId()));

        // assert that the edge has not been deleted from outside the tx
        assertThat(edgeCollection.getEdge(createdDoc.getKey(), BaseEdgeDocument.class, null).get(), is(notNullValue()));

        // assert that the edge has been deleted from within the tx
        assertThat(edgeCollection.getEdge(createdDoc.getKey(), BaseEdgeDocument.class,
                new GraphDocumentReadOptions().streamTransactionId(tx.getId())).get(), is(nullValue()));

        db.commitStreamTransaction(tx.getId()).get();

        // assert that the edge has been deleted after commit
        assertThat(edgeCollection.getEdge(createdDoc.getKey(), BaseEdgeDocument.class, null).get(),
                is(nullValue()));
    }

}
