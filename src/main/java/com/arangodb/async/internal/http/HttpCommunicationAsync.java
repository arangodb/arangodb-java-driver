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

package com.arangodb.async.internal.http;

import com.arangodb.ArangoDBException;
import com.arangodb.async.internal.CommunicationAsync;
import com.arangodb.internal.http.HttpConnection;
import com.arangodb.internal.net.AccessType;
import com.arangodb.internal.net.Host;
import com.arangodb.internal.net.HostHandle;
import com.arangodb.internal.net.HostHandler;
import com.arangodb.internal.util.RequestUtils;
import com.arangodb.util.ArangoSerialization;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * @author Mark Vollmary
 */
public class HttpCommunicationAsync implements CommunicationAsync {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpCommunicationAsync.class);

    public static class Builder {

        private final HostHandler hostHandler;

        public Builder(final HostHandler hostHandler) {
            super();
            this.hostHandler = hostHandler;
        }

        public Builder(final Builder builder) {
            this(builder.hostHandler);
        }

        public HttpCommunicationAsync build(final ArangoSerialization util) {
            return new HttpCommunicationAsync(hostHandler);
        }
    }

    private final HostHandler hostHandler;

    private HttpCommunicationAsync(final HostHandler hostHandler) {
        super();
        this.hostHandler = hostHandler;
    }

    @Override
    public void close() throws IOException {
        hostHandler.close();
    }

    @Override
    public CompletableFuture<Response> execute(final Request request, final HostHandle hostHandle) throws ArangoDBException {
        final AccessType accessType = RequestUtils.determineAccessType(request);
        Host host = hostHandler.get(hostHandle, accessType);
        final HttpConnection connection = (HttpConnection) host.connection();
        return connection.execute(request)
                .doOnNext(r -> {
                    hostHandler.success();
                    hostHandler.confirm();
                })
                .onErrorMap(TimeoutException.class, ArangoDBException::new)
                .toFuture();
    }

}
