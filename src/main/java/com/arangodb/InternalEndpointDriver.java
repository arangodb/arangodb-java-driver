package com.arangodb;

import java.util.List;

import com.arangodb.entity.BooleanResultEntity;
import com.arangodb.entity.Endpoint;
import com.arangodb.impl.BaseDriverInterface;

/**
 * Created by fbartels on 10/27/14.
 */
public interface InternalEndpointDriver  extends BaseDriverInterface {
  BooleanResultEntity createEndpoint(String endpoint, String... databases) throws ArangoException;

  List<Endpoint> getEndpoints() throws ArangoException;

  BooleanResultEntity deleteEndpoint(String endpoint) throws ArangoException;
}
