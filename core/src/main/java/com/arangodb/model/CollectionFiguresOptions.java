/*
 * DISCLAIMER
 *
 * Copyright 2026 ArangoDB GmbH, Cologne, Germany
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

/**
 * Options for retrieving collection statistics.
 */
public final class CollectionFiguresOptions {

    private Boolean details;

    /**
     * @return whether to include extended storage-engine details, or null to use the server default
     */
    public Boolean getDetails() {
        return details;
    }

    /**
     * Controls whether extended storage-engine details are included. The server default is false.
     * These details are intended for debugging ArangoDB, their format is subject to change,
     * and requesting them may add load to the server.
     *
     * @param details whether to include extended details, or null to use the server default
     * @return this
     * @since ArangoDB 3.8.0
     */
    public CollectionFiguresOptions details(final Boolean details) {
        this.details = details;
        return this;
    }
}
