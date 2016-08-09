package com.arangodb.entity;

import java.util.Optional;

import com.arangodb.velocypack.annotations.Expose;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DocumentDeleteResult<T> extends DocumentResult {

	@Expose(deserialize = false)
	private T oldDocument;

	public DocumentDeleteResult() {
		super();
	}

	public Optional<T> getOld() {
		return Optional.ofNullable(oldDocument);
	}

	public void setOld(final T oldDocument) {
		this.oldDocument = oldDocument;
	}
}
