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
public final class WildcardAnalyzerProperties {

    private Integer ngramSize;
    private SearchAnalyzer analyzer;

    /**
     * @return unsigned integer for the n-gram length, needs to be at least 2
     */
    public Integer getNgramSize() {
        return ngramSize;
    }

    /**
     * @param ngramSize unsigned integer for the n-gram length, needs to be at least 2
     */
    public void setNgramSize(Integer ngramSize) {
        this.ngramSize = ngramSize;
    }

    public SearchAnalyzer getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(SearchAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WildcardAnalyzerProperties that = (WildcardAnalyzerProperties) o;
        return Objects.equals(ngramSize, that.ngramSize) && Objects.equals(analyzer, that.analyzer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ngramSize, analyzer);
    }
}
