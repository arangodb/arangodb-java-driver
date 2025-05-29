/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
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

import com.arangodb.Request;

/**
 * @author Mark Vollmary
 */
public enum RequestType {

    DELETE(0),
    GET(1),
    POST(2),
    PUT(3),
    HEAD(4),
    PATCH(5),
    OPTIONS(6);

    private final int type;

    RequestType(final int type) {
        this.type = type;
    }

    public static RequestType from(final Request.Method method) {
        switch (method) {
            case DELETE:
                return DELETE;
            case GET:
                return GET;
            case POST:
                return POST;
            case PUT:
                return PUT;
            case HEAD:
                return HEAD;
            case PATCH:
                return PATCH;
            case OPTIONS:
                return OPTIONS;
            default:
                throw new IllegalArgumentException();
        }
    }

    public int getType() {
        return type;
    }
}
