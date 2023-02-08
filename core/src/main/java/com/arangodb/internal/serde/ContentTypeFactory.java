package com.arangodb.internal.serde;

import com.arangodb.ContentType;
import com.arangodb.Protocol;

public final class ContentTypeFactory {
    private ContentTypeFactory() {
    }

    public static ContentType of(Protocol protocol) {
        switch (protocol) {
            case HTTP_JSON:
            case HTTP2_JSON:
                return ContentType.JSON;
            case VST:
            case HTTP_VPACK:
            case HTTP2_VPACK:
                return ContentType.VPACK;
            default:
                throw new IllegalArgumentException("Unexpected value: " + protocol);
        }
    }

}
