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

import com.arangodb.model.OptionsBuilder;
import com.arangodb.model.ViewRenameOptions;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public abstract class InternalArangoView extends ArangoExecuteable {

    protected static final String PATH_API_VIEW = "/_api/view";
    protected static final String PATH_API_ANALYZER = "/_api/analyzer";

    protected final String dbName;
    protected final String name;

    protected InternalArangoView(final ArangoExecuteable executeable,
                                 final String dbName,
                                 final String name) {
        super(executeable);
        this.dbName = dbName;
        this.name = name;
    }

    public String name() {
        return name;
    }

    protected InternalRequest dropRequest() {
        return request(dbName, RequestType.DELETE, PATH_API_VIEW, name);
    }

    protected InternalRequest renameRequest(final String newName) {
        final InternalRequest request = request(dbName, RequestType.PUT, PATH_API_VIEW, name, "rename");
        request.setBody(getSerde().serialize(OptionsBuilder.build(new ViewRenameOptions(), newName)));
        return request;
    }

    protected InternalRequest getInfoRequest() {
        return request(dbName, RequestType.GET, PATH_API_VIEW, name);
    }

}
