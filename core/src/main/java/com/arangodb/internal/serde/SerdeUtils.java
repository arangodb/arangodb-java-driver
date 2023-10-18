package com.arangodb.internal.serde;

import com.arangodb.ArangoDBException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            if (major != 2 || minor < 10 || minor > 15) {
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
    public JsonNode parseJson(final String json) {
        try {
            return jsonMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw ArangoDBException.wrap(e);
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
            throw ArangoDBException.wrap(e);
        }
    }

}
