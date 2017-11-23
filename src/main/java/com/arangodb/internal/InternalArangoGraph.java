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

import java.util.Collection;

import com.arangodb.entity.EdgeDefinition;
import com.arangodb.entity.GraphEntity;
import com.arangodb.internal.ArangoExecutor.ResponseDeserializer;
import com.arangodb.internal.velocystream.internal.VstConnection;
import com.arangodb.model.OptionsBuilder;
import com.arangodb.model.VertexCollectionCreateOptions;
import com.arangodb.velocypack.Type;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;
import com.arangodb.velocystream.Response;

/**
 * @author Mark Vollmary
 *
 */
public class InternalArangoGraph<A extends InternalArangoDB<E, R, C>, D extends InternalArangoDatabase<A, E, R, C>, E extends ArangoExecutor, R, C extends VstConnection>
		extends ArangoExecuteable<E, R, C> {

	private final D db;
	private final String name;

	public InternalArangoGraph(final D db, final String name) {
		super(db.executor(), db.util());
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
		return new Request(db.name(), RequestType.DELETE,
				executor.createPath(ArangoDBConstants.PATH_API_GHARIAL, name));
	}

	protected Request getInfoRequest() {
		return new Request(db.name(), RequestType.GET, executor.createPath(ArangoDBConstants.PATH_API_GHARIAL, name));
	}

	protected ResponseDeserializer<GraphEntity> getInfoResponseDeserializer() {
		return addVertexCollectionResponseDeserializer();
	}

	protected Request getVertexCollectionsRequest() {
		return new Request(db.name(), RequestType.GET,
				executor.createPath(ArangoDBConstants.PATH_API_GHARIAL, name, ArangoDBConstants.VERTEX));
	}

	protected ResponseDeserializer<Collection<String>> getVertexCollectionsResponseDeserializer() {
		return new ResponseDeserializer<Collection<String>>() {
			@Override
			public Collection<String> deserialize(final Response response) throws VPackException {
				return util().deserialize(response.getBody().get(ArangoDBConstants.COLLECTIONS),
					new Type<Collection<String>>() {
					}.getType());
			}
		};
	}

	protected Request addVertexCollectionRequest(final String name) {
		final Request request = new Request(db.name(), RequestType.POST,
				executor.createPath(ArangoDBConstants.PATH_API_GHARIAL, name(), ArangoDBConstants.VERTEX));
		request.setBody(util().serialize(OptionsBuilder.build(new VertexCollectionCreateOptions(), name)));
		return request;
	}

	protected ResponseDeserializer<GraphEntity> addVertexCollectionResponseDeserializer() {
		return addEdgeDefinitionResponseDeserializer();
	}

	protected Request getEdgeDefinitionsRequest() {
		return new Request(db.name(), RequestType.GET,
				executor.createPath(ArangoDBConstants.PATH_API_GHARIAL, name, ArangoDBConstants.EDGE));
	}

	protected ResponseDeserializer<Collection<String>> getEdgeDefinitionsDeserializer() {
		return new ResponseDeserializer<Collection<String>>() {
			@Override
			public Collection<String> deserialize(final Response response) throws VPackException {
				return util().deserialize(response.getBody().get(ArangoDBConstants.COLLECTIONS),
					new Type<Collection<String>>() {
					}.getType());
			}
		};
	}

	protected Request addEdgeDefinitionRequest(final EdgeDefinition definition) {
		final Request request = new Request(db.name(), RequestType.POST,
				executor.createPath(ArangoDBConstants.PATH_API_GHARIAL, name, ArangoDBConstants.EDGE));
		request.setBody(util().serialize(definition));
		return request;
	}

	protected ResponseDeserializer<GraphEntity> addEdgeDefinitionResponseDeserializer() {
		return new ResponseDeserializer<GraphEntity>() {
			@Override
			public GraphEntity deserialize(final Response response) throws VPackException {
				return util().deserialize(response.getBody().get(ArangoDBConstants.GRAPH), GraphEntity.class);
			}
		};
	}

	protected Request replaceEdgeDefinitionRequest(final EdgeDefinition definition) {
		final Request request = new Request(db.name(), RequestType.PUT, executor.createPath(
			ArangoDBConstants.PATH_API_GHARIAL, name, ArangoDBConstants.EDGE, definition.getCollection()));
		request.setBody(util().serialize(definition));
		return request;
	}

	protected ResponseDeserializer<GraphEntity> replaceEdgeDefinitionResponseDeserializer() {
		return new ResponseDeserializer<GraphEntity>() {
			@Override
			public GraphEntity deserialize(final Response response) throws VPackException {
				return util().deserialize(response.getBody().get(ArangoDBConstants.GRAPH), GraphEntity.class);
			}
		};
	}

	protected Request removeEdgeDefinitionRequest(final String definitionName) {
		return new Request(db.name(), RequestType.DELETE,
				executor.createPath(ArangoDBConstants.PATH_API_GHARIAL, name, ArangoDBConstants.EDGE, definitionName));
	}

	protected ResponseDeserializer<GraphEntity> removeEdgeDefinitionResponseDeserializer() {
		return new ResponseDeserializer<GraphEntity>() {
			@Override
			public GraphEntity deserialize(final Response response) throws VPackException {
				return util().deserialize(response.getBody().get(ArangoDBConstants.GRAPH), GraphEntity.class);
			}
		};
	}

}
