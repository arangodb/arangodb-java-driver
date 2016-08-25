package com.arangodb.entity;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class EdgeDefinition {

	private String collection;
	private Collection<String> from;
	private Collection<String> to;

	public String getCollection() {
		return collection;
	}

	public EdgeDefinition collection(final String collection) {
		this.collection = collection;
		return this;
	}

	public Collection<String> getFrom() {
		return from;
	}

	public EdgeDefinition from(final String... from) {
		this.from = Arrays.asList(from);
		return this;
	}

	public Collection<String> getTo() {
		return to;
	}

	public EdgeDefinition to(final String... to) {
		this.to = Arrays.asList(to);
		return this;
	}

}
