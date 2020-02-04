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

package com.arangodb.velocystream;

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
    OPTIONS(6),
    VSTREAM_CRED(7),
    VSTREAM_REGISTER(8),
    VSTREAM_STATUS(9),
    ILLEGAL(10);

    private final int type;

    RequestType(final int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static RequestType fromType(final int type) {
        for (final RequestType rType : RequestType.values()) {
            if (rType.type == type) {
                return rType;
            }
        }
        return null;
    }
}
