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
import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous version of {@link ArangoEdgeCollection}
 */
@ThreadSafe
public interface ArangoEdgeCollectionAsync extends ArangoSerdeAccessor {

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
     * Asynchronous version of {@link ArangoEdgeCollection#drop()}
     *
     * @deprecated use {@link #remove()} instead
     */
    @Deprecated
    CompletableFuture<Void> drop();

    /**
     * Asynchronous version of {@link ArangoEdgeCollection#drop(EdgeCollectionDropOptions)}
     *
     * @deprecated use {@link #remove(EdgeCollectionRemoveOptions)} instead
     */
    @Deprecated
    CompletableFuture<Void> drop(EdgeCollectionDropOptions options);

    /**
     * Asynchronous version of {@link ArangoEdgeCollection#remove()}
     */
    CompletableFuture<Void> remove();

    /**
     * Asynchronous version of {@link ArangoEdgeCollection#remove(EdgeCollectionRemoveOptions)}
     */
    CompletableFuture<Void> remove(EdgeCollectionRemoveOptions options);

    /**
     * Asynchronous version of {@link ArangoEdgeCollection#insertEdge(Object)}
     */
    CompletableFuture<EdgeEntity> insertEdge(Object value);

    /**
     * Asynchronous version of {@link ArangoEdgeCollection#insertEdge(Object, EdgeCreateOptions)}
     */
    CompletableFuture<EdgeEntity> insertEdge(Object value, EdgeCreateOptions options);

    /**
     * Asynchronous version of {@link ArangoEdgeCollection#getEdge(String, Class)}
     */
    <T> CompletableFuture<T> getEdge(String key, Class<T> type);

    /**
     * Asynchronous version of {@link ArangoEdgeCollection#getEdge(String, Class, GraphDocumentReadOptions)}
     */
    <T> CompletableFuture<T> getEdge(String key, Class<T> type, GraphDocumentReadOptions options);

    /**
     * Asynchronous version of {@link ArangoEdgeCollection#replaceEdge(String, Object)}
     */
    CompletableFuture<EdgeUpdateEntity> replaceEdge(String key, Object value);

    /**
     * Asynchronous version of {@link ArangoEdgeCollection#replaceEdge(String, Object, EdgeReplaceOptions)}
     */
    CompletableFuture<EdgeUpdateEntity> replaceEdge(String key, Object value, EdgeReplaceOptions options);

    /**
     * Asynchronous version of {@link ArangoEdgeCollection#updateEdge(String, Object)}
     */
    CompletableFuture<EdgeUpdateEntity> updateEdge(String key, Object value);

    /**
     * Asynchronous version of {@link ArangoEdgeCollection#updateEdge(String, Object, EdgeUpdateOptions)}
     */
    CompletableFuture<EdgeUpdateEntity> updateEdge(String key, Object value, EdgeUpdateOptions options);

    /**
     * Asynchronous version of {@link ArangoEdgeCollection#deleteEdge(String)}
     */
    CompletableFuture<Void> deleteEdge(String key);

    /**
     * Asynchronous version of {@link ArangoEdgeCollection#deleteEdge(String, EdgeDeleteOptions)}
     */
    CompletableFuture<Void> deleteEdge(String key, EdgeDeleteOptions options);

}
