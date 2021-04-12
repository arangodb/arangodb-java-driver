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
 * An Analyzer capable of breaking up a GeoJSON object into a set of indexable tokens for further usage with
 * ArangoSearch Geo functions.
 *
 * @author Michele Rastelli
 * @see <a href= "https://www.arangodb.com/docs/stable/arangosearch-analyzers.html#geojson">API Documentation</a>
 * @since ArangoDB 3.8
 */
public class GeoJSONAnalyzer extends SearchAnalyzer {
    public GeoJSONAnalyzer() {
        setType(AnalyzerType.geojson);
    }

    private GeoJSONAnalyzerProperties properties;

    public GeoJSONAnalyzerProperties getProperties() {
        return properties;
    }

    public void setProperties(GeoJSONAnalyzerProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GeoJSONAnalyzer that = (GeoJSONAnalyzer) o;
        return Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), properties);
    }
}
