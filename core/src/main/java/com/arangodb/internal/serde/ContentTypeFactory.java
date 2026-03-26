package com.arangodb.internal.serde;

import com.arangodb.ContentType;
import com.arangodb.Protocol;

public final class ContentTypeFactory {
    private ContentTypeFactory() {
    }

    // FIXME: remove, not needed anymore since VPACK has been removed
    public static ContentType of(Protocol protocol) {
        switch (protocol) {
            case HTTP_1_1:
            case HTTP_2:
                return ContentType.JSON;
            default:
                throw new IllegalArgumentException("Unexpected value: " + protocol);
        }
    }

}
