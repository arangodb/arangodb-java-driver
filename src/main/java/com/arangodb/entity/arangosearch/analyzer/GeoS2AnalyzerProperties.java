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
public final class GeoS2AnalyzerProperties {

    private GeoS2AnalyzerType type;
    private GeoAnalyzerOptions options;
    private GeoS2Format format;

    public GeoS2AnalyzerType getType() {
        return type;
    }

    public void setType(GeoS2AnalyzerType type) {
        this.type = type;
    }

    /**
     * @return Options for fine-tuning geo queries {@link GeoS2AnalyzerProperties}. These options should generally
     * remain unchanged.
     */
    public GeoAnalyzerOptions getOptions() {
        return options;
    }

    public void setOptions(GeoAnalyzerOptions options) {
        this.options = options;
    }

    /**
     * @return The internal binary representation to use for storing the geo-spatial data in an index.
     */
    public GeoS2Format getFormat() {
        return format;
    }

    public void setFormat(GeoS2Format format) {
        this.format = format;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeoS2AnalyzerProperties that = (GeoS2AnalyzerProperties) o;
        return type == that.type && Objects.equals(options, that.options) && format == that.format;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, options, format);
    }

    public enum GeoS2AnalyzerType {

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

    public enum GeoS2Format {
        /**
         * Store each latitude and longitude value as an 8-byte floating-point value (16 bytes per coordinate pair).
         * This format preserves numeric values exactly and is more compact than the VelocyPack format used by
         * {@link GeoJSONAnalyzer}. (default)
         */
        latLngDouble,

        /**
         * Store each latitude and longitude value as an 4-byte integer value (8 bytes per coordinate pair). This is the
         * most compact format but the precision is limited to approximately 1 to 10 centimeters.
         */
        latLngInt,

        /**
         * Store each longitude-latitude pair in the native format of Google S2 which is used for geo-spatial
         * calculations (24 bytes per coordinate pair). This is not a particular compact format but it reduces the
         * number of computations necessary when you execute geo-spatial queries. This format preserves numeric values
         * exactly.
         */
        s2Point
    }
}
