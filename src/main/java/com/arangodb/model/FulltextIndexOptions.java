package com.arangodb.model;

import java.util.Collection;

import com.arangodb.entity.IndexType;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class FulltextIndexOptions {

	private Collection<String> fields;
	private final IndexType type = IndexType.fulltext;
	private Integer minLength;

	protected Collection<String> getFields() {
		return fields;
	}

	protected FulltextIndexOptions fields(final Collection<String> fields) {
		this.fields = fields;
		return this;
	}

	protected IndexType getType() {
		return type;
	}

	public Integer getMinLength() {
		return minLength;
	}

	public FulltextIndexOptions minLength(final Integer minLength) {
		this.minLength = minLength;
		return this;
	}

}
