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
public class NGramAnalyzer extends SearchAnalyzer {
    public NGramAnalyzer() {
        setType(AnalyzerType.ngram);
    }

    private NGramAnalyzerProperties properties;

    public NGramAnalyzerProperties getProperties() {
        return properties;
    }

    public void setProperties(NGramAnalyzerProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NGramAnalyzer that = (NGramAnalyzer) o;
        return Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), properties);
    }
}
