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

import java.util.List;

import com.arangodb.internal.Host;

/**
 * @author Mark Vollmary
 *
 */
public class FallbackHostHandler implements HostHandler {

	private Host current;
	private Host lastSuccess;
	private int iterations;
	private final HostResolver resolver;

	public FallbackHostHandler(final HostResolver resolver) {
		this.resolver = resolver;
		iterations = 0;
		current = lastSuccess = resolver.resolve(true, false).get(0);
	}

	@Override
	public Host get() {
		return current != lastSuccess || iterations < 3 ? current : null;
	}

	@Override
	public void success() {
		lastSuccess = current;
	}

	@Override
	public void fail() {
		final List<Host> hosts = resolver.resolve(false, false);
		final int index = hosts.indexOf(current) + 1;
		final boolean inBound = index < hosts.size();
		current = hosts.get(inBound ? index : 0);
		if (!inBound) {
			iterations++;
		}
	}

}
