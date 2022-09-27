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

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public final class BaseEdgeDocument extends AbstractBaseDocument {

    private static final String[] META_PROPS = new String[]{"_id", "_key", "_rev", "_from", "_to"};

    public BaseEdgeDocument() {
        super();
    }

    public BaseEdgeDocument(final String from, final String to) {
        super();
        setFrom(from);
        setTo(to);
    }

    public BaseEdgeDocument(final String key, final String from, final String to) {
        super(key);
        setFrom(from);
        setTo(to);
    }

    public BaseEdgeDocument(final Map<String, Object> properties) {
        super(properties);
    }

    @JsonIgnore
    public String getFrom() {
        return (String) getAttribute("_from");
    }

    public void setFrom(final String from) {
        addAttribute("_from", from);
    }

    @JsonIgnore
    public String getTo() {
        return (String) getAttribute("_to");
    }

    public void setTo(final String to) {
        addAttribute("_to", to);
    }

    @Override
    protected String[] getMetaProps() {
        return META_PROPS;
    }

    @Override
    public String toString() {
        return "BaseEdgeDocument" + super.toString();
    }

}
