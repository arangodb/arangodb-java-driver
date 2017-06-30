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

import java.util.List;

/**
 * @author Mark Vollmary
 *
 */
public class DefaultHostHandler implements HostHandler {

	private final List<Host> hosts;
	private int current;
	private int lastSuccess;

	/**
	 * @param hosts
	 */
	public DefaultHostHandler(final List<Host> hosts) {
		this.hosts = hosts;
		current = lastSuccess = 0;
	}

	@Override
	public Host get() {
		return hosts.get(current);
	}

	@Override
	public Host change() {
		current++;
		if ((current + 1) > hosts.size()) {
			current -= hosts.size();
		}
		return current != lastSuccess ? get() : null;
	}

	@Override
	public void success() {
		lastSuccess = current;
	}

	@Override
	public void fail() {
	}

}
