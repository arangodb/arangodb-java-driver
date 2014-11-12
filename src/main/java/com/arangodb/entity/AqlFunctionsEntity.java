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

/**
 * An entity wrapping a list of AQL functions
 *
 * @author Florian Bartels - f.bartels@triagens.de
 *
 */
public class AqlFunctionsEntity extends BaseEntity {

  /**
   * A map containing the function name as key and the function as value
   */
  Map<String, String> aqlFunctions;

  public AqlFunctionsEntity () {
  }

  AqlFunctionsEntity (Map<String, String> aqlfunctions) {
    this.aqlFunctions = aqlfunctions;
  }

  public Map<String, String> getAqlFunctions() {
    return aqlFunctions;
  }

  public int size() {
    return this.aqlFunctions.size();
  }
  
}
