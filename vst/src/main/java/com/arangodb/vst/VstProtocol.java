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
import com.arangodb.arch.UnstableApi;
import com.arangodb.internal.InternalRequest;
import com.arangodb.internal.InternalResponse;
import com.arangodb.internal.net.CommunicationProtocol;
import com.arangodb.internal.net.HostHandle;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Mark Vollmary
 */
@UnstableApi
public class VstProtocol implements CommunicationProtocol {

    private final VstCommunication communication;
    private final ExecutorService outgoingExecutor = Executors.newCachedThreadPool();

    public VstProtocol(final VstCommunication communication) {
        super();
        this.communication = communication;
    }

    @Override
    public CompletableFuture<InternalResponse> executeAsync(@UnstableApi InternalRequest request, @UnstableApi HostHandle hostHandle) {
        if (outgoingExecutor.isShutdown()) {
            CompletableFuture<InternalResponse> cf = new CompletableFuture<>();
            cf.completeExceptionally(new ArangoDBException("VstProtocol already closed!"));
            return cf;
        }
        return CompletableFuture.completedFuture(null)
                .thenComposeAsync(__ -> communication.executeAsync(request, hostHandle), outgoingExecutor);
    }

    @Override
    public void setJwt(String jwt) {
        communication.setJwt(jwt);
    }

    @Override
    public void close() throws IOException {
        outgoingExecutor.shutdown();
        communication.close();
    }

}
