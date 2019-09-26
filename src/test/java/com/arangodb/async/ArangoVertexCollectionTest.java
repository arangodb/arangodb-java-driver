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
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.VertexEntity;
import com.arangodb.entity.VertexUpdateEntity;
import com.arangodb.model.*;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Mark Vollmary
 */
public class ArangoVertexCollectionTest extends BaseTest {

    private static final String GRAPH_NAME = "db_collection_test";
    private static final String COLLECTION_NAME = "db_vertex_collection_test";

    @BeforeClass
    public static void setup() throws InterruptedException, ExecutionException {
        if (!db.collection(COLLECTION_NAME).exists().get()) {
            db.createCollection(COLLECTION_NAME, null).get();
        }
        final GraphCreateOptions options = new GraphCreateOptions().orphanCollections(COLLECTION_NAME);
        db.createGraph(GRAPH_NAME, null, options).get();
    }

    @After
    public void teardown() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME).truncate().get();
    }

    @Test
    public void dropVertexCollection() throws InterruptedException, ExecutionException {
        db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME).drop().get();
        final Collection<String> vertexCollections = db.graph(GRAPH_NAME).getVertexCollections().get();
        assertThat(vertexCollections, not(hasItem(COLLECTION_NAME)));
    }

    @Test
    public void insertVertex() throws InterruptedException, ExecutionException {
        final VertexEntity vertex = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .insertVertex(new BaseDocument(), null).get();
        assertThat(vertex, is(notNullValue()));
        final BaseDocument document = db.collection(COLLECTION_NAME)
                .getDocument(vertex.getKey(), BaseDocument.class, null).get();
        assertThat(document, is(notNullValue()));
        assertThat(document.getKey(), is(vertex.getKey()));
    }

    @Test
    public void getVertex() throws InterruptedException, ExecutionException {
        final VertexEntity vertex = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .insertVertex(new BaseDocument(), null).get();
        final BaseDocument document = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .getVertex(vertex.getKey(), BaseDocument.class, null).get();
        assertThat(document, is(notNullValue()));
        assertThat(document.getKey(), is(vertex.getKey()));
    }

    @Test
    public void getVertexIfMatch() throws InterruptedException, ExecutionException {
        final VertexEntity vertex = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .insertVertex(new BaseDocument(), null).get();
        final GraphDocumentReadOptions options = new GraphDocumentReadOptions().ifMatch(vertex.getRev());
        final BaseDocument document = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .getVertex(vertex.getKey(), BaseDocument.class, options).get();
        assertThat(document, is(notNullValue()));
        assertThat(document.getKey(), is(vertex.getKey()));
    }

    @Test
    public void getVertexIfMatchFail() throws InterruptedException, ExecutionException {
        final VertexEntity vertex = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .insertVertex(new BaseDocument(), null).get();
        final GraphDocumentReadOptions options = new GraphDocumentReadOptions().ifMatch("no");
        try {
            db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                    .getVertex(vertex.getKey(), BaseDocument.class, options).get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause(), instanceOf(ArangoDBException.class));
        }
    }

    @Test
    public void getVertexIfNoneMatch() throws InterruptedException, ExecutionException {
        final VertexEntity vertex = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .insertVertex(new BaseDocument(), null).get();
        final GraphDocumentReadOptions options = new GraphDocumentReadOptions().ifNoneMatch("no");
        final BaseDocument document = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .getVertex(vertex.getKey(), BaseDocument.class, options).get();
        assertThat(document, is(notNullValue()));
        assertThat(document.getKey(), is(vertex.getKey()));
    }

    @Test
    public void getVertexIfNoneMatchFail() throws InterruptedException, ExecutionException {
        final VertexEntity vertex = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .insertVertex(new BaseDocument(), null).get();
        final GraphDocumentReadOptions options = new GraphDocumentReadOptions().ifNoneMatch(vertex.getRev());
        try {
            db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                    .getVertex(vertex.getKey(), BaseDocument.class, options).get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause(), instanceOf(ArangoDBException.class));
        }
    }

    @Test
    public void replaceVertex() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final VertexEntity createResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME).insertVertex(doc, null)
                .get();
        doc.getProperties().clear();
        doc.addAttribute("b", "test");
        final VertexUpdateEntity replaceResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .replaceVertex(createResult.getKey(), doc, null).get();
        assertThat(replaceResult, is(notNullValue()));
        assertThat(replaceResult.getId(), is(createResult.getId()));
        assertThat(replaceResult.getRev(), is(not(replaceResult.getOldRev())));
        assertThat(replaceResult.getOldRev(), is(createResult.getRev()));

        final BaseDocument readResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .getVertex(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getRevision(), is(replaceResult.getRev()));
        assertThat(readResult.getProperties().keySet(), not(hasItem("a")));
        assertThat(readResult.getAttribute("b"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("b")), is("test"));
    }

    @Test
    public void replaceVertexIfMatch() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final VertexEntity createResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME).insertVertex(doc, null)
                .get();
        doc.getProperties().clear();
        doc.addAttribute("b", "test");
        final VertexReplaceOptions options = new VertexReplaceOptions().ifMatch(createResult.getRev());
        final VertexUpdateEntity replaceResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .replaceVertex(createResult.getKey(), doc, options).get();
        assertThat(replaceResult, is(notNullValue()));
        assertThat(replaceResult.getId(), is(createResult.getId()));
        assertThat(replaceResult.getRev(), is(not(replaceResult.getOldRev())));
        assertThat(replaceResult.getOldRev(), is(createResult.getRev()));

        final BaseDocument readResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .getVertex(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getRevision(), is(replaceResult.getRev()));
        assertThat(readResult.getProperties().keySet(), not(hasItem("a")));
        assertThat(readResult.getAttribute("b"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("b")), is("test"));
    }

    @Test
    public void replaceVertexIfMatchFail() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final VertexEntity createResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME).insertVertex(doc, null)
                .get();
        doc.getProperties().clear();
        doc.addAttribute("b", "test");
        try {
            final VertexReplaceOptions options = new VertexReplaceOptions().ifMatch("no");
            db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME).replaceVertex(createResult.getKey(), doc, options)
                    .get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause(), instanceOf(ArangoDBException.class));
        }
    }

    @Test
    public void updateVertex() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        doc.addAttribute("c", "test");
        final VertexEntity createResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME).insertVertex(doc, null)
                .get();
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        doc.updateAttribute("c", null);
        final VertexUpdateEntity updateResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .updateVertex(createResult.getKey(), doc, null).get();
        assertThat(updateResult, is(notNullValue()));
        assertThat(updateResult.getId(), is(createResult.getId()));
        assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
        assertThat(updateResult.getOldRev(), is(createResult.getRev()));

        final BaseDocument readResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .getVertex(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getAttribute("a"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("a")), is("test1"));
        assertThat(readResult.getAttribute("b"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("b")), is("test"));
        assertThat(readResult.getRevision(), is(updateResult.getRev()));
        assertThat(readResult.getProperties().keySet(), hasItem("c"));
    }

    @Test
    public void updateVertexIfMatch() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        doc.addAttribute("c", "test");
        final VertexEntity createResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME).insertVertex(doc, null)
                .get();
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        doc.updateAttribute("c", null);
        final VertexUpdateOptions options = new VertexUpdateOptions().ifMatch(createResult.getRev());
        final VertexUpdateEntity updateResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .updateVertex(createResult.getKey(), doc, options).get();
        assertThat(updateResult, is(notNullValue()));
        assertThat(updateResult.getId(), is(createResult.getId()));
        assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
        assertThat(updateResult.getOldRev(), is(createResult.getRev()));

        final BaseDocument readResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .getVertex(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getAttribute("a"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("a")), is("test1"));
        assertThat(readResult.getAttribute("b"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("b")), is("test"));
        assertThat(readResult.getRevision(), is(updateResult.getRev()));
        assertThat(readResult.getProperties().keySet(), hasItem("c"));
    }

    @Test
    public void updateVertexIfMatchFail() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        doc.addAttribute("c", "test");
        final VertexEntity createResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME).insertVertex(doc, null)
                .get();
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        doc.updateAttribute("c", null);
        try {
            final VertexUpdateOptions options = new VertexUpdateOptions().ifMatch("no");
            db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME).updateVertex(createResult.getKey(), doc, options)
                    .get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause(), instanceOf(ArangoDBException.class));
        }
    }

    @Test
    public void updateVertexKeepNullTrue() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final VertexEntity createResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME).insertVertex(doc, null)
                .get();
        doc.updateAttribute("a", null);
        final VertexUpdateOptions options = new VertexUpdateOptions().keepNull(true);
        final VertexUpdateEntity updateResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .updateVertex(createResult.getKey(), doc, options).get();
        assertThat(updateResult, is(notNullValue()));
        assertThat(updateResult.getId(), is(createResult.getId()));
        assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
        assertThat(updateResult.getOldRev(), is(createResult.getRev()));

        final BaseDocument readResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .getVertex(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getProperties().keySet().size(), is(1));
        assertThat(readResult.getProperties().keySet(), hasItem("a"));
    }

    @Test
    public void updateVertexKeepNullFalse() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final VertexEntity createResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME).insertVertex(doc, null)
                .get();
        doc.updateAttribute("a", null);
        final VertexUpdateOptions options = new VertexUpdateOptions().keepNull(false);
        final VertexUpdateEntity updateResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .updateVertex(createResult.getKey(), doc, options).get();
        assertThat(updateResult, is(notNullValue()));
        assertThat(updateResult.getId(), is(createResult.getId()));
        assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
        assertThat(updateResult.getOldRev(), is(createResult.getRev()));

        final BaseDocument readResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .getVertex(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getId(), is(createResult.getId()));
        assertThat(readResult.getRevision(), is(notNullValue()));
        assertThat(readResult.getProperties().keySet(), not(hasItem("a")));
    }

    @Test
    public void deleteVertex() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        final VertexEntity createResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME).insertVertex(doc, null)
                .get();
        db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME).deleteVertex(createResult.getKey(), null).get();
        try {
            db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                    .getVertex(createResult.getKey(), BaseDocument.class, null).get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause(), instanceOf(ArangoDBException.class));
        }
    }

    @Test
    public void deleteVertexIfMatch() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        final VertexEntity createResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME).insertVertex(doc, null)
                .get();
        final VertexDeleteOptions options = new VertexDeleteOptions().ifMatch(createResult.getRev());
        db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME).deleteVertex(createResult.getKey(), options).get();
        try {
            db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                    .getVertex(createResult.getKey(), BaseDocument.class, null).get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause(), instanceOf(ArangoDBException.class));
        }
    }

    @Test
    public void deleteVertexIfMatchFail() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        final VertexEntity createResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME).insertVertex(doc, null)
                .get();
        final VertexDeleteOptions options = new VertexDeleteOptions().ifMatch("no");
        try {
            db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME).deleteVertex(createResult.getKey(), options).get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause(), instanceOf(ArangoDBException.class));
        }
    }
}
