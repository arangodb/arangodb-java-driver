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
 */
public class QueryTrackingPropertiesEntity implements Entity {

    private Boolean enabled;
    private Boolean trackSlowQueries;
    private Long maxSlowQueries;
    private Long slowQueryThreshold;
    private Long maxQueryStringLength;

    public QueryTrackingPropertiesEntity() {
        super();
    }

    /**
     * @return If set to true, then queries will be tracked. If set to false, neither queries nor slow queries will be
     * tracked
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * @param enabled If set to true, then queries will be tracked. If set to false, neither queries nor slow queries will
     *                be tracked
     */
    public void setEnabled(final Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return If set to true, then slow queries will be tracked in the list of slow queries if their runtime exceeds
     * the value set in slowQueryThreshold. In order for slow queries to be tracked, the enabled property must
     * also be set to true.
     */
    public Boolean getTrackSlowQueries() {
        return trackSlowQueries;
    }

    /**
     * @param trackSlowQueries If set to true, then slow queries will be tracked in the list of slow queries if their runtime exceeds
     *                         the value set in slowQueryThreshold. In order for slow queries to be tracked, the enabled property
     *                         must also be set to true.
     */
    public void setTrackSlowQueries(final Boolean trackSlowQueries) {
        this.trackSlowQueries = trackSlowQueries;
    }

    /**
     * @return The maximum number of slow queries to keep in the list of slow queries. If the list of slow queries is
     * full, the oldest entry in it will be discarded when additional slow queries occur.
     */
    public Long getMaxSlowQueries() {
        return maxSlowQueries;
    }

    /**
     * @param maxSlowQueries The maximum number of slow queries to keep in the list of slow queries. If the list of slow queries is
     *                       full, the oldest entry in it will be discarded when additional slow queries occur.
     */
    public void setMaxSlowQueries(final Long maxSlowQueries) {
        this.maxSlowQueries = maxSlowQueries;
    }

    /**
     * @return The threshold value for treating a query as slow. A query with a runtime greater or equal to this
     * threshold value will be put into the list of slow queries when slow query tracking is enabled. The value
     * for slowQueryThreshold is specified in seconds.
     */
    public Long getSlowQueryThreshold() {
        return slowQueryThreshold;
    }

    /**
     * @param slowQueryThreshold The threshold value for treating a query as slow. A query with a runtime greater or equal to this
     *                           threshold value will be put into the list of slow queries when slow query tracking is enabled. The
     *                           value for slowQueryThreshold is specified in seconds.
     */
    public void setSlowQueryThreshold(final Long slowQueryThreshold) {
        this.slowQueryThreshold = slowQueryThreshold;
    }

    /**
     * @return The maximum query string length to keep in the list of queries. Query strings can have arbitrary lengths,
     * and this property can be used to save memory in case very long query strings are used. The value is
     * specified in bytes.
     */
    public Long getMaxQueryStringLength() {
        return maxQueryStringLength;
    }

    /**
     * @param maxQueryStringLength The maximum query string length to keep in the list of queries. Query strings can have arbitrary
     *                             lengths, and this property can be used to save memory in case very long query strings are used. The
     *                             value is specified in bytes.
     */
    public void setMaxQueryStringLength(final Long maxQueryStringLength) {
        this.maxQueryStringLength = maxQueryStringLength;
    }

}
