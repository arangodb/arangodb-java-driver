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

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.VertexEntity;
import com.arangodb.entity.VertexUpdateEntity;
import com.arangodb.model.*;
import com.arangodb.util.RawJson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;


/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
class ArangoVertexCollectionAsyncTest extends BaseJunit5 {

    private static final String GRAPH_NAME = "ArangoVertexCollectionTest_graph";
    private static final String COLLECTION_NAME = rndName();

    private static Stream<Arguments> asyncVertices() {
        return asyncDbsStream()
                .map(db -> db.graph(GRAPH_NAME).vertexCollection(COLLECTION_NAME))
                .map(Arguments::of);
    }

    @BeforeAll
    static void init() {
        initCollections(COLLECTION_NAME);
        initGraph(
                GRAPH_NAME,
                null,
                new GraphCreateOptions().orphanCollections(COLLECTION_NAME)
        );
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncVertices")
    void dropVertexCollection(ArangoVertexCollectionAsync vertices) throws ExecutionException, InterruptedException {
        ArangoGraphAsync graph = vertices.graph();
        vertices.drop().get();
        final Collection<String> vertexCollections = graph.getVertexCollections().get();
        assertThat(vertexCollections).isEmpty();
        assertThat(graph.db().collection(COLLECTION_NAME).exists().get()).isTrue();

        // revert
        graph.addVertexCollection(COLLECTION_NAME).get();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncVertices")
    void dropVertexCollectionDropCollectionTrue(ArangoVertexCollectionAsync vertices) throws ExecutionException, InterruptedException {
        ArangoGraphAsync graph = vertices.graph();
        vertices.drop(new VertexCollectionDropOptions().dropCollection(true)).get();
        final Collection<String> vertexCollections = graph.getVertexCollections().get();
        assertThat(vertexCollections).isEmpty();
        assertThat(graph.db().collection(COLLECTION_NAME).exists().get()).isFalse();

        // revert
        initCollections(COLLECTION_NAME);
        graph.addVertexCollection(COLLECTION_NAME).get();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncVertices")
    void insertVertex(ArangoVertexCollectionAsync vertices) throws ExecutionException, InterruptedException {
        final VertexEntity vertex = vertices
                .insertVertex(new BaseDocument(), null).get();
        assertThat(vertex).isNotNull();
        ArangoCollectionAsync collection = vertices.graph().db().collection(vertices.name());
        final BaseDocument document = collection
                .getDocument(vertex.getKey(), BaseDocument.class, null).get();
        assertThat(document).isNotNull();
        assertThat(document.getKey()).isEqualTo(vertex.getKey());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncVertices")
    void insertVertexViolatingUniqueConstraint(ArangoVertexCollectionAsync vertices) throws ExecutionException, InterruptedException {
        ArangoCollectionAsync collection = vertices.graph().db().collection(vertices.name());
        collection
                .ensurePersistentIndex(Collections.singletonList("field"),
                        new PersistentIndexOptions().unique(true).sparse(true)).get();

        VertexEntity inserted = vertices.insertVertex(RawJson.of("{\"field\": 99}")).get();

        try {
            vertices.insertVertex(RawJson.of("{\"field\": 99}"));
        } catch (ArangoDBException e) {
            assertThat(e.getResponseCode()).isEqualTo(409);
            assertThat(e.getErrorNum()).isEqualTo(1210);
        }

        // revert
        vertices.deleteVertex(inserted.getKey()).get();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncVertices")
    void duplicateInsertSameObjectVertex(ArangoVertexCollectionAsync vertices) {

        // #########################################################
        // Create a new BaseDocument
        // #########################################################

        UUID uuid = UUID.randomUUID();
        BaseDocument bd = new BaseDocument(UUID.randomUUID().toString());
        bd.setKey(uuid.toString());
        bd.addAttribute("name", "Paul");

        vertices.insertVertex(bd);

        UUID uuid2 = UUID.randomUUID();
        BaseDocument bd2 = new BaseDocument(UUID.randomUUID().toString());
        bd2.setKey(uuid2.toString());
        bd2.addAttribute("name", "Paul");

        vertices.insertVertex(bd2);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncVertices")
    void insertVertexUpdateRev(ArangoVertexCollectionAsync vertices) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        final VertexEntity vertex = vertices.insertVertex(doc, null).get();
        assertThat(doc.getRevision()).isNull();
        assertThat(vertex.getRev()).isNotNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncVertices")
    void getVertex(ArangoVertexCollectionAsync vertices) throws ExecutionException, InterruptedException {
        final VertexEntity vertex = vertices
                .insertVertex(new BaseDocument(), null).get();
        final BaseDocument document = vertices
                .getVertex(vertex.getKey(), BaseDocument.class, null).get();
        assertThat(document).isNotNull();
        assertThat(document.getKey()).isEqualTo(vertex.getKey());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncVertices")
    void getVertexIfMatch(ArangoVertexCollectionAsync vertices) throws ExecutionException, InterruptedException {
        final VertexEntity vertex = vertices
                .insertVertex(new BaseDocument(), null).get();
        final GraphDocumentReadOptions options = new GraphDocumentReadOptions().ifMatch(vertex.getRev());
        final BaseDocument document = vertices
                .getVertex(vertex.getKey(), BaseDocument.class, options).get();
        assertThat(document).isNotNull();
        assertThat(document.getKey()).isEqualTo(vertex.getKey());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncVertices")
    void getVertexIfMatchFail(ArangoVertexCollectionAsync vertices) throws ExecutionException, InterruptedException {
        final VertexEntity vertex = vertices
                .insertVertex(new BaseDocument(), null).get();
        final GraphDocumentReadOptions options = new GraphDocumentReadOptions().ifMatch("no");
        final BaseDocument vertex2 = vertices
                .getVertex(vertex.getKey(), BaseDocument.class, options).get();
        assertThat(vertex2).isNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncVertices")
    void getVertexIfNoneMatch(ArangoVertexCollectionAsync vertices) throws ExecutionException, InterruptedException {
        final VertexEntity vertex = vertices
                .insertVertex(new BaseDocument(), null).get();
        final GraphDocumentReadOptions options = new GraphDocumentReadOptions().ifNoneMatch("no");
        final BaseDocument document = vertices
                .getVertex(vertex.getKey(), BaseDocument.class, options).get();
        assertThat(document).isNotNull();
        assertThat(document.getKey()).isEqualTo(vertex.getKey());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncVertices")
    void getVertexIfNoneMatchFail(ArangoVertexCollectionAsync vertices) throws ExecutionException, InterruptedException {
        final VertexEntity vertex = vertices
                .insertVertex(new BaseDocument(), null).get();
        final GraphDocumentReadOptions options = new GraphDocumentReadOptions().ifNoneMatch(vertex.getRev());
        final BaseDocument vertex2 = vertices
                .getVertex(vertex.getKey(), BaseDocument.class, options).get();
        assertThat(vertex2).isNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncVertices")
    void replaceVertex(ArangoVertexCollectionAsync vertices) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        final VertexEntity createResult = vertices
                .insertVertex(doc, null).get();
        doc.removeAttribute("a");
        doc.addAttribute("b", "test");
        final VertexUpdateEntity replaceResult = vertices
                .replaceVertex(createResult.getKey(), doc, null).get();
        assertThat(replaceResult).isNotNull();
        assertThat(replaceResult.getId()).isEqualTo(createResult.getId());
        assertThat(replaceResult.getRev()).isNotEqualTo(replaceResult.getOldRev());
        assertThat(replaceResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseDocument readResult = vertices
                .getVertex(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getRevision()).isEqualTo(replaceResult.getRev());
        assertThat(readResult.getProperties().keySet()).doesNotContain("a");
        assertThat(readResult.getAttribute("b")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("b"))).isEqualTo("test");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncVertices")
    void replaceVertexUpdateRev(ArangoVertexCollectionAsync vertices) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        final VertexEntity createResult = vertices
                .insertVertex(doc, null).get();
        final VertexUpdateEntity replaceResult = vertices
                .replaceVertex(createResult.getKey(), doc, null).get();
        assertThat(doc.getRevision()).isNull();
        assertThat(createResult.getRev()).isNotNull();
        assertThat(replaceResult.getRev())
                .isNotNull()
                .isNotEqualTo(createResult.getRev());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncVertices")
    void replaceVertexIfMatch(ArangoVertexCollectionAsync vertices) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        final VertexEntity createResult = vertices
                .insertVertex(doc, null).get();
        doc.removeAttribute("a");
        doc.addAttribute("b", "test");
        final VertexReplaceOptions options = new VertexReplaceOptions().ifMatch(createResult.getRev());
        final VertexUpdateEntity replaceResult = vertices
                .replaceVertex(createResult.getKey(), doc, options).get();
        assertThat(replaceResult).isNotNull();
        assertThat(replaceResult.getId()).isEqualTo(createResult.getId());
        assertThat(replaceResult.getRev()).isNotEqualTo(replaceResult.getOldRev());
        assertThat(replaceResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseDocument readResult = vertices
                .getVertex(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getRevision()).isEqualTo(replaceResult.getRev());
        assertThat(readResult.getProperties().keySet()).doesNotContain("a");
        assertThat(readResult.getAttribute("b")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("b"))).isEqualTo("test");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncVertices")
    void replaceVertexIfMatchFail(ArangoVertexCollectionAsync vertices) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        final VertexEntity createResult = vertices
                .insertVertex(doc, null).get();
        doc.removeAttribute("a");
        doc.addAttribute("b", "test");
        final VertexReplaceOptions options = new VertexReplaceOptions().ifMatch("no");
        Throwable thrown = catchThrowable(() -> vertices.replaceVertex(createResult.getKey(), doc, options).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getResponseCode()).isEqualTo(412);
        assertThat(e.getErrorNum()).isEqualTo(1200);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncVertices")
    void updateVertex(ArangoVertexCollectionAsync vertices) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        doc.addAttribute("c", "test");
        final VertexEntity createResult = vertices
                .insertVertex(doc, null).get();
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        doc.updateAttribute("c", null);
        final VertexUpdateEntity updateResult = vertices
                .updateVertex(createResult.getKey(), doc, null).get();
        assertThat(updateResult).isNotNull();
        assertThat(updateResult.getId()).isEqualTo(createResult.getId());
        assertThat(updateResult.getRev()).isNotEqualTo(updateResult.getOldRev());
        assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseDocument readResult = vertices
                .getVertex(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getAttribute("a")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("a"))).isEqualTo("test1");
        assertThat(readResult.getAttribute("b")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("b"))).isEqualTo("test");
        assertThat(readResult.getRevision()).isEqualTo(updateResult.getRev());
        assertThat(readResult.getProperties()).containsKey("c");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncVertices")
    void updateVertexUpdateRev(ArangoVertexCollectionAsync vertices) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        final VertexEntity createResult = vertices
                .insertVertex(doc, null).get();
        doc.addAttribute("foo", "bar");
        final VertexUpdateEntity updateResult = vertices
                .updateVertex(createResult.getKey(), doc, null).get();
        assertThat(doc.getRevision()).isNull();
        assertThat(createResult.getRev()).isNotNull();
        assertThat(updateResult.getRev())
                .isNotNull()
                .isNotEqualTo(createResult.getRev());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncVertices")
    void updateVertexIfMatch(ArangoVertexCollectionAsync vertices) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        doc.addAttribute("c", "test");
        final VertexEntity createResult = vertices
                .insertVertex(doc, null).get();
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        doc.updateAttribute("c", null);
        final VertexUpdateOptions options = new VertexUpdateOptions().ifMatch(createResult.getRev());
        final VertexUpdateEntity updateResult = vertices
                .updateVertex(createResult.getKey(), doc, options).get();
        assertThat(updateResult).isNotNull();
        assertThat(updateResult.getId()).isEqualTo(createResult.getId());
        assertThat(updateResult.getRev()).isNotEqualTo(updateResult.getOldRev());
        assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseDocument readResult = vertices
                .getVertex(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getAttribute("a")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("a"))).isEqualTo("test1");
        assertThat(readResult.getAttribute("b")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("b"))).isEqualTo("test");
        assertThat(readResult.getRevision()).isEqualTo(updateResult.getRev());
        assertThat(readResult.getProperties()).containsKey("c");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncVertices")
    void updateVertexIfMatchFail(ArangoVertexCollectionAsync vertices) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        doc.addAttribute("c", "test");
        final VertexEntity createResult = vertices
                .insertVertex(doc, null).get();
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        doc.updateAttribute("c", null);
        final VertexUpdateOptions options = new VertexUpdateOptions().ifMatch("no");

        Throwable thrown = catchThrowable(() -> vertices.updateVertex(createResult.getKey(), doc, options).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getResponseCode()).isEqualTo(412);
        assertThat(e.getErrorNum()).isEqualTo(1200);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncVertices")
    void updateVertexKeepNullTrue(ArangoVertexCollectionAsync vertices) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        final VertexEntity createResult = vertices
                .insertVertex(doc, null).get();
        doc.updateAttribute("a", null);
        final VertexUpdateOptions options = new VertexUpdateOptions().keepNull(true);
        final VertexUpdateEntity updateResult = vertices
                .updateVertex(createResult.getKey(), doc, options).get();
        assertThat(updateResult).isNotNull();
        assertThat(updateResult.getId()).isEqualTo(createResult.getId());
        assertThat(updateResult.getRev()).isNotEqualTo(updateResult.getOldRev());
        assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseDocument readResult = vertices
                .getVertex(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getProperties().keySet()).hasSize(4);
        assertThat(readResult.getProperties()).containsKey("a");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncVertices")
    void updateVertexKeepNullFalse(ArangoVertexCollectionAsync vertices) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        final VertexEntity createResult = vertices
                .insertVertex(doc, null).get();
        doc.updateAttribute("a", null);
        final VertexUpdateOptions options = new VertexUpdateOptions().keepNull(false);
        final VertexUpdateEntity updateResult = vertices
                .updateVertex(createResult.getKey(), doc, options).get();
        assertThat(updateResult).isNotNull();
        assertThat(updateResult.getId()).isEqualTo(createResult.getId());
        assertThat(updateResult.getRev()).isNotEqualTo(updateResult.getOldRev());
        assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseDocument readResult = vertices
                .getVertex(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getId()).isEqualTo(createResult.getId());
        assertThat(readResult.getRevision()).isNotNull();
        assertThat(readResult.getProperties().keySet()).doesNotContain("a");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncVertices")
    void deleteVertex(ArangoVertexCollectionAsync vertices) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        final VertexEntity createResult = vertices
                .insertVertex(doc, null).get();
        vertices.deleteVertex(createResult.getKey(), null).get();
        final BaseDocument vertex = vertices
                .getVertex(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(vertex).isNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncVertices")
    void deleteVertexIfMatch(ArangoVertexCollectionAsync vertices) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        final VertexEntity createResult = vertices
                .insertVertex(doc, null).get();
        final VertexDeleteOptions options = new VertexDeleteOptions().ifMatch(createResult.getRev());
        vertices.deleteVertex(createResult.getKey(), options).get();
        final BaseDocument vertex = vertices
                .getVertex(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(vertex).isNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncVertices")
    void deleteVertexIfMatchFail(ArangoVertexCollectionAsync vertices) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        final VertexEntity createResult = vertices
                .insertVertex(doc, null).get();
        final VertexDeleteOptions options = new VertexDeleteOptions().ifMatch("no");
        Throwable thrown = catchThrowable(() -> vertices.deleteVertex(createResult.getKey(), options).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getResponseCode()).isEqualTo(412);
        assertThat(e.getErrorNum()).isEqualTo(1200);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncVertices")
    void vertexKeyWithSpecialChars(ArangoVertexCollectionAsync vertices) throws ExecutionException, InterruptedException {
        final String key = "_-:.@()+,=;$!*'%" + UUID.randomUUID();
        final VertexEntity vertex = vertices
                .insertVertex(new BaseDocument(key), null).get();
        assertThat(vertex).isNotNull();
        ArangoCollectionAsync collection = vertices.graph().db().collection(vertices.name());
        final BaseDocument document = collection
                .getDocument(vertex.getKey(), BaseDocument.class, null).get();
        assertThat(document).isNotNull();
        assertThat(document.getKey()).isEqualTo(key);
    }

}
