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

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Mark Vollmary
 */
public final class VertexCollectionCreateOptions {

    private final Options options = new Options();
    private String collection;

    public VertexCollectionCreateOptions() {
        super();
    }

    public String getCollection() {
        return collection;
    }

    /**
     * @param collection The name of the collection
     * @return options
     */
    VertexCollectionCreateOptions collection(final String collection) {
        this.collection = collection;
        return this;
    }

    public Options getOptions() {
        return options;
    }

    public Collection<String> getSatellites() {
        return options.satellites;
    }

    /**
     * @param satellites collection names that will be used to create SatelliteCollections
     *                   for a Hybrid (Disjoint) SmartGraph (Enterprise Edition only). Each array element
     *                   must be a valid collection name. The collection type cannot be modified later.
     * @return options
     * @since ArangoDB 3.9.0
     */
    public VertexCollectionCreateOptions satellites(final String... satellites) {
        options.satellites = Arrays.asList(satellites);
        return this;
    }

    public static final class Options {
        private Collection<String> satellites;

        public Collection<String> getSatellites() {
            return satellites;
        }
    }

}
