package com.arangodb.http;

import com.arangodb.config.ProtocolConfig;
import io.vertx.core.Vertx;
import io.vertx.core.net.ProxyOptions;

public final class HttpProtocolConfig implements ProtocolConfig {
    private final Vertx vertx;
    private final ProxyOptions proxyOptions;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Vertx vertx;
        private ProxyOptions proxyOptions;

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

        /**
         * @param proxyOptions proxy options for HTTP connections
         * @return this builder
         */
        public Builder proxyOptions(ProxyOptions proxyOptions) {
            this.proxyOptions = proxyOptions;
            return this;
        }

        public HttpProtocolConfig build() {
            return new HttpProtocolConfig(vertx, proxyOptions);
        }
    }

    private HttpProtocolConfig(Vertx vertx, ProxyOptions proxyOptions) {
        this.vertx = vertx;
        this.proxyOptions = proxyOptions;
    }

    public Vertx getVertx() {
        return vertx;
    }

    public ProxyOptions getProxyOptions() {
        return proxyOptions;
    }
}
