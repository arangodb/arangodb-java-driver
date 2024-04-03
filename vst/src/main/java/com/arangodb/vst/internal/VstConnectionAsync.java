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

package com.arangodb.vst.internal;

import com.arangodb.PackageVersion;
import com.arangodb.config.HostDescription;
import com.arangodb.internal.InternalRequest;
import com.arangodb.internal.InternalResponse;
import com.arangodb.internal.config.ArangoConfig;
import com.arangodb.internal.serde.InternalSerde;
import com.arangodb.internal.serde.SerdeContextImpl;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackParserException;
import com.arangodb.vst.internal.utils.CompletableFutureUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Mark Vollmary
 */
public class VstConnectionAsync extends VstConnection<CompletableFuture<Message>> {
    private final static Logger LOGGER = LoggerFactory.getLogger(VstConnectionAsync.class);
    private static final AtomicLong mId = new AtomicLong(0L);
    private static final String X_ARANGO_DRIVER = "JavaDriver/" + PackageVersion.VERSION + " (JVM/" + System.getProperty("java.specification.version") + ")";
    private final Integer chunkSize;
    private final InternalSerde serde;


    public VstConnectionAsync(final ArangoConfig config, final HostDescription host) {
        super(config, host);
        chunkSize = config.getChunkSize();
        serde = config.getInternalSerde();
    }

    @Override
    public synchronized CompletableFuture<Message> write(final Message message, final Collection<Chunk> chunks) {
        final CompletableFuture<Message> future = new CompletableFuture<>();
        final FutureTask<Message> task = new FutureTask<>(() -> {
            try {
                future.complete(messageStore.get(message.getId()));
            } catch (final Exception e) {
                future.completeExceptionally(e);
            }
            return null;
        });
        messageStore.storeMessage(message.getId(), task);
        super.writeIntern(message, chunks);
        if (timeout == null || timeout == 0L) {
            return future;
        } else {
            return CompletableFutureUtils.orTimeout(future, timeout, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    protected void doKeepAlive() {
        sendKeepAlive().join();
    }

    @Override
    public CompletableFuture<InternalResponse> executeAsync(final InternalRequest request) {
        // TODO: refactor using Future composition
        final CompletableFuture<InternalResponse> rfuture = new CompletableFuture<>();
        try {
            final Message message = createMessage(request);
            send(message).whenComplete((m, ex) -> {
                if (m != null) {
                    final InternalResponse response;
                    try {
                        response = createResponse(m);
                    } catch (final Exception e) {
                        rfuture.completeExceptionally(e);
                        return;
                    }
                    rfuture.complete(response);
                } else  {
                    Throwable e = ex instanceof CompletionException ? ex.getCause() : ex;
                    rfuture.completeExceptionally(e);
                }
            });
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            rfuture.completeExceptionally(e);
        }
        return rfuture;
    }

    private Message createMessage(final InternalRequest request) throws VPackParserException {
        request.putHeaderParam("accept", "application/x-velocypack");
        request.putHeaderParam("content-type", "application/x-velocypack");
        request.putHeaderParam("x-arango-driver", X_ARANGO_DRIVER);
        final long id = mId.incrementAndGet();
        return new Message(id, serde.serialize(request), request.getBody());
    }

    private CompletableFuture<Message> send(final Message message) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("Send Message (id=%s, head=%s, body=%s)",
                    message.getId(),
                    serde.toJsonString(message.getHead().toByteArray()),
                    message.getBody() != null ? serde.toJsonString(message.getBody().toByteArray()) : "{}"));
        }
        return write(message, buildChunks(message));
    }

    private Collection<Chunk> buildChunks(final Message message) {
        final Collection<Chunk> chunks = new ArrayList<>();
        final VPackSlice head = message.getHead();
        int size = head.getByteSize();
        final VPackSlice body = message.getBody();
        if (body != null) {
            size += body.getByteSize();
        }
        final int n = size / chunkSize;
        final int numberOfChunks = (size % chunkSize != 0) ? (n + 1) : n;
        int off = 0;
        for (int i = 0; size > 0; i++) {
            final int len = Math.min(chunkSize, size);
            final long messageLength = (i == 0 && numberOfChunks > 1) ? size : -1L;
            final Chunk chunk = new Chunk(message.getId(), i, numberOfChunks, messageLength, off, len);
            size -= len;
            off += len;
            chunks.add(chunk);
        }
        return chunks;
    }

    private InternalResponse createResponse(final Message message) throws VPackParserException {
        InternalResponse response = serde.deserialize(message.getHead().toByteArray(), InternalResponse.class, SerdeContextImpl.EMPTY);
        if (message.getBody() != null) {
            response.setBody(message.getBody().toByteArray());
        }
        return response;
    }
}
