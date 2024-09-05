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
public final class GeoJSONAnalyzerProperties {

    private GeoJSONAnalyzerType type;
    private GeoAnalyzerOptions options;
    private Boolean legacy;

    public GeoJSONAnalyzerType getType() {
        return type;
    }

    public void setType(GeoJSONAnalyzerType type) {
        this.type = type;
    }

    /**
     * @return Options for fine-tuning geo queries {@link GeoJSONAnalyzerProperties}. These options should generally
     * remain unchanged.
     */
    public GeoAnalyzerOptions getOptions() {
        return options;
    }

    public void setOptions(GeoAnalyzerOptions options) {
        this.options = options;
    }

    /**
     * @return This option controls how GeoJSON Polygons are interpreted (introduced in v3.10.5).
     * - If `legacy` is `true`, the smaller of the two regions defined by a
     * linear ring is interpreted as the interior of the ring and a ring can at most
     * enclose half the Earth's surface.
     * - If `legacy` is `false`, the area to the left of the boundary ring's
     * path is considered to be the interior and a ring can enclose the entire
     * surface of the Earth.
     * <p>
     * The default is `false`.
     */
    public Boolean getLegacy() {
        return legacy;
    }

    public void setLegacy(Boolean legacy) {
        this.legacy = legacy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeoJSONAnalyzerProperties that = (GeoJSONAnalyzerProperties) o;
        return type == that.type && Objects.equals(options, that.options) && Objects.equals(legacy, that.legacy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, options, legacy);
    }

    public enum GeoJSONAnalyzerType {

        /**
         * (default): index all GeoJSON geometry types (Point, Polygon etc.)
         */
        shape,

        /**
         * compute and only index the centroid of the input geometry
         */
        centroid,

        /**
         * only index GeoJSON objects of type Point, ignore all other geometry types
         */
        point
    }
}
