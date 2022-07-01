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


package com.arangodb.internal.mapping;

import com.arangodb.jackson.dataformat.velocypack.internal.VPackGenerator;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.internal.util.DateUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * @author Mark Vollmary
 */
public class VPackSerializers {

    public static final JsonSerializer<VPackSlice> VPACK = new JsonSerializer<VPackSlice>() {
        @Override
        public void serialize(final VPackSlice value, final JsonGenerator gen, final SerializerProvider serializers)
                throws IOException {
            if (gen instanceof VPackGenerator) {
                ((VPackGenerator) gen).writeVPack(value);
            } else {
                gen.writeBinary(value.toByteArray());
            }
        }
    };

    public static final JsonSerializer<java.util.Date> UTIL_DATE = new JsonSerializer<java.util.Date>() {
        @Override
        public void serialize(final java.util.Date value, final JsonGenerator gen, final SerializerProvider serializers)
                throws IOException, JsonProcessingException {
            gen.writeString(DateUtil.format(value));
        }
    };

    public static final JsonSerializer<java.sql.Date> SQL_DATE = new JsonSerializer<java.sql.Date>() {
        @Override
        public void serialize(final Date value, final JsonGenerator gen, final SerializerProvider serializers)
                throws IOException, JsonProcessingException {
            gen.writeString(DateUtil.format(value));
        }
    };

    public static final JsonSerializer<java.sql.Timestamp> SQL_TIMESTAMP = new JsonSerializer<java.sql.Timestamp>() {
        @Override
        public void serialize(final Timestamp value, final JsonGenerator gen, final SerializerProvider serializers)
                throws IOException, JsonProcessingException {
            gen.writeString(DateUtil.format(value));
        }
    };

}
