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

import java.io.Serializable;

import com.arangodb.http.HttpResponseEntity;
import com.arangodb.http.InvocationObject;

/**
 * This entity is a representation of the result of a batch part
 *
 * @author tamtam180 - kirscheless at gmail.com
 * 
 */
public class BatchResponseEntity extends BaseEntity {

  /**
   * The context of the function call, this is used to to process the server response to
   * an api entity.
   */
  InvocationObject invocationObject;

  /**
   * The http response of the batch part.
   */
  public HttpResponseEntity httpResponseEntity;

  public BatchResponseEntity(InvocationObject invocationObject) {
    this.invocationObject = invocationObject;
    this.httpResponseEntity = new HttpResponseEntity();
  }

  public InvocationObject getInvocationObject() {

    return  invocationObject;
  }

  public void setInvocationObject(InvocationObject invocationObject) {
    this.invocationObject = invocationObject;
  }


  public HttpResponseEntity getHttpResponseEntity() {
    return httpResponseEntity;
  }

  public void setHttpResponseEntity(HttpResponseEntity httpResponseEntity) {
    this.httpResponseEntity = httpResponseEntity;
  }


}
