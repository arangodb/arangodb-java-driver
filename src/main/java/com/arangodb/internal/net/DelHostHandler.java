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

import com.arangodb.internal.Host;

/**
 * @author Mark Vollmary
 *
 */
public class DelHostHandler implements HostHandler {

	private final HostHandler hostHandler;
	private Host host;

	public DelHostHandler(final HostHandler hostHandler, final Host host) {
		super();
		this.hostHandler = hostHandler;
		this.host = host != null ? host : hostHandler.get();
	}

	@Override
	public Host get() {
		return host != null ? host : hostHandler.get();
	}

	@Override
	public void success() {
		if (host == null) {
			hostHandler.success();
		}
	}

	@Override
	public void fail() {
		if (host == null) {
			hostHandler.fail();
		} else {
			host = null;
		}
	}

	@Override
	public void reset() {
		host = null;
		hostHandler.reset();
	}

}
