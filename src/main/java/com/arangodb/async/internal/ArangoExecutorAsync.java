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

package com.arangodb.async.internal;

import com.arangodb.ArangoDBException;
import com.arangodb.async.internal.velocystream.VstCommunicationAsync;
import com.arangodb.internal.ArangoExecutor;
import com.arangodb.internal.DocumentCache;
import com.arangodb.internal.QueueTimeMetricsImpl;
import com.arangodb.internal.net.HostHandle;
import com.arangodb.internal.util.ArangoSerializationFactory;
import com.arangodb.velocystream.Request;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public class ArangoExecutorAsync extends ArangoExecutor {

    private final VstCommunicationAsync communication;
    private final ExecutorService outgoingExecutor = Executors.newSingleThreadExecutor();

    public ArangoExecutorAsync(final VstCommunicationAsync communication, final ArangoSerializationFactory util,
                               final DocumentCache documentCache, final QueueTimeMetricsImpl qtMetrics, final int timeoutMs) {
        super(util, documentCache, qtMetrics, timeoutMs);
        this.communication = communication;
    }

    public <T> CompletableFuture<T> execute(final Request request, final Type type) {
        return execute(request, (response) -> createResult(type, response));
    }

    public <T> CompletableFuture<T> execute(final Request request, final Type type, final HostHandle hostHandle) {
        return execute(request, (response) -> createResult(type, response), hostHandle);
    }

    public <T> CompletableFuture<T> execute(final Request request, final ResponseDeserializer<T> responseDeserializer) {
        return execute(request, responseDeserializer, null);
    }

    private <T> CompletableFuture<T> execute(
            final Request request,
            final ResponseDeserializer<T> responseDeserializer,
            final HostHandle hostHandle) {

        return CompletableFuture.completedFuture(null)
                .thenComposeAsync((it) -> communication.execute(interceptRequest(request), hostHandle), outgoingExecutor)
                .thenApplyAsync(response -> {
                    interceptResponse(response);
                    return responseDeserializer.deserialize(response);
                });
    }

    public void disconnect() {
        try {
            communication.close();
        } catch (final IOException e) {
            throw new ArangoDBException(e);
        } finally {
            outgoingExecutor.shutdown();
        }
    }

    public void setJwt(String jwt) {
        communication.setJwt(jwt);
    }

}
