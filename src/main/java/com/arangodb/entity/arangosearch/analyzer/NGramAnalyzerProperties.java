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


package com.arangodb.entity.arangosearch.analyzer;

import java.util.Objects;

/**
 * An Analyzer capable of producing n-grams from a specified input in a range of min..max (inclusive). Can optionally
 * preserve the original input.
 * <p>
 * This Analyzer type can be used to implement substring matching. Note that it slices the input based on bytes and not
 * characters by default (streamType). The “binary” mode supports single-byte characters only; multi-byte UTF-8
 * characters raise an Invalid UTF-8 sequence query error.
 *
 * @author Michele Rastelli
 * @see <a href= "https://www.arangodb.com/docs/stable/arangosearch-analyzers.html#n-gram">API Documentation</a>
 */
public class NGramAnalyzerProperties {

    private long min;
    private long max;
    private boolean preserveOriginal;
    private String startMarker;
    private String endMarker;
    private StreamType streamType;

    public NGramAnalyzerProperties() {
        startMarker = "";
        endMarker = "";
        streamType = StreamType.binary;
    }

    /**
     * @return minimum n-gram length
     */
    public long getMin() {
        return min;
    }

    public void setMin(long min) {
        this.min = min;
    }

    /**
     * @return maximum n-gram length
     */
    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    /**
     * @return <code>true</code> to include the original value as well
     *         <code>false</code> to produce the n-grams based on min and max only
     */
    public boolean isPreserveOriginal() {
        return preserveOriginal;
    }

    public void setPreserveOriginal(boolean preserveOriginal) {
        this.preserveOriginal = preserveOriginal;
    }

    /**
     * @return this value will be prepended to n-grams which include the beginning of the input. Can be used for
     * matching prefixes. Choose a character or sequence as marker which does not occur in the input
     */
    public String getStartMarker() {
        return startMarker;
    }

    public void setStartMarker(String startMarker) {
        this.startMarker = startMarker;
    }

    /**
     * @return this value will be appended to n-grams which include the end of the input. Can be used for matching
     * suffixes. Choose a character or sequence as marker which does not occur in the input.
     */
    public String getEndMarker() {
        return endMarker;
    }

    public void setEndMarker(String endMarker) {
        this.endMarker = endMarker;
    }

    public StreamType getStreamType() {
        return streamType;
    }

    public void setStreamType(StreamType streamType) {
        this.streamType = streamType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NGramAnalyzerProperties that = (NGramAnalyzerProperties) o;
        return min == that.min &&
                max == that.max &&
                preserveOriginal == that.preserveOriginal &&
                Objects.equals(startMarker, that.startMarker) &&
                Objects.equals(endMarker, that.endMarker) &&
                streamType == that.streamType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max, preserveOriginal, startMarker, endMarker, streamType);
    }
}
