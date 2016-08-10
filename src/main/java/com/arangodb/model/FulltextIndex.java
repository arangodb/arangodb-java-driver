package com.arangodb.model;

import java.util.Collection;

import com.arangodb.entity.IndexType;

/**
 * @author Mark - mark at arangodb.com
 *
 */
@SuppressWarnings("unused")
public class FulltextIndex {

	private final Collection<String> fields;
	private final IndexType type;
	private final Integer minLength;

	private FulltextIndex(final Collection<String> fields, final IndexType type, final Integer minLength) {
		super();
		this.fields = fields;
		this.type = type;
		this.minLength = minLength;
	}

	public static class Options {

		private Integer minLength;

		public Options minLength(final Integer minLength) {
			this.minLength = minLength;
			return this;
		}

		public FulltextIndex build(final Collection<String> fields) {
			return new FulltextIndex(fields, IndexType.FULLTEXT, minLength);
		}
	}
}
