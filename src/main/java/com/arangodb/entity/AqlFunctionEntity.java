/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
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

/**
 * @author Mark Vollmary
 * @see <a href=
 * "https://www.arangodb.com/docs/stable/http/aql-user-functions.html#return-registered-aql-user-functions">API
 * Documentation</a>
 */
public class AqlFunctionEntity implements Entity {

    private String name;
    private String code;
    private Boolean isDeterministic;

    public AqlFunctionEntity() {
        super();
    }

    /**
     * @return The fully qualified name of the user function
     */
    public String getName() {
        return name;
    }

    /**
     * @return A string representation of the function body
     */
    public String getCode() {
        return code;
    }

    /**
     * @return An optional boolean value to indicate whether the function results are fully deterministic (function
     * return value solely depends on the input value and return value is the same for repeated calls with same
     * input). The isDeterministic attribute is currently not used but may be used later for optimizations.
     */
    public Boolean getIsDeterministic() {
        return isDeterministic;
    }

}
