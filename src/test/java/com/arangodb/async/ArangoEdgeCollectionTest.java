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

import com.arangodb.ArangoDBException;
import com.arangodb.entity.*;
import com.arangodb.model.*;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Mark Vollmary
 */
public class ArangoEdgeCollectionTest extends BaseTest {

    private static final String GRAPH_NAME = "db_collection_test";
    private static final String EDGE_COLLECTION_NAME = "db_edge_collection_test";
    private static final String VERTEX_COLLECTION_NAME = "db_vertex_collection_test";

    @BeforeClass
    public static void setup() throws InterruptedException, ExecutionException {
        if (!db.collection(VERTEX_COLLECTION_NAME).exists().get()) {
            db.createCollection(VERTEX_COLLECTION_NAME, null).get();
        }
        if (!db.collection(EDGE_COLLECTION_NAME).exists().get()) {
            db.createCollection(EDGE_COLLECTION_NAME, new CollectionCreateOptions().type(CollectionType.EDGES)).get();
        }
        final Collection<EdgeDefinition> edgeDefinitions = new ArrayList<>();
        edgeDefinitions.add(new EdgeDefinition().collection(EDGE_COLLECTION_NAME).from(VERTEX_COLLECTION_NAME)
                .to(VERTEX_COLLECTION_NAME));
        db.createGraph(GRAPH_NAME, edgeDefinitions, null).get();
    }

    @After
    public void teardown() throws InterruptedException, ExecutionException {
        for (final String collection : new String[]{VERTEX_COLLECTION_NAME, EDGE_COLLECTION_NAME}) {
            db.collection(collection).truncate().get();
        }
    }

    private BaseEdgeDocument createEdgeValue() throws InterruptedException, ExecutionException {
        final VertexEntity v1 = db.graph(GRAPH_NAME).vertexCollection(VERTEX_COLLECTION_NAME)
                .insertVertex(new BaseDocument(), null).get();
        final VertexEntity v2 = db.graph(GRAPH_NAME).vertexCollection(VERTEX_COLLECTION_NAME)
                .insertVertex(new BaseDocument(), null).get();

        final BaseEdgeDocument value = new BaseEdgeDocument();
        value.setFrom(v1.getId());
        value.setTo(v2.getId());
        return value;
    }

    @Test
    public void insertEdge() throws InterruptedException, ExecutionException {
        final BaseEdgeDocument value = createEdgeValue();
        final EdgeEntity edge = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(value, null).get();
        assertThat(edge, is(notNullValue()));
        final BaseEdgeDocument document = db.collection(EDGE_COLLECTION_NAME)
                .getDocument(edge.getKey(), BaseEdgeDocument.class, null).get();
        assertThat(document, is(notNullValue()));
        assertThat(document.getKey(), is(edge.getKey()));
    }

    @Test
    public void getEdge() throws InterruptedException, ExecutionException {
        final BaseEdgeDocument value = createEdgeValue();
        final EdgeEntity edge = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(value, null).get();
        final BaseDocument document = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                .getEdge(edge.getKey(), BaseDocument.class, null).get();
        assertThat(document, is(notNullValue()));
        assertThat(document.getKey(), is(edge.getKey()));
    }

    @Test
    public void getEdgeIfMatch() throws InterruptedException, ExecutionException {
        final BaseEdgeDocument value = createEdgeValue();
        final EdgeEntity edge = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(value, null).get();
        final GraphDocumentReadOptions options = new GraphDocumentReadOptions().ifMatch(edge.getRev());
        final BaseDocument document = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                .getEdge(edge.getKey(), BaseDocument.class, options).get();
        assertThat(document, is(notNullValue()));
        assertThat(document.getKey(), is(edge.getKey()));
    }

