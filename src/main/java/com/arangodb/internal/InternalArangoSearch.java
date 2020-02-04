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

package com.arangodb.internal;

import com.arangodb.model.arangosearch.ArangoSearchPropertiesOptions;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;

/**
 * @author Mark Vollmary
 */
public class InternalArangoSearch<A extends InternalArangoDB<E>, D extends InternalArangoDatabase<A, E>, E extends ArangoExecutor>
        extends InternalArangoView<A, D, E> {

    protected InternalArangoSearch(final D db, final String name) {
        super(db, name);
    }

    protected Request getPropertiesRequest() {
        return request(db.name(), RequestType.GET, PATH_API_VIEW, name, "properties");
    }

    protected Request replacePropertiesRequest(final ArangoSearchPropertiesOptions options) {
        final Request request = request(db.name(), RequestType.PUT, PATH_API_VIEW, name, "properties");
        request.setBody(util().serialize(options != null ? options : new ArangoSearchPropertiesOptions()));
        return request;
    }

    protected Request updatePropertiesRequest(final ArangoSearchPropertiesOptions options) {
        final Request request = request(db.name(), RequestType.PATCH, PATH_API_VIEW, name, "properties");
        request.setBody(util().serialize(options != null ? options : new ArangoSearchPropertiesOptions()));
        return request;
    }

}
