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
    private Long segmentsBytesMax;
    private Double maxSkewThreshold;
    private Double minDeletionRatio;

    public static ConsolidationPolicy of(final ConsolidationType type) {
        return new ConsolidationPolicy().type(type);
    }

    public ConsolidationPolicy type(final ConsolidationType type) {
        this.type = type;
        return this;
    }

    /**
     * @param threshold value in the range [0.0, 1.0]
     * @return this
     */
    public ConsolidationPolicy threshold(final Double threshold) {
        this.threshold = threshold;
        return this;
    }

    public ConsolidationType getType() {
        return type;
    }

    public Double getThreshold() {
        return threshold;
    }

    public Long getSegmentsBytesMax() {
        return segmentsBytesMax;
    }

    /**
     * @param segmentsBytesMax Maximum allowed size of all consolidated segments in bytes. (default: 5368709120)
     * @return this
     */
    public ConsolidationPolicy segmentsBytesMax(final Long segmentsBytesMax) {
        this.segmentsBytesMax = segmentsBytesMax;
        return this;
    }

    public Double getMaxSkewThreshold() {
        return maxSkewThreshold;
    }

    /**
     * @param maxSkewThreshold The maximum allowed skew in segment sizes to allow consolidation.
     *                         (default: 0.4)
     * @return this
     * @since ArangoDB 3.12.7
     */
    public ConsolidationPolicy maxSkewThreshold(final Double maxSkewThreshold) {
        this.maxSkewThreshold = maxSkewThreshold;
        return this;
    }

    public Double getMinDeletionRatio() {
        return minDeletionRatio;
    }

    /**
     * @param minDeletionRatio The minimum required percentage of total deleted documents in a
     *                         segment (or a group of segments) to execute cleanup on those segments.
     *                         (default: 0.5)
     * @return this
     * @since ArangoDB 3.12.7
     */
    public ConsolidationPolicy minDeletionRatio(final Double minDeletionRatio) {
        this.minDeletionRatio = minDeletionRatio;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ConsolidationPolicy that = (ConsolidationPolicy) o;
        return type == that.type && Objects.equals(threshold, that.threshold) && Objects.equals(segmentsBytesMax, that.segmentsBytesMax) && Objects.equals(maxSkewThreshold, that.maxSkewThreshold) && Objects.equals(minDeletionRatio, that.minDeletionRatio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, threshold, segmentsBytesMax, maxSkewThreshold, minDeletionRatio);
    }

    @Override
    public String toString() {
        return "ConsolidationPolicy{" +
                "type=" + type +
                ", threshold=" + threshold +
                ", segmentsBytesMax=" + segmentsBytesMax +
                ", maxSkewThreshold=" + maxSkewThreshold +
                ", minDeletionRatio=" + minDeletionRatio +
                '}';
    }
}
