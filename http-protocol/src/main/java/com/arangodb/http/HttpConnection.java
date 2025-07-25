/*
 * DISCLAIMER
 *
 * Copyright 2017 ArangoDB GmbH, Cologne, Germany
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

import com.arangodb.*;
import com.arangodb.arch.UnstableApi;
import com.arangodb.config.HostDescription;
import com.arangodb.http.compression.Encoder;
import com.arangodb.internal.InternalRequest;
import com.arangodb.internal.InternalResponse;
import com.arangodb.internal.RequestType;
import com.arangodb.internal.config.ArangoConfig;
import com.arangodb.internal.net.Connection;
import com.arangodb.internal.net.ConnectionPool;
import com.arangodb.internal.serde.ContentTypeFactory;
import com.arangodb.internal.util.EncodeUtils;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.IdentityCipherSuiteFilter;
import io.netty.handler.ssl.JdkSslContext;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.net.JdkSSLEngineOptions;
import io.vertx.core.spi.tls.SslContextFactory;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.arangodb.internal.net.ConnectionPoolImpl.HTTP1_SLOTS_PIPELINING;
import static com.arangodb.internal.net.ConnectionPoolImpl.HTTP2_SLOTS;


/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
@UnstableApi
public class HttpConnection implements Connection {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpConnection.class);
    private static final String CONTENT_TYPE_APPLICATION_JSON_UTF8 = "application/json; charset=utf-8";
    private static final String CONTENT_TYPE_VPACK = "application/x-velocypack";
    private static final String USER_AGENT = getUserAgent();
    private static final AtomicInteger THREAD_COUNT = new AtomicInteger();
    private volatile String auth;
    private final int compressionThreshold;
    private final Encoder encoder;
    private final WebClient client;
    private final Integer timeout;
    private final MultiMap commonHeaders = MultiMap.caseInsensitiveMultiMap();
    private final Vertx vertx;
    private final Vertx vertxToClose;
    private final ConnectionPool pool;

    private static String getUserAgent() {
        return "JavaDriver/" + PackageVersion.VERSION + " (JVM/" + System.getProperty("java.specification.version") + ")";
    }

    HttpConnection(final ArangoConfig config, final HttpProtocolConfig protocolConfig, final HostDescription host, final ConnectionPool pool) {
        this.pool = pool;
        Protocol protocol = config.getProtocol();
        ContentType contentType = ContentTypeFactory.of(protocol);
        if (contentType == ContentType.VPACK) {
            commonHeaders.add(HttpHeaders.ACCEPT.toString(), CONTENT_TYPE_VPACK);
            commonHeaders.add(HttpHeaders.CONTENT_TYPE.toString(), CONTENT_TYPE_VPACK);
        } else if (contentType == ContentType.JSON) {
            commonHeaders.add(HttpHeaders.ACCEPT.toString(), CONTENT_TYPE_APPLICATION_JSON_UTF8);
            commonHeaders.add(HttpHeaders.CONTENT_TYPE.toString(), CONTENT_TYPE_APPLICATION_JSON_UTF8);
        } else {
            throw new IllegalArgumentException("Unsupported protocol: " + protocol);
        }
        compressionThreshold = config.getCompressionThreshold();
        Compression compression = config.getCompression();
        encoder = Encoder.of(compression, config.getCompressionLevel());
        if (encoder.getFormat() != null) {
            commonHeaders.add(HttpHeaders.ACCEPT_ENCODING.toString(), encoder.getFormat());
        }
        commonHeaders.add("x-arango-driver", USER_AGENT);
        timeout = config.getTimeout();
        auth = new UsernamePasswordCredentials(
                config.getUser(), Optional.ofNullable(config.getPassword()).orElse("")
        ).toHttpAuthorization();

        if (protocolConfig.getVertx() != null) {
            // reuse existing Vert.x
            vertx = protocolConfig.getVertx();
            // Vert.x will not be closed when connection is closed
            vertxToClose = null;
            LOGGER.debug("Reusing existing Vert.x instance");
        } else {
            // create a new Vert.x instance
            LOGGER.debug("Creating new Vert.x instance");
            vertx = Vertx.vertx(new VertxOptions().setPreferNativeTransport(true).setEventLoopPoolSize(1));
            vertx.runOnContext(e -> Thread.currentThread().setName("adb-http-" + THREAD_COUNT.getAndIncrement()));
            // Vert.x be closed when connection is closed
            vertxToClose = vertx;
        }

        int intTtl = Optional.ofNullable(config.getConnectionTtl())
                .map(ttl -> Math.toIntExact(ttl / 1000))
                .orElse(0);

        HttpVersion httpVersion = protocol == Protocol.HTTP_JSON || protocol == Protocol.HTTP_VPACK ?
                HttpVersion.HTTP_1_1 : HttpVersion.HTTP_2;

        WebClientOptions webClientOptions = new WebClientOptions()
                .setMaxPoolSize(1)
                .setHttp2MaxPoolSize(1)
                .setConnectTimeout(timeout)
                .setIdleTimeoutUnit(TimeUnit.MILLISECONDS)
                .setIdleTimeout(timeout)
                .setKeepAliveTimeout(intTtl)
                .setHttp2KeepAliveTimeout(intTtl)
                .setUserAgentEnabled(false)
                .setFollowRedirects(false)
                .setLogActivity(true)
                .setKeepAlive(true)
                .setTcpKeepAlive(true)
                .setPipelining(config.getPipelining())
                .setPipeliningLimit(HTTP1_SLOTS_PIPELINING)
                .setHttp2MultiplexingLimit(HTTP2_SLOTS)
                .setReuseAddress(true)
                .setReusePort(true)
                .setHttp2ClearTextUpgrade(false)
                .setProtocolVersion(httpVersion)
                .setDefaultHost(host.getHost())
                .setDefaultPort(host.getPort())
                .setProxyOptions(protocolConfig.getProxyOptions());

        if (compression != Compression.NONE) {
            webClientOptions.setTryUseCompression(true);
        }

        if (Boolean.TRUE.equals(config.getUseSsl())) {
            SSLContext ctx = config.getSslContext();
            webClientOptions
                    .setSsl(true)
                    .setUseAlpn(true)
                    .setAlpnVersions(Collections.singletonList(httpVersion))
                    .setVerifyHost(config.getVerifyHost())
                    .setJdkSslEngineOptions(new JdkSSLEngineOptions() {
                        @Override
                        public JdkSSLEngineOptions copy() {
                            return this;
                        }

                        @Override
                        public SslContextFactory sslContextFactory() {
                            return () -> new JdkSslContext(
                                    ctx,
                                    true,
                                    null,
                                    IdentityCipherSuiteFilter.INSTANCE,
                                    new ApplicationProtocolConfig(
                                            ApplicationProtocolConfig.Protocol.ALPN,
                                            ApplicationProtocolConfig.SelectorFailureBehavior.FATAL_ALERT,
                                            ApplicationProtocolConfig.SelectedListenerFailureBehavior.FATAL_ALERT,
                                            httpVersion.alpnName()
                                    ),
                                    ClientAuth.OPTIONAL,
                                    null,
                                    false
                            );
                        }
                    });
        }

        client = WebClient.create(vertx, webClientOptions);
    }

    private static String buildUrl(final InternalRequest request) {
        StringBuilder sb = new StringBuilder();
        String dbName = request.getDbName();
        if (dbName != null && !dbName.isEmpty()) {
            sb.append("/_db/").append(EncodeUtils.encodeURIComponent(dbName));
        }
        sb.append(request.getPath());
        if (!request.getQueryParam().isEmpty()) {
            sb.append("?");
            for (Iterator<Entry<String, String>> iterator = request.getQueryParam().entrySet().iterator(); iterator.hasNext(); ) {
                Entry<String, String> param = iterator.next();
                if (param.getValue() != null) {
                    sb.append(EncodeUtils.encodeURIComponent(param.getKey()));
                    sb.append("=");
                    sb.append(EncodeUtils.encodeURIComponent(param.getValue()));
                    if (iterator.hasNext()) {
                        sb.append("&");
                    }
                }
            }
        }
        return sb.toString();
    }

    private static void addHeader(final InternalRequest request, final HttpRequest<?> httpRequest) {
        for (final Entry<String, String> header : request.getHeaderParam().entrySet()) {
            httpRequest.putHeader(header.getKey(), header.getValue());
        }
    }

    @Override
    public void close() {
        client.close();
        if (vertxToClose != null) {
            LOGGER.debug("Closing Vert.x instance");
            vertxToClose.close();
        }
    }

    private HttpMethod requestTypeToHttpMethod(RequestType requestType) {
        switch (requestType) {
            case POST:
                return HttpMethod.POST;
            case PUT:
                return HttpMethod.PUT;
            case PATCH:
                return HttpMethod.PATCH;
            case DELETE:
                return HttpMethod.DELETE;
            case HEAD:
                return HttpMethod.HEAD;
            case GET:
            default:
                return HttpMethod.GET;
        }
    }

    @Override
    public void release() {
        vertx.runOnContext(__ -> pool.release(this));
    }

    @Override
    @UnstableApi
    public CompletableFuture<InternalResponse> executeAsync(@UnstableApi final InternalRequest request) {
        CompletableFuture<InternalResponse> rfuture = new CompletableFuture<>();
        doExecute(request, rfuture);
        return rfuture;
    }

    private void doExecute(@UnstableApi final InternalRequest request, @UnstableApi final CompletableFuture<InternalResponse> rfuture) {
        String path = buildUrl(request);
        HttpRequest<Buffer> httpRequest = client
                .request(requestTypeToHttpMethod(request.getRequestType()), path)
                .timeout(timeout);

        httpRequest.putHeaders(commonHeaders);
        addHeader(request, httpRequest);
        httpRequest.putHeader(HttpHeaders.AUTHORIZATION.toString(), auth);

        byte[] reqBody = request.getBody();
        Buffer buffer;
        if (reqBody == null) {
            buffer = Buffer.buffer();
        } else if (reqBody.length > compressionThreshold) {
            httpRequest.putHeader(HttpHeaders.CONTENT_ENCODING.toString(), encoder.getFormat());
            buffer = encoder.encode(reqBody);
        } else {
            buffer = Buffer.buffer(reqBody);
        }

        try {
            httpRequest.sendBuffer(buffer)
                    .map(this::buildResponse)
                    .onSuccess(rfuture::complete)
                    .onFailure(rfuture::completeExceptionally);
        } catch (Exception e) {
            rfuture.completeExceptionally(e);
        }
    }

    private InternalResponse buildResponse(final HttpResponse<Buffer> httpResponse) {
        final InternalResponse response = new InternalResponse();
        response.setResponseCode(httpResponse.statusCode());
        Buffer body = httpResponse.body();
        if (body != null) {
            byte[] bytes = body.getBytes();
            if (bytes.length > 0) {
                response.setBody(bytes);
            }
        }
        for (Entry<String, String> header : httpResponse.headers()) {
            response.putMeta(header.getKey(), header.getValue());
        }
        return response;
    }

    @Override
    public void setJwt(String jwt) {
        if (jwt != null) {
            auth = new TokenCredentials(jwt).toHttpAuthorization();
        }
    }

}
