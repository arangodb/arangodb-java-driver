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

import com.arangodb.*;
import com.arangodb.entity.EdgeDefinition;
import com.arangodb.entity.GraphEntity;
import com.arangodb.model.GraphCreateOptions;
import com.arangodb.model.ReplaceEdgeDefinitionOptions;
import com.arangodb.model.VertexCollectionCreateOptions;

import java.util.Collection;

import static com.arangodb.internal.ArangoErrors.ERROR_GRAPH_NOT_FOUND;
import static com.arangodb.internal.ArangoErrors.matches;

/**
 * @author Mark Vollmary
 */
public class ArangoGraphImpl extends InternalArangoGraph implements ArangoGraph {

    private final ArangoDatabase db;

    protected ArangoGraphImpl(final ArangoDatabaseImpl db, final String name) {
        super(db, db.name(), name);
        this.db = db;
    }

    @Override
    public ArangoDatabase db() {
        return db;
    }

    @Override
    public boolean exists() {
        try {
            getInfo();
            return true;
        } catch (final ArangoDBException e) {
            if (matches(e, 404, ERROR_GRAPH_NOT_FOUND)) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public GraphEntity create(final Iterable<EdgeDefinition> edgeDefinitions) {
        return db().createGraph(name(), edgeDefinitions);
    }

    @Override
    public GraphEntity create(final Iterable<EdgeDefinition> edgeDefinitions, final GraphCreateOptions options) {
        return db().createGraph(name(), edgeDefinitions, options);
    }

    @Override
    public void drop() {
        executorSync().execute(dropRequest(), Void.class);
    }

    @Override
    public void drop(final boolean dropCollections) {
        executorSync().execute(dropRequest(dropCollections), Void.class);
    }

    @Override
    public GraphEntity getInfo() {
        return executorSync().execute(getInfoRequest(), getInfoResponseDeserializer());
    }

    @Override
    public Collection<String> getVertexCollections() {
        return executorSync().execute(getVertexCollectionsRequest(), getVertexCollectionsResponseDeserializer());
    }

    @Override
    public GraphEntity addVertexCollection(final String name) {
        return addVertexCollection(name, new VertexCollectionCreateOptions());
    }

    @Override
    public GraphEntity addVertexCollection(final String name, final VertexCollectionCreateOptions options) {
        return executorSync().execute(addVertexCollectionRequest(name, options), addVertexCollectionResponseDeserializer());
    }

    @Override
    public ArangoVertexCollection vertexCollection(final String name) {
        return new ArangoVertexCollectionImpl(this, name);
    }

    @Override
    public ArangoEdgeCollection edgeCollection(final String name) {
        return new ArangoEdgeCollectionImpl(this, name);
    }

    @Override
    public Collection<String> getEdgeDefinitions() {
        return executorSync().execute(getEdgeDefinitionsRequest(), getEdgeDefinitionsDeserializer());
    }

    @Override
    public GraphEntity addEdgeDefinition(final EdgeDefinition definition) {
        return executorSync().execute(addEdgeDefinitionRequest(definition), addEdgeDefinitionResponseDeserializer());
    }

    @Override
    public GraphEntity replaceEdgeDefinition(final EdgeDefinition definition) {
        return replaceEdgeDefinition(definition, new ReplaceEdgeDefinitionOptions());
    }

    @Override
    public GraphEntity replaceEdgeDefinition(final EdgeDefinition definition, final ReplaceEdgeDefinitionOptions options) {
        return executorSync().execute(replaceEdgeDefinitionRequest(definition, options), replaceEdgeDefinitionResponseDeserializer());
    }

}
