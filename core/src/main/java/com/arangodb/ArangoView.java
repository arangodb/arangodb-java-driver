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

/**
 * Interface for operations on ArangoDB view level.
 *
 * @author Mark Vollmary
 * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/views/">View API Documentation</a>
 * @since ArangoDB 3.4.0
 */
@ThreadSafe
public interface ArangoView extends ArangoSerdeAccessor {

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
     * @see
     * <a href= "https://docs.arango.ai/arangodb/stable/develop/http-api/views/arangosearch-views/#get-information-about-a-view">API
     * Documentation</a>
     */
    boolean exists();

    /**
     * Deletes the view from the database.
     *
     * @see
     * <a href= "https://docs.arango.ai/arangodb/stable/develop/http-api/views/arangosearch-views/#drop-a-view">API Documentation</a>
     */
    void drop();

    /**
     * Renames the view.
     *
     * @param newName The new name
     * @return information about the view
     * @see
     * <a href= "https://docs.arango.ai/arangodb/stable/develop/http-api/views/arangosearch-views/#rename-a-view">API Documentation</a>
     */
    ViewEntity rename(String newName);

    /**
     * Returns information about the view.
     *
     * @return information about the view
     * @see
     * <a href= "https://docs.arango.ai/arangodb/stable/develop/http-api/views/arangosearch-views/#get-information-about-a-view">API
     * Documentation</a>
     */
    ViewEntity getInfo();

}
