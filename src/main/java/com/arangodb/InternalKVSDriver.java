package com.arangodb;

import java.util.Date;
import java.util.Map;

import com.arangodb.entity.KeyValueEntity;
import com.arangodb.impl.BaseDriverInterface;

/**
 * Created by fbartels on 10/27/14.
 */
public interface InternalKVSDriver  extends BaseDriverInterface {
  KeyValueEntity createKeyValue(
    String database,
    String collectionName, String key, Object value,
    Map<String, Object> attributes, Date expiredDate) throws ArangoException;

  KeyValueEntity updateKeyValue(
    String database,
    String collectionName, String key, Object value,
    Map<String, Object> attributes, Date expiredDate,
    boolean create
  ) throws ArangoException;
}
