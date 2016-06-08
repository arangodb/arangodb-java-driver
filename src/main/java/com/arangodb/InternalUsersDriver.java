package com.arangodb;

import java.util.Map;

import com.arangodb.entity.DefaultEntity;
import com.arangodb.entity.UserEntity;
import com.arangodb.entity.UsersEntity;
import com.arangodb.impl.BaseDriverInterface;

/**
 * Created by fbartels on 10/27/14.
 */
public interface InternalUsersDriver extends BaseDriverInterface {
	DefaultEntity createUser(String username, String passwd, Boolean active, Map<String, Object> extra)
			throws ArangoException;

	DefaultEntity deleteUser(String username) throws ArangoException;

	UserEntity getUser(String username) throws ArangoException;

	UsersEntity getUsers() throws ArangoException;

	DefaultEntity replaceUser(String username, String passwd, Boolean active, Map<String, Object> extra)
			throws ArangoException;

	DefaultEntity updateUser(String username, String passwd, Boolean active, Map<String, Object> extra)
			throws ArangoException;

	DefaultEntity grantDatabaseAccess(final String username, final String database) throws ArangoException;
}
