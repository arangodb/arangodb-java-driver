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

import com.arangodb.internal.config.ArangoConfig;
import com.arangodb.internal.net.CommunicationProtocol;
import com.arangodb.internal.net.HostHandle;
import com.arangodb.internal.serde.RequestContextHolder;
import com.arangodb.internal.serde.SerdeUtils;

import java.lang.reflect.Type;

/**
 * @author Mark Vollmary
 */
public class ArangoExecutorSync extends ArangoExecutor {

    public ArangoExecutorSync(final CommunicationProtocol protocol, final ArangoConfig config) {
        super(protocol, config);
    }

    public <T> T execute(final InternalRequest request, final Type type) {
        return execute(request, type, null);
    }

    public <T> T execute(final InternalRequest request, final Type type, final HostHandle hostHandle) {
        return execute(request, (response) -> createResult(type, response), hostHandle);
    }

    public <T> T execute(final InternalRequest request, final ResponseDeserializer<T> responseDeserializer) {
        return execute(request, responseDeserializer, null);
    }

    public <T> T execute(
            final InternalRequest request,
            final ResponseDeserializer<T> responseDeserializer,
            final HostHandle hostHandle) {

        final InternalResponse response = protocol.execute(interceptRequest(request), hostHandle);
        interceptResponse(response);
        return RequestContextHolder.INSTANCE.runWithCtx(SerdeUtils.createRequestContext(request), () ->
                responseDeserializer.deserialize(response));
    }

}
