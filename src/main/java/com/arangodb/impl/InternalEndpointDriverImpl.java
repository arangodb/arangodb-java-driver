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

package com.arangodb.impl;

import java.lang.reflect.Type;
import java.util.List;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoException;
import com.arangodb.entity.BooleanResultEntity;
import com.arangodb.entity.Endpoint;
import com.arangodb.entity.EntityFactory;
import com.arangodb.http.HttpManager;
import com.arangodb.http.HttpResponseEntity;
import com.arangodb.util.MapBuilder;
import com.arangodb.util.StringUtils;
import com.google.gson.reflect.TypeToken;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * @since 1.4
 */
public class InternalEndpointDriverImpl extends BaseArangoDriverImpl implements com.arangodb.InternalEndpointDriver {

  InternalEndpointDriverImpl(ArangoConfigure configure, HttpManager httpManager) {
    super(configure , httpManager);
  }
  
  @Override
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
  
  @Override
  public List<Endpoint> getEndpoints() throws ArangoException {

    Type type = new TypeToken<List<Endpoint>>(){}.getType();
    HttpResponseEntity res = httpManager.doGet(createEndpointUrl(baseUrl, null, "/_api/endpoint"));
    
    // because it is not include common-attribute.
    return EntityFactory.createEntity(res.getText(), type);

  }

  @Override
  public BooleanResultEntity deleteEndpoint(String endpoint) throws ArangoException {
    
    // TODO: validate endpoint
    
    HttpResponseEntity res = httpManager.doDelete(
        createEndpointUrl(baseUrl, null, "/_api/endpoint", StringUtils.encodeUrl(endpoint)),
        null
        );
    
    return createEntity(res, BooleanResultEntity.class);
    
  }
  
}
