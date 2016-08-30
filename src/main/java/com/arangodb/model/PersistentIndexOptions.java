package com.arangodb.model;

import java.util.Collection;

import com.arangodb.entity.IndexType;

/**
 * @author Mark - mark at arangodb.com
 * 
 * @see <a href="https://docs.arangodb.com/current/HTTP/Indexes/Persistent.html#create-a-persistent-index">API
 *      Documentation</a>
 */
public class PersistentIndexOptions {

	private Collection<String> fields;
	protected IndexType type = IndexType.persistent;
	private Boolean unique;
	private Boolean sparse;

	public PersistentIndexOptions() {
		super();
	}

	protected Collection<String> getFields() {
		return fields;
	}

	protected PersistentIndexOptions fields(final Collection<String> fields) {
		this.fields = fields;
		return this;
	}

	protected IndexType getType() {
		return type;
	}

	public Boolean getUnique() {
		return unique;
	}

	/**
	 * @param unique
	 *            if true, then create a unique index
	 * @return options
	 */
	public PersistentIndexOptions unique(final Boolean unique) {
		this.unique = unique;
		return this;
	}

	public Boolean getSparse() {
		return sparse;
	}

	/**
	 * @param sparse
	 *            if true, then create a sparse index
	 * @return options
	 */
	public PersistentIndexOptions sparse(final Boolean sparse) {
		this.sparse = sparse;
		return this;
	}

}
