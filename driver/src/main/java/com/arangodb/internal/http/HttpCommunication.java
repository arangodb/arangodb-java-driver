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
import com.arangodb.Request;
import com.arangodb.RequestType;
import com.arangodb.Response;
import com.arangodb.internal.net.*;
import com.arangodb.internal.serde.InternalSerde;
import com.arangodb.internal.util.HostUtils;
import com.arangodb.internal.util.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Mark Vollmary
 */
public class HttpCommunication implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpCommunication.class);
    private final HostHandler hostHandler;
    private final InternalSerde serde;
    private final AtomicLong reqCount;

    private HttpCommunication(final HostHandler hostHandler, final InternalSerde serde) {
        super();
        this.hostHandler = hostHandler;
        this.serde = serde;
        reqCount = new AtomicLong();
    }

    @Override
    public void close() throws IOException {
        hostHandler.close();
    }

    public Response execute(final Request request, final HostHandle hostHandle) {
        return execute(request, hostHandle, 0);
    }

    private Response execute(final Request request, final HostHandle hostHandle, final int attemptCount) {
        final AccessType accessType = RequestUtils.determineAccessType(request);
        Host host = hostHandler.get(hostHandle, accessType);
        try {
            while (true) {
                long reqId = reqCount.getAndIncrement();
                try {
                    final HttpConnection connection = (HttpConnection) host.connection();
                    if (LOGGER.isDebugEnabled()) {
                        String body = request.getBody() == null ? "" : serde.toJsonString(request.getBody());
                        LOGGER.debug("Send Request [id={}]: {} {}", reqId, request, body);
                    }
                    final Response response = connection.execute(request);
                    if (LOGGER.isDebugEnabled()) {
                        String body = response.getBody() == null ? "" : serde.toJsonString(response.getBody());
                        LOGGER.debug("Received Response [id={}]: {} {}", reqId, response, body);
                    }
                    hostHandler.success();
                    hostHandler.confirm();
                    return response;
                } catch (final SocketTimeoutException e) {
                    // SocketTimeoutException exceptions are wrapped and rethrown.
                    TimeoutException te = new TimeoutException(e.getMessage());
                    te.initCause(e);
                    throw new ArangoDBException(te, reqId);
                } catch (final IOException e) {
                    hostHandler.fail(e);
                    if (hostHandle != null && hostHandle.getHost() != null) {
                        hostHandle.setHost(null);
                    }
                    final Host failedHost = host;
                    host = hostHandler.get(hostHandle, accessType);
                    if (host != null && isSafe(request)) {
                        LOGGER.warn("Could not connect to {} while executing request [id={}]",
                                failedHost.getDescription(), reqId, e);
                        LOGGER.debug("Try connecting to {}", host.getDescription());
                    } else {
                        LOGGER.error(e.getMessage(), e);
                        throw new ArangoDBException(e, reqId);
                    }
                }
            }
        } catch (final ArangoDBRedirectException e) {
            if (attemptCount < 3) {
                final String location = e.getLocation();
                final HostDescription redirectHost = HostUtils.createFromLocation(location);
                hostHandler.failIfNotMatch(redirectHost, e);
                return execute(request, new HostHandle().setHost(redirectHost), attemptCount + 1);
            } else {
                throw e;
            }
        }
    }

    private boolean isSafe(final Request request) {
        RequestType type = request.getRequestType();
        return type == RequestType.GET || type == RequestType.HEAD || type == RequestType.OPTIONS;
    }

    public static class Builder {
        private HostHandler hostHandler;
        private InternalSerde serde;

        public Builder hostHandler(HostHandler hostHandler) {
            this.hostHandler = hostHandler;
            return this;
        }

        public Builder serde(InternalSerde serde) {
            this.serde = serde;
            return this;
        }

        public HttpCommunication build() {
            return new HttpCommunication(hostHandler, serde);
        }
    }

}
