package at.orz.arangodb;

import at.orz.arangodb.entity.BooleanResultEntity;
import at.orz.arangodb.entity.Endpoint;
import at.orz.arangodb.impl.BaseDriverInterface;

import java.util.List;

/**
 * Created by fbartels on 10/27/14.
 */
public interface InternalEndpointDriver  extends BaseDriverInterface {
  BooleanResultEntity createEndpoint(String endpoint, String... databases) throws ArangoException;

  List<Endpoint> getEndpoints() throws ArangoException;

  BooleanResultEntity deleteEndpoint(String endpoint) throws ArangoException;
}
