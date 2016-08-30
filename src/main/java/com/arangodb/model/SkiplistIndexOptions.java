package com.arangodb.model;

import java.util.Collection;

import com.arangodb.entity.IndexType;

/**
 * @author Mark - mark at arangodb.com
 * 
 * @see <a href="https://docs.arangodb.com/current/HTTP/Indexes/Skiplist.html#create-skip-list">API Documentation</a>
 */
public class SkiplistIndexOptions {

	private Collection<String> fields;
	private final IndexType type = IndexType.skiplist;
	private Boolean unique;
	private Boolean sparse;

	public SkiplistIndexOptions() {
		super();
	}

	protected Collection<String> getFields() {
		return fields;
	}

	protected SkiplistIndexOptions fields(final Collection<String> fields) {
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
	public SkiplistIndexOptions unique(final Boolean unique) {
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
	public SkiplistIndexOptions sparse(final Boolean sparse) {
		this.sparse = sparse;
		return this;
	}

}
