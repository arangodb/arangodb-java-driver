package com.arangodb.internal.serde;

import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.serde.ArangoSerde;
import com.arangodb.util.RawBytes;
import com.arangodb.util.RawJson;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.arangodb.internal.serde.SerdeUtils.checkSupportedJacksonVersion;

final class InternalSerdeImpl implements InternalSerde {

    static {
        checkSupportedJacksonVersion();
    }

    private final ArangoSerde userSerde;
    private final ObjectMapper mapper;

    InternalSerdeImpl(final ObjectMapper mapper, final ArangoSerde userSerde, final Module protocolModule) {
        this.mapper = mapper;
        this.userSerde = userSerde;
        mapper.deactivateDefaultTyping();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(InternalModule.INSTANCE.get());
        if (protocolModule != null) {
            mapper.registerModule(protocolModule);
        }
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setAnnotationIntrospector(new InternalAnnotationIntrospector(
                new UserDataSerializer(this),
                new UserDataDeserializer(this)
        ));
    }

    @Override
    public byte[] serialize(final Object value) {
        try {
            return mapper.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw ArangoDBException.of(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] content, Class<T> clazz) {
        return deserialize(content, (Type) clazz);
    }

    @Override
    public String toJsonString(final byte[] content) {
        try {
            return SerdeUtils.INSTANCE.writeJson(mapper.readTree(content));
        } catch (IOException e) {
            throw ArangoDBException.of(e);
        }
    }

    @Override
    public byte[] extract(final byte[] content, final String jsonPointer) {
        try {
            JsonNode target = parse(content).at(jsonPointer);
            return mapper.writeValueAsBytes(target);
        } catch (IOException e) {
            throw ArangoDBException.of(e);
        }
    }

    @Override
    public JsonNode parse(byte[] content) {
        try {
            return mapper.readTree(content);
        } catch (IOException e) {
            throw ArangoDBException.of(e);
        }
    }

    @Override
    public JsonNode parse(byte[] content, String jsonPointer) {
        try {
            return mapper.readTree(content).at(jsonPointer);
        } catch (IOException e) {
            throw ArangoDBException.of(e);
        }
    }

    @Override
    public byte[] serializeUserData(Object value) {
        if (value == null) {
            return serialize(null);
        }
        Class<?> clazz = value.getClass();
        if (isManagedClass(clazz)) {
            return serialize(value);
        } else {
            return userSerde.serialize(value);
        }
    }

    @Override
    public byte[] serializeCollectionUserData(Iterable<?> value) {
        List<JsonNode> jsonNodeCollection = StreamSupport.stream(value.spliterator(), false)
                .map(this::serializeUserData)
                .map(this::parse)
                .collect(Collectors.toList());
        return serialize(jsonNodeCollection);
    }

    @Override
    public <T> T deserializeUserData(byte[] content, Class<T> clazz) {
        if (isManagedClass(clazz)) {
            return deserialize(content, clazz);
        } else {
            return userSerde.deserialize(content, clazz);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserializeUserData(byte[] content, Type type) {
        if (type instanceof Class) {
            return deserializeUserData(content, (Class<T>) type);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public <T> T deserializeUserData(JsonNode node, Type type) {
        return deserializeUserData(serialize(node), type);
    }

    @Override
    public ArangoSerde getUserSerde() {
        return userSerde;
    }

    @Override
    public <T> T deserialize(final JsonNode node, final Type type) {
        try {
            return mapper.readerFor(mapper.constructType(type)).readValue(node);
        } catch (IOException e) {
            throw ArangoDBException.of(e);
        }
    }

    @Override
    public <T> T deserialize(final byte[] content, final Type type) {
        if (content == null) {
            return null;
        }
        try {
            return mapper.readerFor(mapper.constructType(type)).readValue(content);
        } catch (IOException e) {
            throw ArangoDBException.of(e);
        }
    }

    private boolean isManagedClass(Class<?> clazz) {
        return JsonNode.class.isAssignableFrom(clazz) ||
                RawJson.class.equals(clazz) ||
                RawBytes.class.equals(clazz) ||
                BaseDocument.class.equals(clazz) ||
                BaseEdgeDocument.class.equals(clazz);
    }
}
