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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


import java.util.Collection;
import java.util.concurrent.ExecutionException;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Mark Vollmary
 */
class ArangoVertexCollectionTest extends BaseTest {

    private static final String GRAPH_NAME = "db_collection_test";
    private static final String COLLECTION_NAME = "db_vertex_collection_test";

    @BeforeAll
    static void setup() throws InterruptedException, ExecutionException {
        if (!db.collection(COLLECTION_NAME).exists().get()) {
            db.createCollection(COLLECTION_NAME, null).get();
        }
        final GraphCreateOptions options = new GraphCreateOptions().orphanCollections(COLLECTION_NAME);
        db.createGraph(GRAPH_NAME, null, options).get();
    }

    @AfterEach
    void teardown() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME).truncate().get();
    }

    @Test
    void dropVertexCollection() throws InterruptedException, ExecutionException {
        db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME).drop().get();
        final Collection<String> vertexCollections = db.graph(GRAPH_NAME).getVertexCollections().get();
        assertThat(vertexCollections).doesNotContain(COLLECTION_NAME);
    }

    @Test
    void insertVertex() throws InterruptedException, ExecutionException {
        final VertexEntity vertex = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .insertVertex(new BaseDocument(), null).get();
        assertThat(vertex).isNotNull();
        final BaseDocument document = db.collection(COLLECTION_NAME)
                .getDocument(vertex.getKey(), BaseDocument.class, null).get();
        assertThat(document).isNotNull();
        assertThat(document.getKey()).isEqualTo(vertex.getKey());
    }

    @Test
    void getVertex() throws InterruptedException, ExecutionException {
        final VertexEntity vertex = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .insertVertex(new BaseDocument(), null).get();
        final BaseDocument document = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .getVertex(vertex.getKey(), BaseDocument.class, null).get();
        assertThat(document).isNotNull();
        assertThat(document.getKey()).isEqualTo(vertex.getKey());
    }

    @Test
    void getVertexIfMatch() throws InterruptedException, ExecutionException {
        final VertexEntity vertex = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .insertVertex(new BaseDocument(), null).get();
        final GraphDocumentReadOptions options = new GraphDocumentReadOptions().ifMatch(vertex.getRev());
        final BaseDocument document = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .getVertex(vertex.getKey(), BaseDocument.class, options).get();
        assertThat(document).isNotNull();
        assertThat(document.getKey()).isEqualTo(vertex.getKey());
    }

    @Test
    void getVertexIfMatchFail() throws InterruptedException, ExecutionException {
        final VertexEntity vertex = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .insertVertex(new BaseDocument(), null).get();
        final GraphDocumentReadOptions options = new GraphDocumentReadOptions().ifMatch("no").catchException(false);
        try {
            db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                    .getVertex(vertex.getKey(), BaseDocument.class, options).get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
        }
    }

    @Test
    void getVertexIfNoneMatch() throws InterruptedException, ExecutionException {
        final VertexEntity vertex = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .insertVertex(new BaseDocument(), null).get();
        final GraphDocumentReadOptions options = new GraphDocumentReadOptions().ifNoneMatch("no");
        final BaseDocument document = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .getVertex(vertex.getKey(), BaseDocument.class, options).get();
        assertThat(document).isNotNull();
        assertThat(document.getKey()).isEqualTo(vertex.getKey());
    }

    @Test
    void getVertexIfNoneMatchFail() throws InterruptedException, ExecutionException {
        final VertexEntity vertex = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .insertVertex(new BaseDocument(), null).get();
        final GraphDocumentReadOptions options = new GraphDocumentReadOptions().ifNoneMatch(vertex.getRev()).catchException(false);
        try {
            db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                    .getVertex(vertex.getKey(), BaseDocument.class, options).get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
        }
    }

    @Test
    void replaceVertex() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final VertexEntity createResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME).insertVertex(doc, null)
                .get();
        doc.getProperties().clear();
        doc.addAttribute("b", "test");
        final VertexUpdateEntity replaceResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .replaceVertex(createResult.getKey(), doc, null).get();
        assertThat(replaceResult).isNotNull();
        assertThat(replaceResult.getId()).isEqualTo(createResult.getId());
        assertThat(replaceResult.getRev()).isNotEqualTo(replaceResult.getOldRev());
        assertThat(replaceResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseDocument readResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .getVertex(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getRevision()).isEqualTo(replaceResult.getRev());
        assertThat(readResult.getProperties().keySet()).doesNotContain("a");
        assertThat(readResult.getAttribute("b")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("b"))).isEqualTo("test");
    }

    @Test
    void replaceVertexIfMatch() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final VertexEntity createResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME).insertVertex(doc, null)
                .get();
        doc.getProperties().clear();
        doc.addAttribute("b", "test");
        final VertexReplaceOptions options = new VertexReplaceOptions().ifMatch(createResult.getRev());
        final VertexUpdateEntity replaceResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .replaceVertex(createResult.getKey(), doc, options).get();
        assertThat(replaceResult).isNotNull();
        assertThat(replaceResult.getId()).isEqualTo(createResult.getId());
        assertThat(replaceResult.getRev()).isNotEqualTo(replaceResult.getOldRev());
        assertThat(replaceResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseDocument readResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .getVertex(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getRevision()).isEqualTo(replaceResult.getRev());
        assertThat(readResult.getProperties().keySet()).doesNotContain("a");
        assertThat(readResult.getAttribute("b")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("b"))).isEqualTo("test");
    }

    @Test
    void replaceVertexIfMatchFail() throws InterruptedException, ExecutionException {
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
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
        }
    }

    @Test
    void updateVertex() throws InterruptedException, ExecutionException {
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
        assertThat(updateResult).isNotNull();
        assertThat(updateResult.getId()).isEqualTo(createResult.getId());
        assertThat(updateResult.getRev()).isNotEqualTo(updateResult.getOldRev());
        assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseDocument readResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .getVertex(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getAttribute("a")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("a"))).isEqualTo("test1");
        assertThat(readResult.getAttribute("b")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("b"))).isEqualTo("test");
        assertThat(readResult.getRevision()).isEqualTo(updateResult.getRev());
        assertThat(readResult.getProperties()).containsKey("c");
    }

    @Test
    void updateVertexIfMatch() throws InterruptedException, ExecutionException {
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
        assertThat(updateResult).isNotNull();
        assertThat(updateResult.getId()).isEqualTo(createResult.getId());
        assertThat(updateResult.getRev()).isNotEqualTo(updateResult.getOldRev());
        assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseDocument readResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .getVertex(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getAttribute("a")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("a"))).isEqualTo("test1");
        assertThat(readResult.getAttribute("b")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("b"))).isEqualTo("test");
        assertThat(readResult.getRevision()).isEqualTo(updateResult.getRev());
        assertThat(readResult.getProperties()).containsKey("c");
    }

    @Test
    void updateVertexIfMatchFail() throws InterruptedException, ExecutionException {
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
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
        }
    }

    @Test
    void updateVertexKeepNullTrue() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final VertexEntity createResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME).insertVertex(doc, null)
                .get();
        doc.updateAttribute("a", null);
        final VertexUpdateOptions options = new VertexUpdateOptions().keepNull(true);
        final VertexUpdateEntity updateResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .updateVertex(createResult.getKey(), doc, options).get();
        assertThat(updateResult).isNotNull();
        assertThat(updateResult.getId()).isEqualTo(createResult.getId());
        assertThat(updateResult.getRev()).isNotEqualTo(updateResult.getOldRev());
        assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseDocument readResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .getVertex(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getProperties().keySet()).hasSize(1);
        assertThat(readResult.getProperties()).containsKey("a");
    }

    @Test
    void updateVertexKeepNullFalse() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final VertexEntity createResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME).insertVertex(doc, null)
                .get();
        doc.updateAttribute("a", null);
        final VertexUpdateOptions options = new VertexUpdateOptions().keepNull(false);
        final VertexUpdateEntity updateResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .updateVertex(createResult.getKey(), doc, options).get();
        assertThat(updateResult).isNotNull();
        assertThat(updateResult.getId()).isEqualTo(createResult.getId());
        assertThat(updateResult.getRev()).isNotEqualTo(updateResult.getOldRev());
        assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseDocument readResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                .getVertex(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getId()).isEqualTo(createResult.getId());
        assertThat(readResult.getRevision()).isNotNull();
        assertThat(readResult.getProperties().keySet()).doesNotContain("a");
    }

    @Test
    void deleteVertex() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        final VertexEntity createResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME).insertVertex(doc, null)
                .get();
        db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME).deleteVertex(createResult.getKey(), null).get();
        try {
            db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                    .getVertex(createResult.getKey(), BaseDocument.class, new GraphDocumentReadOptions().catchException(false)).get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
        }
    }

    @Test
    void deleteVertexIfMatch() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        final VertexEntity createResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME).insertVertex(doc, null)
                .get();
        final VertexDeleteOptions options = new VertexDeleteOptions().ifMatch(createResult.getRev());
        db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME).deleteVertex(createResult.getKey(), options).get();
        try {
            db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME)
                    .getVertex(createResult.getKey(), BaseDocument.class, new GraphDocumentReadOptions().catchException(false)).get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
        }
    }

    @Test
    void deleteVertexIfMatchFail() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        final VertexEntity createResult = db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME).insertVertex(doc, null)
                .get();
        final VertexDeleteOptions options = new VertexDeleteOptions().ifMatch("no");
        try {
            db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME).deleteVertex(createResult.getKey(), options).get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
        }
    }
}
