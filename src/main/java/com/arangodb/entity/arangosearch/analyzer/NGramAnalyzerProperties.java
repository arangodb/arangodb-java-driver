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
 * @author Michele Rastelli
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

    public long getMin() {
        return min;
    }

    public void setMin(long min) {
        this.min = min;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public boolean isPreserveOriginal() {
        return preserveOriginal;
    }

    public void setPreserveOriginal(boolean preserveOriginal) {
        this.preserveOriginal = preserveOriginal;
    }

    public String getStartMarker() {
        return startMarker;
    }

    public void setStartMarker(String startMarker) {
        this.startMarker = startMarker;
    }

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
