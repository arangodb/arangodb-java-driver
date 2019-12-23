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

/**
 * @author Michele Rastelli
 * @see <a href="https://www.arangodb.com/docs/stable/http/miscellaneous-functions.html#return-server-database-engine-type">API
 * Documentation</a>
 */
public class ArangoDBEngine implements Entity {

    public enum StorageEngineName {
        mmfiles, rocksdb
    }

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

}
