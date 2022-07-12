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

package com.arangodb.mapping;

import com.arangodb.ArangoDBException;
import com.arangodb.jackson.dataformat.velocypack.VPackMapper;
import com.arangodb.serde.DataType;
import com.arangodb.serde.JacksonSerde;
import com.arangodb.util.ArangoSerialization;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.lang.reflect.Type;

/**
 * @author Mark Vollmary
 */
public class ArangoJack implements ArangoSerialization {

    public interface ConfigureFunction {
        void configure(ObjectMapper mapper);
    }

    private final JacksonSerde serde;

    static VPackMapper createDefaultMapper() {
        final VPackMapper mapper = new VPackMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return mapper;
    }

    public ArangoJack() {
        this(createDefaultMapper());
    }

    /**
     * @param mapper configured VPackMapper to use. A defensive copy is created and used.
     */
    public ArangoJack(final VPackMapper mapper) {
        super();
        VPackMapper m = mapper != null ? mapper.copy() : new VPackMapper();
        serde =  JacksonSerde.of(DataType.VPACK, m);
    }

    public void configure(final ArangoJack.ConfigureFunction f) {
        serde.configure(f::configure);
    }

    @Override
    public byte[] serialize(final Object entity) throws ArangoDBException {
        return serde.serialize(entity);
    }

    @Override
    public <T> T deserialize(byte[] content, Type type) {
        return serde.deserialize(content, type);
    }

}
