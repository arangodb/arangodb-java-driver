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

package com.arangodb.async.internal.velocystream;

import com.arangodb.internal.net.Connection;
import com.arangodb.internal.net.ConnectionFactory;
import com.arangodb.internal.net.HostDescription;
import com.arangodb.internal.velocystream.internal.MessageStore;

import javax.net.ssl.SSLContext;

/**
 * @author Mark Vollmary
 */
public class VstConnectionFactoryAsync implements ConnectionFactory {

    private final VstConnectionAsync.Builder builder;

    public VstConnectionFactoryAsync(final HostDescription host, final Integer timeout, final Long connectionTtl,
                                     final Boolean useSsl, final SSLContext sslContext) {
        super();
        builder = new VstConnectionAsync.Builder().timeout(timeout).ttl(connectionTtl).useSsl(useSsl)
                .sslContext(sslContext);
    }

    @Override
    public Connection create(final HostDescription host) {
        return builder.messageStore(new MessageStore()).host(host).build();
    }

}
