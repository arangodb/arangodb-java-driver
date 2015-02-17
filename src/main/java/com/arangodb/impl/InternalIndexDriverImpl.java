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

import java.util.Locale;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoException;
import com.arangodb.entity.EntityFactory;
import com.arangodb.entity.IndexEntity;
import com.arangodb.entity.IndexType;
import com.arangodb.entity.IndexesEntity;
import com.arangodb.http.HttpManager;
import com.arangodb.http.HttpResponseEntity;
import com.arangodb.util.MapBuilder;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class InternalIndexDriverImpl extends BaseArangoDriverWithCursorImpl implements com.arangodb.InternalIndexDriver {

  InternalIndexDriverImpl(ArangoConfigure configure, HttpManager httpManager) {
    super(configure , null,  httpManager);
  }

  @Override
  public IndexEntity createIndex(String database, String collectionName, IndexType type, boolean unique, boolean sparse, String... fields) throws ArangoException {

    if (type == IndexType.PRIMARY) {
      throw new IllegalArgumentException("cannot create primary index.");
    }
    if (type == IndexType.EDGE) {
      throw new IllegalArgumentException("cannot create edge index.");
    }
    if (type == IndexType.CAP) {
      throw new IllegalArgumentException("cannot create cap index. use createCappedIndex.");
    }
    
    validateCollectionName(collectionName);
    HttpResponseEntity res = httpManager.doPost(
        createEndpointUrl(baseUrl, database, "/_api/index"), 
        new MapBuilder("collection", collectionName).get(),
        EntityFactory.toJsonString(
            new MapBuilder()
            .put("type", type.name().toLowerCase(Locale.US))
            .put("unique", unique)
            .put("sparse", sparse)
            .put("fields", fields)
            .get()));
    
    // HTTP:200,201,404
    
    try {
      IndexEntity entity = createEntity(res, IndexEntity.class);
      return entity;
    } catch (ArangoException e) {
      throw e;
    }
    
  }
  
  
  @Override
  public IndexEntity createIndex(String database, String collectionName, IndexType type, boolean unique, String... fields) throws ArangoException {
    return createIndex(database, collectionName, type, unique, false, fields);
  }

  @Override
  public IndexEntity createCappedIndex(String database, String collectionName, int size) throws ArangoException {
    
    validateCollectionName(collectionName);
    HttpResponseEntity res = httpManager.doPost(
        createEndpointUrl(baseUrl, database, "/_api/index"), 
        new MapBuilder("collection", collectionName).get(),
        EntityFactory.toJsonString(
            new MapBuilder()
            .put("type", IndexType.CAP.name().toLowerCase(Locale.US))
            .put("size", size)
            .get()));
    
    // HTTP:200,201,404
    
    try {
      IndexEntity entity = createEntity(res, IndexEntity.class);
      return entity;
    } catch (ArangoException e) {
      throw e;
    }
    
  }

  @Override
  public IndexEntity createCappedByDocumentSizeIndex(String database, String collectionName, int byteSize) throws ArangoException {

    validateCollectionName(collectionName);
    HttpResponseEntity res = httpManager.doPost(
      createEndpointUrl(baseUrl, database, "/_api/index"),
      new MapBuilder("collection", collectionName).get(),
      EntityFactory.toJsonString(
        new MapBuilder()
          .put("type", IndexType.CAP.name().toLowerCase(Locale.US))
          .put("byteSize", byteSize)
          .get()));

    // HTTP:200,201,404

    try {
      IndexEntity entity = createEntity(res, IndexEntity.class);
      return entity;
    } catch (ArangoException e) {
      throw e;
    }

  }
  
  @Override
  public IndexEntity createFulltextIndex(String database, String collectionName, Integer minLength, String... fields) throws ArangoException {

    validateCollectionName(collectionName);
    HttpResponseEntity res = httpManager.doPost(
        createEndpointUrl(baseUrl, database, "/_api/index"), 
        new MapBuilder("collection", collectionName).get(),
        EntityFactory.toJsonString(
            new MapBuilder()
            .put("type", IndexType.FULLTEXT.name().toLowerCase(Locale.US))
            .put("minLength", minLength)
            .put("fields", fields)
            .get()));
    
    try {
      IndexEntity entity = createEntity(res, IndexEntity.class);
      return entity;
    } catch (ArangoException e) {
      throw e;
    }
    
  }
  
  @Override
  public IndexEntity deleteIndex(String database, String indexHandle) throws ArangoException {
    
    validateDocumentHandle(indexHandle); // 書式同じなので
    HttpResponseEntity res = httpManager.doDelete(
        createEndpointUrl(baseUrl, database, "/_api/index", indexHandle), 
        null);
    
    try {
      IndexEntity entity = createEntity(res, IndexEntity.class);
      return entity;
    } catch (ArangoException e) {
      throw e;
    }
    
  }

  @Override
  public IndexEntity getIndex(String database, String indexHandle) throws ArangoException {
    
    validateDocumentHandle(indexHandle);
    HttpResponseEntity res = httpManager.doGet(
        createEndpointUrl(baseUrl, database, "/_api/index", indexHandle));
    
    try {
      IndexEntity entity = createEntity(res, IndexEntity.class);
      return entity;
    } catch (ArangoException e) {
      throw e;
    }
    
  }

  @Override
  public IndexesEntity getIndexes(String database, String collectionName) throws ArangoException {
    
    validateCollectionName(collectionName);
    HttpResponseEntity res = httpManager.doGet(
        createEndpointUrl(baseUrl, database, "/_api/index"),
        new MapBuilder("collection", collectionName).get());
    
    try {
      IndexesEntity entity = createEntity(res, IndexesEntity.class);
      return entity;
    } catch (ArangoException e) {
      throw e;
    }
    
  }
  
}
