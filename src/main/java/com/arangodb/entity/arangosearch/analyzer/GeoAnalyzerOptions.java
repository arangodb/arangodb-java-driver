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
public class GeoAnalyzerOptions {

    private Integer maxCells;
    private Integer minLevel;
    private Integer maxLevel;

    /**
     * @return maximum number of S2 cells (default: 20)
     */
    public Integer getMaxCells() {
        return maxCells;
    }

    public void setMaxCells(Integer maxCells) {
        this.maxCells = maxCells;
    }

    /**
     * @return the least precise S2 level (default: 4)
     */
    public Integer getMinLevel() {
        return minLevel;
    }

    public void setMinLevel(Integer minLevel) {
        this.minLevel = minLevel;
    }

    /**
     * @return the most precise S2 level (default: 23)
     */
    public Integer getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(Integer maxLevel) {
        this.maxLevel = maxLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeoAnalyzerOptions that = (GeoAnalyzerOptions) o;
        return Objects.equals(maxCells, that.maxCells) && Objects.equals(minLevel, that.minLevel) && Objects.equals(maxLevel, that.maxLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxCells, minLevel, maxLevel);
    }
}
