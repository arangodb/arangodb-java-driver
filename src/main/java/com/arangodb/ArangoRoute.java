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

import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocystream.Response;

/**
 * Interface for a specific path to be used to perform arbitrary requests.
 *
 * @author Mark Vollmary
 */
public interface ArangoRoute extends ArangoSerializationAccessor {

    /**
     * Returns a new {@link ArangoRoute} instance for the given path (relative to the current route) that can be used to
     * perform arbitrary requests.
     *
     * @param path The relative URL of the route
     * @return {@link ArangoRoute}
     */
    ArangoRoute route(String... path);

    /**
     * Header that should be sent with each request to the route.
     *
     * @param key   Header key
     * @param value Header value (the {@code toString()} method will be called for the value}
     * @return {@link ArangoRoute}
     */
    ArangoRoute withHeader(String key, Object value);

    /**
     * Query parameter that should be sent with each request to the route.
     *
     * @param key   Query parameter key
     * @param value Query parameter value (the {@code toString()} method will be called for the value}
     * @return {@link ArangoRoute}
     */
    ArangoRoute withQueryParam(String key, Object value);

    /**
     * The response body. The body will be serialized to {@link VPackSlice}.
     *
     * @param body The request body
     * @return {@link ArangoRoute}
     */
    ArangoRoute withBody(Object body);

    /**
     * Performs a DELETE request to the given URL and returns the server response.
     *
     * @return server response
     * @throws ArangoDBException
     */
    Response delete() throws ArangoDBException;

    /**
     * Performs a GET request to the given URL and returns the server response.
     *
     * @return server response
     * @throws ArangoDBException
     */

    Response get() throws ArangoDBException;

    /**
     * Performs a HEAD request to the given URL and returns the server response.
     *
     * @return server response
     * @throws ArangoDBException
     */

    Response head() throws ArangoDBException;

    /**
     * Performs a PATCH request to the given URL and returns the server response.
     *
     * @return server response
     * @throws ArangoDBException
     */

    Response patch() throws ArangoDBException;

    /**
     * Performs a POST request to the given URL and returns the server response.
     *
     * @return server response
     * @throws ArangoDBException
     */

    Response post() throws ArangoDBException;

    /**
     * Performs a PUT request to the given URL and returns the server response.
     *
     * @return server response
     * @throws ArangoDBException
     */

    Response put() throws ArangoDBException;

}
