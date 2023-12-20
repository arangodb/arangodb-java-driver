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

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous version of {@link ArangoVertexCollection}
 */
@ThreadSafe
public interface ArangoVertexCollectionAsync extends ArangoSerdeAccessor {

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
     * Asynchronous version of {@link ArangoVertexCollection#drop()}
     *
     * @deprecated use {@link #remove()} instead
     */
    @Deprecated
    CompletableFuture<Void> drop();

    /**
     * Asynchronous version of {@link ArangoVertexCollection#drop(VertexCollectionDropOptions)}
     *
     * @deprecated use {@link #remove(VertexCollectionRemoveOptions)} instead
     */
    @Deprecated
    CompletableFuture<Void> drop(VertexCollectionDropOptions options);

    /**
     * Asynchronous version of {@link ArangoVertexCollection#remove()}
     */
    CompletableFuture<Void> remove();

    /**
     * Asynchronous version of {@link ArangoVertexCollection#remove(VertexCollectionRemoveOptions)}
     */
    CompletableFuture<Void> remove(VertexCollectionRemoveOptions options);

    /**
     * Asynchronous version of {@link ArangoVertexCollection#insertVertex(Object)}
     */
    CompletableFuture<VertexEntity> insertVertex(Object value);

    /**
     * Asynchronous version of {@link ArangoVertexCollection#insertVertex(Object, VertexCreateOptions)}
     */
    CompletableFuture<VertexEntity> insertVertex(Object value, VertexCreateOptions options);

    /**
     * Asynchronous version of {@link ArangoVertexCollection#getVertex(String, Class)}
     */
    <T> CompletableFuture<T> getVertex(String key, Class<T> type);

    /**
     * Asynchronous version of {@link ArangoVertexCollection#getVertex(String, Class, GraphDocumentReadOptions)}
     */
    <T> CompletableFuture<T> getVertex(String key, Class<T> type, GraphDocumentReadOptions options);

    /**
     * Asynchronous version of {@link ArangoVertexCollection#replaceVertex(String, Object)}
     */
    CompletableFuture<VertexUpdateEntity> replaceVertex(String key, Object value);

    /**
     * Asynchronous version of {@link ArangoVertexCollection#replaceVertex(String, Object, VertexReplaceOptions)}
     */
    CompletableFuture<VertexUpdateEntity> replaceVertex(String key, Object value, VertexReplaceOptions options);

    /**
     * Asynchronous version of {@link ArangoVertexCollection#updateVertex(String, Object)}
     */
    CompletableFuture<VertexUpdateEntity> updateVertex(String key, Object value);

    /**
     * Asynchronous version of {@link ArangoVertexCollection#updateVertex(String, Object, VertexUpdateOptions)}
     */
    CompletableFuture<VertexUpdateEntity> updateVertex(String key, Object value, VertexUpdateOptions options);

    /**
     * Asynchronous version of {@link ArangoVertexCollection#deleteVertex(String)}
     */
    CompletableFuture<Void> deleteVertex(String key);

    /**
     * Asynchronous version of {@link ArangoVertexCollection#deleteVertex(String, VertexDeleteOptions)}
     */
    CompletableFuture<Void> deleteVertex(String key, VertexDeleteOptions options);

}
