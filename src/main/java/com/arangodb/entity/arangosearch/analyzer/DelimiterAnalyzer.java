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
 * An Analyzer capable of breaking up delimited text into tokens as per RFC 4180 (without starting new records on newlines).
 *
 * @author Michele Rastelli
 * @see <a href= "https://www.arangodb.com/docs/stable/arangosearch-analyzers.html#delimiter">API Documentation</a>
 */
public class DelimiterAnalyzer extends SearchAnalyzer {
    public DelimiterAnalyzer() {
        setType(AnalyzerType.delimiter);
    }

    private DelimiterAnalyzerProperties properties;

    public DelimiterAnalyzerProperties getProperties() {
        return properties;
    }

    public void setProperties(DelimiterAnalyzerProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DelimiterAnalyzer that = (DelimiterAnalyzer) o;
        return Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), properties);
    }
}
