package com.arangodb;

import java.util.Map;

import com.arangodb.entity.DefaultEntity;
import com.arangodb.entity.UserEntity;
import com.arangodb.entity.UsersEntity;
import com.arangodb.impl.BaseDriverInterface;

/**
 * Created by fbartels on 10/27/14.
 */
public interface InternalUsersDriver  extends BaseDriverInterface {
  DefaultEntity createUser(String database, String username, String passwd, Boolean active,
                           Map<String, Object> extra) throws ArangoException;

  DefaultEntity deleteUser(String database, String username) throws ArangoException;

  UserEntity getUser(String database, String username) throws ArangoException;

  UsersEntity getUsers(String database) throws ArangoException;

  DefaultEntity replaceUser(String database, String username, String passwd, Boolean active,
                            Map<String, Object> extra) throws ArangoException;

  DefaultEntity updateUser(String database, String username, String passwd, Boolean active,
                           Map<String, Object> extra) throws ArangoException;
}
