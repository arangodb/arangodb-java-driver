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

package com.arangodb.internal.http;

import com.arangodb.ArangoDBException;
import com.arangodb.DbName;
import com.arangodb.Protocol;
import com.arangodb.internal.net.Connection;
import com.arangodb.internal.net.HostDescription;
import com.arangodb.internal.serde.InternalSerde;
import com.arangodb.internal.util.ResponseUtils;
import com.arangodb.ContentType;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;
import com.arangodb.velocystream.Response;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.IdentityCipherSuiteFilter;
import io.netty.handler.ssl.JdkSslContext;
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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public class HttpConnection implements Connection {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpCommunication.class);
    private static final String CONTENT_TYPE_APPLICATION_JSON_UTF8 = "application/json; charset=utf-8";
    private static final String CONTENT_TYPE_VPACK = "application/x-velocypack";
    private static final AtomicInteger THREAD_COUNT = new AtomicInteger();
    private final InternalSerde util;
    private final String baseUrl;
    private final ContentType contentType;
    private String auth;
    private final WebClient client;
    private final Integer timeout;
    private final Vertx vertx;

    private HttpConnection(final HostDescription host, final Integer timeout, final String user, final String password,
                           final Boolean useSsl, final SSLContext sslContext, final Boolean verifyHost,
                           final InternalSerde util, final Protocol protocol, final Long ttl) {
        super();
        this.util = util;
        this.contentType = ContentType.of(protocol);
        this.timeout = timeout;
        baseUrl = buildBaseUrl(host, useSsl);
        vertx = Vertx.vertx(new VertxOptions().setPreferNativeTransport(true).setEventLoopPoolSize(1));
        vertx.runOnContext(e -> {
            Thread.currentThread().setName("adb-eventloop-" + THREAD_COUNT.getAndIncrement());
            auth = new UsernamePasswordCredentials(user, password != null ? password : "").toHttpAuthorization();
        });

        int _ttl = ttl == null ? 0 : Math.toIntExact(ttl / 1000);

        HttpVersion httpVersion = protocol == Protocol.HTTP_JSON || protocol == Protocol.HTTP_VPACK ?
                HttpVersion.HTTP_1_1 : HttpVersion.HTTP_2;

        WebClientOptions webClientOptions = new WebClientOptions()
                .setConnectTimeout(timeout)
                .setIdleTimeoutUnit(TimeUnit.MILLISECONDS)
                .setIdleTimeout(timeout)
                .setKeepAliveTimeout(_ttl)
                .setHttp2KeepAliveTimeout(_ttl)
                .setUserAgentEnabled(false)
                .setFollowRedirects(false)
                .setLogActivity(true)
                .setKeepAlive(true)
                .setTcpKeepAlive(true)
                .setPipelining(true)
                .setReuseAddress(true)
                .setReusePort(true)
                .setHttp2ClearTextUpgrade(false)
                .setProtocolVersion(httpVersion)
                .setDefaultHost(host.getHost())
                .setDefaultPort(host.getPort());


        if (Boolean.TRUE.equals(useSsl)) {
            SSLContext ctx;
            if (sslContext != null) {
                ctx = sslContext;
            } else {
                try {
                    ctx = SSLContext.getDefault();
                } catch (NoSuchAlgorithmException e) {
                    throw new ArangoDBException(e);
                }
            }

            webClientOptions
                    .setSsl(true)
                    .setUseAlpn(true)
                    .setVerifyHost(verifyHost != null ? verifyHost : true)
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
                                    ApplicationProtocolConfig.DISABLED,
                                    ClientAuth.NONE,
                                    null,
                                    false
                            );
                        }
                    });
        }

        client = WebClient.create(vertx, webClientOptions);
    }

    private static String buildUrl(final Request request) {
        StringBuilder sb = new StringBuilder();
        DbName dbName = request.getDbName();
        if (dbName != null && !dbName.get().isEmpty()) {
            sb.append("/_db/").append(dbName.getEncoded());
        }
        sb.append(request.getRequest());
        if (!request.getQueryParam().isEmpty()) {
            sb.append("?");

            try {
                for (Iterator<Entry<String, String>> iterator = request.getQueryParam().entrySet().iterator(); iterator.hasNext(); ) {
                    Entry<String, String> param = iterator.next();
                    if (param.getValue() != null) {
                        sb.append(URLEncoder.encode(param.getKey(), StandardCharsets.UTF_8.toString()));
                        sb.append("=");
                        sb.append(URLEncoder.encode(param.getValue(), StandardCharsets.UTF_8.toString()));
                        if (iterator.hasNext()) {
                            sb.append("&");
                        }
                    }
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

        }
        return sb.toString();
    }

    private static void addHeader(final Request request, final HttpRequest<?> httpRequest) {
        for (final Entry<String, String> header : request.getHeaderParam().entrySet()) {
            httpRequest.putHeader(header.getKey(), header.getValue());
        }
    }

    @Override
    public void close() {
        client.close();
        vertx.close();
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

    private String buildBaseUrl(HostDescription host, boolean useSsl) {
        return (Boolean.TRUE.equals(useSsl) ? "https://" : "http://") + host.getHost() + ":" + host.getPort();
    }

    public CompletableFuture<Response> execute(final Request request) {
        CompletableFuture<Response> rfuture = new CompletableFuture<>();
        vertx.runOnContext(e -> doExecute(request, rfuture));
        return rfuture;
    }

    public void doExecute(final Request request, final CompletableFuture<Response> rfuture) {
        String path = buildUrl(request);
        HttpRequest<Buffer> httpRequest = client
                .request(requestTypeToHttpMethod(request.getRequestType()), path)
                .timeout(timeout);
        if (contentType == ContentType.VPACK) {
            httpRequest.putHeader("Accept", "application/x-velocypack");
        }
        addHeader(request, httpRequest);
        httpRequest.putHeader(HttpHeaders.AUTHORIZATION.toString(), auth);

        if (LOGGER.isDebugEnabled()) {
            CURLLogger.log(baseUrl, path, request, util);
        }

        byte[] reqBody = request.getBody();
        Buffer buffer;
        if (reqBody != null) {
            buffer = Buffer.buffer(reqBody);
            if (contentType == ContentType.VPACK) {
                httpRequest.putHeader(HttpHeaders.CONTENT_TYPE.toString(), CONTENT_TYPE_VPACK);
            } else {
                httpRequest.putHeader(HttpHeaders.CONTENT_TYPE.toString(), CONTENT_TYPE_APPLICATION_JSON_UTF8);
            }
        } else {
            buffer = Buffer.buffer();
        }

        httpRequest.sendBuffer(buffer)
                .map(this::buildResponse)
                .map(this::checkError)
                .onSuccess(rfuture::complete)
                .onFailure(rfuture::completeExceptionally);
    }

    private Response buildResponse(final HttpResponse<Buffer> httpResponse) {
        final Response response = new Response();
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

    protected Response checkError(final Response response) {
        ResponseUtils.checkError(util, response);
        return response;
    }

    @Override
    public void setJwt(String jwt) {
        if (jwt != null) {
            vertx.runOnContext((e) -> auth = new TokenCredentials(jwt).toHttpAuthorization());
        }
    }

    public static class Builder {
        private String user;
        private String password;
        private InternalSerde util;
        private Boolean useSsl;
        private Protocol protocol;
        private HostDescription host;
        private Long ttl;
        private SSLContext sslContext;
        private Boolean verifyHost;
        private Integer timeout;

        public Builder user(final String user) {
            this.user = user;
            return this;
        }

        public Builder password(final String password) {
            this.password = password;
            return this;
        }

        public Builder serializationUtil(final InternalSerde util) {
            this.util = util;
            return this;
        }

        public Builder useSsl(final Boolean useSsl) {
            this.useSsl = useSsl;
            return this;
        }

        public Builder protocol(final Protocol protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder host(final HostDescription host) {
            this.host = host;
            return this;
        }

        public Builder ttl(final Long ttl) {
            this.ttl = ttl;
            return this;
        }

        public Builder sslContext(final SSLContext sslContext) {
            this.sslContext = sslContext;
            return this;
        }

        public Builder verifyHost(final Boolean verifyHost) {
            this.verifyHost = verifyHost;
            return this;
        }

        public Builder timeout(final Integer timeout) {
            this.timeout = timeout;
            return this;
        }

        public HttpConnection build() {
            return new HttpConnection(host, timeout, user, password, useSsl, sslContext, verifyHost, util,
                    protocol, ttl);
        }
    }

}
