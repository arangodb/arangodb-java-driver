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

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.arangodb.internal.net.FallbackHostHandler;
import com.arangodb.internal.net.HostDescription;
import com.arangodb.internal.net.HostHandler;
import com.arangodb.internal.net.HostImpl;
import com.arangodb.internal.net.HostResolver;
import com.arangodb.internal.net.Host;
import com.arangodb.internal.net.RandomHostHandler;
import com.arangodb.internal.net.RoundRobinHostHandler;

/**
 * @author Mark Vollmary
 *
 */
public class HostHandlerTest {

	private static final Host HOST_0 = new HostImpl(null, new HostDescription("127.0.0.1", 8529));
	private static final Host HOST_1 = new HostImpl(null, new HostDescription("127.0.0.2", 8529));
	private static final Host HOST_2 = new HostImpl(null, new HostDescription("127.0.0.3", 8529));

	private static final HostResolver SINGLE_HOST = new HostResolver() {
		@Override
		public List<Host> resolve(final boolean initial, final boolean closeConnections) {
			return Collections.<Host> singletonList(HOST_0);
		}

		@Override
		public void init(final EndpointResolver resolver) {
		}
	};
	private static final HostResolver MULTIPLE_HOSTS = new HostResolver() {
		@Override
		public List<Host> resolve(final boolean initial, final boolean closeConnections) {
			final ArrayList<Host> hosts = new ArrayList<Host>();
			hosts.add(HOST_0);
			hosts.add(HOST_1);
			hosts.add(HOST_2);
			return hosts;
		}

		@Override
		public void init(final EndpointResolver resolver) {
		}
	};

	@Test
	public void fallbachHostHandlerSingleHost() {
		final HostHandler handler = new FallbackHostHandler(SINGLE_HOST);
		assertThat(handler.get(null, null), is(HOST_0));
		handler.fail();
		assertThat(handler.get(null, null), is(HOST_0));
	}

	@Test
	public void fallbackHostHandlerMultipleHosts() {
		final HostHandler handler = new FallbackHostHandler(MULTIPLE_HOSTS);
		for (int i = 0; i < 3; i++) {
			assertThat(handler.get(null, null), is(HOST_0));
			handler.fail();
			assertThat(handler.get(null, null), is(HOST_1));
			handler.fail();
			assertThat(handler.get(null, null), is(HOST_2));
			if (i < 2) {
				handler.fail();
				assertThat(handler.get(null, null), is(HOST_0));
			} else {
				handler.fail();
				assertThat(handler.get(null, null), is(nullValue()));
			}
		}
	}

	@Test
	public void randomHostHandlerSingleHost() {
		final HostHandler handler = new RandomHostHandler(SINGLE_HOST, new FallbackHostHandler(SINGLE_HOST));
		assertThat(handler.get(null, null), is(HOST_0));
		handler.fail();
		assertThat(handler.get(null, null), is(HOST_0));
	}

	@Test
	public void randomHostHandlerMultipeHosts() {
		final HostHandler handler = new RandomHostHandler(MULTIPLE_HOSTS, new FallbackHostHandler(MULTIPLE_HOSTS));
		final Host pick0 = handler.get(null, null);
		assertThat(pick0, anyOf(is(HOST_0), is(HOST_1), is(HOST_2)));
		handler.fail();
		assertThat(handler.get(null, null), anyOf(is(HOST_0), is(HOST_1), is(HOST_2)));
		handler.success();
		assertThat(handler.get(null, null), is(pick0));
	}

	@Test
	public void roundRobinHostHandlerSingleHost() {
		final HostHandler handler = new RoundRobinHostHandler(SINGLE_HOST);
		assertThat(handler.get(null, null), is(HOST_0));
		handler.fail();
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
