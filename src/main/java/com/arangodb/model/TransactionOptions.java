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
 * @author Mark - mark at arangodb.com
 * 
 * @see <a href="https://docs.arangodb.com/current/HTTP/Transaction/index.html#execute-transaction">API
 *      Documentation</a>
 */
public class TransactionOptions {

	private String action;
	private Object params;
	private final TransactionCollectionOptions collections;
	private Integer lockTimeout;
	private Boolean waitForSync;

	public TransactionOptions() {
		super();
		collections = new TransactionCollectionOptions();
	}

	protected String getAction() {
		return action;
	}

	protected TransactionOptions action(final String action) {
		this.action = action;
		return this;
	}

	public Object getParams() {
		return params;
	}

	/**
	 * @param params
	 *            optional arguments passed to action
	 * @return options
	 */
	public TransactionOptions params(final Object params) {
		this.params = params;
		return this;
	}

	public Integer getLockTimeout() {
		return lockTimeout;
	}

	/**
	 * @param lockTimeout
	 *            an optional numeric value that can be used to set a timeout for waiting on collection locks. If not
	 *            specified, a default value will be used. Setting lockTimeout to 0 will make ArangoDB not time out
	 *            waiting for a lock.
	 * @return options
	 */
	public TransactionOptions lockTimeout(final Integer lockTimeout) {
		this.lockTimeout = lockTimeout;
		return this;
	}

	public Boolean getWaitForSync() {
		return waitForSync;
	}

	/**
	 * @param waitForSync
	 *            an optional boolean flag that, if set, will force the transaction to write all data to disk before
	 *            returning
	 * @return options
	 */
	public TransactionOptions waitForSync(final Boolean waitForSync) {
		this.waitForSync = waitForSync;
		return this;
	}

	/**
	 * @param read
	 *            contains the array of collection-names to be used in the transaction (mandatory) for read
	 * @return options
	 */
	public TransactionOptions readCollections(final String... read) {
		collections.read(read);
		return this;
	}

	/**
	 * @param write
	 *            contains the array of collection-names to be used in the transaction (mandatory) for write
	 * @return options
	 */
	public TransactionOptions writeCollections(final String... write) {
		collections.write(write);
		return this;
	}

	public TransactionOptions allowImplicit(final Boolean allowImplicit) {
		collections.allowImplicit(allowImplicit);
		return this;
	}

	public static class TransactionCollectionOptions {

		private Collection<String> read;
		private Collection<String> write;
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

		public Boolean getAllowImplicit() {
			return allowImplicit;
		}

		public TransactionCollectionOptions allowImplicit(final Boolean allowImplicit) {
			this.allowImplicit = allowImplicit;
			return this;
		}

	}

}
