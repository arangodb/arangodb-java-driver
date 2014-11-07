package com.arangodb;

import com.arangodb.entity.JobsEntity;
import com.arangodb.entity.TransactionEntity;
import com.arangodb.entity.TransactionResultEntity;
import com.arangodb.impl.BaseDriverInterface;

import java.util.List;

/**
 * Created by fbartels on 10/28/14.
 */
public interface InternalTransactionDriver extends BaseDriverInterface {


  TransactionEntity createTransaction (String action);


  <T> T  executeTransaction(String database, TransactionEntity transactionEntity, Class clazz) throws ArangoException;


}
