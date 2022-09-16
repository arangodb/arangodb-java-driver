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

import com.arangodb.model.arangosearch.SearchAliasPropertiesOptions;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;

public class InternalSearchAlias<A extends InternalArangoDB<E>, D extends InternalArangoDatabase<A, E>, E extends ArangoExecutor>
        extends InternalArangoView<A, D, E> {

    protected InternalSearchAlias(final D db, final String name) {
        super(db, name);
    }

    protected Request getPropertiesRequest() {
        return request(db.dbName(), RequestType.GET, PATH_API_VIEW, name, "properties");
    }

    protected Request replacePropertiesRequest(final SearchAliasPropertiesOptions options) {
        final Request request = request(db.dbName(), RequestType.PUT, PATH_API_VIEW, name, "properties");
        request.setBody(util().serialize(options != null ? options : new SearchAliasPropertiesOptions()));
        return request;
    }

    protected Request updatePropertiesRequest(final SearchAliasPropertiesOptions options) {
        final Request request = request(db.dbName(), RequestType.PATCH, PATH_API_VIEW, name, "properties");
        request.setBody(util().serialize(options != null ? options : new SearchAliasPropertiesOptions()));
        return request;
    }

}
