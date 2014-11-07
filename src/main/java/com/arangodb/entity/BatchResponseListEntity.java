/*
 * Copyright (C) 2012 tamtam180
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

package com.arangodb.entity;

import java.util.List;
import java.util.Map;

import com.arangodb.ArangoException;
import com.arangodb.http.HttpManager;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class BatchResponseListEntity extends BaseEntity {
  
  List<BatchResponseEntity> batchResponseEntities;

  public List<BatchResponseEntity> getBatchResponseEntities() {
    return batchResponseEntities;
  }


  public void setBatchResponseEntities(List<BatchResponseEntity> batchResponseEntities) {
    this.batchResponseEntities = batchResponseEntities;
  }

  public BatchResponseEntity getResponseFromRequestId(String requestId) throws ArangoException {
    for (BatchResponseEntity bpe : this.batchResponseEntities) {
      if (bpe.getRequestId().equals(requestId)) {
        return bpe;
      }
    }
    throw new ArangoException("RequestId not found in batch.");

  }

}
