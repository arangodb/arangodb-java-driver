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

import java.util.Date;
import java.util.Map;

/**
 * @author Mark Vollmary
 */
public class QueryEntity implements Entity {

    public static final String PROPERTY_STARTED = "started";

    private String id;
    private String query;
    private Date started;
    private Double runTime;
    private Map<String, Object> bindVars;
    private QueryExecutionState state;

    public QueryEntity() {
        super();
    }

    /**
     * @return the query's id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the query string (potentially truncated)
     */
    public String getQuery() {
        return query;
    }

    /**
     * @return the date and time when the query was started
     */
    public Date getStarted() {
        return started;
    }

    /**
     * @return the query's run time up to the point the list of queries was queried
     */
    public Double getRunTime() {
        return runTime;
    }

    /**
     * @return the bind parameter values used by the query
     */
    public Map<String, Object> getBindVars() {
        return bindVars;
    }

    /**
     * @return the query's current execution state
     */
    public QueryExecutionState getState() {
        return state;
    }

}
