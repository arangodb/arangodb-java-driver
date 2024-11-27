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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * @author Mark Vollmary
 */
public final class ResponseUtils {

    private static final int ERROR_STATUS = 300;
    private static final int ERROR_INTERNAL = 503;
    private static final String HEADER_ENDPOINT = "x-arango-endpoint";
    private static final String CONTENT_TYPE = "content-type";
    private static final String TEXT_PLAIN = "text/plain";

    private ResponseUtils() {
        super();
    }

    public static ArangoDBException translateError(InternalSerde serde, InternalResponse response) {
        final int responseCode = response.getResponseCode();
        if (responseCode < ERROR_STATUS) {
            return null;
        }
        if (responseCode == ERROR_INTERNAL && response.containsMeta(HEADER_ENDPOINT)) {
            return new ArangoDBRedirectException(String.format("Response Code: %s", responseCode),
                    response.getMeta(HEADER_ENDPOINT));
        }

        byte[] body = response.getBody();
        if (body == null) {
            return new ArangoDBException(String.format("Response Code: %s", responseCode), responseCode);
        }

        if (isTextPlain(response)) {
            String payload = new String(body, getContentTypeCharset(response));
            return new ArangoDBException("Response Code: " + responseCode + "[" + payload + "]", responseCode);
        }

        ErrorEntity errorEntity;
        try {
            errorEntity = serde.deserialize(body, ErrorEntity.class);
        } catch (Exception e) {
            ArangoDBException adbEx = new ArangoDBException("Response Code: " + responseCode
                    + "[Unparsable data] Response: " + response, responseCode);
            adbEx.addSuppressed(e);
            return adbEx;
        }

        if (errorEntity.getCode() == ERROR_INTERNAL && errorEntity.getErrorNum() == ERROR_INTERNAL) {
            return ArangoDBUnavailableException.from(errorEntity);
        }
        ArangoDBException e = new ArangoDBException(errorEntity);
        if (ArangoErrors.QUEUE_TIME_VIOLATED.equals(e.getErrorNum())) {
            return ArangoDBException.of(new TimeoutException().initCause(e));
        }
        return e;
    }

    private static boolean isTextPlain(InternalResponse response) {
        String contentType = response.getMeta(CONTENT_TYPE);
        return contentType != null && contentType.startsWith(TEXT_PLAIN);
    }

    private static Charset getContentTypeCharset(InternalResponse response) {
        String contentType = response.getMeta(CONTENT_TYPE);
        int paramIdx = contentType.indexOf("charset=");
        if (paramIdx == -1) {
            return StandardCharsets.UTF_8;
        }
        return Charset.forName(contentType.substring(paramIdx + 8));
    }

}
