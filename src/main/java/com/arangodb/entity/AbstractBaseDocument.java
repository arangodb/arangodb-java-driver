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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
abstract class AbstractBaseDocument {

    private static final String[] META_PROPS = new String[]{"_id", "_key", "_rev"};
    private final Map<String, Object> properties;

    AbstractBaseDocument() {
        properties = new HashMap<>();
    }

    AbstractBaseDocument(final String key) {
        this();
        setKey(key);
    }

    AbstractBaseDocument(final Map<String, Object> properties) {
        this();
        setProperties(properties);
    }

    @JsonIgnore
    public String getId() {
        return (String) getAttribute("_id");
    }

    public void setId(final String id) {
        addAttribute("_id", id);
    }

    @JsonIgnore
    public String getKey() {
        return (String) getAttribute("_key");
    }

    public void setKey(final String key) {
        addAttribute("_key", key);
    }

    @JsonIgnore
    public String getRevision() {
        return (String) getAttribute("_rev");
    }

    public void setRevision(final String rev) {
        addAttribute("_rev", rev);
    }

    @JsonInclude
    @JsonAnyGetter
    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    public void setProperties(final Map<String, Object> props) {
        for (String f : getMetaProps()) {
            requireString(f, props.get(f));
        }
        this.properties.putAll(props);
    }

    public Object getAttribute(final String key) {
        return properties.get(key);
    }

    @JsonInclude
    @JsonAnySetter
    public void addAttribute(final String key, final Object value) {
        for (String f : getMetaProps()) {
            if (f.equals(key)) {
                requireString(key, value);
            }
        }
        properties.put(key, value);
    }

    public void updateAttribute(final String key, final Object value) {
        if (properties.containsKey(key)) {
            addAttribute(key, value);
        }
    }

    public void removeAttribute(final String key) {
        properties.remove(key);
    }

    protected String[] getMetaProps() {
        return META_PROPS;
    }

    private void requireString(final String k, final Object v) {
        if (v != null && !(v instanceof String)) {
            throw new IllegalArgumentException(k + " must be a String");
        }
    }

    @Override
    public String toString() {
        return "{" +
                "properties=" + properties +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractBaseDocument that = (AbstractBaseDocument) o;
        return Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(properties);
    }
}
