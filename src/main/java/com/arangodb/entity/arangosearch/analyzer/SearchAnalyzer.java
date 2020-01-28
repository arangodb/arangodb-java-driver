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


import com.arangodb.entity.arangosearch.AnalyzerFeature;
import com.arangodb.entity.arangosearch.AnalyzerType;

import java.util.Objects;
import java.util.Set;

/**
 * @author Michele Rastelli
 */
public abstract class SearchAnalyzer {
    private String name;
    private AnalyzerType type;
    private Set<AnalyzerFeature> features;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AnalyzerType getType() {
        return type;
    }

    public void setType(AnalyzerType type) {
        this.type = type;
    }

    public Set<AnalyzerFeature> getFeatures() {
        return features;
    }

    public void setFeatures(Set<AnalyzerFeature> features) {
        this.features = features;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchAnalyzer analyzer = (SearchAnalyzer) o;
        return Objects.equals(name, analyzer.name) &&
                type == analyzer.type &&
                Objects.equals(features, analyzer.features);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, features);
    }
}
