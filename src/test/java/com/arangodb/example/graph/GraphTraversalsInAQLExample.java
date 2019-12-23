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

package com.arangodb.example.graph;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDBException;
import org.junit.Test;

import java.util.Collection;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Graph traversals in AQL
 *
 * @author a-brandt
 * @see <a href="https://www.arangodb.com/docs/stable/aql/graphs-traversals.html">Graph traversals in AQL</a>
 */
public class GraphTraversalsInAQLExample extends BaseGraphTest {

    @Test
    public void queryAllVertices() throws ArangoDBException {
        String queryString = "FOR v IN 1..3 OUTBOUND 'circles/A' GRAPH 'traversalGraph' RETURN v._key";
        ArangoCursor<String> cursor = db.query(queryString, null, null, String.class);
        Collection<String> result = cursor.asListRemaining();
        assertThat(result.size(), is(10));

        queryString = "WITH circles FOR v IN 1..3 OUTBOUND 'circles/A' edges RETURN v._key";
        cursor = db.query(queryString, null, null, String.class);
        result = cursor.asListRemaining();
        assertThat(result.size(), is(10));
    }

    @Test
    public void queryDepthTwo() throws ArangoDBException {
        String queryString = "FOR v IN 2..2 OUTBOUND 'circles/A' GRAPH 'traversalGraph' return v._key";
        ArangoCursor<String> cursor = db.query(queryString, null, null, String.class);
        Collection<String> result = cursor.asListRemaining();
        assertThat(result.size(), is(4));
        assertThat(result, hasItems("C", "E", "H", "J"));

        queryString = "FOR v IN 2 OUTBOUND 'circles/A' GRAPH 'traversalGraph' return v._key";
        cursor = db.query(queryString, null, null, String.class);
        result = cursor.asListRemaining();
        assertThat(result.size(), is(4));
        assertThat(result, hasItems("C", "E", "H", "J"));
    }

    @Test
    public void queryWithFilter() throws ArangoDBException {
        String queryString = "FOR v, e, p IN 1..3 OUTBOUND 'circles/A' GRAPH 'traversalGraph' FILTER p.vertices[1]._key != 'G' RETURN v._key";
        ArangoCursor<String> cursor = db.query(queryString, null, null, String.class);
        Collection<String> result = cursor.asListRemaining();
        assertThat(result.size(), is(5));
        assertThat(result, hasItems("B", "C", "D", "E", "F"));

        queryString = "FOR v, e, p IN 1..3 OUTBOUND 'circles/A' GRAPH 'traversalGraph' FILTER p.edges[0].label != 'right_foo' RETURN v._key";
        cursor = db.query(queryString, null, null, String.class);
        result = cursor.asListRemaining();
        assertThat(result.size(), is(5));
        assertThat(result, hasItems("B", "C", "D", "E", "F"));

        queryString = "FOR v,e,p IN 1..3 OUTBOUND 'circles/A' GRAPH 'traversalGraph' FILTER p.vertices[1]._key != 'G' FILTER p.edges[1].label != 'left_blub' return v._key";
        cursor = db.query(queryString, null, null, String.class);

        result = cursor.asListRemaining();
        assertThat(result.size(), is(3));
        assertThat(result, hasItems("B", "C", "D"));

        queryString = "FOR v,e,p IN 1..3 OUTBOUND 'circles/A' GRAPH 'traversalGraph' FILTER p.vertices[1]._key != 'G' AND    p.edges[1].label != 'left_blub' return v._key";
        cursor = db.query(queryString, null, null, String.class);
        result = cursor.asListRemaining();
        assertThat(result.size(), is(3));
        assertThat(result, hasItems("B", "C", "D"));
    }

    @Test
    public void queryOutboundInbound() throws ArangoDBException {
        String queryString = "FOR v IN 1..3 OUTBOUND 'circles/E' GRAPH 'traversalGraph' return v._key";
        ArangoCursor<String> cursor = db.query(queryString, null, null, String.class);
        Collection<String> result = cursor.asListRemaining();
        assertThat(result.size(), is(1));
        assertThat(result, hasItems("F"));

        queryString = "FOR v IN 1..3 INBOUND 'circles/E' GRAPH 'traversalGraph' return v._key";
        cursor = db.query(queryString, null, null, String.class);
        result = cursor.asListRemaining();
        assertThat(result.size(), is(2));
        assertThat(result, hasItems("B", "A"));

        queryString = "FOR v IN 1..3 ANY 'circles/E' GRAPH 'traversalGraph' return v._key";
        cursor = db.query(queryString, null, null, String.class);

        result = cursor.asListRemaining();
        assertThat(result.size(), is(6));
        assertThat(result, hasItems("F", "B", "C", "D", "A", "G"));
    }

}
