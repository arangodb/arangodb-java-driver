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

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;


/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
class ArangoEdgeCollectionTest extends BaseJunit5 {

    private static final String GRAPH_NAME = "EdgeCollectionTest_graph";
    private static final String VERTEX_COLLECTION_NAME = "EdgeCollectionTest_vertex_collection";
    private static final String EDGE_COLLECTION_NAME = "EdgeCollectionTest_edge_collection";

    private static Stream<Arguments> args() {
        return dbsStream()
                .map(db -> new Object[]{
                        db.graph(GRAPH_NAME).vertexCollection(VERTEX_COLLECTION_NAME),
                        db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                })
                .map(Arguments::of);
    }

    @BeforeAll
    static void init() {
        initCollections(VERTEX_COLLECTION_NAME);
        initEdgeCollections(EDGE_COLLECTION_NAME);
        initGraph(
                GRAPH_NAME,
                Collections.singletonList(new EdgeDefinition()
                        .collection(EDGE_COLLECTION_NAME)
                        .from(VERTEX_COLLECTION_NAME)
                        .to(VERTEX_COLLECTION_NAME)
                ),
                null
        );
    }

    private BaseEdgeDocument createEdgeValue(ArangoVertexCollection vertices) {
        final VertexEntity v1 = vertices.insertVertex(new BaseDocument());
        final VertexEntity v2 = vertices.insertVertex(new BaseDocument());

        final BaseEdgeDocument value = new BaseEdgeDocument();
        value.setFrom(v1.getId());
        value.setTo(v2.getId());
        return value;
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("args")
    void insertEdge(ArangoVertexCollection vertices, ArangoEdgeCollection edges) {
        final BaseEdgeDocument value = createEdgeValue(vertices);
        final EdgeEntity edge = edges.insertEdge(value);
        assertThat(edge).isNotNull();
        final BaseEdgeDocument document = edges.getEdge(edge.getKey(), BaseEdgeDocument.class);
        assertThat(document).isNotNull();
        assertThat(document.getKey()).isEqualTo(edge.getKey());
        assertThat(document.getFrom()).isNotNull();
        assertThat(document.getTo()).isNotNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("args")
    void insertEdgeUpdateRev(ArangoVertexCollection vertices, ArangoEdgeCollection edges) {
        final BaseEdgeDocument value = createEdgeValue(vertices);
        final EdgeEntity edge = edges.insertEdge(value);
        assertThat(value.getRevision()).isNull();
        assertThat(edge.getRev()).isNotNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("args")
    void insertEdgeViolatingUniqueConstraint(ArangoVertexCollection vertices, ArangoEdgeCollection edges) {
        edges.graph().db().collection(EDGE_COLLECTION_NAME)
                .ensurePersistentIndex(Arrays.asList("_from", "_to"), new PersistentIndexOptions().unique(true));

        BaseEdgeDocument edge = createEdgeValue(vertices);
        edges.insertEdge(edge);

        try {
            edges.insertEdge(edge);
        } catch (ArangoDBException e) {
            assertThat(e.getResponseCode()).isEqualTo(409);
            assertThat(e.getErrorNum()).isEqualTo(1210);
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("args")
    void getEdge(ArangoVertexCollection vertices, ArangoEdgeCollection edges) {
        final BaseEdgeDocument value = createEdgeValue(vertices);
        final EdgeEntity edge = edges.insertEdge(value);
        final BaseEdgeDocument document = edges
                .getEdge(edge.getKey(), BaseEdgeDocument.class);
        assertThat(document).isNotNull();
        assertThat(document.getKey()).isEqualTo(edge.getKey());
        assertThat(document.getFrom()).isNotNull();
        assertThat(document.getTo()).isNotNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("args")
    void getEdgeIfMatch(ArangoVertexCollection vertices, ArangoEdgeCollection edges) {
        final BaseEdgeDocument value = createEdgeValue(vertices);
        final EdgeEntity edge = edges.insertEdge(value);
        final GraphDocumentReadOptions options = new GraphDocumentReadOptions().ifMatch(edge.getRev());
        final BaseDocument document = edges.getEdge(edge.getKey(),
                BaseDocument.class, options);
        assertThat(document).isNotNull();
        assertThat(document.getKey()).isEqualTo(edge.getKey());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("args")
    void getEdgeIfMatchFail(ArangoVertexCollection vertices, ArangoEdgeCollection edges) {
        final BaseEdgeDocument value = createEdgeValue(vertices);
        final EdgeEntity edge = edges.insertEdge(value);
        final GraphDocumentReadOptions options = new GraphDocumentReadOptions().ifMatch("no");
        final BaseEdgeDocument edge2 = edges.getEdge(edge.getKey(),
                BaseEdgeDocument.class, options);
        assertThat(edge2).isNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("args")
    void getEdgeIfNoneMatch(ArangoVertexCollection vertices, ArangoEdgeCollection edges) {
        final BaseEdgeDocument value = createEdgeValue(vertices);
        final EdgeEntity edge = edges.insertEdge(value);
        final GraphDocumentReadOptions options = new GraphDocumentReadOptions().ifNoneMatch("no");
        final BaseDocument document = edges.getEdge(edge.getKey(),
                BaseDocument.class, options);
        assertThat(document).isNotNull();
        assertThat(document.getKey()).isEqualTo(edge.getKey());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("args")
    void getEdgeIfNoneMatchFail(ArangoVertexCollection vertices, ArangoEdgeCollection edges) {
        final BaseEdgeDocument value = createEdgeValue(vertices);
        final EdgeEntity edge = edges.insertEdge(value);
        final GraphDocumentReadOptions options = new GraphDocumentReadOptions().ifNoneMatch(edge.getRev());
        final BaseEdgeDocument edge2 = edges.getEdge(edge.getKey(),
                BaseEdgeDocument.class, options);
        assertThat(edge2).isNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("args")
    void replaceEdge(ArangoVertexCollection vertices, ArangoEdgeCollection edges) {
        final BaseEdgeDocument doc = createEdgeValue(vertices);
        doc.addAttribute("a", "test");
        final EdgeEntity createResult = edges.insertEdge(doc);
        doc.getProperties().clear();
        doc.addAttribute("b", "test");
        final EdgeUpdateEntity replaceResult = edges
                .replaceEdge(createResult.getKey(), doc);
        assertThat(replaceResult).isNotNull();
        assertThat(replaceResult.getId()).isEqualTo(createResult.getId());
        assertThat(replaceResult.getRev()).isNotEqualTo(replaceResult.getOldRev());
        assertThat(replaceResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseEdgeDocument readResult = edges
                .getEdge(createResult.getKey(), BaseEdgeDocument.class);
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getRevision()).isEqualTo(replaceResult.getRev());
        assertThat(readResult.getProperties().keySet()).doesNotContain("a");
        assertThat(readResult.getAttribute("b")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("b"))).isEqualTo("test");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("args")
    void replaceEdgeUpdateRev(ArangoVertexCollection vertices, ArangoEdgeCollection edges) {
        final BaseEdgeDocument doc = createEdgeValue(vertices);
        final EdgeEntity createResult = edges.insertEdge(doc);
        final EdgeUpdateEntity replaceResult = edges
                .replaceEdge(createResult.getKey(), doc);
        assertThat(doc.getRevision()).isNull();
        assertThat(createResult.getRev()).isNotNull();
        assertThat(replaceResult.getRev())
                .isNotNull()
                .isNotEqualTo(createResult.getRev());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("args")
    void replaceEdgeIfMatch(ArangoVertexCollection vertices, ArangoEdgeCollection edges) {
        final BaseEdgeDocument doc = createEdgeValue(vertices);
        doc.addAttribute("a", "test");
        final EdgeEntity createResult = edges.insertEdge(doc);
        doc.getProperties().clear();
        doc.addAttribute("b", "test");
        final EdgeReplaceOptions options = new EdgeReplaceOptions().ifMatch(createResult.getRev());
        final EdgeUpdateEntity replaceResult = edges
                .replaceEdge(createResult.getKey(), doc, options);
        assertThat(replaceResult).isNotNull();
        assertThat(replaceResult.getId()).isEqualTo(createResult.getId());
        assertThat(replaceResult.getRev()).isNotEqualTo(replaceResult.getOldRev());
        assertThat(replaceResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseEdgeDocument readResult = edges
                .getEdge(createResult.getKey(), BaseEdgeDocument.class);
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getRevision()).isEqualTo(replaceResult.getRev());
        assertThat(readResult.getProperties().keySet()).doesNotContain("a");
        assertThat(readResult.getAttribute("b")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("b"))).isEqualTo("test");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("args")
    void replaceEdgeIfMatchFail(ArangoVertexCollection vertices, ArangoEdgeCollection edges) {
        final BaseEdgeDocument doc = createEdgeValue(vertices);
        doc.addAttribute("a", "test");
        final EdgeEntity createResult = edges.insertEdge(doc);
        doc.getProperties().clear();
        doc.addAttribute("b", "test");
        final EdgeReplaceOptions options = new EdgeReplaceOptions().ifMatch("no");
        Throwable thrown = catchThrowable(() -> edges.replaceEdge(createResult.getKey(), doc, options));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        // FIXME: atm the server replies 409 for HTTP_JSON or HTTP_VPACK
        // assertThat(e.getResponseCode()).isEqualTo(412));
        assertThat(e.getErrorNum()).isEqualTo(1200);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("args")
    void updateEdge(ArangoVertexCollection vertices, ArangoEdgeCollection edges) {
        final BaseEdgeDocument doc = createEdgeValue(vertices);
        doc.addAttribute("a", "test");
        doc.addAttribute("c", "test");
        final EdgeEntity createResult = edges.insertEdge(doc);
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        doc.updateAttribute("c", null);
        final EdgeUpdateEntity updateResult = edges
                .updateEdge(createResult.getKey(), doc);
        assertThat(updateResult).isNotNull();
        assertThat(updateResult.getId()).isEqualTo(createResult.getId());
        assertThat(updateResult.getRev()).isNotEqualTo(updateResult.getOldRev());
        assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseEdgeDocument readResult = edges
                .getEdge(createResult.getKey(), BaseEdgeDocument.class);
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getAttribute("a")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("a"))).isEqualTo("test1");
        assertThat(readResult.getAttribute("b")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("b"))).isEqualTo("test");
        assertThat(readResult.getRevision()).isEqualTo(updateResult.getRev());
        assertThat(readResult.getProperties()).containsKey("c");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("args")
    void updateEdgeUpdateRev(ArangoVertexCollection vertices, ArangoEdgeCollection edges) {
        final BaseEdgeDocument doc = createEdgeValue(vertices);
        final EdgeEntity createResult = edges.insertEdge(doc);
        final EdgeUpdateEntity updateResult = edges
                .updateEdge(createResult.getKey(), doc);
        assertThat(doc.getRevision()).isNull();
        assertThat(createResult.getRev()).isNotNull();
        assertThat(updateResult.getRev())
                .isNotNull()
                .isNotEqualTo(createResult.getRev());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("args")
    void updateEdgeIfMatch(ArangoVertexCollection vertices, ArangoEdgeCollection edges) {
        final BaseEdgeDocument doc = createEdgeValue(vertices);
        doc.addAttribute("a", "test");
        doc.addAttribute("c", "test");
        final EdgeEntity createResult = edges.insertEdge(doc);
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        doc.updateAttribute("c", null);
        final EdgeUpdateOptions options = new EdgeUpdateOptions().ifMatch(createResult.getRev());
        final EdgeUpdateEntity updateResult = edges
                .updateEdge(createResult.getKey(), doc, options);
        assertThat(updateResult).isNotNull();
        assertThat(updateResult.getId()).isEqualTo(createResult.getId());
        assertThat(updateResult.getRev()).isNotEqualTo(updateResult.getOldRev());
        assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseEdgeDocument readResult = edges
                .getEdge(createResult.getKey(), BaseEdgeDocument.class);
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getAttribute("a")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("a"))).isEqualTo("test1");
        assertThat(readResult.getAttribute("b")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("b"))).isEqualTo("test");
        assertThat(readResult.getRevision()).isEqualTo(updateResult.getRev());
        assertThat(readResult.getProperties()).containsKey("c");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("args")
    void updateEdgeIfMatchFail(ArangoVertexCollection vertices, ArangoEdgeCollection edges) {
        final BaseEdgeDocument doc = createEdgeValue(vertices);
        doc.addAttribute("a", "test");
        doc.addAttribute("c", "test");
        final EdgeEntity createResult = edges.insertEdge(doc);
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        doc.updateAttribute("c", null);
        final EdgeUpdateOptions options = new EdgeUpdateOptions().ifMatch("no");
        Throwable thrown = catchThrowable(() -> edges.updateEdge(createResult.getKey(), doc, options));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        // FIXME: atm the server replies 409 for HTTP_JSON or HTTP_VPACK
        // assertThat(e.getResponseCode()).isEqualTo(412));
        assertThat(e.getErrorNum()).isEqualTo(1200);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("args")
    void updateEdgeKeepNullTrue(ArangoVertexCollection vertices, ArangoEdgeCollection edges) {
        final BaseEdgeDocument doc = createEdgeValue(vertices);
        doc.addAttribute("a", "test");
        final EdgeEntity createResult = edges.insertEdge(doc);
        doc.updateAttribute("a", null);
        final EdgeUpdateOptions options = new EdgeUpdateOptions().keepNull(true);
        final EdgeUpdateEntity updateResult = edges
                .updateEdge(createResult.getKey(), doc, options);
        assertThat(updateResult).isNotNull();
        assertThat(updateResult.getId()).isEqualTo(createResult.getId());
        assertThat(updateResult.getRev()).isNotEqualTo(updateResult.getOldRev());
        assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseEdgeDocument readResult = edges
                .getEdge(createResult.getKey(), BaseEdgeDocument.class);
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getProperties().keySet()).hasSize(1);
        assertThat(readResult.getProperties()).containsKey("a");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("args")
    void updateEdgeKeepNullFalse(ArangoVertexCollection vertices, ArangoEdgeCollection edges) {
        final BaseEdgeDocument doc = createEdgeValue(vertices);
        doc.addAttribute("a", "test");
        final EdgeEntity createResult = edges.insertEdge(doc);
        doc.updateAttribute("a", null);
        final EdgeUpdateOptions options = new EdgeUpdateOptions().keepNull(false);
        final EdgeUpdateEntity updateResult = edges
                .updateEdge(createResult.getKey(), doc, options);
        assertThat(updateResult).isNotNull();
        assertThat(updateResult.getId()).isEqualTo(createResult.getId());
        assertThat(updateResult.getRev()).isNotEqualTo(updateResult.getOldRev());
        assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseEdgeDocument readResult = edges
                .getEdge(createResult.getKey(), BaseEdgeDocument.class);
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getId()).isEqualTo(createResult.getId());
        assertThat(readResult.getRevision()).isNotNull();
        assertThat(readResult.getProperties().keySet()).doesNotContain("a");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("args")
    void deleteEdge(ArangoVertexCollection vertices, ArangoEdgeCollection edges) {
        final BaseEdgeDocument doc = createEdgeValue(vertices);
        final EdgeEntity createResult = edges.insertEdge(doc);
        edges.deleteEdge(createResult.getKey());
        final BaseEdgeDocument edge = edges
                .getEdge(createResult.getKey(), BaseEdgeDocument.class);
        assertThat(edge).isNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("args")
    void deleteEdgeIfMatch(ArangoVertexCollection vertices, ArangoEdgeCollection edges) {
        final BaseEdgeDocument doc = createEdgeValue(vertices);
        final EdgeEntity createResult = edges.insertEdge(doc);
        final EdgeDeleteOptions options = new EdgeDeleteOptions().ifMatch(createResult.getRev());
        edges.deleteEdge(createResult.getKey(), options);
        final BaseEdgeDocument edge = edges
                .getEdge(createResult.getKey(), BaseEdgeDocument.class);
        assertThat(edge).isNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("args")
    void deleteEdgeIfMatchFail(ArangoVertexCollection vertices, ArangoEdgeCollection edges) {
        final BaseEdgeDocument doc = createEdgeValue(vertices);
        final EdgeEntity createResult = edges.insertEdge(doc);
        final EdgeDeleteOptions options = new EdgeDeleteOptions().ifMatch("no");
        Throwable thrown = catchThrowable(() -> edges.deleteEdge(createResult.getKey(), options));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        // FIXME: atm the server replies 409 for HTTP_JSON or HTTP_VPACK
        //            assertThat(e.getResponseCode()).isEqualTo(412));
        assertThat(e.getErrorNum()).isEqualTo(1200);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("args")
    void edgeKeyWithSpecialChars(ArangoVertexCollection vertices, ArangoEdgeCollection edges) {
        final BaseEdgeDocument value = createEdgeValue(vertices);
        final String key = "_-:.@()+,=;$!*'%" + UUID.randomUUID();
        value.setKey(key);
        final EdgeEntity edge = edges.insertEdge(value);
        assertThat(edge).isNotNull();
        final BaseEdgeDocument document = edges.getEdge(edge.getKey(), BaseEdgeDocument.class);
        assertThat(document).isNotNull();
        assertThat(document.getKey()).isEqualTo(key);
        assertThat(document.getFrom()).isNotNull();
        assertThat(document.getTo()).isNotNull();
    }

}
