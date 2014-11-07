/*
 * Copyright (C) 2012 tamtam180
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

import java.util.Date;
import java.util.Map;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoException;
import com.arangodb.entity.EntityFactory;
import com.arangodb.entity.KeyValueEntity;
import com.arangodb.http.HttpManager;
import com.arangodb.http.HttpResponseEntity;
import com.arangodb.util.DateUtils;
import com.arangodb.util.MapBuilder;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class InternalKVSDriverImpl extends BaseArangoDriverImpl implements com.arangodb.InternalKVSDriver {

  InternalKVSDriverImpl(ArangoConfigure configure, HttpManager httpManager) {
    super(configure , httpManager);
  }

  @Override
  public KeyValueEntity createKeyValue(
    String database,
    String collectionName, String key, Object value,
    Map<String, Object> attributes, Date expiredDate) throws ArangoException {
    
    // TODO Sanitize Key
    
    validateCollectionName(collectionName);
    HttpResponseEntity res = httpManager.doPost(
        createEndpointUrl(baseUrl, database, "/_api/key", collectionName, "/", key), 
        new MapBuilder()
          .put("x-voc-expires", expiredDate == null ? null : DateUtils.format(expiredDate, "yyyy-MM-dd'T'HH:mm:ss'Z'"))
          .put("x-voc-extended", attributes == null ? null : EntityFactory.toJsonString(attributes))
          .get(),
        null, 
        EntityFactory.toJsonString(value));
    
    try {
      KeyValueEntity entity = createEntity(res, KeyValueEntity.class);
      setKeyValueHeader(res, entity);
      return entity;
    } catch (ArangoException e) {
//      if (HttpManager.is404Error(e)) { // コレクションが存在しないか、キーが既に存在する。
//        if (mode == null || mode == Mode.RETURN_NULL) {
//          return null;
//        }
//      }
      throw e;
    }
    
  }
  
  @Override
  public KeyValueEntity updateKeyValue(
    String database,
    String collectionName, String key, Object value,
    Map<String, Object> attributes, Date expiredDate,
    boolean create
  ) throws ArangoException {

    // TODO Sanitize Key
    
    validateCollectionName(collectionName);
    HttpResponseEntity res = httpManager.doPut(
        createEndpointUrl(baseUrl, database, "/_api/key", collectionName, "/", key),
        new MapBuilder()
          .put("x-voc-expires", expiredDate == null ? null : DateUtils.format(expiredDate, "yyyy-MM-dd'T'HH:mm:ss'Z'"))
          .put("x-voc-extended", attributes == null ? null : EntityFactory.toJsonString(attributes))
          .get(),
        null, 
        EntityFactory.toJsonString(value));
    
    try {
      KeyValueEntity entity = createEntity(res, KeyValueEntity.class);
      setKeyValueHeader(res, entity);
      return entity;
    } catch (ArangoException e) {
//      if (HttpManager.is404Error(e)) { // コレクションが存在しないか、キーが既に存在する。
//        if (mode == null || mode == Mode.RETURN_NULL) {
//          return null;
//        }
//      }
      throw e;
    }

  }
  
}
