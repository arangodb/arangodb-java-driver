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
public final class ArangoErrors {

    public static final Integer ERROR_ARANGO_DATA_SOURCE_NOT_FOUND = 1203;
    public static final Integer ERROR_ARANGO_DATABASE_NOT_FOUND = 1228;
    public static final Integer ERROR_GRAPH_NOT_FOUND = 1924;
    public static final Integer QUEUE_TIME_VIOLATED = 21004;

    private ArangoErrors() {
        super();
    }

}
