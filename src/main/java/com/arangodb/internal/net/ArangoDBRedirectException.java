/*
 * DISCLAIMER
 *
 * Copyright 2017 ArangoDB GmbH, Cologne, Germany
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

package com.arangodb.internal.net;

import com.arangodb.ArangoDBException;

/**
 * @author Mark Vollmary
 */
public class ArangoDBRedirectException extends ArangoDBException {

    private static final long serialVersionUID = -94810262465567613L;
    private final String location;

    public ArangoDBRedirectException(final String message, final String location) {
        super(message);
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

}
