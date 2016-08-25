package com.arangodb.model;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Mark - mark at arangodb.com
 *
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

	public String getAction() {
		return action;
	}

	public TransactionOptions action(final String action) {
		this.action = action;
		return this;
	}

	public Object getParams() {
		return params;
	}

	public TransactionOptions params(final Object params) {
		this.params = params;
		return this;
	}

	public Integer getLockTimeout() {
		return lockTimeout;
	}

	public TransactionOptions lockTimeout(final Integer lockTimeout) {
		this.lockTimeout = lockTimeout;
		return this;
	}

	public Boolean getWaitForSync() {
		return waitForSync;
	}

	public TransactionOptions waitForSync(final Boolean waitForSync) {
		this.waitForSync = waitForSync;
		return this;
	}

	public TransactionOptions readCollections(final String... read) {
		collections.read(read);
		return this;
	}

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
