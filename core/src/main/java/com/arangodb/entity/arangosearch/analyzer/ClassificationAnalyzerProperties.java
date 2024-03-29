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


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * @author Michele Rastelli
 * @since ArangoDB 3.10
 */
public final class ClassificationAnalyzerProperties {

    @JsonProperty("model_location")
    private String modelLocation;

    @JsonProperty("top_k")
    private Integer topK;

    private Double threshold;

    public String getModelLocation() {
        return modelLocation;
    }

    public void setModelLocation(String modelLocation) {
        this.modelLocation = modelLocation;
    }

    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassificationAnalyzerProperties that = (ClassificationAnalyzerProperties) o;
        return Objects.equals(modelLocation, that.modelLocation) && Objects.equals(topK, that.topK) && Objects.equals(threshold, that.threshold);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelLocation, topK, threshold);
    }
}
