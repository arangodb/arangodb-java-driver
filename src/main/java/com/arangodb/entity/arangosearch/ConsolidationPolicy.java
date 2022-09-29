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
public class ConsolidationPolicy {

    private ConsolidationType type;
    private Double threshold;
    @Deprecated
    private Long segmentThreshold;
    private Long segmentsMin;
    private Long segmentsMax;
    private Long segmentsBytesMax;
    private Long segmentsBytesFloor;
    private Long minScore;


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
     * @param threshold Defines threshold value of [0.0, 1.0] possible range. Consolidation is performed on segments
     *                  which accumulated size in bytes is less than all segments’ byte size multiplied by the
     *                  threshold; i.e. the following formula is applied for each segment:
     *                  {threshold} > (segment_bytes + sum_of_merge_candidate_segment_bytes) / all_segment_bytes.
     *                  (default: 0.1)
     * @return this
     */
    public ConsolidationPolicy threshold(final Double threshold) {
        this.threshold = threshold;
        return this;
    }

    /**
     * @param segmentThreshold Apply the "consolidation" operation if and only if (default: 300): {segmentThreshold} <
     *                         number_of_segments
     * @return this
     * @deprecated
     */
    @Deprecated
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

    @Deprecated
    public Long getSegmentThreshold() {
        return segmentThreshold;
    }

    public Long getSegmentsMin() {
        return segmentsMin;
    }

    /**
     * @param segmentsMin The minimum number of segments that will be evaluated as candidates for consolidation. (default: 1)
     * @return this
     */
    public ConsolidationPolicy segmentsMin(final Long segmentsMin) {
        this.segmentsMin = segmentsMin;
        return this;
    }

    public Long getSegmentsMax() {
        return segmentsMax;
    }

    /**
     * @param segmentsMax The maximum number of segments that will be evaluated as candidates for consolidation. (default: 10)
     * @return this
     */
    public ConsolidationPolicy segmentsMax(final Long segmentsMax) {
        this.segmentsMax = segmentsMax;
        return this;
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

    public Long getSegmentsBytesFloor() {
        return segmentsBytesFloor;
    }

    /**
     * @param segmentsBytesFloor Defines the value (in bytes) to treat all smaller segments as equal for consolidation selection. (default: 2097152)
     * @return this
     */
    public ConsolidationPolicy segmentsBytesFloor(final Long segmentsBytesFloor) {
        this.segmentsBytesFloor = segmentsBytesFloor;
        return this;
    }

    public Long getMinScore() {
        return minScore;
    }

    /**
     * @param minScore Filter out consolidation candidates with a score less than this. (default: 0)
     * @return this
     */
    public ConsolidationPolicy minScore(final Long minScore) {
        this.minScore = minScore;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsolidationPolicy that = (ConsolidationPolicy) o;
        return type == that.type && Objects.equals(threshold, that.threshold) && Objects.equals(segmentThreshold, that.segmentThreshold) && Objects.equals(segmentsMin, that.segmentsMin) && Objects.equals(segmentsMax, that.segmentsMax) && Objects.equals(segmentsBytesMax, that.segmentsBytesMax) && Objects.equals(segmentsBytesFloor, that.segmentsBytesFloor) && Objects.equals(minScore, that.minScore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, threshold, segmentThreshold, segmentsMin, segmentsMax, segmentsBytesMax, segmentsBytesFloor, minScore);
    }
}
