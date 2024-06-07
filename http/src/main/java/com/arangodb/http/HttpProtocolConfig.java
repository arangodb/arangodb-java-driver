package com.arangodb.http;

import com.arangodb.config.ProtocolConfig;
import io.vertx.core.Vertx;

public final class HttpProtocolConfig implements ProtocolConfig {
    private final Vertx vertx;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Vertx vertx;

        private Builder() {
        }

        /**
         * Set the Vert.x instance to use for creating HTTP connections.
         *
         * @param vertx the Vert.x instance to use
         * @return this builder
         */
        public Builder vertx(Vertx vertx) {
            this.vertx = vertx;
            return this;
        }

        public HttpProtocolConfig build() {
            return new HttpProtocolConfig(vertx);
        }
    }

    private HttpProtocolConfig(Vertx vertx) {
        this.vertx = vertx;
    }

    public Vertx getVertx() {
        return vertx;
    }
}
