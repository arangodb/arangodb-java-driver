package com.arangodb.internal.serde;

import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.util.RawBytes;
import com.arangodb.util.RawJson;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum SerdeUtils {
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(SerdeUtils.class);

    private final ObjectMapper jsonMapper = new ObjectMapper();

    public static Type constructListType(Class<?> clazz) {
        return TypeFactory.defaultInstance().constructCollectionType(List.class, clazz);
    }

    public static Type constructParametricType(Class<?> rawType, Type... rawArgs) {
        if (rawArgs == null || rawArgs.length == 0 || rawArgs[0] == null) {
            return rawType;
        } else {
            return new InternalParameterizedType(rawType, rawArgs);
        }
    }

    public static Type convertToType(final JavaType javaType) {
        List<Type> args = new ArrayList<>();
        for (JavaType it : javaType.getBindings().getTypeParameters()) {
            Type type = convertToType(it);
            args.add(type);
        }
        return constructParametricType(javaType.getRawClass(), args.toArray(new Type[0]));
    }

    static void checkSupportedJacksonVersion() {
        Arrays.asList(
                com.fasterxml.jackson.databind.cfg.PackageVersion.VERSION,
                com.fasterxml.jackson.core.json.PackageVersion.VERSION
        ).forEach(version -> {
            int major = version.getMajorVersion();
            int minor = version.getMinorVersion();
            if (major != 2 || minor < 10 || minor > 18) {
                LOGGER.warn("Unsupported Jackson version: {}", version);
            }
        });
    }

    public ObjectMapper getJsonMapper() {
        return jsonMapper;
    }

    /**
     * Parse a JSON string.
     *
     * @param json JSON string to parse
     * @return root of the parsed tree
     */
    public JsonNode parseJson(final String json) {
        try {
            return jsonMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw ArangoDBException.of(e);
        }
    }

    /**
     * @param data JsonNode
     * @return JSON string
     */
    public String writeJson(final JsonNode data) {
        try {
            return jsonMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw ArangoDBException.of(e);
        }
    }

    /**
     * Extract raw bytes for the current JSON (or VPACK) node
     *
     * @param parser JsonParser with current token pointing to the node to extract
     * @return byte array
     */
    // TODO: move to InternalSerdeImpl, non-static, keep reference to serde to check content-type
    public static byte[] extractBytes(JsonParser parser) throws IOException {
        JsonToken t = parser.currentToken();
        if (t.isStructEnd() || t == JsonToken.FIELD_NAME) {
            throw new ArangoDBException("Unexpected token: " + t);
        }
        byte[] data = (byte[]) parser.currentTokenLocation().contentReference().getRawContent();
        int start = (int) parser.currentTokenLocation().getByteOffset();
        int end = (int) parser.currentLocation().getByteOffset();
        if (t.isStructStart()) {
            int open = 1;
            while (open > 0) {
                t = parser.nextToken();
                if (t.isStructStart()) {
                    open++;
                } else if (t.isStructEnd()) {
                    open--;
                }
            }
        }
        parser.finishToken();
        if ("JSON".equals(parser.getCodec().getFactory().getFormatName())) {
            end = (int) parser.currentLocation().getByteOffset();
        }
        return Arrays.copyOfRange(data, start, end);
    }

    public static boolean isManagedClass(Class<?> clazz) {
        return JsonNode.class.isAssignableFrom(clazz) ||
                RawJson.class.equals(clazz) ||
                RawBytes.class.equals(clazz) ||
                BaseDocument.class.equals(clazz) ||
                BaseEdgeDocument.class.equals(clazz) ||
                isEntityClass(clazz);
    }

    private static boolean isEntityClass(Class<?> clazz) {
        Package pkg = clazz.getPackage();
        if (pkg == null) {
            return false;
        }
        return pkg.getName().startsWith("com.arangodb.entity");
    }
}
