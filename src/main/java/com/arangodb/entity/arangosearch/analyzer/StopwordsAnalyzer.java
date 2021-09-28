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
 * WARNING:
 * The implementation of Stopwords analyzer is not final in ArangoDB 3.8.0, so using it might result in unpredictable behavior.
 * This will be fixed in ArangoDB 3.8.1 and will have a different API.
 * Any usage of the current Java driver API related to it is therefore discouraged.
 * See related <a href="https://github.com/arangodb/arangodb-java-driver/issues/394">bug report</a>
 * <p>
 * <p>
 * <p>
 * An Analyzer capable of removing specified tokens from the input.
 *
 * @author Michele Rastelli
 * @see <a href= "https://www.arangodb.com/docs/stable/arangosearch-analyzers.html#stopwords">API Documentation</a>
 * @since ArangoDB 3.8
 */
public class StopwordsAnalyzer extends SearchAnalyzer {
    public StopwordsAnalyzer() {
        setType(AnalyzerType.stopwords);
    }

    private StopwordsAnalyzerProperties properties;

    public StopwordsAnalyzerProperties getProperties() {
        return properties;
    }

    public void setProperties(StopwordsAnalyzerProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        StopwordsAnalyzer that = (StopwordsAnalyzer) o;
        return Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), properties);
    }
}
