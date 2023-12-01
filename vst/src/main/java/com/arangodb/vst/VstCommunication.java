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

package com.arangodb.vst;

import com.arangodb.ArangoDBException;
import com.arangodb.config.HostDescription;
import com.arangodb.internal.InternalRequest;
import com.arangodb.internal.InternalResponse;
import com.arangodb.internal.config.ArangoConfig;
import com.arangodb.internal.net.*;
import com.arangodb.internal.serde.InternalSerde;
import com.arangodb.internal.util.HostUtils;
import com.arangodb.internal.util.RequestUtils;
import com.arangodb.internal.util.ResponseUtils;
import com.arangodb.vst.internal.AuthenticationRequest;
import com.arangodb.vst.internal.JwtAuthenticationRequest;
import com.arangodb.vst.internal.VstConnectionAsync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

/**
 * @author Mark Vollmary
 */
public final class VstCommunication implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(VstCommunication.class);
    private static final String ENCRYPTION_PLAIN = "plain";
    private static final String ENCRYPTION_JWT = "jwt";
    private final InternalSerde serde;

    private final String user;
    private final String password;
    private final HostHandler hostHandler;
    private volatile String jwt;

    public VstCommunication(final ArangoConfig config, final HostHandler hostHandler) {
        user = config.getUser();
        password = config.getPassword();
        jwt = config.getJwt();
        serde = config.getInternalSerde();
        this.hostHandler = hostHandler;
    }

    private synchronized VstConnectionAsync connect(final HostHandle hostHandle, final AccessType accessType) {
        Host host = hostHandler.get(hostHandle, accessType);
        while (true) {
            if (host == null) {
                hostHandler.reset();
                throw new ArangoDBException("Was not able to connect to any host");
            }
            final VstConnectionAsync connection = (VstConnectionAsync) host.connection();
            if (connection.isOpen()) {
                hostHandler.success();
                return connection;
            } else {
                try {
                    connection.open();
                    hostHandler.success();
                    if (jwt != null || user != null) {
                        tryAuthenticate(connection);
                    }
                    if (!connection.isOpen()) {
                        // see https://github.com/arangodb/arangodb-java-driver/issues/384
                        hostHandler.fail(new IOException("The connection is closed."));
                        host = hostHandler.get(hostHandle, accessType);
                        continue;
                    }
                    return connection;
                } catch (final IOException e) {
                    hostHandler.fail(e);
                    if (hostHandle != null && hostHandle.getHost() != null) {
                        hostHandle.setHost(null);
                    }
                    final Host failedHost = host;
                    host = hostHandler.get(hostHandle, accessType);
                    if (host != null) {
                        LOGGER.warn(String.format("Could not connect to %s", failedHost.getDescription()), e);
                        LOGGER.warn(
                                String.format("Could not connect to %s or SSL Handshake failed. Try connecting to %s",
                                        failedHost.getDescription(), host.getDescription()));
                    } else {
                        LOGGER.error(e.getMessage(), e);
                        throw ArangoDBException.of(e);
                    }
                }
            }
        }
    }

    private void tryAuthenticate(final VstConnectionAsync connection) {
        try {
            authenticate(connection);
        } catch (final ArangoDBException authException) {
            connection.close();
            throw authException;
        }
    }

    private void authenticate(final VstConnectionAsync connection) {
        InternalRequest authRequest;
        if (jwt != null) {
            authRequest = new JwtAuthenticationRequest(jwt, ENCRYPTION_JWT);
        } else {
            authRequest = new AuthenticationRequest(user, password != null ? password : "", ENCRYPTION_PLAIN);
        }

        InternalResponse response;
        try {
            response = execute(authRequest, connection).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ArangoDBException.of(e);
        } catch (ExecutionException e) {
            throw ArangoDBException.of(e.getCause());
        }
        checkError(response);
    }

    @Override
    public void close() throws IOException {
        hostHandler.close();
    }

    public CompletableFuture<InternalResponse> execute(final InternalRequest request, final HostHandle hostHandle) {
        return execute(request, hostHandle, 0);
    }

    private CompletableFuture<InternalResponse> execute(final InternalRequest request, final HostHandle hostHandle, final int attemptCount) {
        final VstConnectionAsync connection = connect(hostHandle, RequestUtils.determineAccessType(request));
        return execute(request, connection, attemptCount);
    }

    private CompletableFuture<InternalResponse> execute(final InternalRequest request, VstConnectionAsync connection) {
        return execute(request, connection, 0);
    }

    private CompletableFuture<InternalResponse> execute(final InternalRequest request, VstConnectionAsync connection, final int attemptCount) {
        final CompletableFuture<InternalResponse> rfuture = new CompletableFuture<>();
        connection.executeAsync(request).whenComplete((m, ex) -> {
            if (m != null) {
                try {
                    checkError(m);
                    rfuture.complete(m);
                } catch (final ArangoDBRedirectException e) {
                    if (attemptCount >= 3) {
                        rfuture.completeExceptionally(e);
                        return;
                    }
                    final String location = e.getLocation();
                    final HostDescription redirectHost = HostUtils.createFromLocation(location);
                    hostHandler.failIfNotMatch(redirectHost, e);
                    execute(request, new HostHandle().setHost(redirectHost), attemptCount + 1)
                            .whenComplete((v, err) -> {
                                if (v != null) {
                                    rfuture.complete(v);
                                } else if (err != null) {
                                    rfuture.completeExceptionally(err instanceof CompletionException ? err.getCause() : err);
                                } else {
                                    rfuture.cancel(true);
                                }
                            });
                } catch (ArangoDBException e) {
                    rfuture.completeExceptionally(e);
                }
            } else {
                Throwable e = ex instanceof CompletionException ? ex.getCause() : ex;
                rfuture.completeExceptionally(e);
            }
        });

        return rfuture;
    }


    private void checkError(final InternalResponse response) {
        ArangoDBException e = ResponseUtils.translateError(serde, response);
        if (e != null) throw e;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

}
