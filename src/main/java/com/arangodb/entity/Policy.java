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

/**
 * Enumeration for update/replace policy
 *
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public enum Policy {
  /**
   * if ERROR an error is returned when there is a revision mismatch
   */
  ERROR,

  /**
   * if LAST the operation is performed even when there is a revision mismatch
   */
  LAST
}
