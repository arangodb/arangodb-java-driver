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

package com.arangodb.model;

import com.arangodb.entity.IndexType;

/**
 * @author Mark Vollmary
 * @see
 * <a href="https://www.arangodb.com/docs/stable/http/indexes-geo.html#create-geo-spatial-index">API Documentation</a>
 */
public final class GeoIndexOptions extends IndexOptions<GeoIndexOptions> {

    private final IndexType type = IndexType.geo;
    private Iterable<String> fields;
    private Boolean geoJson;
    private Boolean legacyPolygons;

    public GeoIndexOptions() {
        super();
    }

    @Override
    GeoIndexOptions getThis() {
        return this;
    }

    public Iterable<String> getFields() {
        return fields;
    }

    /**
     * @param fields A list of attribute paths
     * @return options
     */
    GeoIndexOptions fields(final Iterable<String> fields) {
        this.fields = fields;
        return this;
    }

    public IndexType getType() {
        return type;
    }

    public Boolean getGeoJson() {
        return geoJson;
    }

    /**
     * @param geoJson If a geo-spatial index on a location is constructed and geoJson is true, then the order within the
     *                array is longitude followed by latitude. This corresponds to the format described in
     * @return options
     */
    public GeoIndexOptions geoJson(final Boolean geoJson) {
        this.geoJson = geoJson;
        return this;
    }

    public Boolean getLegacyPolygons() {
        return legacyPolygons;
    }

    /**
     * @param legacyPolygons If `true` will use the old rules (pre-3.10) for the parsing GeoJSON polygons. This
     *                       allows you to let old indexes produce the same, potentially wrong results as before an
     *                       upgrade. A geo index with `legacyPolygons` set to `false` will use the new, correct and
     *                       consistent method for parsing of GeoJSON polygons.
     *                       See <a href="https://www.arangodb.com/docs/stable/indexing-geo.html#legacy-polygons">Legacy Polygons</a>.
     * @return options
     * @since ArangoDB 3.10
     */
    public GeoIndexOptions legacyPolygons(final Boolean legacyPolygons) {
        this.legacyPolygons = legacyPolygons;
        return this;
    }
}
