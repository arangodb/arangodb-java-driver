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

package com.arangodb.entity;

import com.arangodb.model.MDIFieldValueTypes;

import java.util.Collection;
import java.util.Objects;

/**
 * @author Mark Vollmary
 */
public final class IndexEntity {

    private String id;
    private String name;
    private IndexType type;
    private Collection<String> fields;
    private Double selectivityEstimate;
    private Boolean unique;
    private Boolean sparse;
    private Integer minLength;
    private Boolean isNewlyCreated;
    private Boolean geoJson;
    private Boolean constraint;
    private Boolean deduplicate;
    private Integer expireAfter;
    private Boolean inBackground;
    private Boolean estimates;
    private Boolean cacheEnabled;
    private Collection<String> storedValues;
    private Boolean legacyPolygons;
    private MDIFieldValueTypes fieldValueTypes;
    private Collection<String> prefixFields;
    private VectorIndexParams params;

    public IndexEntity() {
        super();
    }

    public String getId() {
        return id;
    }

    public Boolean getInBackground() {
        return inBackground;
    }

    public String getName() {
        return name;
    }

    public IndexType getType() {
        return type;
    }

    public Collection<String> getFields() {
        return fields;
    }

    public Double getSelectivityEstimate() {
        return selectivityEstimate;
    }

    public Boolean getUnique() {
        return unique;
    }

    public Boolean getSparse() {
        return sparse;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public Boolean getIsNewlyCreated() {
        return isNewlyCreated;
    }

    public Boolean getGeoJson() {
        return geoJson;
    }

    public Integer getExpireAfter() {
        return expireAfter;
    }

    public Boolean getConstraint() {
        return constraint;
    }

    public Boolean getDeduplicate() {
        return deduplicate;
    }

    public Boolean getEstimates() {
        return estimates;
    }

    public Boolean getCacheEnabled() {
        return cacheEnabled;
    }

    public Collection<String> getStoredValues() {
        return storedValues;
    }

    public Boolean getLegacyPolygons() {
        return legacyPolygons;
    }

    public MDIFieldValueTypes getFieldValueTypes() {
        return fieldValueTypes;
    }

    public Collection<String> getPrefixFields() {
        return prefixFields;
    }

    public VectorIndexParams getParams() {
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof IndexEntity)) return false;
        IndexEntity that = (IndexEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && type == that.type && Objects.equals(fields, that.fields) && Objects.equals(selectivityEstimate, that.selectivityEstimate) && Objects.equals(unique, that.unique) && Objects.equals(sparse, that.sparse) && Objects.equals(minLength, that.minLength) && Objects.equals(isNewlyCreated, that.isNewlyCreated) && Objects.equals(geoJson, that.geoJson) && Objects.equals(constraint, that.constraint) && Objects.equals(deduplicate, that.deduplicate) && Objects.equals(expireAfter, that.expireAfter) && Objects.equals(inBackground, that.inBackground) && Objects.equals(estimates, that.estimates) && Objects.equals(cacheEnabled, that.cacheEnabled) && Objects.equals(storedValues, that.storedValues) && Objects.equals(legacyPolygons, that.legacyPolygons) && fieldValueTypes == that.fieldValueTypes && Objects.equals(prefixFields, that.prefixFields) && Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type, fields, selectivityEstimate, unique, sparse, minLength, isNewlyCreated, geoJson, constraint, deduplicate, expireAfter, inBackground, estimates, cacheEnabled, storedValues, legacyPolygons, fieldValueTypes, prefixFields, params);
    }
}
