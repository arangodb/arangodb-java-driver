package com.arangodb.serde;

import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import com.arangodb.util.RawBytes;
import com.arangodb.util.RawJson;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.arangodb.serde.SerdeUtils.checkSupportedJacksonVersion;

final class InternalSerdeImpl extends JacksonSerdeImpl implements InternalSerde {

    private final ArangoSerde userSerde;

    static {
        checkSupportedJacksonVersion();
    }

    InternalSerdeImpl(final ObjectMapper mapper, final ArangoSerde userSerde) {
        super(mapper);
        this.userSerde = userSerde;
        mapper.registerModule(InternalModule.INSTANCE.get());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setAnnotationIntrospector(new InternalAnnotationIntrospector(
                new UserDataSerializer(this),
                new UserDataDeserializer(this)
        ));
    }

    @Override
    public String toJsonString(final byte[] content) {
        try {
            return SerdeUtils.INSTANCE.writeJson(mapper.readTree(content));
        } catch (IOException e) {
            throw new ArangoDBException(e);
        }
    }

    @Override
    public byte[] extract(final byte[] content, final String jsonPointer) {
        try {
            JsonNode target = parse(content).at(jsonPointer);
            return mapper.writeValueAsBytes(target);
        } catch (IOException e) {
            throw new ArangoDBException(e);
        }
    }

    @Override
    public JsonNode parse(byte[] content) {
        try {
            return mapper.readTree(content);
        } catch (IOException e) {
            throw new ArangoDBException(e);
        }
    }

    @Override
    public JsonNode parse(byte[] content, String jsonPointer) {
        try {
            return mapper.readTree(content).at(jsonPointer);
        } catch (IOException e) {
            throw new ArangoDBException(e);
        }
    }

    @Override
    public byte[] serializeUserData(Object value) {
        if (value == null) {
            return serialize(null);
        }
        Class<?> clazz = value.getClass();
        if (RawJson.class.isAssignableFrom(clazz) ||
                RawBytes.class.isAssignableFrom(clazz) ||
                JsonNode.class.isAssignableFrom(clazz) ||
                BaseDocument.class.isAssignableFrom(clazz)
        ) {
            return serialize(value);
        } else {
            return userSerde.serialize(value);
        }
    }

    @Override
    public byte[] serializeCollectionUserData(Collection<?> value) {
        List<JsonNode> jsonNodeCollection = value.stream()
                .map(this::serializeUserData)
                .map(this::parse)
                .collect(Collectors.toList());
        return serialize(jsonNodeCollection);
    }

    @Override
    public <T> T deserializeUserData(byte[] content, Class<T> clazz) {
        if (RawJson.class.isAssignableFrom(clazz) ||
                RawBytes.class.isAssignableFrom(clazz) ||
                JsonNode.class.isAssignableFrom(clazz) ||
                BaseDocument.class.isAssignableFrom(clazz)
        ) {
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
            return userSerde.deserialize(content, type);
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
            throw new ArangoDBException(e);
        }
    }

    @Override
    public <T> T deserialize(final byte[] content, final Type type) {
        if (content == null) {
            return null;
        }
        return super.deserialize(content, type);
    }

}
