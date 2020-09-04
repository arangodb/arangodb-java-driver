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
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

/**
 * @author Mark Vollmary
 */
@RunWith(Parameterized.class)
public class ArangoEdgeCollectionTest extends BaseTest {

    private static final String GRAPH_NAME = "EdgeCollectionTest_graph";
    private static final String VERTEX_COLLECTION_NAME = "EdgeCollectionTest_vertex_collection";
    private static final String EDGE_COLLECTION_NAME = "EdgeCollectionTest_edge_collection";

    private final ArangoCollection vertexCollection;
    private final ArangoCollection edgeCollection;

    private final ArangoGraph graph;
    private final ArangoVertexCollection vertices;
    private final ArangoEdgeCollection edges;

    @BeforeClass
    public static void init() {
        BaseTest.initCollections(VERTEX_COLLECTION_NAME);
        BaseTest.initEdgeCollections(EDGE_COLLECTION_NAME);
        BaseTest.initGraph(
                GRAPH_NAME,
                Collections.singletonList(new EdgeDefinition()
                        .collection(EDGE_COLLECTION_NAME)
                        .from(VERTEX_COLLECTION_NAME)
                        .to(VERTEX_COLLECTION_NAME)
                ),
                null
        );
    }

    public ArangoEdgeCollectionTest(final ArangoDB arangoDB) {
        super(arangoDB);

        vertexCollection = db.collection(VERTEX_COLLECTION_NAME);
        edgeCollection = db.collection(EDGE_COLLECTION_NAME);

        graph = db.graph(GRAPH_NAME);
        vertices = graph.vertexCollection(VERTEX_COLLECTION_NAME);
        edges = graph.edgeCollection(EDGE_COLLECTION_NAME);
    }

    private BaseEdgeDocument createEdgeValue() {
        final VertexEntity v1 = vertices
                .insertVertex(new BaseDocument(), null);
        final VertexEntity v2 = vertices
                .insertVertex(new BaseDocument(), null);

        final BaseEdgeDocument value = new BaseEdgeDocument();
        value.setFrom(v1.getId());
        value.setTo(v2.getId());
        return value;
    }

    @Test
    public void insertEdge() {
        final BaseEdgeDocument value = createEdgeValue();
        final EdgeEntity edge = edges.insertEdge(value, null);
        assertThat(edge, is(notNullValue()));
        final BaseEdgeDocument document = edgeCollection.getDocument(edge.getKey(),
                BaseEdgeDocument.class, null);
        assertThat(document, is(notNullValue()));
        assertThat(document.getKey(), is(edge.getKey()));
        assertThat(document.getFrom(), is(notNullValue()));
        assertThat(document.getTo(), is(notNullValue()));
    }

    @Test
    public void insertEdgeUpdateRev() {
        final BaseEdgeDocument value = createEdgeValue();
        final EdgeEntity edge = edges.insertEdge(value, null);
        assertThat(value.getRevision(), is(edge.getRev()));
    }

    @Test
    public void insertEdgeViolatingUniqueConstraint() {
        // FIXME: remove once fix is backported to 3.4
        assumeTrue(isAtLeastVersion(3, 5));

        edgeCollection
                .ensureSkiplistIndex(Arrays.asList("_from", "_to"), new SkiplistIndexOptions().unique(true));

        BaseEdgeDocument edge = createEdgeValue();
        edges.insertEdge(edge, null);

        try {
            edges.insertEdge(edge, null);
        } catch (ArangoDBException e) {
            assertThat(e.getResponseCode(), is(409));
            assertThat(e.getErrorNum(), is(1210));
        }
    }

    @Test
    public void getEdge() {
        final BaseEdgeDocument value = createEdgeValue();
        final EdgeEntity edge = edges.insertEdge(value, null);
        final BaseEdgeDocument document = edges
                .getEdge(edge.getKey(), BaseEdgeDocument.class, null);
        assertThat(document, is(notNullValue()));
        assertThat(document.getKey(), is(edge.getKey()));
        assertThat(document.getFrom(), is(notNullValue()));
        assertThat(document.getTo(), is(notNullValue()));
    }

    @Test
    public void getEdgeIfMatch() {
        final BaseEdgeDocument value = createEdgeValue();
        final EdgeEntity edge = edges.insertEdge(value, null);
        final GraphDocumentReadOptions options = new GraphDocumentReadOptions().ifMatch(edge.getRev());
        final BaseDocument document = edges.getEdge(edge.getKey(),
                BaseDocument.class, options);
        assertThat(document, is(notNullValue()));
        assertThat(document.getKey(), is(edge.getKey()));
    }

