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

import java.util.Collection;

import at.orz.arangodb.ArangoConfigure;
import at.orz.arangodb.ArangoException;
import at.orz.arangodb.entity.EntityFactory;
import at.orz.arangodb.entity.ImportResultEntity;
import at.orz.arangodb.http.HttpResponseEntity;
import at.orz.arangodb.util.MapBuilder;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * @see http://www.arangodb.org/manuals/current/HttpImport.html
 */
public class InternalImportDriverImpl extends BaseArangoDriverImpl {

	InternalImportDriverImpl(ArangoConfigure configure) {
		super(configure);
	}
	
	public ImportResultEntity importDocuments(String database, String collection, Boolean createCollection, Collection<?> values) throws ArangoException {

		HttpResponseEntity res = httpManager.doPost(
				createEndpointUrl(baseUrl, database, "/_api/import"), 
				new MapBuilder().put("collection", collection).put("createCollection", createCollection).put("type", "array").get(), 
				EntityFactory.toJsonString(values));
		
		return createEntity(res, ImportResultEntity.class);
		
	}

//	public void importDocuments(String collection, Boolean createCollection, Iterator<?> itr) throws ArangoException {
//
//		HttpResponseEntity res = httpManager.doPost(
//				baseUrl + "/_api/import", 
//				new MapBuilder().put("collection", collection).put("createCollection", createCollection).put("type", "documents").get(), 
//				EntityFactory.toJsonSequenceEntity(itr));
//		
//	}
	
	
	public ImportResultEntity importDocumentsByHeaderValues(String database, String collection, Boolean createCollection, Collection<? extends Collection<?>> headerValues) throws ArangoException {

		HttpResponseEntity res = httpManager.doPost(
				createEndpointUrl(baseUrl, database,  "/_api/import"), 
				new MapBuilder().put("collection", collection).put("createCollection", createCollection).get(), 
				EntityFactory.toImportHeaderValues(headerValues));
		
		return createEntity(res, ImportResultEntity.class);
		
	}

}
