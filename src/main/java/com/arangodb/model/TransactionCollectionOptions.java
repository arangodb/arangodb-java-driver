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

package com.arangodb.model;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public class TransactionCollectionOptions {

	private Collection<String> read;
	private Collection<String> write;
	private Collection<String> exclusive;
	private Boolean allowImplicit;

	public Collection<String> getRead() {
		return read;
	}

	public TransactionCollectionOptions read(final String... read) {
		this.read = Arrays.asList(read);
		return this;
	}

	public Collection<String> getWrite() {
		return write;
	}

	public TransactionCollectionOptions write(final String... write) {
		this.write = Arrays.asList(write);
		return this;
	}

	public Collection<String> getExclusive() {
		return exclusive;
	}

	public TransactionCollectionOptions exclusive(final String... exclusive) {
		this.exclusive = Arrays.asList(exclusive);
		return this;
	}

	public Boolean getAllowImplicit() {
		return allowImplicit;
	}

	public TransactionCollectionOptions allowImplicit(final Boolean allowImplicit) {
		this.allowImplicit = allowImplicit;
		return this;
	}

}
