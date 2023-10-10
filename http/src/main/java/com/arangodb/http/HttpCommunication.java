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

import com.arangodb.ArangoDBException;
import com.arangodb.config.HostDescription;
import com.arangodb.internal.InternalRequest;
import com.arangodb.internal.RequestType;
import com.arangodb.internal.InternalResponse;
import com.arangodb.internal.config.ArangoConfig;
import com.arangodb.internal.net.*;
import com.arangodb.internal.serde.InternalSerde;
import com.arangodb.internal.util.HostUtils;
import com.arangodb.internal.util.RequestUtils;
import com.arangodb.internal.util.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public class HttpCommunication implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpCommunication.class);
    private final HostHandler hostHandler;
    private final InternalSerde serde;
    private final AtomicLong reqCount;

    HttpCommunication(final HostHandler hostHandler, final ArangoConfig config) {
        super();
        this.hostHandler = hostHandler;
        this.serde = config.getInternalSerde();
        reqCount = new AtomicLong();
    }

    @Override
    public void close() throws IOException {
        hostHandler.close();
    }

    public InternalResponse execute(final InternalRequest request, final HostHandle hostHandle) {
        try {
            return executeAsync(request, hostHandle, hostHandler.get(hostHandle, RequestUtils.determineAccessType(request)), 0).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ArangoDBException.wrap(e);
        } catch (ExecutionException e) {
            throw ArangoDBException.wrap(e.getCause());
        }
    }

    private CompletableFuture<InternalResponse> executeAsync(final InternalRequest request, final HostHandle hostHandle, final Host host, final int attemptCount) {
        final CompletableFuture<InternalResponse> rfuture = new CompletableFuture<>();
        final AccessType accessType = RequestUtils.determineAccessType(request);
        long reqId = reqCount.getAndIncrement();
        final HttpConnection connection = (HttpConnection) host.connection();
        if (LOGGER.isDebugEnabled()) {
            String body = request.getBody() == null ? "" : serde.toJsonString(request.getBody());
            LOGGER.debug("Send Request [id={}]: {} {}", reqId, request, body);
        }
        connection.executeAsync(request)
                .whenComplete((response, e) -> {
                    try {
                        if (e instanceof SocketTimeoutException) {
                            // SocketTimeoutException exceptions are wrapped and rethrown.
                            TimeoutException te = new TimeoutException(e.getMessage());
                            te.initCause(e);
                            rfuture.completeExceptionally(new ArangoDBException(te, reqId));
                        } else if (e instanceof TimeoutException) {
                            rfuture.completeExceptionally(new ArangoDBException(e, reqId));
                        } else if (e != null) {
                            IOException ioEx = wrapIOEx(e);
                            hostHandler.fail(ioEx);
                            if (hostHandle != null && hostHandle.getHost() != null) {
                                hostHandle.setHost(null);
                            }

                            Host nextHost;
                            try {
                                nextHost = hostHandler.get(hostHandle, accessType);
                            } catch (ArangoDBException aEx) {
                                rfuture.completeExceptionally(aEx);
                                return;
                            }

                            if (nextHost != null && isSafe(request)) {
                                LOGGER.warn("Could not connect to {} while executing request [id={}]",
                                        host.getDescription(), reqId, ioEx);
                                LOGGER.debug("Try connecting to {}", nextHost.getDescription());
                                executeAsync(request, hostHandle, nextHost, attemptCount)
                                        .whenComplete((v, err) -> {
                                            if (err != null) {
                                                rfuture.completeExceptionally(err);
                                            } else {
                                                rfuture.complete(v);
                                            }
                                        });
                            } else {
                                ArangoDBException aEx = new ArangoDBException(ioEx, reqId);
                                LOGGER.error(aEx.getMessage(), aEx);
                                rfuture.completeExceptionally(aEx);
                            }
                        } else {
                            if (LOGGER.isDebugEnabled()) {
                                String body = response.getBody() == null ? "" : serde.toJsonString(response.getBody());
                                LOGGER.debug("Received Response [id={}]: {} {}", reqId, response, body);
                            }
                            ArangoDBException errorEntityEx = ResponseUtils.translateError(serde, response);
                            if (errorEntityEx instanceof ArangoDBRedirectException) {
                                if (attemptCount >= 3) {
                                    rfuture.completeExceptionally(errorEntityEx);
                                } else {
                                    final String location = ((ArangoDBRedirectException) errorEntityEx).getLocation();
                                    final HostDescription redirectHost = HostUtils.createFromLocation(location);
                                    hostHandler.failIfNotMatch(redirectHost, errorEntityEx);
                                    executeAsync(request, new HostHandle().setHost(redirectHost), hostHandler.get(hostHandle, accessType), attemptCount + 1)
                                            .whenComplete((v, err) -> {
                                                if (err != null) {
                                                    rfuture.completeExceptionally(err);
                                                } else {
                                                    rfuture.complete(v);
                                                }
                                            });
                                }
                            } else if (errorEntityEx != null) {
                                rfuture.completeExceptionally(errorEntityEx);
                            } else {
                                hostHandler.success();
                                hostHandler.confirm();
                                rfuture.complete(response);
                            }
                        }
                    } catch (Exception ex) {
                        // FIXME: convert to handle() block
                        LOGGER.error("FATAL: Unhandled exception", ex);
                        System.exit(1);
                    }
                });
        return rfuture;
    }

    private static IOException wrapIOEx(Throwable t) {
        if (t instanceof IOException) {
            return (IOException) t;
        } else {
            return new IOException(t);
        }
    }

    private boolean isSafe(final InternalRequest request) {
        RequestType type = request.getRequestType();
        return type == RequestType.GET || type == RequestType.HEAD || type == RequestType.OPTIONS;
    }

}
