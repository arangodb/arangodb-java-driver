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

import com.arangodb.internal.velocystream.internal.Connection;
import com.arangodb.util.ArangoSerialization;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public abstract class ArangoExecuteable<E extends ArangoExecutor, R, C extends Connection> {

	protected final E executor;
	private final ArangoSerialization util;

	public ArangoExecuteable(final E executor, final ArangoSerialization util) {
		super();
		this.executor = executor;
		this.util = util;
	}

	protected E executor() {
		return executor;
	}

	public ArangoSerialization util() {
		return util;
	}
}
