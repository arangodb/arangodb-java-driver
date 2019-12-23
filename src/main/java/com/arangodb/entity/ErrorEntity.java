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

package com.arangodb.entity;

import java.io.Serializable;

/**
 * @author Mark Vollmary
 */
public class ErrorEntity implements Serializable, Entity {

    private static final long serialVersionUID = -5918898261563691261L;

    private String errorMessage;
    private String exception;
    private int code;
    private int errorNum;

    public ErrorEntity() {
        super();
    }

    /**
     * @return a descriptive error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @return the exception message, passed when transaction fails
     */
    public String getException() {
        return exception;
    }

    /**
     * @return the status code
     */
    public int getCode() {
        return code;
    }

    /**
     * @return the server error number
     */
    public int getErrorNum() {
        return errorNum;
    }

}
