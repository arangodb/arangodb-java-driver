/*
 * DISCLAIMER
 *
 * Copyright 2018 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb.http;

import com.arangodb.arch.UnstableApi;
import com.arangodb.config.HostDescription;
import com.arangodb.internal.config.ArangoConfig;
import com.arangodb.internal.net.Connection;
import com.arangodb.internal.net.ConnectionFactory;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@UnstableApi
public class HttpConnectionFactory implements ConnectionFactory {
    private final Logger LOGGER = LoggerFactory.getLogger(HttpConnectionFactory.class);

    private final Vertx vertx;
    private final boolean manageVertx;

    public HttpConnectionFactory(@UnstableApi final ArangoConfig config) {
        Optional<Vertx> existingVertx = Optional.ofNullable(Vertx.currentContext()).map(Context::owner);
        if (config.getReuseVertx() && existingVertx.isPresent()) {
            LOGGER.info("Reusing existing Vert.x instance");
            vertx = existingVertx.get();
            manageVertx = false;
        } else {
            if (existingVertx.isPresent()) {
                LOGGER.warn("Found an existing Vert.x instance, set reuseVertx=true to reuse it");
            }
            LOGGER.info("Creating new Vert.x instance");
            vertx = Vertx.vertx(new VertxOptions().setPreferNativeTransport(true));
            manageVertx = true;
        }
    }

    @Override
    @UnstableApi
    public Connection create(@UnstableApi final ArangoConfig config, final HostDescription host) {
        return new HttpConnection(config, host, vertx);
    }

    @Override
    public synchronized void close() {
        if (manageVertx) {
            LOGGER.info("Closing Vert.x instance");
            vertx.close();
        }
    }
}