    @Test
    public void getEdgeIfMatchFail() {
        final BaseEdgeDocument value = createEdgeValue();
        final EdgeEntity edge = edges.insertEdge(value, null);
        final GraphDocumentReadOptions options = new GraphDocumentReadOptions().ifMatch("no");
        final BaseEdgeDocument edge2 = edges.getEdge(edge.getKey(),
                BaseEdgeDocument.class, options);
        assertThat(edge2, is(nullValue()));
    }

    @Test
    public void getEdgeIfNoneMatch() {
        final BaseEdgeDocument value = createEdgeValue();
        final EdgeEntity edge = edges.insertEdge(value, null);
        final GraphDocumentReadOptions options = new GraphDocumentReadOptions().ifNoneMatch("no");
        final BaseDocument document = edges.getEdge(edge.getKey(),
                BaseDocument.class, options);
        assertThat(document, is(notNullValue()));
        assertThat(document.getKey(), is(edge.getKey()));
    }

    @Test
    public void getEdgeIfNoneMatchFail() {
        final BaseEdgeDocument value = createEdgeValue();
        final EdgeEntity edge = edges.insertEdge(value, null);
        final GraphDocumentReadOptions options = new GraphDocumentReadOptions().ifNoneMatch(edge.getRev());
        final BaseEdgeDocument edge2 = edges.getEdge(edge.getKey(),
                BaseEdgeDocument.class, options);
        assertThat(edge2, is(nullValue()));
    }

