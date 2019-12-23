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

import com.arangodb.entity.EdgeDefinition;
import com.arangodb.entity.GraphEntity;
import com.arangodb.internal.ArangoExecutor.ResponseDeserializer;
import com.arangodb.model.OptionsBuilder;
import com.arangodb.model.VertexCollectionCreateOptions;
import com.arangodb.velocypack.Type;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;

import java.util.Collection;

/**
 * @author Mark Vollmary
 */
public abstract class InternalArangoGraph<A extends InternalArangoDB<E>, D extends InternalArangoDatabase<A, E>, E extends ArangoExecutor>
        extends ArangoExecuteable<E> {

    protected static final String PATH_API_GHARIAL = "/_api/gharial";
    private static final String GRAPH = "graph";
    private static final String VERTEX = "vertex";
    private static final String EDGE = "edge";

    private final D db;
    private final String name;

    protected InternalArangoGraph(final D db, final String name) {
        super(db.executor, db.util, db.context);
        this.db = db;
        this.name = name;
    }

    public D db() {
        return db;
    }

    public String name() {
        return name;
    }

    protected Request dropRequest() {
        return dropRequest(false);
    }

    protected Request dropRequest(final boolean dropCollections) {
        final Request request = request(db.name(), RequestType.DELETE, PATH_API_GHARIAL, name);
        if (dropCollections) {
            request.putQueryParam("dropCollections", true);
        }
        return request;
    }

    protected Request getInfoRequest() {
        return request(db.name(), RequestType.GET, PATH_API_GHARIAL, name);
    }

    protected ResponseDeserializer<GraphEntity> getInfoResponseDeserializer() {
        return addVertexCollectionResponseDeserializer();
    }

    protected Request getVertexCollectionsRequest() {
        return request(db.name(), RequestType.GET, PATH_API_GHARIAL, name, VERTEX);
    }

    protected ResponseDeserializer<Collection<String>> getVertexCollectionsResponseDeserializer() {
        return response -> util().deserialize(response.getBody().get("collections"), new Type<Collection<String>>() {
        }.getType());
    }

    protected Request addVertexCollectionRequest(final String name) {
        final Request request = request(db.name(), RequestType.POST, PATH_API_GHARIAL, name(), VERTEX);
        request.setBody(util().serialize(OptionsBuilder.build(new VertexCollectionCreateOptions(), name)));
        return request;
    }

    protected ResponseDeserializer<GraphEntity> addVertexCollectionResponseDeserializer() {
        return addEdgeDefinitionResponseDeserializer();
    }

    protected Request getEdgeDefinitionsRequest() {
        return request(db.name(), RequestType.GET, PATH_API_GHARIAL, name, EDGE);
    }

    protected ResponseDeserializer<Collection<String>> getEdgeDefinitionsDeserializer() {
        return response -> util().deserialize(response.getBody().get("collections"), new Type<Collection<String>>() {
        }.getType());
    }

    protected Request addEdgeDefinitionRequest(final EdgeDefinition definition) {
        final Request request = request(db.name(), RequestType.POST, PATH_API_GHARIAL, name, EDGE);
        request.setBody(util().serialize(definition));
        return request;
    }

    protected ResponseDeserializer<GraphEntity> addEdgeDefinitionResponseDeserializer() {
        return response -> util().deserialize(response.getBody().get(GRAPH), GraphEntity.class);
    }

    protected Request replaceEdgeDefinitionRequest(final EdgeDefinition definition) {
        final Request request = request(db.name(), RequestType.PUT, PATH_API_GHARIAL, name, EDGE,
                definition.getCollection());
        request.setBody(util().serialize(definition));
        return request;
    }

    protected ResponseDeserializer<GraphEntity> replaceEdgeDefinitionResponseDeserializer() {
        return response -> util().deserialize(response.getBody().get(GRAPH), GraphEntity.class);
    }

    protected Request removeEdgeDefinitionRequest(final String definitionName) {
        return request(db.name(), RequestType.DELETE, PATH_API_GHARIAL, name, EDGE, definitionName);
    }

    protected ResponseDeserializer<GraphEntity> removeEdgeDefinitionResponseDeserializer() {
        return response -> util().deserialize(response.getBody().get(GRAPH), GraphEntity.class);
    }

}
