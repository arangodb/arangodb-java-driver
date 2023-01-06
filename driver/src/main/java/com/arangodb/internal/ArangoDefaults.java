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

/**
 * @author Mark Vollmary
 */
public final class ArangoDefaults {

    public static final int INTEGER_BYTES = Integer.SIZE / Byte.SIZE;
    public static final int LONG_BYTES = Long.SIZE / Byte.SIZE;
    public static final int CHUNK_MIN_HEADER_SIZE = INTEGER_BYTES + INTEGER_BYTES + LONG_BYTES;
    public static final int CHUNK_MAX_HEADER_SIZE = CHUNK_MIN_HEADER_SIZE + LONG_BYTES;
    public static final int MAX_CONNECTIONS_VST_DEFAULT = 1;
    public static final int MAX_CONNECTIONS_HTTP_DEFAULT = 20;
    public static final int MAX_CONNECTIONS_HTTP2_DEFAULT = 1;

    private ArangoDefaults() {
        super();
    }

}
