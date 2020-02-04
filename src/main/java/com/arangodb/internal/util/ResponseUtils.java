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
import com.arangodb.internal.net.ArangoDBRedirectException;
import com.arangodb.util.ArangoSerialization;
import com.arangodb.velocypack.exception.VPackParserException;
import com.arangodb.velocystream.Response;

/**
 * @author Mark Vollmary
 */
public final class ResponseUtils {

    private static final int ERROR_STATUS = 300;
    private static final int ERROR_INTERNAL = 503;
    private static final String HEADER_ENDPOINT = "X-Arango-Endpoint";

    private ResponseUtils() {
        super();
    }

    public static void checkError(final ArangoSerialization util, final Response response) throws ArangoDBException {
        try {
            final int responseCode = response.getResponseCode();
            if (responseCode >= ERROR_STATUS) {
                if (responseCode == ERROR_INTERNAL && response.getMeta().containsKey(HEADER_ENDPOINT)) {
                    throw new ArangoDBRedirectException(String.format("Response Code: %s", responseCode),
                            response.getMeta().get(HEADER_ENDPOINT));
                } else if (response.getBody() != null) {
                    final ErrorEntity errorEntity = util.deserialize(response.getBody(), ErrorEntity.class);
                    throw new ArangoDBException(errorEntity);
                } else {
                    throw new ArangoDBException(String.format("Response Code: %s", responseCode), responseCode);
                }
            }
        } catch (final VPackParserException e) {
            throw new ArangoDBException(e);
        }
    }
}
