package com.arangodb.model;

import java.util.Collection;

import com.arangodb.entity.IndexType;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class SkiplistIndexOptions {

	private Collection<String> fields;
	private final IndexType type = IndexType.skiplist;
	private Boolean unique;
	private Boolean sparse;

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

	public SkiplistIndexOptions unique(final Boolean unique) {
		this.unique = unique;
		return this;
	}

	public Boolean getSparse() {
		return sparse;
	}

	public SkiplistIndexOptions sparse(final Boolean sparse) {
		this.sparse = sparse;
		return this;
	}

}
