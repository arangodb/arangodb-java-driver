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

import com.arangodb.ArangoSerializationAccessor;
import com.arangodb.entity.VertexEntity;
import com.arangodb.entity.VertexUpdateEntity;
import com.arangodb.model.*;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for operations on ArangoDB vertex collection level.
 *
 * @author Mark Vollmary
 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Vertices.html">API Documentation</a>
 */
@SuppressWarnings("unused")
public interface ArangoVertexCollectionAsync extends ArangoSerializationAccessor {

    /**
     * The the handler of the named graph the edge collection is within
     *
     * @return graph handler
     */
    ArangoGraphAsync graph();

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
     * @return void
     * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#remove-vertex-collection">API
     * Documentation</a>
     */
    CompletableFuture<Void> drop();

    /**
     * Creates a new vertex in the collection
     *
     * @param value A representation of a single vertex (POJO, VPackSlice or String for Json)
     * @return information about the vertex
     * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Vertices.html#create-a-vertex">API Documentation</a>
     */
    <T> CompletableFuture<VertexEntity> insertVertex(final T value);

    /**
     * Creates a new vertex in the collection
     *
     * @param value   A representation of a single vertex (POJO, VPackSlice or String for Json)
     * @param options Additional options, can be null
     * @return information about the vertex
     * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Vertices.html#create-a-vertex">API Documentation</a>
     */
    <T> CompletableFuture<VertexEntity> insertVertex(final T value, final VertexCreateOptions options);

    /**
     * Fetches an existing vertex
     *
     * @param key  The key of the vertex
     * @param type The type of the vertex-document (POJO class, VPackSlice or String for Json)
     * @return the vertex identified by the key
     * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Vertices.html#get-a-vertex">API Documentation</a>
     */
    <T> CompletableFuture<T> getVertex(final String key, final Class<T> type);

    /**
     * Fetches an existing vertex
     *
     * @param key     The key of the vertex
     * @param type    The type of the vertex-document (POJO class, VPackSlice or String for Json)
     * @param options Additional options, can be null
     * @return the vertex identified by the key
     * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Vertices.html#get-a-vertex">API Documentation</a>
     */
    <T> CompletableFuture<T> getVertex(final String key, final Class<T> type, final GraphDocumentReadOptions options);

    /**
     * Replaces the vertex with key with the one in the body, provided there is such a vertex and no precondition is
     * violated
     *
     * @param key The key of the vertex
     * @return information about the vertex
     * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Vertices.html#replace-a-vertex">API
     * Documentation</a>
     */
    <T> CompletableFuture<VertexUpdateEntity> replaceVertex(final String key, final T value);

    /**
     * Replaces the vertex with key with the one in the body, provided there is such a vertex and no precondition is
     * violated
     *
     * @param key     The key of the vertex
     * @param options Additional options, can be null
     * @return information about the vertex
     * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Vertices.html#replace-a-vertex">API
     * Documentation</a>
     */
    <T> CompletableFuture<VertexUpdateEntity> replaceVertex(
            final String key,
            final T value,
            final VertexReplaceOptions options);

    /**
     * Partially updates the vertex identified by document-key. The value must contain a document with the attributes to
     * patch (the patch document). All attributes from the patch document will be added to the existing document if they
     * do not yet exist, and overwritten in the existing document if they do exist there.
     *
     * @param key The key of the vertex
     * @return information about the vertex
     * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Vertices.html#modify-a-vertex">API Documentation</a>
     */
    <T> CompletableFuture<VertexUpdateEntity> updateVertex(final String key, final T value);

    /**
     * Partially updates the vertex identified by document-key. The value must contain a document with the attributes to
     * patch (the patch document). All attributes from the patch document will be added to the existing document if they
     * do not yet exist, and overwritten in the existing document if they do exist there.
     *
     * @param key     The key of the vertex
     * @param options Additional options, can be null
     * @return information about the vertex
     * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Vertices.html#modify-a-vertex">API Documentation</a>
     */
    <T> CompletableFuture<VertexUpdateEntity> updateVertex(
            final String key,
            final T value,
            final VertexUpdateOptions options);

    /**
     * Removes a vertex
     *
     * @param key The key of the vertex
     * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Vertices.html#remove-a-vertex">API Documentation</a>
     */
    CompletableFuture<Void> deleteVertex(final String key);

    /**
     * Removes a vertex
     *
     * @param key     The key of the vertex
     * @param options Additional options, can be null
     * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Vertices.html#remove-a-vertex">API Documentation</a>
     */
    CompletableFuture<Void> deleteVertex(final String key, final VertexDeleteOptions options);

}
