/*
 * Copyright (C) 2012,2013 tamtam180
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arangodb.impl;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoException;
import com.arangodb.entity.JobsEntity;
import com.arangodb.http.HttpManager;
import com.arangodb.http.HttpResponseEntity;
import com.arangodb.http.InvocationObject;
import com.arangodb.util.MapBuilder;

import java.util.List;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * @see http://www.arangodb.com/manuals/current/HttpImport.html
 */
public class InternalJobsDriverImpl extends BaseArangoDriverImpl implements com.arangodb.InternalJobsDriver {

  InternalJobsDriverImpl(ArangoConfigure configure, HttpManager httpManager) {
    super(configure , httpManager);
  }
  
  @Override
  public List<String> getJobs(String database, JobsEntity.JobState jobState, int count) throws ArangoException {
    HttpResponseEntity res = httpManager.doGet(
      createEndpointUrl(baseUrl, database, "/_api/job", jobState.getName()),
      new MapBuilder().put("count", count).get()
    );
    return createEntity(res, JobsEntity.class).getJobs();
  }

  @Override
  public List<String> getJobs(String database, JobsEntity.JobState jobState) throws ArangoException {
    HttpResponseEntity res = httpManager.doGet(
      createEndpointUrl(baseUrl, database, "/_api/job", jobState.getName())
    );
    return createEntity(res, JobsEntity.class).getJobs();
  }

  @Override
  public void deleteAllJobs(String database) throws ArangoException {
    HttpResponseEntity res = httpManager.doDelete(
      createEndpointUrl(baseUrl, database, "/_api/job", "all"),
      null
    );
  }

  @Override
  public void deleteJobById(String database, String JobId) throws ArangoException {
    HttpResponseEntity res = httpManager.doDelete(
      createEndpointUrl(baseUrl, database, "/_api/job", JobId),
      null
    );
  }


  @Override
  public void deleteExpiredJobs(String database, int timeStamp) throws ArangoException {
    HttpResponseEntity res = httpManager.doDelete(
      createEndpointUrl(baseUrl, database, "/_api/job", "expired"),
      new MapBuilder().put("stamp", timeStamp).get()
    );
  }

  @Override
  public <T> T getJobResult(String database, String jobId) throws ArangoException {
    InvocationObject io = this.getHttpManager().getJobs().get(jobId);
    if (io == null) {
      throw new ArangoException("No result for JobId.");
    }
    this.getHttpManager().getJobs().remove(jobId);
    this.getHttpManager().setPreDefinedResponse(httpManager.doPut(
      createEndpointUrl(baseUrl, database, "/_api/job", jobId), null, null
    ));
    T result;
    try {
       result = (T) io.getMethod().invoke(
        io.getArangoDriver(),
        io.getArgs()
      );
    } catch (Exception e) {
      this.getHttpManager().setPreDefinedResponse(null);
      throw new ArangoException(e);
    }
    this.getHttpManager().setPreDefinedResponse(null);
    return result;
  }
}
