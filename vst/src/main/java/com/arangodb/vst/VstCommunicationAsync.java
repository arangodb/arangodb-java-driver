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
import com.arangodb.internal.net.ArangoDBRedirectException;
import com.arangodb.internal.net.HostHandle;
import com.arangodb.internal.net.HostHandler;
import com.arangodb.internal.util.HostUtils;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocypack.exception.VPackParserException;
import com.arangodb.vst.internal.AuthenticationRequest;
import com.arangodb.vst.internal.JwtAuthenticationRequest;
import com.arangodb.vst.internal.Message;
import com.arangodb.vst.internal.VstConnectionAsync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

/**
 * @author Mark Vollmary
 */
public class VstCommunicationAsync extends VstCommunication<CompletableFuture<InternalResponse>, VstConnectionAsync> {

    private static final Logger LOGGER = LoggerFactory.getLogger(VstCommunicationAsync.class);

    public VstCommunicationAsync(final ArangoConfig config, final HostHandler hostHandler) {
        super(config, hostHandler);
    }

    @Override
    protected CompletableFuture<InternalResponse> execute(final InternalRequest request, final VstConnectionAsync connection) {
        return execute(request, connection, 0);
    }

    @Override
    protected CompletableFuture<InternalResponse> execute(final InternalRequest request, final VstConnectionAsync connection, final int attemptCount) {
        final CompletableFuture<InternalResponse> rfuture = new CompletableFuture<>();
        try {
            final Message message = createMessage(request);
            send(message, connection).whenComplete((m, ex) -> {
                if (m != null) {
                    final InternalResponse response;
                    try {
                        response = createResponse(m);
                    } catch (final VPackParserException e) {
                        LOGGER.error(e.getMessage(), e);
                        rfuture.completeExceptionally(e);
                        return;
                    }

                    try {
                        checkError(response);
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
                        return;
                    } catch (ArangoDBException e) {
                        rfuture.completeExceptionally(e);
                    }
                    rfuture.complete(response);
                } else if (ex != null) {
                    Throwable e = ex instanceof CompletionException ? ex.getCause() : ex;
                    LOGGER.error(e.getMessage(), e);
                    rfuture.completeExceptionally(e);
                } else {
                    rfuture.cancel(true);
                }
            });
        } catch (final VPackException e) {
            LOGGER.error(e.getMessage(), e);
            rfuture.completeExceptionally(e);
        }
        return rfuture;
    }

    private CompletableFuture<Message> send(final Message message, final VstConnectionAsync connection) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("Send Message (id=%s, head=%s, body=%s)", message.getId(), message.getHead(),
                    message.getBody() != null ? message.getBody() : "{}"));
        }
        return connection.write(message, buildChunks(message));
    }

    @Override
    protected void authenticate(final VstConnectionAsync connection) {
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
            throw ArangoDBException.wrap(e);
        } catch (ExecutionException e) {
            throw ArangoDBException.wrap(e.getCause());
        }
        checkError(response);
    }

}