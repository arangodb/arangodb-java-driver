package com.arangodb;

import com.arangodb.entity.DefaultEntity;
import com.arangodb.entity.QueryCachePropertiesEntity;
import com.arangodb.impl.BaseDriverInterface;

/**
 * Created by a-brandt on 2015-09-11.
 */
public interface InternalQueryCacheDriver extends BaseDriverInterface {

	DefaultEntity deleteQueryCache() throws ArangoException;

	QueryCachePropertiesEntity getQueryCacheProperties() throws ArangoException;

	QueryCachePropertiesEntity setQueryCacheProperties(QueryCachePropertiesEntity properties) throws ArangoException;

}
