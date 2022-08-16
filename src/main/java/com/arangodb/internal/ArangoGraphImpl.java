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
import com.arangodb.ArangoEdgeCollection;
import com.arangodb.ArangoGraph;
import com.arangodb.ArangoVertexCollection;
import com.arangodb.entity.EdgeDefinition;
import com.arangodb.entity.GraphEntity;
import com.arangodb.model.GraphCreateOptions;
import com.arangodb.model.VertexCollectionCreateOptions;

import java.util.Collection;

/**
 * @author Mark Vollmary
 */
public class ArangoGraphImpl extends InternalArangoGraph<ArangoDBImpl, ArangoDatabaseImpl, ArangoExecutorSync>
        implements ArangoGraph {

    protected ArangoGraphImpl(final ArangoDatabaseImpl db, final String name) {
        super(db, name);
    }

    @Override
    public boolean exists() {
        try {
            getInfo();
            return true;
        } catch (final ArangoDBException e) {
            if (ArangoErrors.ERROR_GRAPH_NOT_FOUND.equals(e.getErrorNum())) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public GraphEntity create(final Collection<EdgeDefinition> edgeDefinitions) {
        return db().createGraph(name(), edgeDefinitions);
    }

    @Override
    public GraphEntity create(final Collection<EdgeDefinition> edgeDefinitions, final GraphCreateOptions options) {
        return db().createGraph(name(), edgeDefinitions, options);
    }

    @Override
    public void drop() {
        executor.execute(dropRequest(), Void.class);
    }

    @Override
    public void drop(final boolean dropCollections) {
        executor.execute(dropRequest(dropCollections), Void.class);
    }

    @Override
    public GraphEntity getInfo() {
        return executor.execute(getInfoRequest(), getInfoResponseDeserializer());
    }

    @Override
    public Collection<String> getVertexCollections() {
        return executor.execute(getVertexCollectionsRequest(), getVertexCollectionsResponseDeserializer());
    }

    @Override
    public GraphEntity addVertexCollection(final String name) {
        return addVertexCollection(name, new VertexCollectionCreateOptions());
    }

    @Override
    public GraphEntity addVertexCollection(final String name, final VertexCollectionCreateOptions options) {
        return executor.execute(addVertexCollectionRequest(name, options), addVertexCollectionResponseDeserializer());
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
        return executor.execute(getEdgeDefinitionsRequest(), getEdgeDefinitionsDeserializer());
    }

    @Override
    public GraphEntity addEdgeDefinition(final EdgeDefinition definition) {
        return executor.execute(addEdgeDefinitionRequest(definition), addEdgeDefinitionResponseDeserializer());
    }

    @Override
    public GraphEntity replaceEdgeDefinition(final EdgeDefinition definition) {
        return executor.execute(replaceEdgeDefinitionRequest(definition), replaceEdgeDefinitionResponseDeserializer());
    }

    @Override
    public GraphEntity removeEdgeDefinition(final String definitionName) {
        return executor.execute(removeEdgeDefinitionRequest(definitionName),
                removeEdgeDefinitionResponseDeserializer());
    }

}
