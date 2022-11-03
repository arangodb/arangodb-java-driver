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

package com.arangodb;

import com.arangodb.model.QueueTimeSample;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Interface for accessing queue time latency metrics, reported by the "X-Arango-Queue-Time-Seconds" response header.
 * This header contains the most recent request (de)queuing time (in seconds) as tracked by the server’s scheduler.
 *
 * @author Michele Rastelli
 * @see <a href="https://www.arangodb.com/docs/stable/http/general.html#overload-control">API Documentation</a>
 * @since ArangoDB 3.9
 */
@ThreadSafe
public interface QueueTimeMetrics {

    /**
     * @return all the n values observed
     */
    QueueTimeSample[] getValues();

    /**
     * @return the average of the last n values observed, 0.0 if no value has been observed (i.e. in ArangoDB versions
     * prior to 3.9).
     */
    double getAvg();
}
