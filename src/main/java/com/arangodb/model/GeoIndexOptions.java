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
 * @see <a href="https://www.arangodb.com/docs/stable/http/indexes-geo.html#create-geo-spatial-index">API Documentation</a>
 */
public class GeoIndexOptions extends IndexOptions<GeoIndexOptions> {

    private Iterable<String> fields;
    private final IndexType type = IndexType.geo;
    private Boolean geoJson;

    public GeoIndexOptions() {
        super();
    }

    @Override
    protected GeoIndexOptions getThis() {
        return this;
    }

    protected Iterable<String> getFields() {
        return fields;
    }

    /**
     * @param fields A list of attribute paths
     * @return options
     */
    protected GeoIndexOptions fields(final Iterable<String> fields) {
        this.fields = fields;
        return this;
    }

    protected IndexType getType() {
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

}
