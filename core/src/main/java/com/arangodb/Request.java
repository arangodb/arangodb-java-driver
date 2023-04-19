package com.arangodb;

import java.util.HashMap;
import java.util.Map;

public final class Request<T> {
    private final String db;
    private final Method method;
    private final String path;
    private final Map<String, String> queryParams;
    private final Map<String, String> headers;
    private final T body;

    public enum Method {
        DELETE,
        GET,
        POST,
        PUT,
        HEAD,
        PATCH,
        OPTIONS
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    private Request(String db, Method method, String path, Map<String, String> queryParams, Map<String, String> headers, T body) {
        this.db = db;
        this.method = method;
        this.path = path;
        this.queryParams = queryParams;
        this.headers = headers;
        this.body = body;
    }

    public String getDb() {
        return db;
    }

    public Method getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public T getBody() {
        return body;
    }

    public static final class Builder<T> {
        private String db;
        private Request.Method method;
        private String path;
        private final Map<String, String> queryParams;
        private final Map<String, String> headers;
        private T body;

        public Builder() {
            queryParams = new HashMap<>();
            headers = new HashMap<>();
        }

        public Builder<T> db(String db) {
            this.db = db;
            return this;
        }

        public Builder<T> method(Request.Method method) {
            this.method = method;
            return this;
        }

        public Builder<T> path(String path) {
            this.path = path;
            return this;
        }

        public Builder<T> queryParam(final String key, final String value) {
            if (value != null) {
                queryParams.put(key, value);
            }
            return this;
        }

        public Builder<T> queryParams(Map<String, String> queryParams) {
            if (queryParams != null) {
                for (Map.Entry<String, String> it : queryParams.entrySet()) {
                    queryParam(it.getKey(), it.getValue());
                }
            }
            return this;
        }

        public Builder<T> header(final String key, final String value) {
            if (value != null) {
                headers.put(key, value);
            }
            return this;
        }

        public Builder<T> headers(Map<String, String> headers) {
            if (headers != null) {
                for (Map.Entry<String, String> it : headers.entrySet()) {
                    header(it.getKey(), it.getValue());
                }
            }
            return this;
        }

        public Builder<T> body(T body) {
            this.body = body;
            return this;
        }

        public Request<T> build() {
            return new Request<>(db, method, path, queryParams, headers, body);
        }
    }
}
