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

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoException;
import com.arangodb.entity.DefaultEntity;
import com.arangodb.entity.EntityFactory;
import com.arangodb.entity.QueryCachePropertiesEntity;
import com.arangodb.http.HttpManager;
import com.arangodb.http.HttpResponseEntity;

/**
 * @author a-brandt
 * 
 */
public class InternalQueryCacheDriverImpl extends BaseArangoDriverImpl
		implements com.arangodb.InternalQueryCacheDriver {

	InternalQueryCacheDriverImpl(final ArangoConfigure configure, final HttpManager httpManager) {
		super(configure, httpManager);
	}

	@Override
	public DefaultEntity deleteQueryCache(final String database) throws ArangoException {

		final HttpResponseEntity res = httpManager.doDelete(createEndpointUrl(database, "/_api/query-cache"), null);

		return createEntity(res, DefaultEntity.class);
	}

	@Override
	public QueryCachePropertiesEntity getQueryCacheProperties(final String database) throws ArangoException {

		final HttpResponseEntity res = httpManager.doGet(createEndpointUrl(database, "/_api/query-cache"), null);

		return createEntity(res, QueryCachePropertiesEntity.class);

	}

	@Override
	public QueryCachePropertiesEntity setQueryCacheProperties(
		final String database,
		final QueryCachePropertiesEntity properties) throws ArangoException {

		final HttpResponseEntity res = httpManager.doPut(createEndpointUrl(database, "/_api/query-cache/properties"),
			null, EntityFactory.toJsonString(properties));

		return createEntity(res, QueryCachePropertiesEntity.class);
	}

}
