/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb;

import com.arangodb.entity.ErrorEntity;

/**
 * @author Mark Vollmary
 *
 */
public class ArangoDBException extends RuntimeException {

	private static final long serialVersionUID = 6165638002614173801L;
	private ErrorEntity entity = null;

	public ArangoDBException(final ErrorEntity errorEntity) {
		super(String.format("Response: %s, Error: %s - %s", errorEntity.getCode(), errorEntity.getErrorNum(),
			errorEntity.getErrorMessage()));
		this.entity = errorEntity;
	}

	public ArangoDBException(final String message) {
		super(message);
	}

	public ArangoDBException(final Throwable cause) {
		super(cause);
	}

	public String getErrorMessage() {
		return entity != null ? entity.getErrorMessage() : null;
	}

	public String getException() {
		return entity != null ? entity.getException() : null;
	}

	public int getResponseCode() {
		return entity != null ? entity.getCode() : null;
	}

	public int getErrorNum() {
		return entity != null ? entity.getErrorNum() : null;
	}

}