    @Test
    public void replaceEdge() {
        final BaseEdgeDocument doc = createEdgeValue();
        doc.addAttribute("a", "test");
        final EdgeEntity createResult = edges.insertEdge(doc, null);
        doc.getProperties().clear();
        doc.addAttribute("b", "test");
        final EdgeUpdateEntity replaceResult = edges
                .replaceEdge(createResult.getKey(), doc, null);
        assertThat(replaceResult, is(notNullValue()));
        assertThat(replaceResult.getId(), is(createResult.getId()));
        assertThat(replaceResult.getRev(), is(not(replaceResult.getOldRev())));
        assertThat(replaceResult.getOldRev(), is(createResult.getRev()));

        final BaseEdgeDocument readResult = edges
                .getEdge(createResult.getKey(), BaseEdgeDocument.class, null);
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getRevision(), is(replaceResult.getRev()));
        assertThat(readResult.getProperties().keySet(), not(hasItem("a")));
        assertThat(readResult.getAttribute("b"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("b")), is("test"));
    }

    @Test
    public void replaceEdgeUpdateRev() {
        final BaseEdgeDocument doc = createEdgeValue();
        final EdgeEntity createResult = edges.insertEdge(doc, null);
        assertThat(doc.getRevision(), is(createResult.getRev()));
        final EdgeUpdateEntity replaceResult = edges
                .replaceEdge(createResult.getKey(), doc, null);
        assertThat(doc.getRevision(), is(replaceResult.getRev()));
    }

    @Test
    public void replaceEdgeIfMatch() {
        final BaseEdgeDocument doc = createEdgeValue();
        doc.addAttribute("a", "test");
        final EdgeEntity createResult = edges.insertEdge(doc, null);
        doc.getProperties().clear();
        doc.addAttribute("b", "test");
        final EdgeReplaceOptions options = new EdgeReplaceOptions().ifMatch(createResult.getRev());
        final EdgeUpdateEntity replaceResult = edges
                .replaceEdge(createResult.getKey(), doc, options);
        assertThat(replaceResult, is(notNullValue()));
        assertThat(replaceResult.getId(), is(createResult.getId()));
        assertThat(replaceResult.getRev(), is(not(replaceResult.getOldRev())));
        assertThat(replaceResult.getOldRev(), is(createResult.getRev()));

        final BaseEdgeDocument readResult = edges
                .getEdge(createResult.getKey(), BaseEdgeDocument.class, null);
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getRevision(), is(replaceResult.getRev()));
        assertThat(readResult.getProperties().keySet(), not(hasItem("a")));
        assertThat(readResult.getAttribute("b"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("b")), is("test"));
    }

    @Test
    public void replaceEdgeIfMatchFail() {
        final BaseEdgeDocument doc = createEdgeValue();
        doc.addAttribute("a", "test");
        final EdgeEntity createResult = edges.insertEdge(doc, null);
        doc.getProperties().clear();
        doc.addAttribute("b", "test");
        try {
            final EdgeReplaceOptions options = new EdgeReplaceOptions().ifMatch("no");
            edges.replaceEdge(createResult.getKey(), doc, options);
            fail();
        } catch (final ArangoDBException e) {
            if (isAtLeastVersion(3, 4)) {
                // FIXME: atm the server replies 409 for HTTP_JSON or HTTP_VPACK
                // assertThat(e.getResponseCode(), is(412));
                assertThat(e.getErrorNum(), is(1200));
            } else {
                assertThat(e.getResponseCode(), is(412));
                assertThat(e.getErrorNum(), is(1903));
            }
        }
    }

    @Test
    public void updateEdge() {
        final BaseEdgeDocument doc = createEdgeValue();
        doc.addAttribute("a", "test");
        doc.addAttribute("c", "test");
        final EdgeEntity createResult = edges.insertEdge(doc, null);
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        doc.updateAttribute("c", null);
        final EdgeUpdateEntity updateResult = edges
                .updateEdge(createResult.getKey(), doc, null);
        assertThat(updateResult, is(notNullValue()));
        assertThat(updateResult.getId(), is(createResult.getId()));
        assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
        assertThat(updateResult.getOldRev(), is(createResult.getRev()));

        final BaseEdgeDocument readResult = edges
                .getEdge(createResult.getKey(), BaseEdgeDocument.class, null);
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getAttribute("a"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("a")), is("test1"));
        assertThat(readResult.getAttribute("b"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("b")), is("test"));
        assertThat(readResult.getRevision(), is(updateResult.getRev()));
        assertThat(readResult.getProperties().keySet(), hasItem("c"));
    }

    @Test
    public void updateEdgeUpdateRev() {
        final BaseEdgeDocument doc = createEdgeValue();
        final EdgeEntity createResult = edges.insertEdge(doc, null);
        assertThat(doc.getRevision(), is(createResult.getRev()));
        final EdgeUpdateEntity updateResult = edges
                .updateEdge(createResult.getKey(), doc, null);
        assertThat(doc.getRevision(), is(updateResult.getRev()));
    }

    @Test
    public void updateEdgeIfMatch() {
        final BaseEdgeDocument doc = createEdgeValue();
        doc.addAttribute("a", "test");
        doc.addAttribute("c", "test");
        final EdgeEntity createResult = edges.insertEdge(doc, null);
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        doc.updateAttribute("c", null);
        final EdgeUpdateOptions options = new EdgeUpdateOptions().ifMatch(createResult.getRev());
        final EdgeUpdateEntity updateResult = edges
                .updateEdge(createResult.getKey(), doc, options);
        assertThat(updateResult, is(notNullValue()));
        assertThat(updateResult.getId(), is(createResult.getId()));
        assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
        assertThat(updateResult.getOldRev(), is(createResult.getRev()));

        final BaseEdgeDocument readResult = edges
                .getEdge(createResult.getKey(), BaseEdgeDocument.class, null);
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getAttribute("a"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("a")), is("test1"));
        assertThat(readResult.getAttribute("b"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("b")), is("test"));
        assertThat(readResult.getRevision(), is(updateResult.getRev()));
        assertThat(readResult.getProperties().keySet(), hasItem("c"));
    }

    @Test
    public void updateEdgeIfMatchFail() {
        final BaseEdgeDocument doc = createEdgeValue();
        doc.addAttribute("a", "test");
        doc.addAttribute("c", "test");
        final EdgeEntity createResult = edges.insertEdge(doc, null);
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        doc.updateAttribute("c", null);
        try {
            final EdgeUpdateOptions options = new EdgeUpdateOptions().ifMatch("no");
            edges.updateEdge(createResult.getKey(), doc, options);
            fail();
        } catch (final ArangoDBException e) {
            if (isAtLeastVersion(3, 4)) {
                // FIXME: atm the server replies 409 for HTTP_JSON or HTTP_VPACK
                // assertThat(e.getResponseCode(), is(412));
                assertThat(e.getErrorNum(), is(1200));
            } else {
                assertThat(e.getResponseCode(), is(412));
                assertThat(e.getErrorNum(), is(1903));
            }
        }
    }

    @Test
    public void updateEdgeKeepNullTrue() {
        final BaseEdgeDocument doc = createEdgeValue();
        doc.addAttribute("a", "test");
        final EdgeEntity createResult = edges.insertEdge(doc, null);
        doc.updateAttribute("a", null);
        final EdgeUpdateOptions options = new EdgeUpdateOptions().keepNull(true);
        final EdgeUpdateEntity updateResult = edges
                .updateEdge(createResult.getKey(), doc, options);
        assertThat(updateResult, is(notNullValue()));
        assertThat(updateResult.getId(), is(createResult.getId()));
        assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
        assertThat(updateResult.getOldRev(), is(createResult.getRev()));

        final BaseEdgeDocument readResult = edges
                .getEdge(createResult.getKey(), BaseEdgeDocument.class, null);
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getProperties().keySet().size(), is(1));
        assertThat(readResult.getProperties().keySet(), hasItem("a"));
    }

    @Test
    public void updateEdgeKeepNullFalse() {
        final BaseEdgeDocument doc = createEdgeValue();
        doc.addAttribute("a", "test");
        final EdgeEntity createResult = edges.insertEdge(doc, null);
        doc.updateAttribute("a", null);
        final EdgeUpdateOptions options = new EdgeUpdateOptions().keepNull(false);
        final EdgeUpdateEntity updateResult = edges
                .updateEdge(createResult.getKey(), doc, options);
        assertThat(updateResult, is(notNullValue()));
        assertThat(updateResult.getId(), is(createResult.getId()));
        assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
        assertThat(updateResult.getOldRev(), is(createResult.getRev()));

        final BaseEdgeDocument readResult = edges
                .getEdge(createResult.getKey(), BaseEdgeDocument.class, null);
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getId(), is(createResult.getId()));
        assertThat(readResult.getRevision(), is(notNullValue()));
        assertThat(readResult.getProperties().keySet(), not(hasItem("a")));
    }

    @Test
    public void deleteEdge() {
        final BaseEdgeDocument doc = createEdgeValue();
        final EdgeEntity createResult = edges.insertEdge(doc, null);
        edges.deleteEdge(createResult.getKey(), null);
        final BaseEdgeDocument edge = edges
                .getEdge(createResult.getKey(), BaseEdgeDocument.class, null);
        assertThat(edge, is(nullValue()));
    }

    @Test
    public void deleteEdgeIfMatch() {
        final BaseEdgeDocument doc = createEdgeValue();
        final EdgeEntity createResult = edges.insertEdge(doc, null);
        final EdgeDeleteOptions options = new EdgeDeleteOptions().ifMatch(createResult.getRev());
        edges.deleteEdge(createResult.getKey(), options);
        final BaseEdgeDocument edge = edges
                .getEdge(createResult.getKey(), BaseEdgeDocument.class, null);
        assertThat(edge, is(nullValue()));
    }

    @Test
    public void deleteEdgeIfMatchFail() {
        final BaseEdgeDocument doc = createEdgeValue();
        final EdgeEntity createResult = edges.insertEdge(doc, null);
        final EdgeDeleteOptions options = new EdgeDeleteOptions().ifMatch("no");
        try {
            edges.deleteEdge(createResult.getKey(), options);
            fail();
        } catch (final ArangoDBException e) {
            if (isAtLeastVersion(3, 4)) {
                // FIXME: atm the server replies 409 for HTTP_JSON or HTTP_VPACK
                //            assertThat(e.getResponseCode(), is(412));
                assertThat(e.getErrorNum(), is(1200));
            } else {
                assertThat(e.getResponseCode(), is(412));
                assertThat(e.getErrorNum(), is(1903));
            }
        }
    }

    @Test
    public void edgeKeyWithSpecialChars() {
        final BaseEdgeDocument value = createEdgeValue();
        final String key = "_-:.@()+,=;$!*'%" + UUID.randomUUID().toString();
        value.setKey(key);
        final EdgeEntity edge = edges.insertEdge(value, null);
        assertThat(edge, is(notNullValue()));
        final BaseEdgeDocument document = edgeCollection.getDocument(edge.getKey(),
                BaseEdgeDocument.class, null);
        assertThat(document, is(notNullValue()));
        assertThat(document.getKey(), is(key));
        assertThat(document.getFrom(), is(notNullValue()));
        assertThat(document.getTo(), is(notNullValue()));
    }

}
