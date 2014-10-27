package at.orz.arangodb;

import at.orz.arangodb.entity.BooleanResultEntity;
import at.orz.arangodb.entity.DatabaseEntity;
import at.orz.arangodb.entity.StringsResultEntity;
import at.orz.arangodb.entity.UserEntity;
import at.orz.arangodb.impl.BaseDriverInterface;

/**
 * Created by fbartels on 10/27/14.
 */
public interface InternalDatabaseDriver  extends BaseDriverInterface {
  DatabaseEntity getCurrentDatabase() throws ArangoException;

  StringsResultEntity getDatabases(boolean currentUserAccessableOnly, String username, String password) throws ArangoException;

  BooleanResultEntity createDatabase(String database, UserEntity... users) throws ArangoException;

  BooleanResultEntity deleteDatabase(String database) throws ArangoException;
}
