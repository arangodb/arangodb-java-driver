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
import com.arangodb.QueueTimeMetrics;
import com.arangodb.internal.config.ArangoConfig;
import com.arangodb.internal.net.CommunicationProtocol;
import com.arangodb.internal.serde.InternalSerde;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @author Mark Vollmary
 */
public abstract class ArangoExecutor {

    protected final CommunicationProtocol protocol;
    private final QueueTimeMetricsImpl qtMetrics;
    private final InternalSerde serde;
    private final String timeoutS;

    protected ArangoExecutor(final CommunicationProtocol protocol, final ArangoConfig config ) {
        this.protocol = protocol;
        qtMetrics = new QueueTimeMetricsImpl(config.getResponseQueueTimeSamples());
        serde = config.getInternalSerde();
        timeoutS = config.getTimeout() >= 1000 ? Integer.toString(config.getTimeout() / 1000) : null;
    }

    public void disconnect() {
        try {
            protocol.close();
        } catch (final IOException e) {
            throw ArangoDBException.of(e);
        }
    }

    public void setJwt(String jwt) {
        protocol.setJwt(jwt);
    }

    protected <T> T createResult(final Type type, final InternalResponse response) {
        return serde.deserialize(response.getBody(), type);
    }

    protected final void interceptResponse(InternalResponse response) {
        String queueTime = response.getMeta("X-Arango-Queue-Time-Seconds");
        if (queueTime != null) {
            qtMetrics.add(Double.parseDouble(queueTime));
        }
    }

    protected final InternalRequest interceptRequest(InternalRequest request) {
        request.putHeaderParam("x-arango-queue-time-seconds", timeoutS);
        return request;
    }

    public QueueTimeMetrics getQueueTimeMetrics() {
        return qtMetrics;
    }

    public interface ResponseDeserializer<T> {
        T deserialize(InternalResponse response);
    }
}
