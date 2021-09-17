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

package com.arangodb.util;

import java.text.Normalizer;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public final class UnicodeUtils {

    private UnicodeUtils() {
    }

    /**
     * Normalizes a unicode string according to ArangoDB extended naming convention.
     *
     * @param value string to normalize
     * @return NFC normalized string
     */
    public static String normalize(final String value) {
        if (value == null) {
            return null;
        }
        return Normalizer.normalize(value, Normalizer.Form.NFC);
    }

    public static void checkNormalized(final String value) {
        if (value != null && !normalize(value).equals(value))
            throw new IllegalArgumentException("Unicode String not normalized: NFC normal form is required.");
    }
}
