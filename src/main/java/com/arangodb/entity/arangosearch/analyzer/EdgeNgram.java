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
public class EdgeNgram {
    private long min;
    private long max;
    private boolean preserveOriginal;

    /**
     * @return minimal n-gram length
     */
    public long getMin() {
        return min;
    }

    public void setMin(long min) {
        this.min = min;
    }

    /**
     * @return maximal n-gram length
     */
    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    /**
     * @return whether to include the original token even if its length is less than min or greater than max
     */
    public boolean isPreserveOriginal() {
        return preserveOriginal;
    }

    public void setPreserveOriginal(boolean preserveOriginal) {
        this.preserveOriginal = preserveOriginal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EdgeNgram edgeNgram = (EdgeNgram) o;
        return min == edgeNgram.min &&
                max == edgeNgram.max &&
                preserveOriginal == edgeNgram.preserveOriginal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max, preserveOriginal);
    }
}
