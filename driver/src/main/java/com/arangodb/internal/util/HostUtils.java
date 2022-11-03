/*
 * DISCLAIMER
 *
 * Copyright 2017 ArangoDB GmbH, Cologne, Germany
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

import com.arangodb.internal.net.*;

/**
 * @author Mark Vollmary
 */
public final class HostUtils {

    private HostUtils() {
        super();
    }

    public static HostDescription createFromLocation(final String location) {
        final HostDescription host;
        if (location != null) {
            final String[] tmp = location.replaceAll(".*://", "").replaceAll("/.*", "").split(":");
            host = tmp.length == 2 ? new HostDescription(tmp[0], Integer.parseInt(tmp[1])) : null;
        } else {
            host = null;
        }
        return host;
    }

    public static Host createHost(
            final HostDescription description,
            final int maxConnections,
            final ConnectionFactory factory) {
        return new HostImpl(new ConnectionPoolImpl(description, maxConnections, factory), description);
    }
}
