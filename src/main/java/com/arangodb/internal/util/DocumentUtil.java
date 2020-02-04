/*
 * DISCLAIMER
 *
 * Copyright 2018 ArangoDB GmbH, Cologne, Germany
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

import java.util.regex.Pattern;

/**
 * @author Mark Vollmary
 */
public final class DocumentUtil {

    private static final String SLASH = "/";
    public static final String REGEX_KEY = "[^/]+";
    public static final String REGEX_ID = "[^/]+/[^/]+";

    private DocumentUtil() {
        super();
    }

    public static void validateIndexId(final String id) {
        validateName("index id", REGEX_ID, id);
    }

    public static void validateDocumentKey(final String key) throws ArangoDBException {
        validateName("document key", REGEX_KEY, key);
    }

    public static void validateDocumentId(final String id) throws ArangoDBException {
        validateName("document id", REGEX_ID, id);
    }

    public static String createDocumentHandle(final String collection, final String key) {
        validateDocumentKey(key);
        return collection + SLASH + key;
    }

    private static void validateName(final String type, final String regex, final CharSequence name)
            throws ArangoDBException {
        if (!Pattern.matches(regex, name)) {
            throw new ArangoDBException(String.format("%s %s is not valid.", type, name));
        }
    }
}
