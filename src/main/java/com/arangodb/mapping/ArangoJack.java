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
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.internal.mapping.ArangoAnnotationIntrospector;
import com.arangodb.internal.mapping.VPackDeserializers;
import com.arangodb.internal.mapping.VPackSerializers;
import com.arangodb.jackson.dataformat.velocypack.VPackMapper;
import com.arangodb.util.ArangoSerialization;
import com.arangodb.util.ArangoSerializer;
import com.arangodb.velocypack.VPackParser;
import com.arangodb.velocypack.VPackSlice;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Iterator;

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
    private final VPackParser vpackParser;

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

        final SimpleModule module = new ArangoJack.ArangoModule();
        module.addSerializer(VPackSlice.class, VPackSerializers.VPACK);
        module.addSerializer(java.util.Date.class, VPackSerializers.UTIL_DATE);
        module.addSerializer(java.sql.Date.class, VPackSerializers.SQL_DATE);
        module.addSerializer(java.sql.Timestamp.class, VPackSerializers.SQL_TIMESTAMP);
        module.addSerializer(BaseDocument.class, VPackSerializers.BASE_DOCUMENT);
        module.addSerializer(BaseEdgeDocument.class, VPackSerializers.BASE_EDGE_DOCUMENT);

        module.addDeserializer(VPackSlice.class, VPackDeserializers.VPACK);
        module.addDeserializer(java.util.Date.class, VPackDeserializers.UTIL_DATE);
        module.addDeserializer(java.sql.Date.class, VPackDeserializers.SQL_DATE);
        module.addDeserializer(java.sql.Timestamp.class, VPackDeserializers.SQL_TIMESTAMP);
        module.addDeserializer(BaseDocument.class, VPackDeserializers.BASE_DOCUMENT);
        module.addDeserializer(BaseEdgeDocument.class, VPackDeserializers.BASE_EDGE_DOCUMENT);

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
        vpackParser = new VPackParser.Builder().build();
    }

    public void configure(final ArangoJack.ConfigureFunction f) {
        f.configure(vpackMapper);
        f.configure(vpackMapperNull);
        f.configure(jsonMapper);
    }

    @Override
    public VPackSlice serialize(final Object entity) throws ArangoDBException {
        return serialize(entity, new ArangoSerializer.Options());
    }

    @SuppressWarnings("unchecked")
    @Override
    public VPackSlice serialize(final Object entity, final Options options) throws ArangoDBException {
        if (options.getType() == null) {
            options.type(entity.getClass());
        }
        try {
            final VPackSlice vpack;
            final Class<? extends Object> type = entity.getClass();
            final boolean serializeNullValues = options.isSerializeNullValues();
            if (String.class.isAssignableFrom(type)) {
                vpack = vpackParser.fromJson((String) entity, serializeNullValues);
            } else if (options.isStringAsJson() && Iterable.class.isAssignableFrom(type)) {
                final Iterator<?> iterator = Iterable.class.cast(entity).iterator();
                if (iterator.hasNext() && String.class.isAssignableFrom(iterator.next().getClass())) {
                    vpack = vpackParser.fromJson((Iterable<String>) entity, serializeNullValues);
                } else {
                    final ObjectMapper vp = serializeNullValues ? vpackMapperNull : vpackMapper;
                    vpack = new VPackSlice(vp.writeValueAsBytes(entity));
                }
            } else {
                final ObjectMapper vp = serializeNullValues ? vpackMapperNull : vpackMapper;
                vpack = new VPackSlice(vp.writeValueAsBytes(entity));
            }
            return vpack;
        } catch (final JsonProcessingException e) {
            throw new ArangoDBException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(final VPackSlice vpack, final Type type) throws ArangoDBException {
        try {
            final T doc;
            if (type == String.class && !vpack.isString() && !vpack.isNull()) {
                final JsonNode node = vpackMapper.readTree(
                        Arrays.copyOfRange(vpack.getBuffer(), vpack.getStart(), vpack.getStart() + vpack.getByteSize()));
                doc = (T) jsonMapper.writeValueAsString(node);
            } else {
                doc = vpackMapper.readValue(vpack.getBuffer(), vpack.getStart(), vpack.getStart() + vpack.getByteSize(),
                        (Class<T>) type);
            }
            return doc;
        } catch (final IOException e) {
            throw new ArangoDBException(e);
        }
    }

}
