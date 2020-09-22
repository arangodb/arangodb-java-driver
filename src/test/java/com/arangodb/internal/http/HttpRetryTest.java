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

package com.arangodb.internal.http;


import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.Protocol;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.protocol.HttpContext;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

/**
 * @author Michele Rastelli
 */
public class HttpRetryTest {

    private final static int RETRIES = 2;

    private static class TestRetryHandler implements HttpRequestRetryHandler {
        private int retriesCounter = 0;

        @Override
        public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
            return ++retriesCounter < RETRIES;
        }

    }

    /**
     * remove host from src/test/resources/arangodb.properties to run this test
     */
    @Test
    @Ignore
    public void retry() {
        TestRetryHandler retryHandler = new TestRetryHandler();
        ArangoDB arangoDB = new ArangoDB.Builder()
                .host("wrongHost", 8529)
                .useProtocol(Protocol.HTTP_JSON)
                .httpRequestRetryHandler(retryHandler)
                .build();

        try {
            arangoDB.getVersion();
            fail("it should throw I/O exception");
        } catch (ArangoDBException e) {
            assertThat(retryHandler.retriesCounter, is(RETRIES));
        }

    }

}
