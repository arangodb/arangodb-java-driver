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

package com.arangodb.async.internal.velocystream;

import com.arangodb.ArangoDBException;
import com.arangodb.internal.net.ArangoDBRedirectException;
import com.arangodb.internal.net.HostDescription;
import com.arangodb.internal.net.HostHandle;
import com.arangodb.internal.net.HostHandler;
import com.arangodb.internal.util.HostUtils;
import com.arangodb.internal.velocystream.VstCommunication;
import com.arangodb.internal.velocystream.internal.AuthenticationRequest;
import com.arangodb.internal.velocystream.internal.JwtAuthenticationRequest;
import com.arangodb.internal.velocystream.internal.Message;
import com.arangodb.util.ArangoSerialization;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocypack.exception.VPackParserException;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author Mark Vollmary
 */
public class VstCommunicationAsync extends VstCommunication<CompletableFuture<Response>, VstConnectionAsync> {

    private static final Logger LOGGER = LoggerFactory.getLogger(VstCommunicationAsync.class);

    private VstCommunicationAsync(final HostHandler hostHandler, final Integer timeout, final String user,
                                  final String password, final String jwt, final Boolean useSsl, final SSLContext sslContext, final ArangoSerialization util,
                                  final Integer chunksize, final Integer maxConnections, final Long connectionTtl) {
        super(timeout, user, password, jwt, useSsl, sslContext, util, chunksize, hostHandler);
    }

    @Override
    protected CompletableFuture<Response> execute(final Request request, final VstConnectionAsync connection) {
        return execute(request, connection, 0);
    }

    @Override
    protected CompletableFuture<Response> execute(final Request request, final VstConnectionAsync connection, final int attemptCount) {
        final CompletableFuture<Response> rfuture = new CompletableFuture<>();
        try {
            final Message message = createMessage(request);
            send(message, connection).whenComplete((m, ex) -> {
                if (m != null) {
                    final Response response;
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
                                        rfuture.completeExceptionally(err);
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
                    LOGGER.error(ex.getMessage(), ex);
                    rfuture.completeExceptionally(ex);
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
        Request authRequest;
        if (jwt != null) {
            authRequest = new JwtAuthenticationRequest(jwt, ENCRYPTION_JWT);
        } else {
            authRequest = new AuthenticationRequest(user, password != null ? password : "", ENCRYPTION_PLAIN);
        }

        Response response;
        try {
            response = execute(authRequest, connection).get();
        } catch (final InterruptedException | ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ArangoDBException) {
                throw (ArangoDBException) cause;
            } else {
                throw new ArangoDBException(e.getCause());
            }
        }
        checkError(response);
    }

    public static class Builder {

        private final HostHandler hostHandler;
        private Integer timeout;
        private Long connectionTtl;
        private String user;
        private String password;
        private String jwt;
        private Boolean useSsl;
        private SSLContext sslContext;
        private Integer chunksize;
        private Integer maxConnections;

        public Builder(final HostHandler hostHandler) {
            super();
            this.hostHandler = hostHandler;
        }

        public Builder timeout(final Integer timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder user(final String user) {
            this.user = user;
            return this;
        }

        public Builder password(final String password) {
            this.password = password;
            return this;
        }

        public Builder jwt(final String jwt) {
            this.jwt = jwt;
            return this;
        }

        public Builder useSsl(final Boolean useSsl) {
            this.useSsl = useSsl;
            return this;
        }

        public Builder sslContext(final SSLContext sslContext) {
            this.sslContext = sslContext;
            return this;
        }

        public Builder chunksize(final Integer chunksize) {
            this.chunksize = chunksize;
            return this;
        }

        public Builder maxConnections(final Integer maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        public Builder connectionTtl(final Long connectionTtl) {
            this.connectionTtl = connectionTtl;
            return this;
        }

        public VstCommunicationAsync build(final ArangoSerialization util) {
            return new VstCommunicationAsync(hostHandler, timeout, user, password, jwt, useSsl, sslContext, util, chunksize,
                    maxConnections, connectionTtl);
        }
    }

}
