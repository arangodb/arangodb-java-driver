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

package com.arangodb.async.example.graph;

import com.arangodb.async.ArangoCursorAsync;
import org.junit.jupiter.api.Test;


import java.util.Collection;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Graph traversals in AQL
 *
 * @author a-brandt
 * @see <a href="https://www.arangodb.com/docs/stable/aql/graphs-traversals.html">Graph traversals in AQL</a>
 */
class GraphTraversalsInAQLExampleTest extends BaseGraphTest {

    @Test
    void queryAllVertices() throws InterruptedException, ExecutionException {
        String queryString = "FOR v IN 1..3 OUTBOUND 'circles/A' GRAPH 'traversalGraph' RETURN v._key";
        ArangoCursorAsync<String> cursor = db.query(queryString, null, null, String.class).get();
        Collection<String> result = cursor.asListRemaining();
        assertThat(result).hasSize(10);

        queryString = "WITH circles FOR v IN 1..3 OUTBOUND 'circles/A' edges RETURN v._key";
        cursor = db.query(queryString, null, null, String.class).get();
        result = cursor.asListRemaining();
        assertThat(result).hasSize(10);
    }

    @Test
    void queryDepthTwo() throws InterruptedException, ExecutionException {
        String queryString = "FOR v IN 2..2 OUTBOUND 'circles/A' GRAPH 'traversalGraph' return v._key";
        ArangoCursorAsync<String> cursor = db.query(queryString, null, null, String.class).get();
        Collection<String> result = cursor.asListRemaining();
        assertThat(result).hasSize(4);
        assertThat(result).contains("C", "E", "H", "J");

        queryString = "FOR v IN 2 OUTBOUND 'circles/A' GRAPH 'traversalGraph' return v._key";
        cursor = db.query(queryString, null, null, String.class).get();
        result = cursor.asListRemaining();
        assertThat(result).hasSize(4);
        assertThat(result).contains("C", "E", "H", "J");
    }

    @Test
    void queryWithFilter() throws InterruptedException, ExecutionException {
        String queryString = "FOR v, e, p IN 1..3 OUTBOUND 'circles/A' GRAPH 'traversalGraph' FILTER p.vertices[1]._key != 'G' RETURN v._key";
        ArangoCursorAsync<String> cursor = db.query(queryString, null, null, String.class).get();
        Collection<String> result = cursor.asListRemaining();
        assertThat(result).hasSize(5);
        assertThat(result).contains("B", "C", "D", "E", "F");

        queryString = "FOR v, e, p IN 1..3 OUTBOUND 'circles/A' GRAPH 'traversalGraph' FILTER p.edges[0].label != 'right_foo' RETURN v._key";
        cursor = db.query(queryString, null, null, String.class).get();
        result = cursor.asListRemaining();
        assertThat(result).hasSize(5);
        assertThat(result).contains("B", "C", "D", "E", "F");

        queryString = "FOR v,e,p IN 1..3 OUTBOUND 'circles/A' GRAPH 'traversalGraph' FILTER p.vertices[1]._key != 'G' FILTER p.edges[1].label != 'left_blub' return v._key";
        cursor = db.query(queryString, null, null, String.class).get();

        result = cursor.asListRemaining();
        assertThat(result).hasSize(3);
        assertThat(result).contains("B", "C", "D");

        queryString = "FOR v,e,p IN 1..3 OUTBOUND 'circles/A' GRAPH 'traversalGraph' FILTER p.vertices[1]._key != 'G' AND    p.edges[1].label != 'left_blub' return v._key";
        cursor = db.query(queryString, null, null, String.class).get();
        result = cursor.asListRemaining();
        assertThat(result).hasSize(3);
        assertThat(result).contains("B", "C", "D");
    }

    @Test
    void queryOutboundInbound() throws InterruptedException, ExecutionException {
        String queryString = "FOR v IN 1..3 OUTBOUND 'circles/E' GRAPH 'traversalGraph' return v._key";
        ArangoCursorAsync<String> cursor = db.query(queryString, null, null, String.class).get();
        Collection<String> result = cursor.asListRemaining();
        assertThat(result).hasSize(1);
        assertThat(result).contains("F");

        queryString = "FOR v IN 1..3 INBOUND 'circles/E' GRAPH 'traversalGraph' return v._key";
        cursor = db.query(queryString, null, null, String.class).get();
        result = cursor.asListRemaining();
        assertThat(result).hasSize(2);
        assertThat(result).contains("B", "A");

        queryString = "FOR v IN 1..3 ANY 'circles/E' GRAPH 'traversalGraph' return v._key";
        cursor = db.query(queryString, null, null, String.class).get();

        result = cursor.asListRemaining();
        assertThat(result).hasSize(6);
        assertThat(result).contains("F", "B", "C", "D", "A", "G");
    }

}
