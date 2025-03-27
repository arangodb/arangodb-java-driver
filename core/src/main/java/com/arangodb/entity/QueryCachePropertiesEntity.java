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

import java.util.Objects;

/**
 * @author Mark Vollmary
 */
public final class QueryCachePropertiesEntity {

    private CacheMode mode;
    private Long maxResults;

    public QueryCachePropertiesEntity() {
        super();
    }

    /**
     * @return the mode the AQL query cache operates in. The mode is one of the following values: off, on or demand
     */
    public CacheMode getMode() {
        return mode;
    }

    /**
     * @param mode the mode the AQL query cache operates in. The mode is one of the following values: off, on or demand
     */
    public void setMode(final CacheMode mode) {
        this.mode = mode;
    }

    /**
     * @return the maximum number of query results that will be stored per database-specific cache
     */
    public Long getMaxResults() {
        return maxResults;
    }

    /**
     * @param maxResults the maximum number of query results that will be stored per database-specific cache
     */
    public void setMaxResults(final Long maxResults) {
        this.maxResults = maxResults;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof QueryCachePropertiesEntity)) return false;
        QueryCachePropertiesEntity that = (QueryCachePropertiesEntity) o;
        return mode == that.mode && Objects.equals(maxResults, that.maxResults);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, maxResults);
    }

    public enum CacheMode {
        off, on, demand
    }

}
