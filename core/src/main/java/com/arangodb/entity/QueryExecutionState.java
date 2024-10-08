/*
 * DISCLAIMER
 *
 * Copyright 2018 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Mark Vollmary
 */
public enum QueryExecutionState {
    @JsonProperty("initializing")
    INITIALIZING,

    @JsonProperty("parsing")
    PARSING,

    @JsonProperty("optimizing ast")
    OPTIMIZING_AST,

    @JsonProperty("loading collections")
    LOADING_COLLECTIONS,

    @JsonProperty("instantiating plan")
    INSTANTIATING_PLAN,

    @JsonProperty("instantiating executors")
    INSTANTIATING_EXECUTORS,

    @JsonProperty("optimizing plan")
    OPTIMIZING_PLAN,

    @JsonProperty("executing")
    EXECUTING,

    @JsonProperty("finalizing")
    FINALIZING,

    @JsonProperty("finished")
    FINISHED,

    @JsonProperty("killed")
    KILLED,

    @JsonProperty("invalid")
    INVALID
}
