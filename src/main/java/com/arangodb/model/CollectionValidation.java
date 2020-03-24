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
 */
public class CollectionValidation {
    private String rule;
    private Level level;
    private String message;

    public String getRule() {
        return rule;
    }

    public CollectionValidation setRule(String rule) {
        this.rule = rule;
        return this;
    }

    public Level getLevel() {
        return level;
    }

    public CollectionValidation setLevel(Level level) {
        this.level = level;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public CollectionValidation setMessage(String message) {
        this.message = message;
        return this;
    }

    public enum Level {
        NONE("none"),
        NEW("new"),
        MODERATE("moderate"),
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
