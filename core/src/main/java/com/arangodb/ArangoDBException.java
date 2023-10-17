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
 */
public class ArangoDBException extends RuntimeException {

    private static final long serialVersionUID = 6165638002614173801L;
    private final ErrorEntity entity;
    private final Integer responseCode;
    private final Long requestId;

    public ArangoDBException(final ErrorEntity errorEntity) {
        super(String.format("Response: %s, Error: %s - %s", errorEntity.getCode(), errorEntity.getErrorNum(),
                errorEntity.getErrorMessage()));
        this.entity = errorEntity;
        this.responseCode = null;
        this.requestId = null;
    }

    public ArangoDBException(final String message) {
        super(message);
        this.entity = null;
        this.responseCode = null;
        this.requestId = null;
    }

    public ArangoDBException(final String message, final Integer responseCode) {
        super(message);
        this.entity = null;
        this.responseCode = responseCode;
        this.requestId = null;
    }

    public ArangoDBException(final Throwable cause) {
        super(cause);
        this.entity = null;
        this.responseCode = null;
        this.requestId = null;
    }

    public ArangoDBException(final String message, final Throwable cause) {
        super(message, cause);
        this.entity = null;
        this.responseCode = null;
        this.requestId = null;
    }

    public ArangoDBException(Throwable cause, long requestId) {
        super(cause);
        this.entity = null;
        this.responseCode = null;
        this.requestId = requestId;
    }

    private ArangoDBException(final ArangoDBException e) {
        super(e.getMessage(), e);
        this.entity = e.entity;
        this.responseCode = e.responseCode;
        this.requestId = e.requestId;
    }

    public static ArangoDBException wrap(Throwable t) {
        if (t instanceof ArangoDBException) {
            if (t.getCause() == null) {
                return new ArangoDBException((ArangoDBException) t);
            } else {
                return wrap(t.getCause());
            }
        } else {
            return new ArangoDBException(t);
        }
    }

    /**
     * @return ArangoDB error message
     */
    public String getErrorMessage() {
        return entity != null ? entity.getErrorMessage() : null;
    }

    /**
     * @return ArangoDB exception
     */
    public String getException() {
        return entity != null ? entity.getException() : null;
    }

    /**
     * @return HTTP response code
     */
    public Integer getResponseCode() {
        Integer entityResponseCode = entity != null ? entity.getCode() : null;
        return responseCode != null ? responseCode : entityResponseCode;
    }

    /**
     * @return ArangoDB error number
     */
    public Integer getErrorNum() {
        return entity != null ? entity.getErrorNum() : null;
    }

    /**
     * @return request id
     */
    public Long getRequestId() {
        return requestId;
    }
}
