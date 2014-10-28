package com.arangodb;

import com.arangodb.entity.AqlFunctionsEntity;
import com.arangodb.entity.DefaultEntity;
import com.arangodb.impl.BaseDriverInterface;

/**
 * Created by fbartels on 10/27/14.
 */
public interface InternalAqlFunctionsDriver  extends BaseDriverInterface {
  DefaultEntity createAqlFunction(String name, String code) throws ArangoException;

  AqlFunctionsEntity getAqlFunctions(String namespace) throws ArangoException;

  DefaultEntity deleteAqlFunction(String name, boolean isNameSpace) throws ArangoException;
}
