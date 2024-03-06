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


import com.arangodb.entity.arangosearch.AnalyzerType;

import java.util.Objects;

/**
 * An Analyzer that creates n-grams to enable fast partial matching for wildcard queries if you have large string
 * values, especially if you want to search for suffixes or substrings in the middle of strings (infixes) as opposed to
 * prefixes.
 * It can apply an Analyzer of your choice before creating the n-grams, for example, to normalize text for
 * case-insensitive and accent-insensitive search.
 *
 * @author Michele Rastelli
 * @see <a href= "https://docs.arangodb.com/devel/index-and-search/analyzers/#wildcard">API Documentation</a>
 */
public final class WildcardAnalyzer extends SearchAnalyzer {
    private WildcardAnalyzerProperties properties;

    public WildcardAnalyzer() {
        setType(AnalyzerType.wildcard);
    }

    public WildcardAnalyzerProperties getProperties() {
        return properties;
    }

    public void setProperties(WildcardAnalyzerProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        WildcardAnalyzer that = (WildcardAnalyzer) o;
        return Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), properties);
    }
}
