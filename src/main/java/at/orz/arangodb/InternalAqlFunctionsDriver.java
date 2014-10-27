package at.orz.arangodb;

import at.orz.arangodb.entity.AqlFunctionsEntity;
import at.orz.arangodb.entity.DefaultEntity;
import at.orz.arangodb.impl.BaseDriverInterface;

/**
 * Created by fbartels on 10/27/14.
 */
public interface InternalAqlFunctionsDriver  extends BaseDriverInterface {
  DefaultEntity createAqlFunction(String name, String code) throws ArangoException;

  AqlFunctionsEntity getAqlFunctions(String namespace) throws ArangoException;

  DefaultEntity deleteAqlFunction(String name, boolean isNameSpace) throws ArangoException;
}
