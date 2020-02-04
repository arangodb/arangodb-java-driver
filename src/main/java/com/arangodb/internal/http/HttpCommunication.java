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

package com.arangodb.internal.http;

import com.arangodb.ArangoDBException;
import com.arangodb.internal.net.*;
import com.arangodb.internal.util.HostUtils;
import com.arangodb.internal.util.RequestUtils;
import com.arangodb.util.ArangoSerialization;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketException;

/**
 * @author Mark Vollmary
 */
public class HttpCommunication implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpCommunication.class);

    public static class Builder {

        private final HostHandler hostHandler;

        public Builder(final HostHandler hostHandler) {
            super();
            this.hostHandler = hostHandler;
        }

        public Builder(final Builder builder) {
            this(builder.hostHandler);
        }

        public HttpCommunication build(final ArangoSerialization util) {
            return new HttpCommunication(hostHandler);
        }
    }

    private final HostHandler hostHandler;

    private HttpCommunication(final HostHandler hostHandler) {
        super();
        this.hostHandler = hostHandler;
    }

    @Override
    public void close() throws IOException {
        hostHandler.close();
    }

    public Response execute(final Request request, final HostHandle hostHandle) throws ArangoDBException, IOException {
        final AccessType accessType = RequestUtils.determineAccessType(request);
        Host host = hostHandler.get(hostHandle, accessType);
        try {
            while (true) {
                try {
                    final HttpConnection connection = (HttpConnection) host.connection();
                    final Response response = connection.execute(request);
                    hostHandler.success();
                    hostHandler.confirm();
                    return response;
                } catch (final SocketException se) {
                    hostHandler.fail();
                    if (hostHandle != null && hostHandle.getHost() != null) {
                        hostHandle.setHost(null);
                    }
                    final Host failedHost = host;
                    host = hostHandler.get(hostHandle, accessType);
                    if (host != null) {
                        LOGGER.warn(String.format("Could not connect to %s. Try connecting to %s",
                                failedHost.getDescription(), host.getDescription()));
                    } else {
                        throw se;
                    }
                }
            }
        } catch (final ArangoDBException e) {
            if (e instanceof ArangoDBRedirectException) {
                final String location = ((ArangoDBRedirectException) e).getLocation();
                final HostDescription redirectHost = HostUtils.createFromLocation(location);
                hostHandler.closeCurrentOnError();
                hostHandler.fail();
                return execute(request, new HostHandle().setHost(redirectHost));
            } else {
                throw e;
            }
        }
    }

}
