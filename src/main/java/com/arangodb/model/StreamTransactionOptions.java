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

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 * @see <a href="https://docs.arangodb.com/current/HTTP/transaction-stream-transaction.html">API Documentation</a>
 * @since ArangoDB 3.5.0
 */
public class StreamTransactionOptions {

	private final TransactionCollectionOptions collections;
	private Integer lockTimeout;
	private Boolean waitForSync;
	private Long maxTransactionSize;

	public StreamTransactionOptions() {
		super();
		collections = new TransactionCollectionOptions();
	}

	public Integer getLockTimeout() {
		return lockTimeout;
	}

	/**
	 * @param lockTimeout an optional numeric value that can be used to set a timeout for waiting on collection locks. If not
	 *                    specified, a default value will be used. Setting lockTimeout to 0 will make ArangoDB not time out
	 *                    waiting for a lock.
	 * @return options
	 */
	public StreamTransactionOptions lockTimeout(final Integer lockTimeout) {
		this.lockTimeout = lockTimeout;
		return this;
	}

	public Boolean getWaitForSync() {
		return waitForSync;
	}

	/**
	 * @param waitForSync an optional boolean flag that, if set, will force the transaction to write all data to disk before
	 *                    returning
	 * @return options
	 */
	public StreamTransactionOptions waitForSync(final Boolean waitForSync) {
		this.waitForSync = waitForSync;
		return this;
	}

	/**
	 * @param read contains the array of collection-names to be used in the transaction (mandatory) for read
	 * @return options
	 */
	public StreamTransactionOptions readCollections(final String... read) {
		collections.read(read);
		return this;
	}

	/**
	 * @param write contains the array of collection-names to be used in the transaction (mandatory) for write
	 * @return options
	 */
	public StreamTransactionOptions writeCollections(final String... write) {
		collections.write(write);
		return this;
	}

	/**
	 * @param exclusive contains the array of collection-names to be used in the transaction (mandatory) for exclusive write
	 * @return options
	 */
	public StreamTransactionOptions exclusiveCollections(final String... exclusive) {
		collections.exclusive(exclusive);
		return this;
	}

	/**
	 * @param allowImplicit Collections that will be written to in the transaction must be declared with the write attribute or it
	 *                      will fail, whereas non-declared collections from which is solely read will be added lazily. The
	 *                      optional attribute allowImplicit can be set to false to let transactions fail in case of undeclared
	 *                      collections for reading. Collections for reading should be fully declared if possible, to avoid
	 *                      deadlocks.
	 * @return options
	 */
	public StreamTransactionOptions allowImplicit(final Boolean allowImplicit) {
		collections.allowImplicit(allowImplicit);
		return this;
	}

	public Long getMaxTransactionSize() {
		return maxTransactionSize;
	}

	/**
	 * @param maxTransactionSize Transaction size limit in bytes. Honored by the RocksDB storage engine only.
	 * @return options
	 */
	public StreamTransactionOptions maxTransactionSize(final Long maxTransactionSize) {
		this.maxTransactionSize = maxTransactionSize;
		return this;
	}

}
