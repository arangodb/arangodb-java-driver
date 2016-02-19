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

import java.util.Collection;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoException;
import com.arangodb.entity.EntityFactory;
import com.arangodb.entity.ImportResultEntity;
import com.arangodb.http.HttpManager;
import com.arangodb.http.HttpResponseEntity;
import com.arangodb.util.MapBuilder;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * @see <a href=
 *      "https://docs.arangodb.com/HttpBulkImports/ImportingSelfContained.html">
 *      HttpBulkImports documentation</a>
 */
public class InternalImportDriverImpl extends BaseArangoDriverImpl implements com.arangodb.InternalImportDriver {

	InternalImportDriverImpl(ArangoConfigure configure, HttpManager httpManager) {
		super(configure, httpManager);
	}

	@Override
	public ImportResultEntity importDocuments(
		String database,
		String collection,
		Boolean createCollection,
		Collection<?> values) throws ArangoException {

		HttpResponseEntity res = httpManager.doPost(
			createEndpointUrl(database, "/_api/import"), new MapBuilder().put("collection", collection)
					.put("createCollection", createCollection).put("type", "array").get(),
			EntityFactory.toJsonString(values));

		return createEntity(res, ImportResultEntity.class);

	}

	@Override
	public ImportResultEntity importDocumentsByHeaderValues(
		String database,
		String collection,
		Boolean createCollection,
		Collection<? extends Collection<?>> headerValues) throws ArangoException {

		HttpResponseEntity res = httpManager.doPost(createEndpointUrl(database, "/_api/import"),
			new MapBuilder().put("collection", collection).put("createCollection", createCollection).get(),
			EntityFactory.toImportHeaderValues(headerValues));

		return createEntity(res, ImportResultEntity.class);

	}

}
