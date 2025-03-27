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
import java.util.Collections;
import java.util.Objects;

/**
 * @author Mark Vollmary
 */
public final class EdgeDefinition {
    private String collection;
    private Collection<String> from;
    private Collection<String> to;
    private final Options options = new Options();

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

    public Collection<String> getSatellites() {
        return options.satellites;
    }

    public Options getOptions() {
        return options;
    }

    /**
     * @param satellites collection names that will be used to create SatelliteCollections
     *                   for a Hybrid (Disjoint) SmartGraph (Enterprise Edition only). Each array element
     *                   must be a valid collection name. The collection type cannot be modified later.
     * @return this
     * @since ArangoDB 3.9.0
     */
    public EdgeDefinition satellites(final String... satellites) {
        options.satellites = Arrays.asList(satellites);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EdgeDefinition)) return false;
        EdgeDefinition that = (EdgeDefinition) o;
        return Objects.equals(collection, that.collection) && Objects.equals(from, that.from) && Objects.equals(to, that.to) && Objects.equals(options, that.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(collection, from, to, options);
    }

    public static final class Options {
        private Collection<String> satellites = Collections.emptyList();

        public Collection<String> getSatellites() {
            return satellites;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Options)) return false;
            Options options = (Options) o;
            return Objects.equals(satellites, options.satellites);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(satellites);
        }
    }
}
