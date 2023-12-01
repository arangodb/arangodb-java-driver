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

package com.arangodb.http;

import com.arangodb.internal.config.ArangoConfig;
import com.arangodb.internal.net.Communication;
import com.arangodb.internal.net.Connection;
import com.arangodb.internal.net.HostHandler;

import java.io.IOException;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public class HttpCommunication extends Communication {

    HttpCommunication(final ArangoConfig config, final HostHandler hostHandler) {
        super(config, hostHandler);
    }

    @Override
    protected void connect(Connection conn) throws IOException {
        // no-op
    }

}
