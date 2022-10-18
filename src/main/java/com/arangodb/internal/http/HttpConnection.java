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
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.http.HttpHeaders.AUTHORIZATION;

/**
 * @author Mark Vollmary
 */
public class HttpConnection implements Connection {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpCommunication.class);
    private static final String CONTENT_TYPE_APPLICATION_JSON_UTF8 = "application/json; charset=utf-8";
    private static final String CONTENT_TYPE_VPACK = "application/x-velocypack";
    private static final AtomicInteger THREAD_COUNT = new AtomicInteger();
    private final InternalSerde util;
    private final String baseUrl;
    private final Protocol contentType;
    private volatile String auth;
    private final WebClient client;
    private final Integer timeout;
    private final Vertx vertx;

    private HttpConnection(final HostDescription host, final Integer timeout, final String user, final String password,
                           final Boolean useSsl, final SSLContext sslContext, final Boolean verifyHost,
                           final InternalSerde util, final Protocol contentType, final Long ttl,
                           final String httpCookieSpec) {
        super();
        this.util = util;
        this.contentType = contentType;
        this.timeout = timeout;
        baseUrl = buildBaseUrl(host, useSsl);
        auth = new UsernamePasswordCredentials(user, password != null ? password : "").toHttpAuthorization();
        vertx = Vertx.vertx(new VertxOptions().setPreferNativeTransport(true).setEventLoopPoolSize(1));
        vertx.runOnContext(e -> Thread.currentThread().setName("adb-eventloop-" + THREAD_COUNT.getAndIncrement()));

        int _ttl = ttl == null ? 0 : Math.toIntExact(ttl / 1000);

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
                //TODO: allow configuring HTTP_2 or HTTP_1_1
//                .setProtocolVersion(HttpVersion.HTTP_1_1)
                .setProtocolVersion(HttpVersion.HTTP_2)
                .setUseAlpn(true)
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
//        if (httpCookieSpec != null && httpCookieSpec.length() > 1) {
//            requestConfig.setCookieSpec(httpCookieSpec);
//        }

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
            if (request.getRequest().contains("?")) {
                sb.append("&");
            } else {
                sb.append("?");
            }
            final String paramString = URLEncodedUtils.format(toList(request.getQueryParam()), "utf-8");
            sb.append(paramString);
        }
        return sb.toString();
    }

    private static List<NameValuePair> toList(final Map<String, String> parameters) {
        final ArrayList<NameValuePair> paramList = new ArrayList<>(parameters.size());
        for (final Entry<String, String> param : parameters.entrySet()) {
            if (param.getValue() != null) {
                paramList.add(new BasicNameValuePair(param.getKey(), param.getValue()));
            }
        }
        return paramList;
    }

    private static void addHeader(final Request request, final HttpRequest<?> httpRequest) {
        for (final Entry<String, String> header : request.getHeaderParam().entrySet()) {
            httpRequest.putHeader(header.getKey(), header.getValue());
        }
    }

    @Override
    public void close() throws IOException {
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

    public Response execute(final Request request) throws IOException {
        String path = buildUrl(request);
        HttpRequest<Buffer> httpRequest = client
                .request(requestTypeToHttpMethod(request.getRequestType()), path)
                .timeout(timeout);
        if (contentType == Protocol.HTTP_VPACK) {
            httpRequest.putHeader("Accept", "application/x-velocypack");
        }
        addHeader(request, httpRequest);
        httpRequest.putHeader(AUTHORIZATION, auth);

        if (LOGGER.isDebugEnabled()) {
            CURLLogger.log(baseUrl, path, request, util);
        }

        byte[] reqBody = request.getBody();
        Buffer buffer;
        if (reqBody != null) {
            buffer = Buffer.buffer(reqBody);
            if (contentType == Protocol.HTTP_VPACK) {
                httpRequest.putHeader(HttpHeaders.CONTENT_TYPE.toString(), CONTENT_TYPE_VPACK);
            } else {
                httpRequest.putHeader(HttpHeaders.CONTENT_TYPE.toString(), CONTENT_TYPE_APPLICATION_JSON_UTF8);
            }
        } else {
            buffer = Buffer.buffer();
        }
        HttpResponse<Buffer> bufferResponse;
        try {
            // FIXME: make async API
            bufferResponse = httpRequest.sendBuffer(buffer).toCompletionStage().toCompletableFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            } else {
                throw new ArangoDBException(e);
            }
        }
        Response response = buildResponse(bufferResponse);
        checkError(response);
        return response;
    }

    public Response buildResponse(final HttpResponse<Buffer> httpResponse) throws UnsupportedOperationException {
        final Response response = new Response();
        response.setResponseCode(httpResponse.statusCode());
        Buffer body = httpResponse.body();
        if (body != null) {
            byte[] bytes = body.getBytes();
            if (bytes.length > 0) {
                response.setBody(bytes);
            }
        }
        final Map<String, String> meta = response.getMeta();
        for (Entry<String, String> header : httpResponse.headers()) {
            meta.put(header.getKey(), header.getValue());
        }
        return response;
    }

    protected void checkError(final Response response) {
        ResponseUtils.checkError(util, response);
    }

    @Override
    public void setJwt(String jwt) {
        if (jwt != null) {
            auth = new TokenCredentials(jwt).toHttpAuthorization();
        }
    }

    public static class Builder {
        private String user;
        private String password;
        private InternalSerde util;
        private Boolean useSsl;
        private String httpCookieSpec;
        private Protocol contentType;
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

        public Builder httpCookieSpec(String httpCookieSpec) {
            this.httpCookieSpec = httpCookieSpec;
            return this;
        }

        public Builder contentType(final Protocol contentType) {
            this.contentType = contentType;
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
                    contentType, ttl, httpCookieSpec);
        }
    }

}
