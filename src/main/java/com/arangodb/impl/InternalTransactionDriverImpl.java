package com.arangodb.impl;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoException;
import com.arangodb.InternalTransactionDriver;
import com.arangodb.entity.BaseEntity;
import com.arangodb.entity.EntityFactory;
import com.arangodb.entity.TransactionEntity;
import com.arangodb.entity.TransactionResultEntity;
import com.arangodb.http.HttpManager;
import com.arangodb.http.HttpResponseEntity;
import com.arangodb.util.MapBuilder;

/**
 * Created by fbartels on 10/30/14.
 */
public class InternalTransactionDriverImpl extends BaseArangoDriverImpl implements InternalTransactionDriver {

  InternalTransactionDriverImpl(ArangoConfigure configure,HttpManager httpManager) {
      super(configure, httpManager);
    }

  @Override
  public TransactionEntity createTransaction(String action) {
    return new TransactionEntity(action);
  }

  @Override
  public TransactionResultEntity  executeTransaction(String database, TransactionEntity transactionEntity)
    throws ArangoException {
    HttpResponseEntity res = httpManager.doPost(
      createEndpointUrl(baseUrl, database, "/_api/transaction"),
      null,
      EntityFactory.toJsonString(
        new MapBuilder()
          .put("collections", transactionEntity.getCollections())
          .put("action", transactionEntity.getAction())
          .put("lockTimeout", transactionEntity.getLockTimeout())
          .put("params", transactionEntity.getParams())
          .get())
    );
    return createEntity(res, TransactionResultEntity.class);
  }
}
