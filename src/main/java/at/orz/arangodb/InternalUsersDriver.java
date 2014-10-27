package at.orz.arangodb;

import at.orz.arangodb.entity.DefaultEntity;
import at.orz.arangodb.entity.UserEntity;
import at.orz.arangodb.entity.UsersEntity;
import at.orz.arangodb.impl.BaseDriverInterface;

import java.util.Map;

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
