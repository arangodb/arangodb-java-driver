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

import com.arangodb.entity.LogLevel;

/**
 * @author Mark Vollmary
 * @see <a href=
 * "https://www.arangodb.com/docs/stable/http/administration-and-monitoring.html#read-global-logs-from-the-server">API
 * Documentation</a>
 */
public class LogOptions {

    public static final String PROPERTY_UPTO = "upto";
    public static final String PROPERTY_LEVEL = "level";
    public static final String PROPERTY_START = "start";
    public static final String PROPERTY_SIZE = "size";
    public static final String PROPERTY_OFFSET = "offset";
    public static final String PROPERTY_SEARCH = "search";
    public static final String PROPERTY_SORT = "sort";

    public enum SortOrder {
        asc, desc
    }

    private LogLevel upto;
    private LogLevel level;
    private Long start;
    private Integer size;
    private Integer offset;
    private String search;
    private SortOrder sort;

    public LogOptions() {
        super();
    }

    public LogLevel getUpto() {
        return upto;
    }

    /**
     * @param upto Returns all log entries up to log level upto
     * @return options
     */
    public LogOptions upto(final LogLevel upto) {
        this.upto = upto;
        return this;
    }

    public LogLevel getLevel() {
        return level;
    }

    /**
     * @param level Returns all log entries of log level level. Note that the query parameters upto and level are mutually
     *              exclusive
     * @return options
     */
    public LogOptions level(final LogLevel level) {
        this.level = level;
        return this;
    }

    public Long getStart() {
        return start;
    }

    /**
     * @param start Returns all log entries such that their log entry identifier (lid value) is greater or equal to start
     * @return options
     */
    public LogOptions start(final Long start) {
        this.start = start;
        return this;
    }

    public Integer getSize() {
        return size;
    }

    /**
     * @param size Restricts the result to at most size log entries
     * @return options
     */
    public LogOptions size(final Integer size) {
        this.size = size;
        return this;
    }

    public Integer getOffset() {
        return offset;
    }

    /**
     * @param offset Starts to return log entries skipping the first offset log entries. offset and size can be used for
     *               pagination
     * @return options
     */
    public LogOptions offset(final Integer offset) {
        this.offset = offset;
        return this;
    }

    public String getSearch() {
        return search;
    }

    /**
     * @param search Only return the log entries containing the text specified in search
     * @return options
     */
    public LogOptions search(final String search) {
        this.search = search;
        return this;
    }

    public SortOrder getSort() {
        return sort;
    }

    /**
     * @param sort Sort the log entries either ascending (if sort is asc) or descending (if sort is desc) according to
     *             their lid values. Note that the lid imposes a chronological order. The default value is asc
     * @return options
     */
    public LogOptions sort(final SortOrder sort) {
        this.sort = sort;
        return this;
    }

}
