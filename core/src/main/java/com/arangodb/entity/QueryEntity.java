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
import java.util.Objects;

/**
 * @author Mark Vollmary
 */
public final class QueryEntity {

    private String id;
    private String database;
    private String user;
    private String query;
    private Map<String, Object> bindVars;
    private Date started;
    private Double runTime;
    private Long peakMemoryUsage;
    private QueryExecutionState state;
    private Boolean stream;

    /**
     * @return the query's id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the name of the database the query runs in
     */
    public String getDatabase() {
        return database;
    }

    /**
     * @return the name of the user that started the query
     */
    public String getUser() {
        return user;
    }

    /**
     * @return the query string (potentially truncated)
     */
    public String getQuery() {
        return query;
    }

    /**
     * @return the bind parameter values used by the query
     */
    public Map<String, Object> getBindVars() {
        return bindVars;
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
     * @return the query’s peak memory usage in bytes (in increments of 32KB)
     */
    public Long getPeakMemoryUsage() {
        return peakMemoryUsage;
    }

    /**
     * @return the query's current execution state
     */
    public QueryExecutionState getState() {
        return state;
    }

    /**
     * @return whether or not the query uses a streaming cursor
     */
    public Boolean getStream() {
        return stream;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof QueryEntity)) return false;
        QueryEntity that = (QueryEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(database, that.database) && Objects.equals(user, that.user) && Objects.equals(query, that.query) && Objects.equals(bindVars, that.bindVars) && Objects.equals(started, that.started) && Objects.equals(runTime, that.runTime) && Objects.equals(peakMemoryUsage, that.peakMemoryUsage) && state == that.state && Objects.equals(stream, that.stream);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, database, user, query, bindVars, started, runTime, peakMemoryUsage, state, stream);
    }
}
