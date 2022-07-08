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
import com.arangodb.internal.mapping.ArangoAnnotationIntrospector;
import com.arangodb.internal.mapping.VPackDeserializers;
import com.arangodb.internal.mapping.VPackSerializers;
import com.arangodb.jackson.dataformat.velocypack.VPackMapper;
import com.arangodb.serde.DataType;
import com.arangodb.serde.JacksonSerde;
import com.arangodb.util.ArangoSerialization;
import com.arangodb.velocypack.VPackSlice;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.lang.reflect.Type;

/**
 * @author Mark Vollmary
 */
public class ArangoJack implements ArangoSerialization {

    public interface ConfigureFunction {
        void configure(ObjectMapper mapper);
    }

    private final ObjectMapper vpackMapper;
    private final ObjectMapper vpackMapperNull;
    private final ObjectMapper jsonMapper;

    private final JacksonSerde serde;

    private static final class ArangoModule extends SimpleModule {
        @Override
        public void setupModule(SetupContext context) {
            super.setupModule(context);
            context.insertAnnotationIntrospector(new ArangoAnnotationIntrospector());
        }
    }

    static VPackMapper createDefaultMapper() {
        final VPackMapper mapper = new VPackMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return configureDefaultMapper(mapper);
    }

    static VPackMapper configureDefaultMapper(final VPackMapper mapper) {
        final SimpleModule module = new ArangoJack.ArangoModule();
        module.addSerializer(VPackSlice.class, VPackSerializers.VPACK);
        module.addSerializer(java.util.Date.class, VPackSerializers.UTIL_DATE);
        module.addSerializer(java.sql.Date.class, VPackSerializers.SQL_DATE);
        module.addSerializer(java.sql.Timestamp.class, VPackSerializers.SQL_TIMESTAMP);

        module.addDeserializer(VPackSlice.class, VPackDeserializers.VPACK);
        module.addDeserializer(java.util.Date.class, VPackDeserializers.UTIL_DATE);
        module.addDeserializer(java.sql.Date.class, VPackDeserializers.SQL_DATE);
        module.addDeserializer(java.sql.Timestamp.class, VPackDeserializers.SQL_TIMESTAMP);

        mapper.registerModule(module);
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
        vpackMapper = mapper.copy().setSerializationInclusion(Include.NON_NULL);
        vpackMapperNull = mapper.copy().setSerializationInclusion(Include.ALWAYS);
        jsonMapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
        serde =  JacksonSerde.of(DataType.VPACK, configureDefaultMapper(new VPackMapper()));
    }

    public void configure(final ArangoJack.ConfigureFunction f) {
        f.configure(vpackMapper);
        f.configure(vpackMapperNull);
        f.configure(jsonMapper);
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
