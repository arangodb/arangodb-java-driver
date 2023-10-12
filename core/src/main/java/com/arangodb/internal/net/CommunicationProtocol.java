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
import com.arangodb.internal.InternalRequest;
import com.arangodb.internal.InternalResponse;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

/**
 * @author Mark Vollmary
 */
public interface CommunicationProtocol extends Closeable {

    default InternalResponse execute(final InternalRequest request, final HostHandle hostHandle) {
        try {
            return executeAsync(request, hostHandle).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ArangoDBException.wrap(e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof CompletionException) {
                throw ArangoDBException.wrap(cause.getCause());
            } else {
                throw ArangoDBException.wrap(cause);
            }
        }
    }

    CompletableFuture<InternalResponse> executeAsync(final InternalRequest request, final HostHandle hostHandle);

    void setJwt(String jwt);

}
