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

/**
 * Interface for operations on ArangoDB view level.
 *
 * @author Mark Vollmary
 * @see <a href="https://www.arangodb.com/docs/stable/http/views.html">View API Documentation</a>
 * @since ArangoDB 3.4.0
 */
@SuppressWarnings("UnusedReturnValue")
public interface ArangoView extends ArangoSerializationAccessor {

    /**
     * The the handler of the database the collection is within
     *
     * @return database handler
     */
    ArangoDatabase db();

    /**
     * The name of the view
     *
     * @return view name
     */
    String name();

    /**
     * Checks whether the view exists.
     *
     * @return true if the view exists, otherwise false
     * @throws ArangoDBException
     */
    boolean exists() throws ArangoDBException;

    /**
     * Deletes the view from the database.
     *
     * @throws ArangoDBException
     * @see <a href= "https://www.arangodb.com/docs/stable/http/views-arangosearch.html#drops-a-view">API Documentation</a>
     */
    void drop() throws ArangoDBException;

    /**
     * Renames the view.
     *
     * @param newName The new name
     * @return information about the view
     * @throws ArangoDBException
     * @see <a href= "https://www.arangodb.com/docs/stable/http/views-arangosearch.html#rename-a-view">API Documentation</a>
     */
    ViewEntity rename(String newName) throws ArangoDBException;

    /**
     * Returns information about the view.
     *
     * @return information about the view
     * @throws ArangoDBException
     * @see <a href= "https://www.arangodb.com/docs/stable/http/views-arangosearch.html#return-information-about-a-view">API
     * Documentation</a>
     */
    ViewEntity getInfo() throws ArangoDBException;

}
