/*
 * Copyright (C) 2012,2013 tamtam180
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.orz.arangodb.impl;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.reflect.TypeToken;

import at.orz.arangodb.ArangoConfigure;
import at.orz.arangodb.ArangoException;
import at.orz.arangodb.entity.BooleanResultEntity;
import at.orz.arangodb.entity.Endpoint;
import at.orz.arangodb.entity.EntityFactory;
import at.orz.arangodb.http.HttpResponseEntity;
import at.orz.arangodb.util.MapBuilder;
import at.orz.arangodb.util.StringUtils;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * @since 1.4
 */
public class InternalEndpointDriverImpl extends BaseArangoDriverImpl {

	InternalEndpointDriverImpl(ArangoConfigure configure) {
		super(configure);
	}
	
	public BooleanResultEntity createEndpoint(String endpoint, String... databases) throws ArangoException {
		
		// TODO: validate endpoint
		
		// validate databases
		if (databases != null) {
			for (String db: databases) {
				validateDatabaseName(db, false);
			}
		}
		
		HttpResponseEntity res = httpManager.doPost(
				createEndpointUrl(baseUrl, null, "/_api/endpoint"), 
				null, 
				EntityFactory.toJsonString(
						new MapBuilder()
						.put("endpoint", endpoint)
						.put("databases", databases)
						.get()
						));
		
		return createEntity(res, BooleanResultEntity.class);
		
	}
	
	public List<Endpoint> getEndpoints() throws ArangoException {

		Type type = new TypeToken<List<Endpoint>>(){}.getType();
		HttpResponseEntity res = httpManager.doGet(createEndpointUrl(baseUrl, null, "/_api/endpoint"));
		
		// because it is not include common-attribute.
		return EntityFactory.createEntity(res.getText(), type);

	}

	public BooleanResultEntity deleteEndpoint(String endpoint) throws ArangoException {
		
		// TODO: validate endpoint
		
		HttpResponseEntity res = httpManager.doDelete(
				createEndpointUrl(baseUrl, null, "/_api/endpoint", StringUtils.encodeUrl(endpoint)),
				null
				);
		
		return createEntity(res, BooleanResultEntity.class);
		
	}
	
}
