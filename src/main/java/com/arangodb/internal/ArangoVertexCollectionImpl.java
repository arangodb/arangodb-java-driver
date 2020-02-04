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
import com.arangodb.ArangoVertexCollection;
import com.arangodb.entity.VertexEntity;
import com.arangodb.entity.VertexUpdateEntity;
import com.arangodb.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mark Vollmary
 */
public class ArangoVertexCollectionImpl
        extends InternalArangoVertexCollection<ArangoDBImpl, ArangoDatabaseImpl, ArangoGraphImpl, ArangoExecutorSync>
        implements ArangoVertexCollection {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArangoVertexCollectionImpl.class);

    protected ArangoVertexCollectionImpl(final ArangoGraphImpl graph, final String name) {
        super(graph, name);
    }

    @Override
    public void drop() throws ArangoDBException {
        executor.execute(dropRequest(), Void.class);
    }

    @Override
    public <T> VertexEntity insertVertex(final T value) throws ArangoDBException {
        return executor.execute(insertVertexRequest(value, new VertexCreateOptions()),
                insertVertexResponseDeserializer(value));
    }

    @Override
    public <T> VertexEntity insertVertex(final T value, final VertexCreateOptions options) throws ArangoDBException {
        return executor.execute(insertVertexRequest(value, options), insertVertexResponseDeserializer(value));
    }

    @Override
    public <T> T getVertex(final String key, final Class<T> type) throws ArangoDBException {
        try {
            return executor.execute(getVertexRequest(key, new GraphDocumentReadOptions()),
                    getVertexResponseDeserializer(type));
        } catch (final ArangoDBException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(e.getMessage(), e);
            }
            return null;
        }
    }

    @Override
    public <T> T getVertex(final String key, final Class<T> type, final GraphDocumentReadOptions options)
            throws ArangoDBException {
        try {
            return executor.execute(getVertexRequest(key, options), getVertexResponseDeserializer(type));
        } catch (final ArangoDBException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(e.getMessage(), e);
            }
            return null;
        }
    }

    @Override
    public <T> VertexUpdateEntity replaceVertex(final String key, final T value) throws ArangoDBException {
        return executor.execute(replaceVertexRequest(key, value, new VertexReplaceOptions()),
                replaceVertexResponseDeserializer(value));
    }

    @Override
    public <T> VertexUpdateEntity replaceVertex(final String key, final T value, final VertexReplaceOptions options)
            throws ArangoDBException {
        return executor.execute(replaceVertexRequest(key, value, options), replaceVertexResponseDeserializer(value));
    }

    @Override
    public <T> VertexUpdateEntity updateVertex(final String key, final T value) throws ArangoDBException {
        return executor.execute(updateVertexRequest(key, value, new VertexUpdateOptions()),
                updateVertexResponseDeserializer(value));
    }

    @Override
    public <T> VertexUpdateEntity updateVertex(final String key, final T value, final VertexUpdateOptions options)
            throws ArangoDBException {
        return executor.execute(updateVertexRequest(key, value, options), updateVertexResponseDeserializer(value));
    }

    @Override
    public void deleteVertex(final String key) throws ArangoDBException {
        executor.execute(deleteVertexRequest(key, new VertexDeleteOptions()), Void.class);
    }

    @Override
    public void deleteVertex(final String key, final VertexDeleteOptions options) throws ArangoDBException {
        executor.execute(deleteVertexRequest(key, options), Void.class);
    }

}
