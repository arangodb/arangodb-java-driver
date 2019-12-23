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

import com.arangodb.entity.VertexEntity;
import com.arangodb.entity.VertexUpdateEntity;
import com.arangodb.model.*;

/**
 * Interface for operations on ArangoDB vertex collection level.
 *
 * @author Mark Vollmary
 * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-vertices.html">API Documentation</a>
 */
public interface ArangoVertexCollection extends ArangoSerializationAccessor {

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
     * Removes a vertex collection from the graph and optionally deletes the collection, if it is not used in any other
     * graph
     *
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-management.html#remove-vertex-collection">API
     * Documentation</a>
     */
    void drop() throws ArangoDBException;

    /**
     * Creates a new vertex in the collection
     *
     * @param value A representation of a single vertex (POJO, VPackSlice or String for JSON)
     * @return information about the vertex
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-vertices.html#create-a-vertex">API Documentation</a>
     */
    <T> VertexEntity insertVertex(T value) throws ArangoDBException;

    /**
     * Creates a new vertex in the collection
     *
     * @param value   A representation of a single vertex (POJO, VPackSlice or String for JSON)
     * @param options Additional options, can be null
     * @return information about the vertex
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-vertices.html#create-a-vertex">API Documentation</a>
     */
    <T> VertexEntity insertVertex(T value, VertexCreateOptions options) throws ArangoDBException;

    /**
     * Retrieves the vertex document with the given {@code key} from the collection.
     *
     * @param key  The key of the vertex
     * @param type The type of the vertex-document (POJO class, VPackSlice or String for JSON)
     * @return the vertex identified by the key
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-vertices.html#get-a-vertex">API Documentation</a>
     */
    <T> T getVertex(String key, Class<T> type) throws ArangoDBException;

    /**
     * Retrieves the vertex document with the given {@code key} from the collection.
     *
     * @param key     The key of the vertex
     * @param type    The type of the vertex-document (POJO class, VPackSlice or String for JSON)
     * @param options Additional options, can be null
     * @return the vertex identified by the key
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-vertices.html#get-a-vertex">API Documentation</a>
     */
    <T> T getVertex(String key, Class<T> type, GraphDocumentReadOptions options) throws ArangoDBException;

    /**
     * Replaces the vertex with key with the one in the body, provided there is such a vertex and no precondition is
     * violated
     *
     * @param key   The key of the vertex
     * @param value A representation of a single vertex (POJO, VPackSlice or String for JSON)
     * @return information about the vertex
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-vertices.html#replace-a-vertex">API
     * Documentation</a>
     */
    <T> VertexUpdateEntity replaceVertex(String key, T value) throws ArangoDBException;

    /**
     * Replaces the vertex with key with the one in the body, provided there is such a vertex and no precondition is
     * violated
     *
     * @param key     The key of the vertex
     * @param value   A representation of a single vertex (POJO, VPackSlice or String for JSON)
     * @param options Additional options, can be null
     * @return information about the vertex
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-vertices.html#replace-a-vertex">API
     * Documentation</a>
     */
    <T> VertexUpdateEntity replaceVertex(String key, T value, VertexReplaceOptions options) throws ArangoDBException;

    /**
     * Partially updates the vertex identified by document-key. The value must contain a document with the attributes to
     * patch (the patch document). All attributes from the patch document will be added to the existing document if they
     * do not yet exist, and overwritten in the existing document if they do exist there.
     *
     * @param key The key of the vertex
     * @param <T> The type of the vertex-document (POJO class, VPackSlice or String for JSON)
     * @return information about the vertex
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-vertices.html#modify-a-vertex">API Documentation</a>
     */
    <T> VertexUpdateEntity updateVertex(String key, T value) throws ArangoDBException;

    /**
     * Partially updates the vertex identified by document-key. The value must contain a document with the attributes to
     * patch (the patch document). All attributes from the patch document will be added to the existing document if they
     * do not yet exist, and overwritten in the existing document if they do exist there.
     *
     * @param key     The key of the vertex
     * @param <T>     The type of the vertex-document (POJO class, VPackSlice or String for JSON)
     * @param options Additional options, can be null
     * @return information about the vertex
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-vertices.html#modify-a-vertex">API Documentation</a>
     */
    <T> VertexUpdateEntity updateVertex(String key, T value, VertexUpdateOptions options) throws ArangoDBException;

    /**
     * Deletes the vertex with the given {@code key} from the collection.
     *
     * @param key The key of the vertex
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-vertices.html#remove-a-vertex">API Documentation</a>
     */
    void deleteVertex(String key) throws ArangoDBException;

    /**
     * Deletes the vertex with the given {@code key} from the collection.
     *
     * @param key     The key of the vertex
     * @param options Additional options, can be null
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-vertices.html#remove-a-vertex">API Documentation</a>
     */
    void deleteVertex(String key, VertexDeleteOptions options) throws ArangoDBException;

}
