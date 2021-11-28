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


import com.arangodb.velocypack.annotations.SerializedName;

import java.util.Objects;

/**
 * @author Michele Rastelli
 * @since ArangoDB 3.9
 */
public class SegmentationAnalyzerProperties {

    @SerializedName("break")
    private BreakMode breakMode;

    @SerializedName("case")
    private SearchAnalyzerCase analyzerCase;

    public BreakMode getBreakMode() {
        return breakMode;
    }

    /**
     * @param breakMode defaults to {@link BreakMode#alpha}
     */
    public void setBreakMode(BreakMode breakMode) {
        this.breakMode = breakMode;
    }

    public SearchAnalyzerCase getAnalyzerCase() {
        return analyzerCase;
    }

    /**
     * @param analyzerCase defaults to {@link SearchAnalyzerCase#lower}
     */
    public void setAnalyzerCase(SearchAnalyzerCase analyzerCase) {
        this.analyzerCase = analyzerCase;
    }

    public enum BreakMode {
        all, alpha, graphic
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SegmentationAnalyzerProperties that = (SegmentationAnalyzerProperties) o;
        return breakMode == that.breakMode && analyzerCase == that.analyzerCase;
    }

    @Override
    public int hashCode() {
        return Objects.hash(breakMode, analyzerCase);
    }
}
