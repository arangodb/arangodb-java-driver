package com.arangodb.internal.serde;

import com.arangodb.ArangoDBException;
import com.arangodb.internal.RequestContextHolder;
import com.arangodb.serde.ArangoSerde;
import com.arangodb.util.RawBytes;
import com.arangodb.util.RawJson;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.json.JsonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.*;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.*;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.jsonp.JSONPModule;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import static com.arangodb.internal.serde.SerdeUtils.checkSupportedJacksonVersion;
import static com.arangodb.internal.serde.SerdeUtils.extractBytes;

final class InternalSerdeImpl implements InternalSerde {
    private static final Logger LOG = LoggerFactory.getLogger(InternalSerdeImpl.class);

    static {
        checkSupportedJacksonVersion();
    }

    private final ArangoSerde userSerde;
    private final JsonMapper mapper;

    InternalSerdeImpl(final JsonMapper mapper, final ArangoSerde userSerde) {
        var builder = mapper.rebuild()
                .deactivateDefaultTyping()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .addModule(InternalModule.get(this))
                .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
                .changeDefaultPropertyInclusion(i -> i.withValueInclusion(JsonInclude.Include.NON_NULL))
                .annotationIntrospector(new InternalAnnotationIntrospector(
                        new UserDataSerializer(this),
                        new UserDataDeserializer(this)
                ));


        // JSON-P datatypes
        JSONPModule jsonPModule = null;
        try {
            jsonPModule = new JSONPModule();
        } catch (JsonException e) {
            LOG.debug("Jakarta JSON-P provider not found, handling of JSON-P datatypes is disabled", e);
        }

        if (jsonPModule != null) {
            builder.addModule(jsonPModule);
        }

        this.userSerde = userSerde;
        this.mapper = builder.build();
    }

    @Override
    public byte[] serialize(final Object value) {
        return mapper.writeValueAsBytes(value);
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
        try (JsonParser parser = mapper.tokenStreamFactory().createParser(content)) {
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
                if (token == JsonToken.PROPERTY_NAME && match == level && parts[match].equals(parser.getString())) {
                    match++;
                    if (match == parts.length) {
                        parser.nextToken();
                        return extractBytes(parser);
                    }
                }
            }
        }
    }

    @Override
    public JsonNode parse(byte[] content, String jsonPointer) {
        return mapper.readTree(content).at(jsonPointer);
    }

    @Override
    public byte[] serializeUserData(Object value) {
        if (value == null) {
            return serialize(null);
        }
        Class<?> clazz = value.getClass();
        if (RawBytes.class.equals(clazz)) {
            return ((RawBytes) value).get();
        } else if (RawJson.class.equals(clazz) && JsonFactory.FORMAT_NAME_JSON.equals(mapper.tokenStreamFactory().getFormatName())) {
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
        try (JsonGenerator gen = mapper.tokenStreamFactory().createGenerator(os)) {
            gen.writeStartArray();
            for (Object o : value) {
                gen.writeRawValue(new RawUserDataValue(serializeUserData(o)));
            }
            gen.writeEndArray();
            gen.flush();
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
        if (SerdeUtils.isManagedClass(clazz.getRawClass())) {
            return mapper.readerFor(clazz).readValue(content);
        } else {
            return deserializeUserData(content, (Class<? extends T>) clazz.getRawClass());
        }
    }

    @Override
    public boolean isDocument(byte[] content) {
        try (JsonParser p = mapper.tokenStreamFactory().createParser(content)) {
            if (p.nextToken() != JsonToken.START_OBJECT) {
                return false;
            }

            int level = 1;
            while (level >= 1) {
                JsonToken t = p.nextToken();
                if (level == 1 && t == JsonToken.PROPERTY_NAME) {
                    String fieldName = p.getString();
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
                throw MismatchedInputException.from(p, "Expected END_OBJECT but got " + p.currentToken());
            }
        }
        return false;
    }

    @Override
    public ArangoSerde getUserSerde() {
        return userSerde;
    }

    @Override
    public <T> T deserialize(final JsonNode node, final Type type) {
        return mapper.readerFor(mapper.constructType(type)).readValue(node);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(final byte[] content, final Type type) {
        if (content == null || content.length == 0) {
            return null;
        }
        if (RawBytes.class.equals(type)) {
            return (T) RawBytes.of(content);
        } else if (RawJson.class.equals(type) && JsonFactory.FORMAT_NAME_JSON.equals(mapper.tokenStreamFactory().getFormatName())) {
            return (T) RawJson.of(new String(content, StandardCharsets.UTF_8));
        } else {
            return mapper.readerFor(mapper.constructType(type)).readValue(content);
        }
    }

}
