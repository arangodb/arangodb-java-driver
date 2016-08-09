package com.arangodb.entity;

import java.util.Optional;

import com.arangodb.velocypack.annotations.Expose;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DocumentCreateResult<T> extends DocumentResult {

	@Expose(deserialize = false)
	private T newDocument;

	public DocumentCreateResult() {
		super();
	}

	public Optional<T> getNew() {
		return Optional.ofNullable(newDocument);
	}

	public void setNew(final T newDocument) {
		this.newDocument = newDocument;
	}

}
