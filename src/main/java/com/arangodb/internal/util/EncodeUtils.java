package com.arangodb.internal.util;

import com.arangodb.ArangoDBException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class EncodeUtils {
    private EncodeUtils() {
    }

    /**
     * Encodes a string by replacing each instance of certain characters by one, two, three, or four escape sequences
     * representing the UTF-8 encoding of the character.
     * It behaves the same as Javascript <code>encodeURIComponent()</code>.
     *
     * @param value string to encode
     * @return encoded string
     */
    public static String encodeURIComponent(final String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name())
                    .replace("+", "%20")
                    .replace("%21", "!")
                    .replace("%27", "'")
                    .replace("%28", "(")
                    .replace("%29", ")")
                    .replace("%7E", "~");
        } catch (UnsupportedEncodingException e) {
            throw new ArangoDBException(e);
        }
    }

}
