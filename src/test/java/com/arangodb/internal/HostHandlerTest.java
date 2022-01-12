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
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import static org.junit.Assert.fail;

/**
 * @author Mark Vollmary
 */
public class HostHandlerTest {

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
        public void close() throws IOException {

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
    public void fallbackHostHandlerSingleHost() {
        final HostHandler handler = new FallbackHostHandler(SINGLE_HOST);
        assertThat(handler.get(null, null), is(HOST_0));
        handler.fail(new RuntimeException());
        assertThat(handler.get(null, null), is(HOST_0));
    }

    @Test
    public void fallbackHostHandlerMultipleHosts() {
        final HostHandler handler = new FallbackHostHandler(MULTIPLE_HOSTS);
        for (int i = 0; i < 3; i++) {
            assertThat(handler.get(null, null), is(HOST_0));
            handler.fail(new RuntimeException("HOST_0 failed"));
            assertThat(handler.get(null, null), is(HOST_1));
            handler.fail(new RuntimeException("HOST_1 failed"));
            assertThat(handler.get(null, null), is(HOST_2));
            handler.fail(new RuntimeException("HOST_2 failed"));
            if (i < 2) {
                assertThat(handler.get(null, null), is(HOST_0));
            } else {
                try {
                    handler.get(null, null);
                    fail();
                } catch (ArangoDBException e) {
                    assertThat(e.getCause(), is(notNullValue()));
                    assertThat(e.getCause(), is(instanceOf(ArangoDBMultipleException.class)));
                    List<Throwable> exceptions = ((ArangoDBMultipleException) e.getCause()).getExceptions();
                    assertThat(exceptions.get(0), is(instanceOf(RuntimeException.class)));
                    assertThat(exceptions.get(0).getMessage(), is("HOST_0 failed"));
                    assertThat(exceptions.get(1), is(instanceOf(RuntimeException.class)));
                    assertThat(exceptions.get(1).getMessage(), is("HOST_1 failed"));
                    assertThat(exceptions.get(2), is(instanceOf(RuntimeException.class)));
                    assertThat(exceptions.get(2).getMessage(), is("HOST_2 failed"));
                }
            }
        }
    }

    @Test
    public void randomHostHandlerSingleHost() {
        final HostHandler handler = new RandomHostHandler(SINGLE_HOST, new FallbackHostHandler(SINGLE_HOST));
        assertThat(handler.get(null, null), is(HOST_0));
        handler.fail(new RuntimeException());
        assertThat(handler.get(null, null), is(HOST_0));
    }

    @Test
    public void randomHostHandlerMultipeHosts() {
        final HostHandler handler = new RandomHostHandler(MULTIPLE_HOSTS, new FallbackHostHandler(MULTIPLE_HOSTS));

        final Host pick0 = handler.get(null, null);
        assertThat(pick0, anyOf(is(HOST_0), is(HOST_1), is(HOST_2)));
        handler.fail(new RuntimeException());

        final Host pick1 = handler.get(null, null);
        assertThat(pick1, anyOf(is(HOST_0), is(HOST_1), is(HOST_2)));
        handler.success();

        final Host pick3 = handler.get(null, null);
        assertThat(pick3, anyOf(is(HOST_0), is(HOST_1), is(HOST_2)));
        assertThat(pick3, is(pick1));
    }

    @Test
    public void roundRobinHostHandlerSingleHost() {
        final HostHandler handler = new RoundRobinHostHandler(SINGLE_HOST);
        assertThat(handler.get(null, null), is(HOST_0));
        handler.fail(new RuntimeException());
        assertThat(handler.get(null, null), is(HOST_0));
    }

    @Test
    public void roundRobinHostHandlerMultipleHosts() {
        final HostHandler handler = new RoundRobinHostHandler(MULTIPLE_HOSTS);
        final Host pick0 = handler.get(null, null);
        assertThat(pick0, anyOf(is(HOST_0), is(HOST_1), is(HOST_2)));
        final Host pick1 = handler.get(null, null);
        assertThat(pick1, anyOf(is(HOST_0), is(HOST_1), is(HOST_2)));
        assertThat(pick1, is(not(pick0)));
        final Host pick2 = handler.get(null, null);
        assertThat(pick2, anyOf(is(HOST_0), is(HOST_1), is(HOST_2)));
        assertThat(pick2, not(anyOf(is(pick0), is(pick1))));
        final Host pick4 = handler.get(null, null);
        assertThat(pick4, is(pick0));
    }

}
