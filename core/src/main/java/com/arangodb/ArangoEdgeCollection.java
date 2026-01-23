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

import com.arangodb.entity.EdgeEntity;
import com.arangodb.entity.EdgeUpdateEntity;
import com.arangodb.model.*;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Interface for operations on ArangoDB edge collection level.
 *
 * @author Mark Vollmary
 * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/graphs/named-graphs/">API Documentation</a>
 */
@ThreadSafe
public interface ArangoEdgeCollection extends ArangoSerdeAccessor {

    /**
     * The the handler of the named graph the edge collection is within
     *
     * @return graph handler
     */
    ArangoGraph graph();

    /**
     * The name of the edge collection
     *
     * @return collection name
     */
    String name();

    /**
     * Remove one edge definition from the graph.
     *
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/graphs/named-graphs/#remove-an-edge-definition">API
     * Documentation</a>
     *
     * @deprecated use {@link #remove()} instead
     */
    @Deprecated
    void drop();

    /**
     * Remove one edge definition from the graph.
     *
     * @param options options
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/graphs/named-graphs/#remove-an-edge-definition">API
     * Documentation</a>
     *
     * @deprecated use {@link #remove(EdgeCollectionRemoveOptions)} instead
     */
    @Deprecated
    void drop(EdgeCollectionDropOptions options);

    /**
     * Remove one edge definition from the graph.
     *
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/graphs/named-graphs/#remove-an-edge-definition">API
     * Documentation</a>
     */
    void remove();

    /**
     * Remove one edge definition from the graph.
     *
     * @param options options
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/graphs/named-graphs/#remove-an-edge-definition">API
     * Documentation</a>
     */
    void remove(EdgeCollectionRemoveOptions options);

    /**
     * Creates a new edge in the collection
     *
     * @param value A representation of a single edge (POJO or {@link com.arangodb.util.RawData})
     * @return information about the edge
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/graphs/named-graphs/#create-an-edge">API Documentation</a>
     */
    EdgeEntity insertEdge(Object value);

    /**
     * Creates a new edge in the collection
     *
     * @param value   A representation of a single edge (POJO or {@link com.arangodb.util.RawData})
     * @param options Additional options, can be null
     * @return information about the edge
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/graphs/named-graphs/#create-an-edge">API Documentation</a>
     */
    EdgeEntity insertEdge(Object value, EdgeCreateOptions options);

    /**
     * Fetches an existing edge
     *
     * @param key  The key of the edge
     * @param type The type of the edge-document (POJO or {@link com.arangodb.util.RawData})
     * @return the edge identified by the key
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/graphs/named-graphs/#get-an-edge">API Documentation</a>
     */
    <T> T getEdge(String key, Class<T> type);

    /**
     * Fetches an existing edge
     *
     * @param key     The key of the edge
     * @param type    The type of the edge-document (POJO or {@link com.arangodb.util.RawData})
     * @param options Additional options, can be null
     * @return the edge identified by the key
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/graphs/named-graphs/#get-an-edge">API Documentation</a>
     */
    <T> T getEdge(String key, Class<T> type, GraphDocumentReadOptions options);

    /**
     * Replaces the edge with key with the one in the body, provided there is such a edge and no precondition is
     * violated
     *
     * @param key   The key of the edge
     * @param value A representation of a single edge (POJO or {@link com.arangodb.util.RawData})
     * @return information about the edge
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/graphs/named-graphs/#replace-an-edge">API Documentation</a>
     */
    EdgeUpdateEntity replaceEdge(String key, Object value);

    /**
     * Replaces the edge with key with the one in the body, provided there is such a edge and no precondition is
     * violated
     *
     * @param key     The key of the edge
     * @param value   A representation of a single edge (POJO or {@link com.arangodb.util.RawData})
     * @param options Additional options, can be null
     * @return information about the edge
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/graphs/named-graphs/#replace-an-edge">API Documentation</a>
     */
    EdgeUpdateEntity replaceEdge(String key, Object value, EdgeReplaceOptions options);

    /**
     * Partially updates the edge identified by document-key. The value must contain a document with the attributes to
     * patch (the patch document). All attributes from the patch document will be added to the existing document if they
     * do not yet exist, and overwritten in the existing document if they do exist there.
     *
     * @param key   The key of the edge
     * @param value A representation of a single edge (POJO or {@link com.arangodb.util.RawData})
     * @return information about the edge
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/graphs/named-graphs/#update-an-edge">API Documentation</a>
     */
    EdgeUpdateEntity updateEdge(String key, Object value);

    /**
     * Partially updates the edge identified by document-key. The value must contain a document with the attributes to
     * patch (the patch document). All attributes from the patch document will be added to the existing document if they
     * do not yet exist, and overwritten in the existing document if they do exist there.
     *
     * @param key     The key of the edge
     * @param value   A representation of a single edge (POJO or {@link com.arangodb.util.RawData})
     * @param options Additional options, can be null
     * @return information about the edge
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/graphs/named-graphs/#update-an-edge">API Documentation</a>
     */
    EdgeUpdateEntity updateEdge(String key, Object value, EdgeUpdateOptions options);

    /**
     * Removes a edge
     *
     * @param key The key of the edge
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/graphs/named-graphs/#remove-an-edge">API Documentation</a>
     */
    void deleteEdge(String key);

    /**
     * Removes a edge
     *
     * @param key     The key of the edge
     * @param options Additional options, can be null
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/graphs/named-graphs/#remove-an-edge">API Documentation</a>
     */
    void deleteEdge(String key, EdgeDeleteOptions options);

}
