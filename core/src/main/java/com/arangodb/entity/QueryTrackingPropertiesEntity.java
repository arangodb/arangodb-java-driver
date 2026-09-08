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
public final class QueryTrackingPropertiesEntity {

    private Boolean enabled;
    private Boolean trackSlowQueries;
    private Long maxSlowQueries;
    private Long slowQueryThreshold;
    private Long slowStreamingQueryThreshold;
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
     * @param enabled If set to true, then queries will be tracked. If set to false, neither queries nor slow queries
     *                will
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
     * @param trackSlowQueries If set to true, then slow queries will be tracked in the list of slow queries if their
     *                         runtime exceeds
     *                         the value set in slowQueryThreshold. In order for slow queries to be tracked, the
     *                         enabled property
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
     * @param maxSlowQueries The maximum number of slow queries to keep in the list of slow queries. If the list of
     *                       slow queries is
     *                       full, the oldest entry in it will be discarded when additional slow queries occur.
     */
    public void setMaxSlowQueries(final Long maxSlowQueries) {
        this.maxSlowQueries = maxSlowQueries;
    }

    /**
     * @return If the runtime of a regular query (in seconds) is greater or equal to this value, it is added to the list of
     * slow queries if slow query tracking is enabled.
     * @see #getSlowStreamingQueryThreshold()
     */
    public Long getSlowQueryThreshold() {
        return slowQueryThreshold;
    }

    /**
     * @param slowQueryThreshold If the runtime of a regular query (in seconds) is greater or equal to this value, it is
     *                           added to the list of slow queries if slow query tracking is enabled.
     * @see #setSlowStreamingQueryThreshold(Long)
     */
    public void setSlowQueryThreshold(final Long slowQueryThreshold) {
        this.slowQueryThreshold = slowQueryThreshold;
    }

    /**
     * @return If the runtime of a streaming query ({@code stream} set to {@code true}; in seconds) is greater or equal
     * to this value, it is added to the list of slow queries if slow query tracking is enabled.
     * @see #getSlowQueryThreshold()
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/queries/aql-queries/#get-the-aql-query-tracking-configuration">API Documentation</a>
     */
    public Long getSlowStreamingQueryThreshold() {
        return slowStreamingQueryThreshold;
    }

    /**
     * @param slowStreamingQueryThreshold The threshold value for treating a streaming query as slow (in seconds).
     *                                    A query with "stream" set to {@code true} and a runtime greater or equal to this
     *                                    threshold value is put into the list of slow queries if slow query tracking
     *                                    is enabled.
     *                                    Default: Controlled by the {@code --query.slow-streaming-threshold} startup option.
     * @see #setSlowQueryThreshold(Long)
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/queries/aql-queries/#update-the-aql-query-tracking-configuration">API Documentation</a>
     */
    public void setSlowStreamingQueryThreshold(final Long slowStreamingQueryThreshold) {
        this.slowStreamingQueryThreshold = slowStreamingQueryThreshold;
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
     * @param maxQueryStringLength The maximum query string length to keep in the list of queries. Query strings can
     *                             have arbitrary
     *                             lengths, and this property can be used to save memory in case very long query
     *                             strings are used. The
     *                             value is specified in bytes.
     */
    public void setMaxQueryStringLength(final Long maxQueryStringLength) {
        this.maxQueryStringLength = maxQueryStringLength;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof QueryTrackingPropertiesEntity)) return false;
        QueryTrackingPropertiesEntity that = (QueryTrackingPropertiesEntity) o;
        return Objects.equals(enabled, that.enabled) && Objects.equals(trackSlowQueries, that.trackSlowQueries) && Objects.equals(maxSlowQueries, that.maxSlowQueries) && Objects.equals(slowQueryThreshold, that.slowQueryThreshold) && Objects.equals(slowStreamingQueryThreshold, that.slowStreamingQueryThreshold) && Objects.equals(maxQueryStringLength, that.maxQueryStringLength);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, trackSlowQueries, maxSlowQueries, slowQueryThreshold, slowStreamingQueryThreshold, maxQueryStringLength);
    }
}
