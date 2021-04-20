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


import java.util.Arrays;
import java.util.Objects;

/**
 * @author Michele Rastelli
 */
public class GeoPointAnalyzerProperties {

    private String[] latitude;
    private String[] longitude;
    private GeoAnalyzerOptions options;

    /**
     * @return array of strings that describes the attribute path of the latitude value relative to the field for which
     * the Analyzer is defined in the View
     */
    public String[] getLatitude() {
        return latitude;
    }

    public void setLatitude(String[] latitude) {
        this.latitude = latitude;
    }

    /**
     * @return array of strings that describes the attribute path of the longitude value relative to the field for which
     * the Analyzer is defined in the View
     */
    public String[] getLongitude() {
        return longitude;
    }

    public void setLongitude(String[] longitude) {
        this.longitude = longitude;
    }

    /**
     * @return Options for fine-tuning geo queries {@link GeoPointAnalyzerProperties}. These options should generally
     * remain unchanged.
     */
    public GeoAnalyzerOptions getOptions() {
        return options;
    }

    public void setOptions(GeoAnalyzerOptions options) {
        this.options = options;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeoPointAnalyzerProperties that = (GeoPointAnalyzerProperties) o;
        return Arrays.equals(latitude, that.latitude) && Arrays.equals(longitude, that.longitude) && Objects.equals(options, that.options);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(options);
        result = 31 * result + Arrays.hashCode(latitude);
        result = 31 * result + Arrays.hashCode(longitude);
        return result;
    }
}
