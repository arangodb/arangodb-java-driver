package com.arangodb.entity;

import java.util.Optional;

import com.arangodb.velocypack.annotations.Expose;
import com.arangodb.velocypack.annotations.SerializedName;

/**
 * @author Mark - mark at arangodb.com
 * @param <T>
 *
 */
public class DocumentUpdateResult<T> extends DocumentResult {

	@SerializedName("_oldRev")
	private String oldRev;
	@Expose(deserialize = false)
	private T newDocument;
	@Expose(deserialize = false)
	private T oldDocument;

	public DocumentUpdateResult() {
		super();
	}

	public String getOldRev() {
		return oldRev;
	}

	public Optional<T> getNew() {
		return Optional.ofNullable(newDocument);
	}

	public void setNew(final T newDocument) {
		this.newDocument = newDocument;
	}

	public Optional<T> getOld() {
		return Optional.ofNullable(oldDocument);
	}

	public void setOld(final T oldDocument) {
		this.oldDocument = oldDocument;
	}

}
