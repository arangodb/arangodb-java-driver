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
import com.arangodb.internal.serde.InternalSerde;
import com.arangodb.internal.util.HostUtils;
import com.arangodb.internal.util.RequestUtils;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author Mark Vollmary
 */
public class HttpCommunication implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpCommunication.class);
    private final HostHandler hostHandler;

    private HttpCommunication(final HostHandler hostHandler) {
        super();
        this.hostHandler = hostHandler;
    }

    @Override
    public void close() throws IOException {
        hostHandler.close();
    }

    public Response execute(final Request request, final HostHandle hostHandle) {
        try {
            return execute(request, hostHandle, 0).get();
        } catch (InterruptedException e) {
            throw new ArangoDBException(e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ArangoDBException) {
                throw (ArangoDBException) cause;
            } else {
                throw new ArangoDBException(cause);
            }
        }
    }

    private CompletableFuture<Response> execute(final Request request, final HostHandle hostHandle, final int attemptCount) {
        final CompletableFuture<Response> rfuture = new CompletableFuture<>();
        final AccessType accessType = RequestUtils.determineAccessType(request);
        Host host = hostHandler.get(hostHandle, accessType);
        final HttpConnection connection = (HttpConnection) host.connection();
        connection.execute(request).whenComplete(((resp, err) -> {
            if (resp != null) {
                hostHandler.success();
                hostHandler.confirm();
                rfuture.complete(resp);
            } else if (err != null) {
                Throwable e = err instanceof CompletionException ? err.getCause() : err;
                if (e instanceof SocketTimeoutException) {
                    // SocketTimeoutException exceptions are wrapped and rethrown.
                    // Differently from other IOException exceptions they must not be retried,
                    // since the requests could not be idempotent.
                    TimeoutException te = new TimeoutException(e.getMessage());
                    te.initCause(e);
                    rfuture.completeExceptionally(new ArangoDBException(te));
                } else if (e instanceof IOException) {
                    hostHandler.fail((IOException) e);
                    if (hostHandle != null && hostHandle.getHost() != null) {
                        hostHandle.setHost(null);
                    }

                    Host nextHost = hostHandler.get(hostHandle, accessType);
                    if (nextHost != null) {
                        LOGGER.warn(String.format("Could not connect to %s", host.getDescription()), e);
                        LOGGER.warn(String.format("Could not connect to %s. Try connecting to %s",
                                host.getDescription(), nextHost.getDescription()));
                        CompletableFuture<Response> req =
                                execute(request, new HostHandle().setHost(nextHost.getDescription()), attemptCount);
                        mirrorFuture(req, rfuture);
                    } else {
                        LOGGER.error(e.getMessage(), e);
                        rfuture.completeExceptionally(new ArangoDBException(e));
                    }
                } else if (e instanceof ArangoDBRedirectException) {
                    if (attemptCount < 3) {
                        ArangoDBRedirectException redirEx = (ArangoDBRedirectException) e;
                        final String location = redirEx.getLocation();
                        final HostDescription redirectHost = HostUtils.createFromLocation(location);
                        hostHandler.failIfNotMatch(redirectHost, redirEx);
                        CompletableFuture<Response> req =
                                execute(request, new HostHandle().setHost(redirectHost), attemptCount + 1);
                        mirrorFuture(req, rfuture);
                    } else {
                        rfuture.completeExceptionally(e);
                    }
                } else {
                    rfuture.completeExceptionally(e);
                }
            }
        }));
        return rfuture;
    }

    private void mirrorFuture(CompletableFuture<Response> upstream, CompletableFuture<Response> downstream) {
        upstream.whenComplete((v, err) -> {
            if (v != null) {
                downstream.complete(v);
            } else if (err != null) {
                downstream.completeExceptionally(err);
            }
        });
    }

    public static class Builder {

        private final HostHandler hostHandler;

        public Builder(final HostHandler hostHandler) {
            super();
            this.hostHandler = hostHandler;
        }

        public Builder(final Builder builder) {
            this(builder.hostHandler);
        }

        public HttpCommunication build(final InternalSerde util) {
            return new HttpCommunication(hostHandler);
        }
    }

}
