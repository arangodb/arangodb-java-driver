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
import com.arangodb.internal.InternalResponse;
import com.arangodb.internal.RequestType;
import com.arangodb.internal.config.ArangoConfig;
import com.arangodb.internal.net.ArangoDBRedirectException;
import com.arangodb.internal.net.Host;
import com.arangodb.internal.net.HostHandle;
import com.arangodb.internal.net.HostHandler;
import com.arangodb.internal.serde.InternalSerde;
import com.arangodb.internal.util.HostUtils;
import com.arangodb.internal.util.RequestUtils;
import com.arangodb.internal.util.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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

    public CompletableFuture<InternalResponse> executeAsync(final InternalRequest request, final HostHandle hostHandle) {
        return executeAsync(request, hostHandle, hostHandler.get(hostHandle, RequestUtils.determineAccessType(request)), 0);
    }

    private CompletableFuture<InternalResponse> executeAsync(final InternalRequest request, final HostHandle hostHandle, final Host host, final int attemptCount) {
        final CompletableFuture<InternalResponse> rfuture = new CompletableFuture<>();
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
                            rfuture.completeExceptionally(ArangoDBException.of(te, reqId));
                        } else if (e instanceof TimeoutException) {
                            rfuture.completeExceptionally(ArangoDBException.of(e, reqId));
                        } else if (e instanceof ConnectException) {
                            handleException(true, e, hostHandle, request, host, reqId, attemptCount, rfuture);
                        } else if (e != null) {
                            handleException(isSafe(request), e, hostHandle, request, host, reqId, attemptCount, rfuture);
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
                                    mirror(
                                            executeAsync(request, new HostHandle().setHost(redirectHost), hostHandler.get(hostHandle, RequestUtils.determineAccessType(request)), attemptCount + 1),
                                            rfuture
                                    );
                                }
                            } else if (errorEntityEx != null) {
                                rfuture.completeExceptionally(errorEntityEx);
                            } else {
                                hostHandler.success();
                                rfuture.complete(response);
                            }
                        }
                    } catch (Exception ex) {
                        rfuture.completeExceptionally(ArangoDBException.of(ex, reqId));
                    }
                });
        return rfuture;
    }

    private void handleException(boolean isSafe, Throwable e, HostHandle hostHandle, InternalRequest request, Host host,
                                 long reqId, int attemptCount, CompletableFuture<InternalResponse> rfuture) {
        IOException ioEx = wrapIOEx(e);
        hostHandler.fail(ioEx);
        if (hostHandle != null && hostHandle.getHost() != null) {
            hostHandle.setHost(null);
        }
        boolean hasNextHost = hostHandler.hasNext(hostHandle, RequestUtils.determineAccessType(request));
        if (hasNextHost && isSafe) {
            Host nextHost = hostHandler.get(hostHandle, RequestUtils.determineAccessType(request));
            LOGGER.warn("Could not connect to {} while executing request [id={}]",
                    host.getDescription(), reqId, ioEx);
            LOGGER.debug("Try connecting to {}", nextHost.getDescription());
            mirror(
                    executeAsync(request, hostHandle, nextHost, attemptCount),
                    rfuture
            );
        } else {
            ArangoDBException aEx = ArangoDBException.of(ioEx, reqId);
            rfuture.completeExceptionally(aEx);
        }
    }

    private void mirror(CompletableFuture<InternalResponse> up, CompletableFuture<InternalResponse> down) {
        up.whenComplete((v, err) -> {
            if (err != null) {
                down.completeExceptionally(err instanceof CompletionException ? err.getCause() : err);
            } else {
                down.complete(v);
            }
        });
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
