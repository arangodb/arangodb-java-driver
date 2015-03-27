/**
 * Copyright 2004-2014 triAGENS GmbH, Cologne, Germany
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is triAGENS GmbH, Cologne, Germany
 *
 * @author gschwab
 * @author Copyright 2014, triAGENS GmbH, Cologne, Germany
 */
package com.arangodb;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.EdgeEntity;

public class ArangoDriverGraphEdgeUpdateTest extends BaseGraphTest {

    String graphName = "UnitTestGraph";
    String edgeCollectionName = "edge-1";

    public ArangoDriverGraphEdgeUpdateTest(ArangoConfigure configure, ArangoDriver driver) {
        super(configure, driver);
    }

    @Test
    public void test_graphUpdateEdge() throws ArangoException {

        driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
        DocumentEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(this.graphName, "from1-1",
            new TestComplexEntity01("v1-user", "desc1", 10), null);
        DocumentEntity<TestComplexEntity01> v2 = driver.graphCreateVertex(this.graphName, "to1-1",
            new TestComplexEntity01("v2-user", "desc2", 12), null);
        driver.graphCreateVertex(this.graphName, "from1-1", new TestComplexEntity01("v3-user", "desc3", 14), null);
        driver.graphCreateVertex(this.graphName, "to1-1", new TestComplexEntity01("v4-user", "desc4", 20), null);

        EdgeEntity<?> edge = driver.graphCreateEdge(this.graphName, this.edgeCollectionName, null,
            v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1, 2, 3), null);
        assertThat(edge.getCode(), is(202));

        EdgeEntity<TestComplexEntity01> updatedEdge = driver.graphUpdateEdge(this.graphName, this.edgeCollectionName,
            edge.getDocumentKey(), new TestComplexEntity01("xx", "yy", 20), true);
        assertThat(updatedEdge.getCode(), is(202));
        assertThat(updatedEdge.isError(), is(false));
        assertThat(updatedEdge.getDocumentKey(), is(edge.getDocumentKey()));
        assertThat(updatedEdge.getDocumentRevision(), is(not(edge.getDocumentRevision())));
        assertThat(updatedEdge.getDocumentHandle(), is(edge.getDocumentHandle()));
        updatedEdge = driver.graphGetEdge(this.graphName, this.edgeCollectionName, updatedEdge.getDocumentKey(),
            TestComplexEntity01.class);
        assertThat(updatedEdge.getFromVertexHandle(), is(v1.getDocumentHandle()));
        assertThat(updatedEdge.getToVertexHandle(), is(v2.getDocumentHandle()));

