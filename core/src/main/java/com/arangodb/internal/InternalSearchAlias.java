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

public class InternalSearchAlias extends InternalArangoView {

    private static final String PROPERTIES_PATH = "properties";
    private final String dbName;

    protected InternalSearchAlias(final ArangoDatabaseImpl db, final String name) {
        super(db, db.name(), name);
        dbName = db.name();
    }

    protected InternalRequest getPropertiesRequest() {
        return request(dbName, RequestType.GET, PATH_API_VIEW, name, PROPERTIES_PATH);
    }

    protected InternalRequest replacePropertiesRequest(final SearchAliasPropertiesOptions options) {
        final InternalRequest request = request(dbName, RequestType.PUT, PATH_API_VIEW, name, PROPERTIES_PATH);
        request.setBody(getSerde().serialize(options != null ? options : new SearchAliasPropertiesOptions()));
        return request;
    }

    protected InternalRequest updatePropertiesRequest(final SearchAliasPropertiesOptions options) {
        final InternalRequest request = request(dbName, RequestType.PATCH, PATH_API_VIEW, name, PROPERTIES_PATH);
        request.setBody(getSerde().serialize(options != null ? options : new SearchAliasPropertiesOptions()));
        return request;
    }

}
