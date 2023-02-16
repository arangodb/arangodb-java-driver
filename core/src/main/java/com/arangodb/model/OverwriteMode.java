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

package com.arangodb.model;


/**
 * @author Michele Rastelli
 * @since ArangoDB 3.7
 */
public enum OverwriteMode {

    /**
     * if a document with the specified _key value exists already, nothing will be done and no write operation will be
     * carried out. The insert operation will return success in this case. This mode does not support returning the old
     * document version using RETURN OLD. When using RETURN NEW, null will be returned in case the document already
     * existed.
     */
    ignore("ignore"),

    /**
     * if a document with the specified _key value exists already, it will be overwritten with the specified document
     * value. This mode will also be used when no overwrite mode is specified but the overwrite flag is set to true.
     */
    replace("replace"),

    /**
     * if a document with the specified _key value exists already, it will be patched (partially updated) with the
     * specified document value. The overwrite mode can be further controlled via the keepNull and mergeObjects
     * parameters.
     */
    update("update"),

    /**
     * if a document with the specified _key value exists already, return a unique constraint violation error so that
     * the insert operation fails. This is also the default behavior in case the overwrite mode is not set, and the
     * overwrite flag is false or not set either.
     */
    conflict("conflict");

    private final String value;

    OverwriteMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
