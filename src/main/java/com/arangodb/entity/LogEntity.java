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

import java.util.List;

/**
 * @author Mark Vollmary
 * @see <a href=
 * "https://www.arangodb.com/docs/stable/http/administration-and-monitoring.html#read-global-logs-from-the-server">API
 * Documentation</a>
 */
public class LogEntity implements Entity {

    private List<Long> lid;
    private List<LogLevel> level;
    private List<Long> timestamp;
    private List<String> text;
    private Long totalAmount;

    /**
     * @return a list of log entry identifiers. Each log message is uniquely identified by its @LIT{lid} and the
     * identifiers are in ascending order
     */
    public List<Long> getLid() {
        return lid;
    }

    /**
     * @return a list of the log-levels for all log entries
     */
    public List<LogLevel> getLevel() {
        return level;
    }

    /**
     * @return a list of the timestamps as seconds since 1970-01-01 for all log entries
     */
    public List<Long> getTimestamp() {
        return timestamp;
    }

    /**
     * @return a list of the texts of all log entries
     */
    public List<String> getText() {
        return text;
    }

    /**
     * @return the total amount of log entries before pagination
     */
    public Long getTotalAmount() {
        return totalAmount;
    }

}
