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


import com.arangodb.internal.serde.InternalDeserializers;
import com.arangodb.internal.serde.InternalSerializers;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Michele Rastelli
 * @see <a href="https://www.arangodb.com/docs/stable/data-modeling-documents-schema-validation.html">API Documentation</a>
 * @since ArangoDB 3.7
 */
public final class CollectionSchema {

    private String rule;
    private Level level;
    private String message;

    /**
     * @return JSON Schema description
     */
    @JsonSerialize(using = InternalSerializers.CollectionSchemaRuleSerializer.class)
    public String getRule() {
        return rule;
    }

    @JsonDeserialize(using = InternalDeserializers.CollectionSchemaRuleDeserializer.class)
    public CollectionSchema setRule(String rule) {
        this.rule = rule;
        return this;
    }

    /**
     * @return controls when the validation will be applied
     */
    public Level getLevel() {
        return level;
    }

    public CollectionSchema setLevel(Level level) {
        this.level = level;
        return this;
    }

    /**
     * @return the message that will be used when validation fails
     */
    public String getMessage() {
        return message;
    }

    public CollectionSchema setMessage(String message) {
        this.message = message;
        return this;
    }

    public enum Level {

        /**
         * The rule is inactive and validation thus turned off.
         */
        @JsonProperty("none")
        NONE("none"),

        /**
         * Only newly inserted documents are validated.
         */
        @JsonProperty("new")
        NEW("new"),

        /**
         * New and modified documents must pass validation, except for modified documents where the OLD value did not
         * pass validation already. This level is useful if you have documents which do not match your target structure,
         * but you want to stop the insertion of more invalid documents and prohibit that valid documents are changed to
         * invalid documents.
         */
        @JsonProperty("moderate")
        MODERATE("moderate"),

        /**
         * All new and modified document must strictly pass validation. No exceptions are made (default).
         */
        @JsonProperty("strict")
        STRICT("strict");

        private final String value;

        public static Level of(String label) {
            for (Level e : values()) {
                if (e.value.equals(label)) {
                    return e;
                }
            }
            return null;
        }

        Level(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

}
