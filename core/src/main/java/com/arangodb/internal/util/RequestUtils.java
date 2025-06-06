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

package com.arangodb.internal.util;

import com.arangodb.internal.InternalRequest;

/**
 * @author Mark Vollmary
 */
public final class RequestUtils {

    public static final String HEADER_ALLOW_DIRTY_READ = "x-arango-allow-dirty-read";

    private RequestUtils() {
        super();
    }

    public static InternalRequest allowDirtyRead(final InternalRequest request) {
        return request.putHeaderParam(HEADER_ALLOW_DIRTY_READ, "true");
    }

}
