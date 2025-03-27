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

import java.util.Objects;

/**
 * @author Michele Rastelli
 */
public final class ArangoDBEngine {

    private StorageEngineName name;

    public ArangoDBEngine() {
        super();
    }

    /**
     * @return the storage engine name
     */
    public StorageEngineName getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ArangoDBEngine)) return false;
        ArangoDBEngine that = (ArangoDBEngine) o;
        return name == that.name;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    public enum StorageEngineName {
        mmfiles, rocksdb
    }

}
