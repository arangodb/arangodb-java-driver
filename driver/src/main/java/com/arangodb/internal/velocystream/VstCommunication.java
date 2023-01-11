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

package com.arangodb.internal.velocystream;

import com.arangodb.ArangoDBException;
import com.arangodb.config.ArangoConfigProperties;
import com.arangodb.internal.ArangoDefaults;
import com.arangodb.internal.net.AccessType;
import com.arangodb.internal.net.Host;
import com.arangodb.internal.net.HostHandle;
import com.arangodb.internal.net.HostHandler;
import com.arangodb.internal.serde.InternalSerde;
import com.arangodb.internal.util.RequestUtils;
import com.arangodb.internal.util.ResponseUtils;
import com.arangodb.internal.velocystream.internal.Chunk;
import com.arangodb.internal.velocystream.internal.Message;
import com.arangodb.internal.velocystream.internal.VstConnection;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackParserException;
import com.arangodb.internal.InternalRequest;
import com.arangodb.internal.InternalResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Mark Vollmary
 */
public abstract class VstCommunication<R, C extends VstConnection<?>> implements Closeable {

    protected static final String ENCRYPTION_PLAIN = "plain";
    protected static final String ENCRYPTION_JWT = "jwt";
    protected static final AtomicLong mId = new AtomicLong(0L);
    private static final Logger LOGGER = LoggerFactory.getLogger(VstCommunication.class);
    protected final InternalSerde util;

    protected final String user;
    protected final String password;
    protected final Integer chunksize;
    protected final HostHandler hostHandler;
    protected volatile String jwt;

    protected VstCommunication(final Integer timeout, final String user, final String password, final String jwt,
                               final Boolean useSsl, final SSLContext sslContext, final InternalSerde util,
                               final Integer chunksize, final HostHandler hostHandler) {
        this.user = user;
        this.password = password;
        this.jwt = jwt;
        this.util = util;
        this.hostHandler = hostHandler;
        this.chunksize = chunksize;
    }

    @SuppressWarnings("unchecked")
    protected synchronized C connect(final HostHandle hostHandle, final AccessType accessType) {
        Host host = hostHandler.get(hostHandle, accessType);
        while (true) {
            if (host == null) {
                hostHandler.reset();
                throw new ArangoDBException("Was not able to connect to any host");
            }
            final C connection = (C) host.connection();
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
                    hostHandler.confirm();
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
                        throw new ArangoDBException(e);
                    }
                }
            }
        }
    }

    private void tryAuthenticate(final C connection) {
        try {
            authenticate(connection);
        } catch (final ArangoDBException authException) {
            connection.close();
            throw authException;
        }
    }

    protected abstract void authenticate(final C connection);

    @Override
    public void close() throws IOException {
        hostHandler.close();
    }

    public R execute(final InternalRequest request, final HostHandle hostHandle) {
        return execute(request, hostHandle, 0);
    }

    protected R execute(final InternalRequest request, final HostHandle hostHandle, final int attemptCount) {
        final C connection = connect(hostHandle, RequestUtils.determineAccessType(request));
        return execute(request, connection, attemptCount);
    }

    protected abstract R execute(final InternalRequest request, C connection);

    protected abstract R execute(final InternalRequest request, C connection, final int attemptCount);

    protected void checkError(final InternalResponse response) {
        ResponseUtils.checkError(util, response);
    }

    protected InternalResponse createResponse(final Message message) throws VPackParserException {
        final InternalResponse response = util.deserialize(message.getHead().toByteArray(), InternalResponse.class);
        if (message.getBody() != null) {
            response.setBody(message.getBody().toByteArray());
        }
        return response;
    }

    protected final Message createMessage(final InternalRequest request) throws VPackParserException {
        request.putHeaderParam("accept", "application/x-velocypack");
        request.putHeaderParam("content-type", "application/x-velocypack");
        final long id = mId.incrementAndGet();
        return new Message(id, util.serialize(request), request.getBody());
    }

    protected Collection<Chunk> buildChunks(final Message message) {
        final Collection<Chunk> chunks = new ArrayList<>();
        final VPackSlice head = message.getHead();
        int size = head.getByteSize();
        final VPackSlice body = message.getBody();
        if (body != null) {
            size += body.getByteSize();
        }
        final int n = size / chunksize;
        final int numberOfChunks = (size % chunksize != 0) ? (n + 1) : n;
        int off = 0;
        for (int i = 0; size > 0; i++) {
            final int len = Math.min(chunksize, size);
            final long messageLength = (i == 0 && numberOfChunks > 1) ? size : -1L;
            final Chunk chunk = new Chunk(message.getId(), i, numberOfChunks, messageLength, off, len);
            size -= len;
            off += len;
            chunks.add(chunk);
        }
        return chunks;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }


}
