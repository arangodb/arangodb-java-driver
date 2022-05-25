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

package com.arangodb.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Mark Vollmary
 * @deprecated Use {@link Id}, {@link Key}, {@link Rev}, {@link From} or {@link To} instead.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Deprecated
public @interface DocumentField {

    @Deprecated
    enum Type {

        /**
         * @deprecated Use {@link Id} instead.
         */
        @Deprecated
        ID("_id"),

        /**
         * @deprecated Use {@link Key} instead.
         */
        @Deprecated
        KEY("_key"),

        /**
         * @deprecated Use {@link Rev} instead.
         */
        @Deprecated
        REV("_rev"),

        /**
         * @deprecated Use {@link From} instead.
         */
        @Deprecated
        FROM("_from"),

        /**
         * @deprecated Use {@link To} instead.
         */
        @Deprecated
        TO("_to");

        private final String serializeName;

        Type(final String serializeName) {
            this.serializeName = serializeName;
        }

        public String getSerializeName() {
            return serializeName;
        }
    }

    Type value();

}
