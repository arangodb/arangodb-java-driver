package com.arangodb;

import com.arangodb.entity.BooleanResultEntity;
import com.arangodb.entity.DatabaseEntity;
import com.arangodb.entity.StringsResultEntity;
import com.arangodb.entity.UserEntity;
import com.arangodb.impl.BaseDriverInterface;

/**
 * Created by fbartels on 10/27/14.
 */
public interface InternalDatabaseDriver extends BaseDriverInterface {

	DatabaseEntity getCurrentDatabase(String database) throws ArangoException;

	StringsResultEntity getDatabases(boolean currentUserAccessableOnly, String username, String password)
			throws ArangoException;

	BooleanResultEntity createDatabase(String database, UserEntity... users) throws ArangoException;

	BooleanResultEntity deleteDatabase(String database) throws ArangoException;
}
