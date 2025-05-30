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

package com.arangodb.vst;

import com.arangodb.arch.UnstableApi;
import com.arangodb.config.HostDescription;
import com.arangodb.internal.config.ArangoConfig;
import com.arangodb.internal.net.Connection;
import com.arangodb.internal.net.ConnectionFactory;
import com.arangodb.internal.net.ConnectionPool;
import com.arangodb.vst.internal.VstConnectionAsync;

/**
 * @author Mark Vollmary
 */
@UnstableApi
public class VstConnectionFactoryAsync implements ConnectionFactory {

    @Override
    @UnstableApi
    public Connection create(@UnstableApi final ArangoConfig config, final HostDescription host, @UnstableApi final ConnectionPool pool) {
        return new VstConnectionAsync(config, host, pool);
    }

}
