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

package com.arangodb.internal;

import com.arangodb.ArangoDBException;
import com.arangodb.internal.config.ArangoConfig;
import com.arangodb.internal.net.CommunicationProtocol;
import com.arangodb.internal.net.HostHandle;
import com.arangodb.RequestContext;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public class ArangoExecutorAsync extends ArangoExecutor {
    private final Executor downstreamExecutor;

    public ArangoExecutorAsync(final CommunicationProtocol protocol, final ArangoConfig config) {
        super(protocol, config);
        downstreamExecutor = config.getAsyncExecutor();
    }

    public <T> CompletableFuture<T> execute(final Supplier<InternalRequest> requestSupplier, final Type type) {
        return execute(requestSupplier, type, null);
    }

    public <T> CompletableFuture<T> execute(final Supplier<InternalRequest> requestSupplier, final Type type, final HostHandle hostHandle) {
        return execute(requestSupplier, (response) -> createResult(type, response), hostHandle);
    }

    public <T> CompletableFuture<T> execute(final Supplier<InternalRequest> requestSupplier, final ResponseDeserializer<T> responseDeserializer) {
        return execute(requestSupplier, responseDeserializer, null);
    }

    public <T> CompletableFuture<T> execute(
            final Supplier<InternalRequest> requestSupplier,
            final ResponseDeserializer<T> responseDeserializer,
            final HostHandle hostHandle) {

        CompletableFuture<T> cf = CompletableFuture.completedFuture(requestSupplier)
                .thenApply(Supplier::get)
                .thenCompose(request -> protocol
                        .executeAsync(interceptRequest(request), hostHandle)
                        .thenApply(resp -> new ResponseWithRequest(resp, new RequestContextImpl(request)))
                )
                .handle((r, e) -> {
                    if (e != null) {
                        throw ArangoDBException.of(e);
                    } else {
                        interceptResponse(r.response);
                        return RequestContextHolder.INSTANCE.runWithCtx(r.context, () ->
                                responseDeserializer.deserialize(r.response));
                    }
                });

        if (downstreamExecutor != null) {
            return cf.thenApplyAsync(Function.identity(), downstreamExecutor);
        } else {
            return cf;
        }
    }

    private static class ResponseWithRequest {
        final InternalResponse response;
        final RequestContext context;

        ResponseWithRequest(InternalResponse response, RequestContext context) {
            this.response = response;
            this.context = context;
        }
    }

}
