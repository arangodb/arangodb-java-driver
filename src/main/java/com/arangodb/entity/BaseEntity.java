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

import com.arangodb.annotations.Exclude;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public abstract class BaseEntity implements Serializable {

  /**
   * If true an error occurred while creating this entity
   */
  @Exclude(deserialize=false)
  boolean error;

  /**
   * The http response code of the response
   */
  @Exclude(deserialize=false)
  int code;

  /**
   * The Arango error number of the error
   */
  @Exclude(deserialize=false)
  int errorNumber;

  /**
   * If an error occurred this is the error message
   */
  @Exclude(deserialize=false)
  String errorMessage;

  /**
   * The http status code of the response
   */
  @Exclude(deserialize=false)
  int statusCode;

  /**
   * The check sum of the requested resource
   */
  @Exclude(deserialize=false)
  long etag;

  /**
   * The requestId, this attribute is only used for batch requests.
   */
  @Exclude(deserialize=false)
  String requestId;

  /**
   * If the resource has been modified it returns true
   *
   * @return boolean
   */
  public boolean isNotModified() {
    return statusCode == 304; //HttpStatus.SC_NOT_MODIFIED;
  }

  /**
   * If the request is unauthorized this returns true
   *
   * @return boolean
   */
  public boolean isUnauthorized() {
    return statusCode == 401;
  }

//  /**
//   * If the requested resource has not been modified it returns true
//   *
//   * @return boolean
//   */
//  public boolean isNotFound() {
//    return statusCode == 404;
//  }

  /**
   * If this is the response of a batch request it returns true
   *
   * @return boolean
   */
  public boolean isBatchResponseEntity() {
    return statusCode == 206;
  }

  public boolean isError() {
    return error;
  }

  public void setError(boolean error) {
    this.error = error;
  }

  public int getCode() {
    return code;
  }

  public int getErrorNumber() {
    return errorNumber;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public void setErrorNumber(int errorNumber) {
    this.errorNumber = errorNumber;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public long getEtag() {
    return etag;
  }

  public void setEtag(long etag) {
    this.etag = etag;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }


  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }
}
