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

package com.arangodb.internal.net;

import com.arangodb.ArangoDBException;
import com.arangodb.arch.UsedInApi;
import com.arangodb.internal.InternalRequest;
import com.arangodb.internal.InternalResponse;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author Mark Vollmary
 */
@UsedInApi
public interface CommunicationProtocol extends Closeable {

    default InternalResponse execute(final InternalRequest request, final HostHandle hostHandle) {
        try {
            return executeAsync(request, hostHandle).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ArangoDBException.of(e);
        } catch (ExecutionException e) {
            throw ArangoDBException.of(e.getCause());
        }
    }

    CompletableFuture<InternalResponse> executeAsync(final InternalRequest request, final HostHandle hostHandle);

    void setJwt(String jwt);

}
