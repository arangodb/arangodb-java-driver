package com.arangodb.internal.serde;

import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.util.RawBytes;
import com.arangodb.util.RawJson;
import jakarta.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SerdeUtils {

    private SerdeUtils() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SerdeUtils.class);

    public static Type constructListType(Class<?> clazz) {
        return JsonMapper.shared().getTypeFactory().constructCollectionType(List.class, clazz);
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
                tools.jackson.databind.cfg.PackageVersion.VERSION,
                tools.jackson.core.json.PackageVersion.VERSION
        ).forEach(version -> {
            int major = version.getMajorVersion();
            int minor = version.getMinorVersion();
            if (major != 3 || minor > 1) {
                LOGGER.warn("Unsupported Jackson version: {}", version);
            }
        });
    }

    /**
     * Parse a JSON string.
     *
     * @param json JSON string to parse
     * @return root of the parsed tree
     */
    public static JsonNode parseJson(final String json) {
        return JsonMapper.shared().readTree(json);
    }

    /**
     * @param data JsonNode
     * @return JSON string
     */
    public static String writeJson(final JsonNode data) {
        return JsonMapper.shared().writeValueAsString(data);
    }

    /**
     * Extract raw bytes for the current JSON node
     *
     * @param parser JsonParser with the current token pointing to the node to extract
     * @return byte array
     */
    public static byte[] extractBytes(JsonParser parser) {
        JsonToken t = parser.currentToken();
        if (t.isStructEnd() || t == JsonToken.PROPERTY_NAME) {
            throw new ArangoDBException("Unexpected token: " + t);
        }
        byte[] data = (byte[]) parser.currentTokenLocation().contentReference().getRawContent();
        int start = (int) parser.currentTokenLocation().getByteOffset();
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
        int end = (int) parser.currentLocation().getByteOffset();
        return Arrays.copyOfRange(data, start, end);
    }

    public static boolean isManagedClass(Class<?> clazz) {
        return JsonNode.class.isAssignableFrom(clazz) ||        // jackson datatypes
                JsonValue.class.isAssignableFrom(clazz) ||      // JSON-B datatypes
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
