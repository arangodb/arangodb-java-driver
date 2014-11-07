package com.arangodb;

import com.arangodb.entity.JobsEntity;
import com.arangodb.impl.BaseDriverInterface;

import java.util.List;

/**
 * Created by fbartels on 10/28/14.
 */
public interface InternalJobsDriver  extends BaseDriverInterface {

  List<String> getJobs(String database, JobsEntity.JobState jobState, int count) throws ArangoException;

  List<String> getJobs(String database, JobsEntity.JobState jobState) throws ArangoException;

  void deleteAllJobs(String database) throws ArangoException;

  void deleteJobById(String database, String JobId) throws ArangoException;

  void deleteExpiredJobs(String database, int timeStamp) throws ArangoException;

  <T> T getJobResult(String database, String jobId) throws ArangoException;

}
