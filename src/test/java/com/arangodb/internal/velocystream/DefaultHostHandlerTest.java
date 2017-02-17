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

package com.arangodb.internal.velocystream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DefaultHostHandlerTest {

	@Test
	public void singleHost() {
		final Host h1 = new Host("127.0.0.1", 8529);
		final List<Host> hosts = Collections.<Host> singletonList(h1);
		final HostHandler hh = new DefaultHostHandler(hosts);
		assertThat(hh.get(), is(h1));
	}

	@Test
	public void multipleHosts() {
		final Host h1 = new Host("127.0.0.1", 8529);
		final Host h2 = new Host("127.0.0.2", 8529);
		final List<Host> hosts = new ArrayList<Host>();
		hosts.add(h1);
		hosts.add(h2);

		final HostHandler hh = new DefaultHostHandler(hosts);
		assertThat(hh.get(), is(h1));
		assertThat(hh.change(), is(h2));
		assertThat(hh.get(), is(h2));
		assertThat(hh.change(), is(nullValue()));
	}

}
