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

/**
 * Interface for operations on ArangoDB view level for ArangoSearch views.
 *
 * @author Mark Vollmary
 * @see <a href="https://www.arangodb.com/docs/stable/http/views-arangosearch.html">View API Documentation</a>
 * @since ArangoDB 3.4.0
 */
@ThreadSafe
public interface ArangoSearch extends ArangoView {

    /**
     * Creates a view, then returns view information from the server.
     *
     * @return information about the view
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/views-arangosearch.html#create-an-arangosearch-view">API
     * Documentation</a>
     */
    ViewEntity create() throws ArangoDBException;

    /**
     * Creates a view with the given {@code options}, then returns view information from the server.
     *
     * @param options Additional options, can be null
     * @return information about the view
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/views-arangosearch.html#create-an-arangosearch-view">API
     * Documentation</a>
     */
    ViewEntity create(ArangoSearchCreateOptions options) throws ArangoDBException;

    /**
     * Reads the properties of the specified view.
     *
     * @return properties of the view
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/views-arangosearch.html#read-properties-of-a-view">API
     * Documentation</a>
     */
    ArangoSearchPropertiesEntity getProperties() throws ArangoDBException;

    /**
     * Partially changes properties of the view.
     *
     * @param options properties to change
     * @return properties of the view
     * @throws ArangoDBException
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/views-arangosearch.html#partially-changes-properties-of-an-arangosearch-view">API
     * Documentation</a>
     */
    ArangoSearchPropertiesEntity updateProperties(ArangoSearchPropertiesOptions options) throws ArangoDBException;

    /**
     * Changes properties of the view.
     *
     * @param options properties to change
     * @return properties of the view
     * @throws ArangoDBException
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/views-arangosearch.html#change-properties-of-an-arangosearch-view">API
     * Documentation</a>
     */
    ArangoSearchPropertiesEntity replaceProperties(ArangoSearchPropertiesOptions options) throws ArangoDBException;

}
