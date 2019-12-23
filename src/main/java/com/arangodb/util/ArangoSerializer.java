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

import com.arangodb.ArangoDBException;
import com.arangodb.velocypack.VPackSlice;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

/**
 * @author Mark Vollmary
 */
public interface ArangoSerializer {

    class Options {
        private Type type;
        private boolean serializeNullValues;
        private Map<String, Object> additionalFields;
        private boolean stringAsJson;

        public Options() {
            super();
            serializeNullValues = false;
            stringAsJson = false;
            additionalFields = Collections.emptyMap();
        }

        /**
         * @param type The source type of the Object.
         * @return options
         */
        public Options type(final Type type) {
            this.type = type;
            return this;
        }

        /**
         * @param serializeNullValues Whether or not null values should be excluded from serialization.
         * @return options
         */
        public Options serializeNullValues(final boolean serializeNullValues) {
            this.serializeNullValues = serializeNullValues;
            return this;
        }

        /**
         * @param additionalFields Additional Key/Value pairs to include in the created VelocyPack.
         * @return options
         */
        public Options additionalFields(final Map<String, Object> additionalFields) {
            this.additionalFields = additionalFields;
            return this;
        }

        /**
         * @param stringAsJson Wheter or not String should be interpreted as json
         * @return options
         */
        public Options stringAsJson(final boolean stringAsJson) {
            this.stringAsJson = stringAsJson;
            return this;
        }

        public Type getType() {
            return type;
        }

        public boolean isSerializeNullValues() {
            return serializeNullValues;
        }

        public Map<String, Object> getAdditionalFields() {
            return additionalFields;
        }

        public boolean isStringAsJson() {
            return stringAsJson;
        }

    }

    /**
     * Serialize a given Object to VelocyPack
     *
     * @param entity The Object to serialize. If it is from type String, it will be handled as a JSON.
     * @return The serialized VelocyPack
     * @throws ArangoDBException
     */
    VPackSlice serialize(final Object entity) throws ArangoDBException;

    /**
     * Serialize a given Object to VelocyPack
     *
     * @param entity  The Object to serialize. If it is from type String, it will be handled as a JSON.
     * @param options Additional options
     * @return the serialized VelocyPack
     * @throws ArangoDBException
     */
    VPackSlice serialize(final Object entity, final Options options) throws ArangoDBException;

}
