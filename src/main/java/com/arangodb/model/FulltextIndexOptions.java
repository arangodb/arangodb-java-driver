package com.arangodb.model;

import java.util.Collection;

import com.arangodb.entity.IndexType;

/**
 * @author Mark - mark at arangodb.com
 *
 * @see <a href="https://docs.arangodb.com/current/HTTP/Indexes/Fulltext.html#create-fulltext-index">API
 *      Documentation</a>
 */
public class FulltextIndexOptions {

	private Collection<String> fields;
	private final IndexType type = IndexType.fulltext;
	private Integer minLength;

	public FulltextIndexOptions() {
		super();
	}

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

	/**
	 * @param minLength
	 *            Minimum character length of words to index. Will default to a server-defined value if unspecified. It
	 *            is thus recommended to set this value explicitly when creating the index.
	 * @return options
	 */
	public FulltextIndexOptions minLength(final Integer minLength) {
		this.minLength = minLength;
		return this;
	}

}
