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

import com.arangodb.util.CollectionUtils;

import java.util.Iterator;
import java.util.List;

/**
 * This entity represents a list of job ids.
 *
 *
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class JobsEntity extends BaseEntity {

  /**
   * The enumeration containing the job state
   */
  public static enum JobState {
    DONE , PENDING;
    public java.lang.String getName() {
      if (this == DONE) {
        return "done";
      }
      if (this == PENDING) {
        return "pending";
      }
      return null;
    }
  }

  List<String> jobs;

  public List<String> getJobs() {
    return jobs;
  }

  public void setJobs(List<String> jobs) {
    this.jobs = jobs;
  }


  public JobsEntity(List<String> jobs) {
    this.jobs = jobs;
  }
}
