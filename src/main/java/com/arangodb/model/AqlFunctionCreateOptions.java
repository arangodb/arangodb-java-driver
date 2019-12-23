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

package com.arangodb.model;

/**
 * @author Mark Vollmary
 * @see <a href="https://www.arangodb.com/docs/stable/http/aql-user-functions.html#create-aql-user-function">API
 * Documentation</a>
 */
public class AqlFunctionCreateOptions {

    private String name;
    private String code;
    private Boolean isDeterministic;

    public AqlFunctionCreateOptions() {
        super();
    }

    /**
     * @param name the fully qualified name of the user functions
     * @return options
     */
    protected AqlFunctionCreateOptions name(final String name) {
        this.name = name;
        return this;
    }

    protected String getName() {
        return name;
    }

    /**
     * @param code a string representation of the function body
     * @return options
     */
    protected AqlFunctionCreateOptions code(final String code) {
        this.code = code;
        return this;
    }

    protected String getCode() {
        return code;
    }

    /**
     * @param isDeterministic an optional boolean value to indicate that the function results are fully deterministic (function
     *                        return value solely depends on the input value and return value is the same for repeated calls with
     *                        same input)
     * @return options
     */
    public AqlFunctionCreateOptions isDeterministic(final Boolean isDeterministic) {
        this.isDeterministic = isDeterministic;
        return this;
    }

    public Boolean getIsDeterministic() {
        return isDeterministic;
    }

}
