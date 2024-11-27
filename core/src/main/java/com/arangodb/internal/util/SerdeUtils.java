package com.arangodb.internal.util;

import com.arangodb.internal.serde.InternalSerde;

public class SerdeUtils {
    private SerdeUtils() {
    }

    public static String toJsonString(InternalSerde serde, byte[] data) {
        if (data == null) {
            return "";
        }
        try {
            return serde.toJsonString(data);
        } catch (Exception e) {
            return "[Unparsable data]";
        }
    }
}