    @Test
    public void getEdgeIfMatchFail() throws InterruptedException, ExecutionException {
        final BaseEdgeDocument value = createEdgeValue();
        final EdgeEntity edge = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(value, null).get();
        final GraphDocumentReadOptions options = new GraphDocumentReadOptions().ifMatch("no").catchException(false);
        try {
            db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                    .getEdge(edge.getKey(), BaseEdgeDocument.class, options).get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause(), instanceOf(ArangoDBException.class));
        }
    }

    @Test
    public void getEdgeIfNoneMatch() throws InterruptedException, ExecutionException {
        final BaseEdgeDocument value = createEdgeValue();
        final EdgeEntity edge = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(value, null).get();
        final GraphDocumentReadOptions options = new GraphDocumentReadOptions().ifNoneMatch("no");
        final BaseDocument document = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                .getEdge(edge.getKey(), BaseDocument.class, options).get();
        assertThat(document, is(notNullValue()));
        assertThat(document.getKey(), is(edge.getKey()));
    }

    @Test
    public void getEdgeIfNoneMatchFail() throws InterruptedException, ExecutionException {
        final BaseEdgeDocument value = createEdgeValue();
        final EdgeEntity edge = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(value, null).get();
        final GraphDocumentReadOptions options = new GraphDocumentReadOptions().ifNoneMatch(edge.getRev()).catchException(false);
        try {
            db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                    .getEdge(edge.getKey(), BaseEdgeDocument.class, options).get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause(), instanceOf(ArangoDBException.class));
        }
    }

    @Test
    public void replaceEdge() throws InterruptedException, ExecutionException {
        final BaseEdgeDocument doc = createEdgeValue();
        doc.addAttribute("a", "test");
        final EdgeEntity createResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(doc, null)
                .get();
        doc.getProperties().clear();
        doc.addAttribute("b", "test");
        final EdgeUpdateEntity replaceResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                .replaceEdge(createResult.getKey(), doc, null).get();
        assertThat(replaceResult, is(notNullValue()));
        assertThat(replaceResult.getId(), is(createResult.getId()));
        assertThat(replaceResult.getRev(), is(not(replaceResult.getOldRev())));
        assertThat(replaceResult.getOldRev(), is(createResult.getRev()));

        final BaseEdgeDocument readResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                .getEdge(createResult.getKey(), BaseEdgeDocument.class, null).get();
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getRevision(), is(replaceResult.getRev()));
        assertThat(readResult.getProperties().keySet(), not(hasItem("a")));
        assertThat(readResult.getAttribute("b"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("b")), is("test"));
    }

    @Test
    public void replaceEdgeIfMatch() throws InterruptedException, ExecutionException {
        final BaseEdgeDocument doc = createEdgeValue();
        doc.addAttribute("a", "test");
        final EdgeEntity createResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(doc, null)
                .get();
        doc.getProperties().clear();
        doc.addAttribute("b", "test");
        final EdgeReplaceOptions options = new EdgeReplaceOptions().ifMatch(createResult.getRev());
        final EdgeUpdateEntity replaceResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                .replaceEdge(createResult.getKey(), doc, options).get();
        assertThat(replaceResult, is(notNullValue()));
        assertThat(replaceResult.getId(), is(createResult.getId()));
        assertThat(replaceResult.getRev(), is(not(replaceResult.getOldRev())));
        assertThat(replaceResult.getOldRev(), is(createResult.getRev()));

        final BaseEdgeDocument readResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                .getEdge(createResult.getKey(), BaseEdgeDocument.class, null).get();
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getRevision(), is(replaceResult.getRev()));
        assertThat(readResult.getProperties().keySet(), not(hasItem("a")));
        assertThat(readResult.getAttribute("b"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("b")), is("test"));
    }

    @Test
    public void replaceEdgeIfMatchFail() throws InterruptedException, ExecutionException {
        final BaseEdgeDocument doc = createEdgeValue();
        doc.addAttribute("a", "test");
        final EdgeEntity createResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(doc, null)
                .get();
        doc.getProperties().clear();
        doc.addAttribute("b", "test");
        try {
            final EdgeReplaceOptions options = new EdgeReplaceOptions().ifMatch("no");
            db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).replaceEdge(createResult.getKey(), doc, options)
                    .get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause(), instanceOf(ArangoDBException.class));
        }
    }

    @Test
    public void updateEdge() throws InterruptedException, ExecutionException {
        final BaseEdgeDocument doc = createEdgeValue();
        doc.addAttribute("a", "test");
        doc.addAttribute("c", "test");
        final EdgeEntity createResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(doc, null)
                .get();
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        doc.updateAttribute("c", null);
        final EdgeUpdateEntity updateResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                .updateEdge(createResult.getKey(), doc, null).get();
        assertThat(updateResult, is(notNullValue()));
        assertThat(updateResult.getId(), is(createResult.getId()));
        assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
        assertThat(updateResult.getOldRev(), is(createResult.getRev()));

        final BaseEdgeDocument readResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                .getEdge(createResult.getKey(), BaseEdgeDocument.class, null).get();
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getAttribute("a"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("a")), is("test1"));
        assertThat(readResult.getAttribute("b"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("b")), is("test"));
        assertThat(readResult.getRevision(), is(updateResult.getRev()));
        assertThat(readResult.getProperties().keySet(), hasItem("c"));
    }

    @Test
    public void updateEdgeIfMatch() throws InterruptedException, ExecutionException {
        final BaseEdgeDocument doc = createEdgeValue();
        doc.addAttribute("a", "test");
        doc.addAttribute("c", "test");
        final EdgeEntity createResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(doc, null)
                .get();
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        doc.updateAttribute("c", null);
        final EdgeUpdateOptions options = new EdgeUpdateOptions().ifMatch(createResult.getRev());
        final EdgeUpdateEntity updateResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                .updateEdge(createResult.getKey(), doc, options).get();
        assertThat(updateResult, is(notNullValue()));
        assertThat(updateResult.getId(), is(createResult.getId()));
        assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
        assertThat(updateResult.getOldRev(), is(createResult.getRev()));

        final BaseEdgeDocument readResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                .getEdge(createResult.getKey(), BaseEdgeDocument.class, null).get();
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getAttribute("a"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("a")), is("test1"));
        assertThat(readResult.getAttribute("b"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("b")), is("test"));
        assertThat(readResult.getRevision(), is(updateResult.getRev()));
        assertThat(readResult.getProperties().keySet(), hasItem("c"));
    }

    @Test
    public void updateEdgeIfMatchFail() throws InterruptedException, ExecutionException {
        final BaseEdgeDocument doc = createEdgeValue();
        doc.addAttribute("a", "test");
        doc.addAttribute("c", "test");
        final EdgeEntity createResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(doc, null)
                .get();
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        doc.updateAttribute("c", null);
        try {
            final EdgeUpdateOptions options = new EdgeUpdateOptions().ifMatch("no");
            db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).updateEdge(createResult.getKey(), doc, options)
                    .get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause(), instanceOf(ArangoDBException.class));
        }
    }

    @Test
    public void updateEdgeKeepNullTrue() throws InterruptedException, ExecutionException {
        final BaseEdgeDocument doc = createEdgeValue();
        doc.addAttribute("a", "test");
        final EdgeEntity createResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(doc, null)
                .get();
        doc.updateAttribute("a", null);
        final EdgeUpdateOptions options = new EdgeUpdateOptions().keepNull(true);
        final EdgeUpdateEntity updateResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                .updateEdge(createResult.getKey(), doc, options).get();
        assertThat(updateResult, is(notNullValue()));
        assertThat(updateResult.getId(), is(createResult.getId()));
        assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
        assertThat(updateResult.getOldRev(), is(createResult.getRev()));

        final BaseEdgeDocument readResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                .getEdge(createResult.getKey(), BaseEdgeDocument.class, null).get();
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getProperties().keySet().size(), is(1));
        assertThat(readResult.getProperties().keySet(), hasItem("a"));
    }

    @Test
    public void updateEdgeKeepNullFalse() throws InterruptedException, ExecutionException {
        final BaseEdgeDocument doc = createEdgeValue();
        doc.addAttribute("a", "test");
        final EdgeEntity createResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(doc, null)
                .get();
        doc.updateAttribute("a", null);
        final EdgeUpdateOptions options = new EdgeUpdateOptions().keepNull(false);
        final EdgeUpdateEntity updateResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                .updateEdge(createResult.getKey(), doc, options).get();
        assertThat(updateResult, is(notNullValue()));
        assertThat(updateResult.getId(), is(createResult.getId()));
        assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
        assertThat(updateResult.getOldRev(), is(createResult.getRev()));

        final BaseEdgeDocument readResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                .getEdge(createResult.getKey(), BaseEdgeDocument.class, null).get();
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getId(), is(createResult.getId()));
        assertThat(readResult.getRevision(), is(notNullValue()));
        assertThat(readResult.getProperties().keySet(), not(hasItem("a")));
    }

    @Test
    public void deleteEdge() throws InterruptedException, ExecutionException {
        final BaseEdgeDocument doc = createEdgeValue();
        final EdgeEntity createResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(doc, null)
                .get();
        db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).deleteEdge(createResult.getKey(), null).get();
        try {
            db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                    .getEdge(createResult.getKey(), BaseEdgeDocument.class, new GraphDocumentReadOptions().catchException(false)).get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause(), instanceOf(ArangoDBException.class));
        }
    }

    @Test
    public void deleteEdgeIfMatch() throws InterruptedException, ExecutionException {
        final BaseEdgeDocument doc = createEdgeValue();
        final EdgeEntity createResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(doc, null)
                .get();
        final EdgeDeleteOptions options = new EdgeDeleteOptions().ifMatch(createResult.getRev());
        db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).deleteEdge(createResult.getKey(), options).get();
        try {
            db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                    .getEdge(createResult.getKey(), BaseEdgeDocument.class, new GraphDocumentReadOptions().catchException(false)).get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause(), instanceOf(ArangoDBException.class));
        }
    }

    @Test
    public void deleteEdgeIfMatchFail() throws InterruptedException, ExecutionException {
        final BaseEdgeDocument doc = createEdgeValue();
        final EdgeEntity createResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(doc, null)
                .get();
        final EdgeDeleteOptions options = new EdgeDeleteOptions().ifMatch("no");
        try {
            db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).deleteEdge(createResult.getKey(), options).get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause(), instanceOf(ArangoDBException.class));
        }
    }
}
