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

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Mark Vollmary
 * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-management.html#create-a-graph">API Documentation</a>
 */
public class EdgeDefinition {

    private String collection;
    private Collection<String> from;
    private Collection<String> to;

    public String getCollection() {
        return collection;
    }

    public EdgeDefinition collection(final String collection) {
        this.collection = collection;
        return this;
    }

    public Collection<String> getFrom() {
        return from;
    }

    public EdgeDefinition from(final String... from) {
        this.from = Arrays.asList(from);
        return this;
    }

    public Collection<String> getTo() {
        return to;
    }

    public EdgeDefinition to(final String... to) {
        this.to = Arrays.asList(to);
        return this;
    }

}
