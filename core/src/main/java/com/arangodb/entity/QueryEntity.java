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
    private Boolean modificationQuery;
    private Long warnings;
    private Integer exitCode;

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

    /**
     * For running queries: Whether the query writes data ({@code true}) or only reads ({@code false}).
     * <p>
     * For slow queries: Whether the query wrote data ({@code true}) or only read ({@code false}).
     *
     * @return whether the query modifies data, or {@code null} if not reported by the server
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/queries/aql-queries/#list-the-running-aql-queries">API Documentation</a>
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/queries/aql-queries/#list-the-slow-aql-queries">API Documentation</a>
     */
    public Boolean getModificationQuery() {
        return modificationQuery;
    }

    /**
     * The number of query warnings that occurred.
     * <p>
     * For running queries:
     * Values other than {@code 0} may not be observable because this information
     * typically becomes available when the query finishes, at which point
     * it is no longer listed as a running query. However, other values can be
     * observed when enabling {@code stream} and there is more than one batch of
     * results.
     *
     * @return the number of query warnings, or {@code null} if not reported by the server
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/queries/aql-queries/#list-the-running-aql-queries">API Documentation</a>
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/queries/aql-queries/#list-the-slow-aql-queries">API Documentation</a>
     */
    public Long getWarnings() {
        return warnings;
    }

    /**
     * An error code ({@code errorNum}) that indicates why the query
     * failed, or {@code 0} on success. See
     * <a href="https://docs.arango.ai/arangodb/stable/develop/error-codes/">The error codes of ArangoDB and their meanings</a>.
     * <p>
     * This attribute is reported for slow queries, not for currently running queries.
     *
     * @return the query exit code, or {@code null} if not reported by the server
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/queries/aql-queries/#list-the-slow-aql-queries">API Documentation</a>
     */
    public Integer getExitCode() {
        return exitCode;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof QueryEntity)) return false;
        QueryEntity that = (QueryEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(database, that.database) && Objects.equals(user, that.user) && Objects.equals(query, that.query) && Objects.equals(bindVars, that.bindVars) && Objects.equals(started, that.started) && Objects.equals(runTime, that.runTime) && Objects.equals(peakMemoryUsage, that.peakMemoryUsage) && state == that.state && Objects.equals(stream, that.stream) && Objects.equals(modificationQuery, that.modificationQuery) && Objects.equals(warnings, that.warnings) && Objects.equals(exitCode, that.exitCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, database, user, query, bindVars, started, runTime, peakMemoryUsage, state, stream, modificationQuery, warnings, exitCode);
    }
}
