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

import com.arangodb.internal.net.AccessType;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;

/**
 * @author Mark Vollmary
 */
public final class RequestUtils {

    public static final String HEADER_ALLOW_DIRTY_READ = "X-Arango-Allow-Dirty-Read";

    private RequestUtils() {
        super();
    }

    public static Request allowDirtyRead(final Request request) {
        return request.putHeaderParam(HEADER_ALLOW_DIRTY_READ, "true");
    }

    public static AccessType determineAccessType(final Request request) {
        if (request.getHeaderParam().containsKey(HEADER_ALLOW_DIRTY_READ)) {
            return AccessType.DIRTY_READ;
        }
        if (request.getRequestType() == RequestType.GET) {
            return AccessType.READ;
        }
        return AccessType.WRITE;
    }

}
