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

import java.util.Map;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoException;
import com.arangodb.InternalCursorDriver;
import com.arangodb.entity.EdgeEntity;
import com.arangodb.entity.EntityFactory;
import com.arangodb.http.HttpManager;
import com.arangodb.http.HttpResponseEntity;
import com.arangodb.util.MapBuilder;

/**
 * @author gschwab
 */
public class InternalEdgeDriverImpl extends BaseArangoDriverWithCursorImpl implements com.arangodb.InternalEdgeDriver {

  InternalEdgeDriverImpl(ArangoConfigure configure, InternalCursorDriver cursorDriver, HttpManager httpManager) {
    super(configure, cursorDriver, httpManager);
  }

  @Override
  public <T> EdgeEntity<T> createEdge(
      String databaseName,
      String collectionName,
      Object object,
      String from,
      String to,
      Boolean createCollection,
      Boolean waitForSync) throws ArangoException {
    
    Map<String, Object> params = 
        new MapBuilder()
    .put("collection", collectionName)
    .put("from", from)
    .put("to", to)
    .put("createCollection", createCollection)
    .put("waitForSync", waitForSync)
    .get(); 

    String body = EntityFactory.toJsonString(object);
    
    HttpResponseEntity response = httpManager.doPost(
        createEndpointUrl(baseUrl, databaseName, "/_api/edge"),
        params,
        body);
    
    EdgeEntity<T> edgeEntity = createEntity(response, EdgeEntity.class);
    edgeEntity.setEntity((T) object);
    return edgeEntity;
  }

}
