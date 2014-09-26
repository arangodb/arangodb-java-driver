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

package at.orz.arangodb.impl;

import at.orz.arangodb.ArangoConfigure;
import at.orz.arangodb.ArangoException;
import at.orz.arangodb.entity.AdminLogEntity;
import at.orz.arangodb.entity.ArangoUnixTime;
import at.orz.arangodb.entity.ArangoVersion;
import at.orz.arangodb.entity.DefaultEntity;
import at.orz.arangodb.entity.StatisticsDescriptionEntity;
import at.orz.arangodb.entity.StatisticsEntity;
import at.orz.arangodb.http.HttpResponseEntity;
import at.orz.arangodb.util.MapBuilder;


/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class InternalAdminDriverImpl extends BaseArangoDriverImpl {

	// MEMO: ADMINはdatabase関係ない

	InternalAdminDriverImpl(ArangoConfigure configure) {
		super(configure);
	}

	public AdminLogEntity getServerLog(
			Integer logLevel, Boolean logLevelUpTo,
			Integer start,
			Integer size, Integer offset,
			Boolean sortAsc,
			String text
			) throws ArangoException {
		
		// パラメータを作る
		MapBuilder param = new MapBuilder();
		if (logLevel != null) {
			if (logLevelUpTo != null && logLevelUpTo.booleanValue()) {
				param.put("upto", logLevel);
			} else {
				param.put("level", logLevel);
			}
		}
		param.put("start", start);
		param.put("size", size);
		param.put("offset", offset);
		if (sortAsc != null) {
			param.put("sort", sortAsc.booleanValue() ?  "asc" : "desc");
		}
		param.put("search", text);
		
		// 実行
		HttpResponseEntity res = httpManager.doGet(createEndpointUrl(baseUrl, null, "/_admin/log"), param.get());
		
		// 結果変換
		try {
			AdminLogEntity entity = createEntity(res, AdminLogEntity.class);
			return entity;
		} catch (ArangoException e) {
			throw e;
			//return null;
		}
		
	}
	
	public StatisticsEntity getStatistics() throws ArangoException {
		
		HttpResponseEntity res = httpManager.doGet(createEndpointUrl(baseUrl, null, "/_admin/statistics"));
		
		try {
			return createEntity(res, StatisticsEntity.class);
		} catch (ArangoException e) {
			throw e;
		}
		
	}

	public StatisticsDescriptionEntity getStatisticsDescription() throws ArangoException {
		
		HttpResponseEntity res = httpManager.doGet(createEndpointUrl(baseUrl, null, "/_admin/statistics-description"));
		
		try {
			return createEntity(res, StatisticsDescriptionEntity.class);
		} catch (ArangoException e) {
			throw e;
		}
		
	}


	/**
	 * 
	 * @return
	 * @throws ArangoException
	 * @see http://www.arangodb.org/manuals/current/HttpMisc.html#HttpMiscVersion
	 */
	public ArangoVersion getVersion() throws ArangoException {
		HttpResponseEntity res = httpManager.doGet(createEndpointUrl(baseUrl, null, "/_api/version"));
		return createEntity(res, ArangoVersion.class);
	}

	public ArangoUnixTime getTime() throws ArangoException {
		HttpResponseEntity res = httpManager.doGet(createEndpointUrl(baseUrl, null, "/_admin/time"));
		return createEntity(res, ArangoUnixTime.class);
	}
	
	public DefaultEntity flushModules() throws ArangoException {
		HttpResponseEntity res = httpManager.doPost(createEndpointUrl(baseUrl, null, "/_admin/modules/flush"), null, (String)null);
		return createEntity(res, DefaultEntity.class, null, false);
	}

	public DefaultEntity reloadRouting() throws ArangoException {
		HttpResponseEntity res = httpManager.doPost(createEndpointUrl(baseUrl, null, "/_admin/routing/reload"), null, (String)null);
		return createEntity(res, DefaultEntity.class, null, false);
	}
	
	public DefaultEntity executeScript(String database, String jsCode) throws ArangoException {
		
		HttpResponseEntity res = httpManager.doPost(
				createEndpointUrl(baseUrl, database, "/_admin/execute"), 
				null, 
				jsCode);
		
		return createEntity(res, DefaultEntity.class);
		
	}
	
}
