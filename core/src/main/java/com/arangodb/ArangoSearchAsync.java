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
import com.arangodb.entity.arangosearch.ArangoSearchPropertiesEntity;
import com.arangodb.model.arangosearch.ArangoSearchCreateOptions;
import com.arangodb.model.arangosearch.ArangoSearchPropertiesOptions;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous version of {@link ArangoSearch}
 */
@ThreadSafe
public interface ArangoSearchAsync extends ArangoViewAsync {

    /**
     * Asynchronous version of {@link ArangoSearch#create()}
     */
    CompletableFuture<ViewEntity> create();

    /**
     * Asynchronous version of {@link ArangoSearch#create(ArangoSearchCreateOptions)}
     */
    CompletableFuture<ViewEntity> create(ArangoSearchCreateOptions options);

    /**
     * Asynchronous version of {@link ArangoSearch#getProperties()}
     */
    CompletableFuture<ArangoSearchPropertiesEntity> getProperties();

    /**
     * Asynchronous version of {@link ArangoSearch#updateProperties(ArangoSearchPropertiesOptions)}
     */
    CompletableFuture<ArangoSearchPropertiesEntity> updateProperties(ArangoSearchPropertiesOptions options);

    /**
     * Asynchronous version of {@link ArangoSearch#replaceProperties(ArangoSearchPropertiesOptions)}
     */
    CompletableFuture<ArangoSearchPropertiesEntity> replaceProperties(ArangoSearchPropertiesOptions options);

}
