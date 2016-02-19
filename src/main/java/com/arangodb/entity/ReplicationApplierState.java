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

package com.arangodb.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * the state of the replication applier, regardless of whether the applier is currently running or not.
 *
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ReplicationApplierState implements Serializable {

  /**
   * whether or not the applier is active and running
   */
  Boolean running;

  /**
   * the last tick value from the continuous replication log the applier has applied
   */
  Long lastAppliedContinuousTick;

  /**
   * the last tick value from the continuous replication log the applier has processed.
   */
  Long lastProcessedContinuousTick;

  /**
   * the last tick value the logger server can provide.
   */
  Long lastAvailableContinuousTick;

  /**
   * the time on the applier server.
   */
  Date time;

  /**
   * the total number of requests the applier has made to the endpoint.
   */
  Long totalRequests;

  /**
   * the total number of failed connection attempts the applier has made.
   */
  Long totalFailedConnects;

  /**
   * the total number of log events the applier has processed.
   */
  Long totalEvents;

  /**
   * details about the last error that happened on the applier.
   */
  LastError lastError;

  /**
   * details about the replication applier progress.
   * @see com.arangodb.entity.ReplicationApplierState.Progress
   */
  Progress progress;
  
  public static class Progress implements Serializable {
    /**
     * the date and time the progress was logged
     */
    Date time;

    /**
     * a textual description of the progress
     */
    String message;

    /**
     * the current number of failed connection attempts
     */
    Long failedConnects;

    public Date getTime() {
      return time;
    }
    public String getMessage() {
      return message;
    }
    public Long getFailedConnects() {
      return failedConnects;
    }
    public void setTime(Date time) {
      this.time = time;
    }
    public void setMessage(String message) {
      this.message = message;
    }
    public void setFailedConnects(Long failedConnects) {
      this.failedConnects = failedConnects;
    }
  }
  
  public static class LastError implements Serializable {
    /**
     * the date and time the error occurred In case no error has occurred, lastError will be empty.
     */
    Date time;

    /**
     * a textual error description
     */
    String errorMessage;

    /**
     * a numerical error code
     */
    Integer errorNum;

    public Date getTime() {
      return time;
    }
    public String getErrorMessage() {
      return errorMessage;
    }
    public Integer getErrorNum() {
      return errorNum;
    }
    public void setTime(Date time) {
      this.time = time;
    }
    public void setErrorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
    }
    public void setErrorNum(Integer errorNum) {
      this.errorNum = errorNum;
    }
  }

  public Boolean getRunning() {
    return running;
  }

  public Long getLastAppliedContinuousTick() {
    return lastAppliedContinuousTick;
  }

  public Long getLastProcessedContinuousTick() {
    return lastProcessedContinuousTick;
  }

  public Long getLastAvailableContinuousTick() {
    return lastAvailableContinuousTick;
  }

  public Date getTime() {
    return time;
  }

  public Long getTotalRequests() {
    return totalRequests;
  }

  public Long getTotalFailedConnects() {
    return totalFailedConnects;
  }

  public Long getTotalEvents() {
    return totalEvents;
  }

  public LastError getLastError() {
    return lastError;
  }

  public Progress getProgress() {
    return progress;
  }

  public void setRunning(Boolean running) {
    this.running = running;
  }

  public void setLastAppliedContinuousTick(Long lastAppliedContinuousTick) {
    this.lastAppliedContinuousTick = lastAppliedContinuousTick;
  }

  public void setLastProcessedContinuousTick(Long lastProcessedContinuousTick) {
    this.lastProcessedContinuousTick = lastProcessedContinuousTick;
  }

  public void setLastAvailableContinuousTick(Long lastAvailableContinuousTick) {
    this.lastAvailableContinuousTick = lastAvailableContinuousTick;
  }

  public void setTime(Date time) {
    this.time = time;
  }

  public void setTotalRequests(Long totalRequests) {
    this.totalRequests = totalRequests;
  }

  public void setTotalFailedConnects(Long totalFailedConnects) {
    this.totalFailedConnects = totalFailedConnects;
  }

  public void setTotalEvents(Long totalEvents) {
    this.totalEvents = totalEvents;
  }

  public void setLastError(LastError lastError) {
    this.lastError = lastError;
  }

  public void setProgress(Progress progress) {
    this.progress = progress;
  }
  
}
