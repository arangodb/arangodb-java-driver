/*
 * DISCLAIMER
 *
 * Copyright 2018 ArangoDB GmbH, Cologne, Germany
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

package com.arangodb;

import com.arangodb.entity.ViewEntity;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous version of {@link ArangoView}
 */
@ThreadSafe
public interface ArangoViewAsync extends ArangoSerdeAccessor {

    /**
     * The handler of the database the collection is within
     *
     * @return database handler
     */
    ArangoDatabaseAsync db();

    /**
     * The name of the view
     *
     * @return view name
     */
    String name();

    /**
     * Asynchronous version of {@link ArangoView#exists()}
     */
    CompletableFuture<Boolean> exists();

    /**
     * Asynchronous version of {@link ArangoView#drop()}
     */
    CompletableFuture<Void> drop();

    /**
     * Asynchronous version of {@link ArangoView#rename(String)}
     */
    CompletableFuture<ViewEntity> rename(String newName);

    /**
     * Asynchronous version of {@link ArangoView#getInfo()}
     */
    CompletableFuture<ViewEntity> getInfo();

}
