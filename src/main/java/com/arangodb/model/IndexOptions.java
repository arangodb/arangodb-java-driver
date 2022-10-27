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

package com.arangodb.model;

/**
 * @author Heiko Kernbach
 * <p>
 * This final class is used for all index similarities
 */
public abstract class IndexOptions<T extends IndexOptions<T>> {

    private Boolean inBackground;
    private String name;

    protected IndexOptions() {
    }

    abstract T getThis();

    /**
     * @param inBackground create the the index in the background
     *                     this is a RocksDB only flag.
     * @return options
     */
    public T inBackground(final Boolean inBackground) {
        this.inBackground = inBackground;
        return getThis();
    }

    public Boolean getInBackground() {
        return inBackground;
    }

    /**
     * @param name the name of the index
     * @return options
     */
    public T name(final String name) {
        this.name = name;
        return getThis();
    }

    public String getName() {
        return name;
    }
}
