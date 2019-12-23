/*
 * DISCLAIMER
 *
 * Copyright 2019 ArangoDB GmbH, Cologne, Germany
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

package com.arangodb.entity.arangosearch;

/**
 * @author Heiko Kernbach
 */
public class PrimarySort {

    private final String fieldName;
    private Boolean ascending;

    private PrimarySort(final String fieldName) {
        super();
        this.fieldName = fieldName;
    }

    public static PrimarySort on(final String fieldName) {
        return new PrimarySort(fieldName);
    }

    /**
     * @param ascending
     * @return primarySort
     */
    public PrimarySort ascending(final Boolean ascending) {
        this.ascending = ascending;
        return this;
    }

    public Boolean getAscending() {
        return ascending;
    }

    public String getFieldName() {
        return fieldName;
    }
}
