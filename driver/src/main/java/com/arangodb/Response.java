package com.arangodb;

import java.util.Map;

public final class Response<T> {
    private final int responseCode;
    private final Map<String, String> headers;
    private final T body;

    public Response(int responseCode, Map<String, String> headers, T body) {
        this.responseCode = responseCode;
        this.headers = headers;
        this.body = body;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public T getBody() {
        return body;
    }
}
