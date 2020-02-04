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
 * @see <a href="https://www.arangodb.com/docs/stable/http/aql-query.html#parse-an-aql-query">API Documentation</a>
 */
public class AqlQueryParseOptions {

    private String query;

    public AqlQueryParseOptions() {
        super();
    }

    protected String getQuery() {
        return query;
    }

    /**
     * @param query the query which you want parse
     * @return options
     */
    protected AqlQueryParseOptions query(final String query) {
        this.query = query;
        return this;
    }

}