        assertThat(updatedEdge.getEntity(), instanceOf(TestComplexEntity01.class));
        assertThat(updatedEdge.getEntity().getUser(), is("xx"));
        assertThat(updatedEdge.getEntity().getDesc(), is("yy"));
        assertThat(updatedEdge.getEntity().getAge(), is(20));

    }

    @Test
    public void test_updateEdge_null() throws ArangoException {

        driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
        DocumentEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(this.graphName, "from1-1",
            new TestComplexEntity01("v1-user", "desc1", 10), null);
        DocumentEntity<TestComplexEntity01> v2 = driver.graphCreateVertex(this.graphName, "to1-1",
            new TestComplexEntity01("v2-user", "desc2", 12), null);
        driver.graphCreateVertex(this.graphName, "from1-1", new TestComplexEntity01("v3-user", "desc3", 14), null);
        driver.graphCreateVertex(this.graphName, "to1-1", new TestComplexEntity01("v4-user", "desc4", 20), null);

        EdgeEntity<?> edge = driver.graphCreateEdge(this.graphName, this.edgeCollectionName, null,
            v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1, 2, 3), null);
        assertThat(edge.getCode(), is(202));

        EdgeEntity<?> updatedEdge = driver.graphUpdateEdge(this.graphName, this.edgeCollectionName,
            edge.getDocumentKey(), null, null);
        assertThat(updatedEdge.getCode(), is(202));
        assertThat(updatedEdge.isError(), is(false));
        assertThat(updatedEdge.getDocumentKey(), is(edge.getDocumentKey()));
        assertThat(updatedEdge.getDocumentRevision(), is(not(edge.getDocumentRevision())));
        assertThat(updatedEdge.getDocumentHandle(), is(edge.getDocumentHandle()));

        updatedEdge = driver.graphGetEdge(this.graphName, this.edgeCollectionName, updatedEdge.getDocumentKey(), null);

        assertThat(updatedEdge.getFromVertexHandle(), is(v1.getDocumentHandle()));
        assertThat(updatedEdge.getToVertexHandle(), is(v2.getDocumentHandle()));
        assertThat(updatedEdge.getEntity(), is(nullValue()));

    }

    @Test
    public void test_updateEdge_waitForSync() throws ArangoException {

        driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
        DocumentEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(this.graphName, "from1-1",
            new TestComplexEntity01("v1-user", "desc1", 10), null);
        DocumentEntity<TestComplexEntity01> v2 = driver.graphCreateVertex(this.graphName, "to1-1",
            new TestComplexEntity01("v2-user", "desc2", 12), null);
        driver.graphCreateVertex(this.graphName, "from1-1", new TestComplexEntity01("v3-user", "desc3", 14), null);
        driver.graphCreateVertex(this.graphName, "to1-1", new TestComplexEntity01("v4-user", "desc4", 20), null);

        EdgeEntity<?> edge = driver.graphCreateEdge(this.graphName, this.edgeCollectionName, null,
            v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1, 2, 3), null);
        assertThat(edge.getCode(), is(202));

        EdgeEntity<?> updatedEdge = driver.graphUpdateEdge(this.graphName, this.edgeCollectionName,
            edge.getDocumentKey(), null, false, true, null, null);
        assertThat(updatedEdge.getCode(), is(202));
        assertThat(updatedEdge.isError(), is(false));
        assertThat(updatedEdge.getDocumentKey(), is(edge.getDocumentKey()));
        assertThat(updatedEdge.getDocumentRevision(), is(not(edge.getDocumentRevision())));
        assertThat(updatedEdge.getDocumentHandle(), is(edge.getDocumentHandle()));
        updatedEdge = driver.graphGetEdge(this.graphName, this.edgeCollectionName, edge.getDocumentKey(), null);
        assertThat(updatedEdge.getFromVertexHandle(), is(v1.getDocumentHandle()));
        assertThat(updatedEdge.getToVertexHandle(), is(v2.getDocumentHandle()));
        assertThat(updatedEdge.getEntity(), is(nullValue()));

    }

    @Test
    public void test_updateEdge_rev_eq() throws ArangoException {

        driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
        DocumentEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(this.graphName, "from1-1",
            new TestComplexEntity01("v1-user", "desc1", 10), null);
        DocumentEntity<TestComplexEntity01> v2 = driver.graphCreateVertex(this.graphName, "to1-1",
            new TestComplexEntity01("v2-user", "desc2", 12), null);
        driver.graphCreateVertex(this.graphName, "from1-1", new TestComplexEntity01("v3-user", "desc3", 14), null);
        driver.graphCreateVertex(this.graphName, "to1-1", new TestComplexEntity01("v4-user", "desc4", 20), null);

        EdgeEntity<?> edge = driver.graphCreateEdge(this.graphName, this.edgeCollectionName, null,
            v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1, 2, 3), null);
        assertThat(edge.getCode(), is(202));

        Long rev = edge.getDocumentRevision();
        EdgeEntity<TestComplexEntity01> updatedEdge = driver.graphUpdateEdge(this.graphName, this.edgeCollectionName,
            edge.getDocumentKey(), new TestComplexEntity01("xx", "yy", 20), null, null, rev, null);
        assertThat(updatedEdge.getCode(), is(202));
        assertThat(updatedEdge.isError(), is(false));
        assertThat(updatedEdge.getDocumentKey(), is(edge.getDocumentKey()));
        assertThat(updatedEdge.getDocumentRevision(), is(not(edge.getDocumentRevision())));
        assertThat(updatedEdge.getDocumentHandle(), is(edge.getDocumentHandle()));
        updatedEdge = driver.graphGetEdge(this.graphName, this.edgeCollectionName, edge.getDocumentKey(),
            TestComplexEntity01.class);
        assertThat(updatedEdge.getFromVertexHandle(), is(v1.getDocumentHandle()));
        assertThat(updatedEdge.getToVertexHandle(), is(v2.getDocumentHandle()));
        assertThat(updatedEdge.getEntity(), instanceOf(TestComplexEntity01.class));
        assertThat(updatedEdge.getEntity().getUser(), is("xx"));
        assertThat(updatedEdge.getEntity().getDesc(), is("yy"));
        assertThat(updatedEdge.getEntity().getAge(), is(20));

    }

    @Test
    public void test_updateEdge_rev_ne() throws ArangoException {

        driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
        DocumentEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(this.graphName, "from1-1",
            new TestComplexEntity01("v1-user", "desc1", 10), null);
        DocumentEntity<TestComplexEntity01> v2 = driver.graphCreateVertex(this.graphName, "to1-1",
            new TestComplexEntity01("v2-user", "desc2", 12), null);
        driver.graphCreateVertex(this.graphName, "from1-1", new TestComplexEntity01("v3-user", "desc3", 14), null);
        driver.graphCreateVertex(this.graphName, "to1-1", new TestComplexEntity01("v4-user", "desc4", 20), null);

        EdgeEntity<?> edge = driver.graphCreateEdge(this.graphName, this.edgeCollectionName, null,
            v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1, 2, 3), null);
        assertThat(edge.getCode(), is(202));

        try {
            Long rev = edge.getDocumentRevision() + 1;
            driver.graphUpdateEdge(this.graphName, this.edgeCollectionName, edge.getDocumentKey(),
                new TestComplexEntity01("xx", "yy", 20), null, null, rev, null);
            fail();
        } catch (ArangoException e) {
            assertThat(e.getCode(), is(412));
            assertThat(e.getErrorNumber(), is(1903));
            assertThat(e.getErrorMessage(), is("wrong revision"));
        }

    }

    @Test
    public void test_updateEdge_match_eq() throws ArangoException {

        driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
        DocumentEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(this.graphName, "from1-1",
            new TestComplexEntity01("v1-user", "desc1", 10), null);
        DocumentEntity<TestComplexEntity01> v2 = driver.graphCreateVertex(this.graphName, "to1-1",
            new TestComplexEntity01("v2-user", "desc2", 12), null);
        driver.graphCreateVertex(this.graphName, "from1-1", new TestComplexEntity01("v3-user", "desc3", 14), null);
        driver.graphCreateVertex(this.graphName, "to1-1", new TestComplexEntity01("v4-user", "desc4", 20), null);

        EdgeEntity<?> edge = driver.graphCreateEdge(this.graphName, this.edgeCollectionName, null,
            v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1, 2, 3), null);
        assertThat(edge.getCode(), is(202));

        Long rev = edge.getDocumentRevision();
        EdgeEntity<TestComplexEntity01> updatedEdge = driver.graphUpdateEdge(this.graphName, this.edgeCollectionName,
            edge.getDocumentKey(), new TestComplexEntity01("xx", "yy", 20), null, null, rev, null);

        assertThat(updatedEdge.getCode(), is(202));
        assertThat(updatedEdge.isError(), is(false));
        assertThat(updatedEdge.getDocumentKey(), is(edge.getDocumentKey()));
        assertThat(updatedEdge.getDocumentRevision(), is(not(edge.getDocumentRevision())));
        updatedEdge = driver.graphGetEdge(this.graphName, this.edgeCollectionName, edge.getDocumentKey(),
            TestComplexEntity01.class);
        assertThat(updatedEdge.getDocumentHandle(), is(edge.getDocumentHandle()));
        assertThat(updatedEdge.getFromVertexHandle(), is(v1.getDocumentHandle()));
        assertThat(updatedEdge.getToVertexHandle(), is(v2.getDocumentHandle()));

        assertThat(updatedEdge.getEntity(), instanceOf(TestComplexEntity01.class));
        assertThat(updatedEdge.getEntity().getUser(), is("xx"));
        assertThat(updatedEdge.getEntity().getDesc(), is("yy"));
        assertThat(updatedEdge.getEntity().getAge(), is(20));

    }

    @Test
    public void test_updateEdge_match_ne() throws ArangoException {

        driver.createGraph(this.graphName, this.createEdgeDefinitions(2, 0), this.createOrphanCollections(2), true);
        DocumentEntity<TestComplexEntity01> v1 = driver.graphCreateVertex(this.graphName, "from1-1",
            new TestComplexEntity01("v1-user", "desc1", 10), null);
        DocumentEntity<TestComplexEntity01> v2 = driver.graphCreateVertex(this.graphName, "to1-1",
            new TestComplexEntity01("v2-user", "desc2", 12), null);
        driver.graphCreateVertex(this.graphName, "from1-1", new TestComplexEntity01("v3-user", "desc3", 14), null);
        driver.graphCreateVertex(this.graphName, "to1-1", new TestComplexEntity01("v4-user", "desc4", 20), null);

        EdgeEntity<?> edge = driver.graphCreateEdge(this.graphName, this.edgeCollectionName, null,
            v1.getDocumentHandle(), v2.getDocumentHandle(), new TestComplexEntity02(1, 2, 3), null);
        assertThat(edge.getCode(), is(202));

        try {
            Long rev = edge.getDocumentRevision() + 1;
            driver.graphUpdateEdge(this.graphName, this.edgeCollectionName, edge.getDocumentKey(),
                new TestComplexEntity01("xx", "yy", 20), null, null, rev, null);
            fail();
        } catch (ArangoException e) {
            assertThat(e.getCode(), is(412));
            assertThat(e.getErrorNumber(), is(1903));
            assertThat(e.getErrorMessage(), is("wrong revision"));
        }

    }
}
