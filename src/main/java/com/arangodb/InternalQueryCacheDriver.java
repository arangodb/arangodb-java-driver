package com.arangodb;

import com.arangodb.entity.DefaultEntity;
import com.arangodb.entity.QueryCachePropertiesEntity;
import com.arangodb.impl.BaseDriverInterface;

/**
 * Created by a-brandt on 2015-09-11.
 */
public interface InternalQueryCacheDriver extends BaseDriverInterface {

	DefaultEntity deleteQueryCache(String database) throws ArangoException;

	QueryCachePropertiesEntity getQueryCacheProperties(String database) throws ArangoException;

	QueryCachePropertiesEntity setQueryCacheProperties(String database, QueryCachePropertiesEntity properties)
			throws ArangoException;

}
