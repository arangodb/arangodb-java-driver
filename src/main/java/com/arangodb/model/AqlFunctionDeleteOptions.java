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
 * @see <a href=
 * "https://www.arangodb.com/docs/stable/http/aql-user-functions.html#remove-existing-aql-user-function">API
 * Documentation</a>
 */
public class AqlFunctionDeleteOptions {

    private Boolean group;

    public AqlFunctionDeleteOptions() {
        super();
    }

    public Boolean getGroup() {
        return group;
    }

    /**
     * @param group If set to true, then the function name provided in name is treated as a namespace prefix, and all
     *              functions in the specified namespace will be deleted. If set to false, the function name provided in
     *              name must be fully qualified, including any namespaces.
     * @return options
     */
    public AqlFunctionDeleteOptions group(final Boolean group) {
        this.group = group;
        return this;
    }

}
