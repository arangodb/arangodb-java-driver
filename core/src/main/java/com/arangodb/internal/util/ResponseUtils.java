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

package com.arangodb.internal.util;

import com.arangodb.ArangoDBException;
import com.arangodb.entity.ErrorEntity;
import com.arangodb.internal.ArangoErrors;
import com.arangodb.internal.InternalResponse;
import com.arangodb.internal.net.ArangoDBRedirectException;
import com.arangodb.internal.net.ArangoDBUnavailableException;
import com.arangodb.internal.serde.InternalSerde;
import com.arangodb.serde.SerdeContext;

import java.util.concurrent.TimeoutException;

/**
 * @author Mark Vollmary
 */
public final class ResponseUtils {

    private static final int ERROR_STATUS = 300;
    private static final int ERROR_INTERNAL = 503;
    private static final String HEADER_ENDPOINT = "x-arango-endpoint";

    private ResponseUtils() {
        super();
    }

    public static ArangoDBException translateError(final InternalSerde util, final InternalResponse response) {
        final int responseCode = response.getResponseCode();
        if (responseCode < ERROR_STATUS) {
            return null;
        }
        if (responseCode == ERROR_INTERNAL && response.containsMeta(HEADER_ENDPOINT)) {
            return new ArangoDBRedirectException(String.format("Response Code: %s", responseCode),
                    response.getMeta(HEADER_ENDPOINT));
        }
        if (response.getBody() != null) {
            final ErrorEntity errorEntity = util.deserialize(response.getBody(), ErrorEntity.class, SerdeContext.EMPTY);
            if (errorEntity.getCode() == ERROR_INTERNAL && errorEntity.getErrorNum() == ERROR_INTERNAL) {
                return ArangoDBUnavailableException.from(errorEntity);
            }
            ArangoDBException e = new ArangoDBException(errorEntity);
            if (ArangoErrors.QUEUE_TIME_VIOLATED.equals(e.getErrorNum())) {
                return ArangoDBException.of(new TimeoutException().initCause(e));
            }
            return e;
        }
        return new ArangoDBException(String.format("Response Code: %s", responseCode), responseCode);
    }
}
