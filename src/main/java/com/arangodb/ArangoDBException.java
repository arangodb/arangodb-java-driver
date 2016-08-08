package com.arangodb;

import java.util.Optional;

import com.arangodb.entity.ErrorEntity;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ArangoDBException extends RuntimeException {

	private Optional<ErrorEntity> errorEntity = Optional.empty();

	public ArangoDBException(final String message) {
		super(message);
	}

	public ArangoDBException(final Exception e) {
		super(e);
	}

	public ArangoDBException(final ErrorEntity errorEntity) {
		super(errorEntity.getErrorMessage());
		this.errorEntity = Optional.of(errorEntity);
	}

	public boolean isError() {
		return errorEntity.isPresent() && errorEntity.get().isError();
	}

	public Optional<String> getErrorMessage() {
		return errorEntity.flatMap(e -> {
			return Optional.ofNullable(e.getErrorMessage());
		});
	}

	public Optional<Integer> getCode() {
		return errorEntity.flatMap(e -> {
			return Optional.ofNullable(e.getCode());
		});
	}

	public Optional<Integer> getErrorNum() {
		return errorEntity.flatMap(e -> {
			return Optional.ofNullable(e.getErrorNum());
		});
	}

}
