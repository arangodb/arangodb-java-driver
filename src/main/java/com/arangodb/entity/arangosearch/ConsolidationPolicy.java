/*
 * DISCLAIMER
 *
 * Copyright 2018 ArangoDB GmbH, Cologne, Germany
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

package com.arangodb.entity.arangosearch;

import java.util.Objects;

/**
 * @author Mark Vollmary
 */
public final class ConsolidationPolicy {

    private ConsolidationType type;
    private Double threshold;
    private Long segmentThreshold;

    public ConsolidationPolicy() {
    }

    public static ConsolidationPolicy of(final ConsolidationType type) {
        return new ConsolidationPolicy().type(type);
    }

    public ConsolidationPolicy type(final ConsolidationType type) {
        this.type = type;
        return this;
    }

    /**
     * @param threshold Select a given segment for "consolidation" if and only if the formula based on type (as
     *                  defined above)
     *                  evaluates to true, valid value range [0.0, 1.0] (default: 0.85)
     * @return policy
     */
    public ConsolidationPolicy threshold(final Double threshold) {
        this.threshold = threshold;
        return this;
    }

    /**
     * @param segmentThreshold Apply the "consolidation" operation if and only if (default: 300): {segmentThreshold} <
     *                         number_of_segments
     * @return policy
     */
    public ConsolidationPolicy segmentThreshold(final Long segmentThreshold) {
        this.segmentThreshold = segmentThreshold;
        return this;
    }

    public ConsolidationType getType() {
        return type;
    }

    public Double getThreshold() {
        return threshold;
    }

    public Long getSegmentThreshold() {
        return segmentThreshold;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsolidationPolicy that = (ConsolidationPolicy) o;
        return type == that.type && Objects.equals(threshold, that.threshold) && Objects.equals(segmentThreshold, that.segmentThreshold);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, threshold, segmentThreshold);
    }
}
