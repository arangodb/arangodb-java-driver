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
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Shortest Path in AQL
 *
 * @author a-brandt
 * @see <a href="https://www.arangodb.com/docs/stable/aql/graphs-shortest-path.html">Shortest Path in AQL</a>
 */
class ShortestPathInAQLExampleTest extends BaseGraphTest {

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static class Pair {

        private String vertex;
        private String edge;

        String getVertex() {
            return vertex;
        }

        void setVertex(final String vertex) {
            this.vertex = vertex;
        }

        String getEdge() {
            return edge;
        }

        void setEdge(final String edge) {
            this.edge = edge;
        }

    }

    @Test
    void queryShortestPathFromAToD() throws ArangoDBException {
        String queryString = "FOR v, e IN OUTBOUND SHORTEST_PATH 'circles/A' TO 'circles/D' GRAPH 'traversalGraph' RETURN {'vertex': v._key, 'edge': e._key}";
        ArangoCursor<Pair> cursor = db.query(queryString, null, null, Pair.class);
        final Collection<String> collection = toVertexCollection(cursor);
        assertThat(collection.size()).isEqualTo(4);
        assertThat(collection).containsExactlyInAnyOrder("A", "B", "C", "D");

        queryString = "WITH circles FOR v, e IN OUTBOUND SHORTEST_PATH 'circles/A' TO 'circles/D' edges RETURN {'vertex': v._key, 'edge': e._key}";
        db.query(queryString, null, null, Pair.class);
        assertThat(collection.size()).isEqualTo(4);
        assertThat(collection).containsExactlyInAnyOrder("A", "B", "C", "D");
    }

    @Test
    void queryShortestPathByFilter() throws ArangoDBException {
        String queryString = "FOR a IN circles FILTER a._key == 'A' FOR d IN circles FILTER d._key == 'D' FOR v, e IN OUTBOUND SHORTEST_PATH a TO d GRAPH 'traversalGraph' RETURN {'vertex':v._key, 'edge':e._key}";
        ArangoCursor<Pair> cursor = db.query(queryString, null, null, Pair.class);
        final Collection<String> collection = toVertexCollection(cursor);
        assertThat(collection.size()).isEqualTo(4);
        assertThat(collection).containsExactlyInAnyOrder("A", "B", "C", "D");

        queryString = "FOR a IN circles FILTER a._key == 'A' FOR d IN circles FILTER d._key == 'D' FOR v, e IN OUTBOUND SHORTEST_PATH a TO d edges RETURN {'vertex': v._key, 'edge': e._key}";
        db.query(queryString, null, null, Pair.class);
        assertThat(collection.size()).isEqualTo(4);
        assertThat(collection).containsExactlyInAnyOrder("A", "B", "C", "D");
    }

    private Collection<String> toVertexCollection(final ArangoCursor<Pair> cursor) {
        final List<String> result = new ArrayList<>();
        while (cursor.hasNext()) {
            final Pair pair = cursor.next();
            result.add(pair.getVertex());
        }
        return result;
    }

}
