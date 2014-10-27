package at.orz.arangodb;

import at.orz.arangodb.entity.KeyValueEntity;
import at.orz.arangodb.impl.BaseDriverInterface;

import java.util.Date;
import java.util.Map;

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
