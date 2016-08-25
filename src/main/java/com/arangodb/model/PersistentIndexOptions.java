package com.arangodb.model;

import java.util.Collection;

import com.arangodb.entity.IndexType;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class PersistentIndexOptions {

	private Collection<String> fields;
	protected IndexType type = IndexType.persistent;
	private Boolean unique;
	private Boolean sparse;

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

	public PersistentIndexOptions unique(final Boolean unique) {
		this.unique = unique;
		return this;
	}

	public Boolean getSparse() {
		return sparse;
	}

	public PersistentIndexOptions sparse(final Boolean sparse) {
		this.sparse = sparse;
		return this;
	}

}
