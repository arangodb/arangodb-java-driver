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
import com.arangodb.ArangoDBMultipleException;
import com.arangodb.internal.net.*;
import com.arangodb.util.ArangoSerialization;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Mark Vollmary
 */
class HostHandlerTest {

    private static final ConnectionPool mockCP = new ConnectionPool() {
        @Override
        public Connection createConnection(HostDescription host) {
            return null;
        }

        @Override
        public Connection connection() {
            return null;
        }

        @Override
        public void setJwt(String jwt) {

        }

        @Override
        public void close() {

        }
    };

    private static final Host HOST_0 = new HostImpl(mockCP, new HostDescription("127.0.0.1", 8529));
    private static final Host HOST_1 = new HostImpl(mockCP, new HostDescription("127.0.0.2", 8529));
    private static final Host HOST_2 = new HostImpl(mockCP, new HostDescription("127.0.0.3", 8529));

    private static final HostResolver SINGLE_HOST = new HostResolver() {

        @Override
        public HostSet resolve(final boolean initial, final boolean closeConnections) {

            HostSet set = new HostSet();
            set.addHost(HOST_0);
            return set;
        }

        @Override
        public void init(ArangoExecutorSync executor, ArangoSerialization arangoSerialization) {

        }

    };

    private static final HostResolver MULTIPLE_HOSTS = new HostResolver() {

        @Override
        public HostSet resolve(final boolean initial, final boolean closeConnections) {

            HostSet set = new HostSet();
            set.addHost(HOST_0);
            set.addHost(HOST_1);
            set.addHost(HOST_2);
            return set;
        }

        @Override
        public void init(ArangoExecutorSync executor, ArangoSerialization arangoSerialization) {

        }

    };

    @Test
    void fallbackHostHandlerSingleHost() {
        final HostHandler handler = new FallbackHostHandler(SINGLE_HOST);
        assertThat(handler.get(null, null)).isEqualTo(HOST_0);
        handler.fail(new RuntimeException());
        assertThat(handler.get(null, null)).isEqualTo(HOST_0);
    }

    @Test
    void fallbackHostHandlerMultipleHosts() {
        final HostHandler handler = new FallbackHostHandler(MULTIPLE_HOSTS);
        for (int i = 0; i < 3; i++) {
            assertThat(handler.get(null, null)).isEqualTo(HOST_0);
            handler.fail(new RuntimeException("HOST_0 failed"));
            assertThat(handler.get(null, null)).isEqualTo(HOST_1);
            handler.fail(new RuntimeException("HOST_1 failed"));
            assertThat(handler.get(null, null)).isEqualTo(HOST_2);
            handler.fail(new RuntimeException("HOST_2 failed"));
            if (i < 2) {
                assertThat(handler.get(null, null)).isEqualTo(HOST_0);
            } else {
                try {
                    handler.get(null, null);
                    fail();
                } catch (ArangoDBException e) {
                    assertThat(e.getCause()).isNotNull();
                    assertThat(e.getCause()).isInstanceOf(ArangoDBMultipleException.class);
                    List<Throwable> exceptions = ((ArangoDBMultipleException) e.getCause()).getExceptions();
                    assertThat(exceptions.get(0)).isInstanceOf(RuntimeException.class);
                    assertThat(exceptions.get(0).getMessage()).isEqualTo("HOST_0 failed");
                    assertThat(exceptions.get(1)).isInstanceOf(RuntimeException.class);
                    assertThat(exceptions.get(1).getMessage()).isEqualTo("HOST_1 failed");
                    assertThat(exceptions.get(2)).isInstanceOf(RuntimeException.class);
                    assertThat(exceptions.get(2).getMessage()).isEqualTo("HOST_2 failed");
                }
            }
        }
    }

    @Test
    void randomHostHandlerSingleHost() {
        final HostHandler handler = new RandomHostHandler(SINGLE_HOST, new FallbackHostHandler(SINGLE_HOST));
        assertThat(handler.get(null, null)).isEqualTo(HOST_0);
        handler.fail(new RuntimeException());
        assertThat(handler.get(null, null)).isEqualTo(HOST_0);
    }

    @Test
    void randomHostHandlerMultipleHosts() {
        final HostHandler handler = new RandomHostHandler(MULTIPLE_HOSTS, new FallbackHostHandler(MULTIPLE_HOSTS));

        final Host pick0 = handler.get(null, null);
        assertThat(pick0).isIn(HOST_0, HOST_1, HOST_2);
        handler.fail(new RuntimeException());

        final Host pick1 = handler.get(null, null);
        assertThat(pick1).isIn(HOST_0, HOST_1, HOST_2);
        handler.success();

        final Host pick3 = handler.get(null, null);
        assertThat(pick3)
                .isIn(HOST_0, HOST_1, HOST_2)
                .isEqualTo(pick1);
    }

    @Test
    void roundRobinHostHandlerSingleHost() {
        final HostHandler handler = new RoundRobinHostHandler(SINGLE_HOST);
        assertThat(handler.get(null, null)).isEqualTo(HOST_0);
        handler.fail(new RuntimeException());
        assertThat(handler.get(null, null)).isEqualTo(HOST_0);
    }

    @Test
    void roundRobinHostHandlerMultipleHosts() {
        final HostHandler handler = new RoundRobinHostHandler(MULTIPLE_HOSTS);
        final Host pick0 = handler.get(null, null);
        assertThat(pick0).isIn(HOST_0, HOST_1, HOST_2);
        final Host pick1 = handler.get(null, null);
        assertThat(pick1)
                .isIn(HOST_0, HOST_1, HOST_2)
                .isNotEqualTo(pick0);
        final Host pick2 = handler.get(null, null);
        assertThat(pick2)
                .isIn(HOST_0, HOST_1, HOST_2)
                .isNotIn(pick0, pick1);
        final Host pick4 = handler.get(null, null);
        assertThat(pick4).isEqualTo(pick0);
    }

}
