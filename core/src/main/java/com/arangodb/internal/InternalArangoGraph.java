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
import com.arangodb.model.ReplaceEdgeDefinitionOptions;
import com.arangodb.model.VertexCollectionCreateOptions;

import java.util.Collection;

import static com.arangodb.internal.serde.SerdeUtils.constructListType;

/**
 * @author Mark Vollmary
 */
public abstract class InternalArangoGraph extends ArangoExecuteable {

    protected static final String PATH_API_GHARIAL = "/_api/gharial";
    private static final String GRAPH = "/graph";
    private static final String VERTEX = "vertex";
    private static final String EDGE = "edge";

    protected final String dbName;
    protected final String name;

    protected InternalArangoGraph(final ArangoExecuteable executeable, final String dbName, final String name) {
        super(executeable);
        this.dbName = dbName;
        this.name = name;
    }

    public String name() {
        return name;
    }

    protected InternalRequest dropRequest() {
        return dropRequest(false);
    }

    protected InternalRequest dropRequest(final boolean dropCollections) {
        final InternalRequest request = request(dbName, RequestType.DELETE, PATH_API_GHARIAL, name);
        if (dropCollections) {
            request.putQueryParam("dropCollections", true);
        }
        return request;
    }

    protected InternalRequest getInfoRequest() {
        return request(dbName, RequestType.GET, PATH_API_GHARIAL, name);
    }

    protected ResponseDeserializer<GraphEntity> getInfoResponseDeserializer() {
        return addVertexCollectionResponseDeserializer();
    }

    protected InternalRequest getVertexCollectionsRequest() {
        return request(dbName, RequestType.GET, PATH_API_GHARIAL, name, VERTEX);
    }

    protected ResponseDeserializer<Collection<String>> getVertexCollectionsResponseDeserializer() {
        return (response, ctx) -> getSerde().deserialize(response.getBody(), "/collections",
                constructListType(String.class), ctx);
    }

    protected InternalRequest addVertexCollectionRequest(final String name, final VertexCollectionCreateOptions options) {
        final InternalRequest request = request(dbName, RequestType.POST, PATH_API_GHARIAL, name(), VERTEX);
        request.setBody(getSerde().serialize(OptionsBuilder.build(options, name)));
        return request;
    }

    protected ResponseDeserializer<GraphEntity> addVertexCollectionResponseDeserializer() {
        return addEdgeDefinitionResponseDeserializer();
    }

    protected InternalRequest getEdgeDefinitionsRequest() {
        return request(dbName, RequestType.GET, PATH_API_GHARIAL, name, EDGE);
    }

    protected ResponseDeserializer<Collection<String>> getEdgeDefinitionsDeserializer() {
        return (response, ctx) -> getSerde().deserialize(response.getBody(), "/collections",
                constructListType(String.class), ctx);
    }

    protected InternalRequest addEdgeDefinitionRequest(final EdgeDefinition definition) {
        final InternalRequest request = request(dbName, RequestType.POST, PATH_API_GHARIAL, name, EDGE);
        request.setBody(getSerde().serialize(definition));
        return request;
    }

    protected ResponseDeserializer<GraphEntity> addEdgeDefinitionResponseDeserializer() {
        return (response, ctx) -> getSerde().deserialize(response.getBody(), GRAPH, GraphEntity.class, ctx);
    }

    protected InternalRequest replaceEdgeDefinitionRequest(final EdgeDefinition definition, final ReplaceEdgeDefinitionOptions options) {
        final InternalRequest request =
                request(dbName, RequestType.PUT, PATH_API_GHARIAL, name, EDGE, definition.getCollection())
                        .putQueryParam("waitForSync", options.getWaitForSync())
                        .putQueryParam("dropCollections", options.getDropCollections());
        request.setBody(getSerde().serialize(definition));
        return request;
    }

    protected ResponseDeserializer<GraphEntity> replaceEdgeDefinitionResponseDeserializer() {
        return (response, ctx) -> getSerde().deserialize(response.getBody(), GRAPH, GraphEntity.class, ctx);
    }

}
