package com.arangodb.internal.serde;

import com.arangodb.ArangoDBException;
import com.arangodb.internal.RequestContextHolder;
import com.arangodb.serde.ArangoSerde;
import com.arangodb.util.RawBytes;
import com.arangodb.util.RawJson;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import static com.arangodb.internal.serde.SerdeUtils.checkSupportedJacksonVersion;
import static com.arangodb.internal.serde.SerdeUtils.extractBytes;

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
        mapper.enable(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION);
        mapper.registerModule(InternalModule.get(this));
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
        if (content == null) {
            return "";
        }
        try {
            return SerdeUtils.INSTANCE.writeJson(mapper.readTree(content));
        } catch (Exception e) {
            return "[Unparsable data]";
        }
    }

    @Override
    public byte[] extract(final byte[] content, final String jsonPointer) {
        if (!jsonPointer.startsWith("/")) {
            throw new ArangoDBException("Unsupported JSON pointer: " + jsonPointer);
        }
        String[] parts = jsonPointer.substring(1).split("/");
        try (JsonParser parser = mapper.createParser(content)) {
            int match = 0;
            int level = 0;
            JsonToken token = parser.nextToken();
            if (token != JsonToken.START_OBJECT) {
                throw new ArangoDBException("Unable to parse token: " + token);
            }
            while (true) {
                token = parser.nextToken();
                if (token == JsonToken.START_OBJECT) {
                    level++;
                }
                if (token == JsonToken.END_OBJECT) {
                    level--;
                }
                if (token == null || level < match) {
                    throw new ArangoDBException("Unable to parse JSON pointer: " + jsonPointer);
                }
                if (token == JsonToken.FIELD_NAME && match == level && parts[match].equals(parser.getText())) {
                    match++;
                    if (match == parts.length) {
                        parser.nextToken();
                        return extractBytes(parser);
                    }
                }
            }
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
        if (RawBytes.class.equals(clazz)) {
            return ((RawBytes) value).get();
        } else if (RawJson.class.equals(clazz) && JsonFactory.FORMAT_NAME_JSON.equals(mapper.getFactory().getFormatName())) {
            return ((RawJson) value).get().getBytes(StandardCharsets.UTF_8);
        } else if (SerdeUtils.isManagedClass(clazz)) {
            return serialize(value);
        } else {
            return userSerde.serialize(value);
        }
    }

    @Override
    public byte[] serializeCollectionUserData(Iterable<?> value) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try (JsonGenerator gen = mapper.createGenerator(os)) {
            gen.writeStartArray();
            for (Object o : value) {
                gen.writeRawValue(new RawUserDataValue(serializeUserData(o)));
            }
            gen.writeEndArray();
            gen.flush();
        } catch (IOException e) {
            throw ArangoDBException.of(e);
        }
        return os.toByteArray();
    }

    @Override
    public <T> T deserializeUserData(byte[] content, Class<T> clazz) {
        if (SerdeUtils.isManagedClass(clazz)) {
            return deserialize(content, clazz);
        } else {
            return userSerde.deserialize(content, clazz, RequestContextHolder.INSTANCE.getCtx());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserializeUserData(byte[] content, JavaType clazz) {
        try {
            if (SerdeUtils.isManagedClass(clazz.getRawClass())) {
                return mapper.readerFor(clazz).readValue(content);
            } else {
                return deserializeUserData(content, (Class<? extends T>) clazz.getRawClass());
            }
        } catch (IOException e) {
            throw ArangoDBException.of(e);
        }
    }

    @Override
    public <T> T deserializeUserData(JsonParser parser, JavaType clazz) {
        try {
            if (SerdeUtils.isManagedClass(clazz.getRawClass())) {
                return mapper.readerFor(clazz).readValue(parser);
            } else {
                return deserializeUserData(extractBytes(parser), clazz);
            }
        } catch (IOException e) {
            throw ArangoDBException.of(e);
        }
    }

    @Override
    public boolean isDocument(byte[] content) {
        try (JsonParser p = mapper.createParser(content)) {
            if (p.nextToken() != JsonToken.START_OBJECT) {
                return false;
            }

            int level = 1;
            while (level >= 1) {
                JsonToken t = p.nextToken();
                if (level == 1 && t == JsonToken.FIELD_NAME) {
                    String fieldName = p.getText();
                    if (fieldName.equals("_id") || fieldName.equals("_key") || fieldName.equals("_rev")) {
                        return true;
                    }
                }
                if (t.isStructStart()) {
                    level++;
                } else if (t.isStructEnd()) {
                    level--;
                }
            }

            if (p.currentToken() != JsonToken.END_OBJECT) {
                throw new JsonMappingException(p, "Expected END_OBJECT but got " + p.currentToken());
            }
        } catch (IOException e) {
            throw ArangoDBException.of(e);
        }
        return false;
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
        if (content == null || content.length == 0) {
            return null;
        }
        try {
            return mapper.readerFor(mapper.constructType(type)).readValue(content);
        } catch (IOException e) {
            throw ArangoDBException.of(e);
        }
    }

}
