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

package com.arangodb.internal;

import com.arangodb.ArangoDBException;
import com.arangodb.ArangoGraph;
import com.arangodb.ArangoVertexCollection;
import com.arangodb.entity.VertexEntity;
import com.arangodb.entity.VertexUpdateEntity;
import com.arangodb.model.*;

import static com.arangodb.internal.ArangoErrors.*;

/**
 * @author Mark Vollmary
 */
public class ArangoVertexCollectionImpl extends InternalArangoVertexCollection implements ArangoVertexCollection {

    private final ArangoGraph graph;

    protected ArangoVertexCollectionImpl(final ArangoGraphImpl graph, final String name) {
        super(graph, graph.db().name(), graph.name(), name);
        this.graph = graph;
    }

    @Override
    public ArangoGraph graph() {
        return graph;
    }

    @Deprecated
    @Override
    public void drop() {
        drop(new VertexCollectionDropOptions());
    }

    @Deprecated
    @Override
    public void drop(final VertexCollectionDropOptions options) {
        executorSync().execute(dropRequest(options), Void.class);
    }

    @Override
    public void remove() {
        remove(new VertexCollectionRemoveOptions());
    }

    @Override
    public void remove(final VertexCollectionRemoveOptions options) {
        executorSync().execute(removeVertexCollectionRequest(options), Void.class);
    }

    @Override
    public VertexEntity insertVertex(final Object value) {
        return executorSync().execute(insertVertexRequest(value, new VertexCreateOptions()),
                insertVertexResponseDeserializer());
    }

    @Override
    public VertexEntity insertVertex(final Object value, final VertexCreateOptions options) {
        return executorSync().execute(insertVertexRequest(value, options), insertVertexResponseDeserializer());
    }

    @Override
    public <T> T getVertex(final String key, final Class<T> type) {
        return getVertex(key, type, null);
    }

    @Override
    public <T> T getVertex(final String key, final Class<T> type, final GraphDocumentReadOptions options) {
        try {
            return executorSync().execute(getVertexRequest(key, options), getVertexResponseDeserializer(type));
        } catch (final ArangoDBException e) {
            if (matches(e, 304)
                    || matches(e, 404, ERROR_ARANGO_DOCUMENT_NOT_FOUND)
                    || matches(e, 412, ERROR_ARANGO_CONFLICT)
            ) {
                return null;
            }
            throw e;
        }
    }

    @Override
    public VertexUpdateEntity replaceVertex(final String key, final Object value) {
        return executorSync().execute(replaceVertexRequest(key, value, new VertexReplaceOptions()),
                replaceVertexResponseDeserializer());
    }

    @Override
    public VertexUpdateEntity replaceVertex(final String key, final Object value, final VertexReplaceOptions options) {
        return executorSync().execute(replaceVertexRequest(key, value, options), replaceVertexResponseDeserializer());
    }

    @Override
    public VertexUpdateEntity updateVertex(final String key, final Object value) {
        return executorSync().execute(updateVertexRequest(key, value, new VertexUpdateOptions()),
                updateVertexResponseDeserializer());
    }

    @Override
    public VertexUpdateEntity updateVertex(final String key, final Object value, final VertexUpdateOptions options) {
        return executorSync().execute(updateVertexRequest(key, value, options), updateVertexResponseDeserializer());
    }

    @Override
    public void deleteVertex(final String key) {
        executorSync().execute(deleteVertexRequest(key, new VertexDeleteOptions()), Void.class);
    }

    @Override
    public void deleteVertex(final String key, final VertexDeleteOptions options) {
        executorSync().execute(deleteVertexRequest(key, options), Void.class);
    }

}
