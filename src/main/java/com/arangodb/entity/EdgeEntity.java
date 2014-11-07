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

import com.google.gson.annotations.SerializedName;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * @author gschwab
 *
 */
public class EdgeEntity<T> extends DocumentEntity<T> {

  @SerializedName("_from")
  String fromVertexHandle;
  @SerializedName("_to")
  String toVertexHandle;

  public String getFromVertexHandle() {
    return fromVertexHandle;
  }

  public String getToVertexHandle() {
    return toVertexHandle;
  }

  public void setFromVertexHandle(String fromVertexHandle) {
    this.fromVertexHandle = fromVertexHandle;
  }

  public void setToVertexHandle(String toVertexHandle) {
    this.toVertexHandle = toVertexHandle;
  }

}
