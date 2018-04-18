/*
 * DISCLAIMER
 *
 * Copyright 2017 ArangoDB GmbH, Cologne, Germany
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

import java.util.List;

import com.arangodb.internal.Host;

/**
 * @author Mark Vollmary
 *
 */
public class RoundRobinHostHandler implements HostHandler {

	private final HostResolver resolver;
	private Host current;
	private int fails;

	public RoundRobinHostHandler(final HostResolver resolver) {
		super();
		this.resolver = resolver;
		current = resolver.resolve(true, false).get(0);
		fails = 0;
	}

	@Override
	public Host get() {
		final List<Host> hosts = resolver.resolve(false, false);
		if (fails > hosts.size()) {
			return null;
		}
		final int index = hosts.indexOf(current) + 1;
		current = hosts.get(index < hosts.size() ? index : 0);
		return current;
	}

	@Override
	public void success() {
		fails = 0;
	}

	@Override
	public void fail() {
		fails++;
	}

	@Override
	public void reset() {
		fails = 0;
	}

}
