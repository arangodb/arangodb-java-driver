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

package com.arangodb.async;

import com.arangodb.entity.ViewEntity;
import com.arangodb.entity.arangosearch.SearchAliasPropertiesEntity;
import com.arangodb.model.arangosearch.SearchAliasCreateOptions;
import com.arangodb.model.arangosearch.SearchAliasPropertiesOptions;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for operations on ArangoDB view level for SearchAlias views.
 *
 * @author Michele Rastelli
 * @see <a href="https://www.arangodb.com/docs/stable/http/views-search-alias.html">View API Documentation</a>
 * @since ArangoDB 3.10
 */
public interface SearchAliasAsync extends ArangoViewAsync {

    /**
     * Creates a view, then returns view information from the server.
     *
     * @return information about the view
     * @see <a href="https://www.arangodb.com/docs/stable/http/views-search-alias.html#create-a-search-alias-view">API
     * Documentation</a>
     */
    CompletableFuture<ViewEntity> create();

    /**
     * Creates a view with the given {@code options}, then returns view information from the server.
     *
     * @param options Additional options, can be null
     * @return information about the view
     * @see <a href="https://www.arangodb.com/docs/stable/http/views-search-alias.html#create-a-search-alias-view">API
     * Documentation</a>
     */
    CompletableFuture<ViewEntity> create(SearchAliasCreateOptions options);

    /**
     * Reads the properties of the specified view.
     *
     * @return properties of the view
     * @see <a href="https://www.arangodb.com/docs/stable/http/views-search-alias.html#read-properties-of-a-view">API
     * Documentation</a>
     */
    CompletableFuture<SearchAliasPropertiesEntity> getProperties();

    /**
     * Partially changes properties of the view.
     *
     * @param options properties to change
     * @return properties of the view
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/views-search-alias.html#partially-changes-properties-of-a-search-alias-view">API
     * Documentation</a>
     */
    CompletableFuture<SearchAliasPropertiesEntity> updateProperties(SearchAliasPropertiesOptions options);

    /**
     * Changes properties of the view.
     *
     * @param options properties to change
     * @return properties of the view
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/views-search-alias.html#changes-properties-of-a-search-alias-view">API
     * Documentation</a>
     */
    CompletableFuture<SearchAliasPropertiesEntity> replaceProperties(SearchAliasPropertiesOptions options);

}
