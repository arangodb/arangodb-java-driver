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
import com.arangodb.Protocol;
import com.arangodb.internal.net.Connection;
import com.arangodb.internal.net.HostDescription;
import com.arangodb.internal.util.CURLLogger;
import com.arangodb.internal.util.IOUtils;
import com.arangodb.internal.util.ResponseUtils;
import com.arangodb.util.ArangoSerialization;
import com.arangodb.util.ArangoSerializer;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;
import com.arangodb.velocystream.Response;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.ssl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.netty.ByteBufMono;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientResponse;
import reactor.netty.resources.ConnectionProvider;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static reactor.netty.resources.ConnectionProvider.DEFAULT_POOL_ACQUIRE_TIMEOUT;

/**
 * @author Mark Vollmary
 */
public class HttpConnection implements Connection {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpConnection.class);
    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json; charset=UTF-8";
    private static final String CONTENT_TYPE_VPACK = "application/x-velocypack";

    public static class Builder {
        private String user;
        private String password;
        private ArangoSerialization util;
        private Boolean useSsl;
        private Boolean resendCookies;
        private Protocol contentType;
        private HostDescription host;
        private Long ttl;
        private SSLContext sslContext;
        private Integer timeout;

        public Builder user(final String user) {
            this.user = user;
            return this;
        }

        public Builder password(final String password) {
            this.password = password;
            return this;
        }

        public Builder serializationUtil(final ArangoSerialization util) {
            this.util = util;
            return this;
        }

        public Builder useSsl(final Boolean useSsl) {
            this.useSsl = useSsl;
            return this;
        }

        public Builder httpResendCookies(Boolean resendCookies) {
            this.resendCookies = resendCookies;
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

        public Builder timeout(final Integer timeout) {
            this.timeout = timeout;
            return this;
        }

        public HttpConnection build() {
            return new HttpConnection(host, timeout, user, password, useSsl, sslContext, util, contentType, ttl, resendCookies);
        }
    }

    private final String user;
    private final String password;
    private final ArangoSerialization util;
    private final Boolean useSsl;
    private final SSLContext sslContext;
    private final Protocol contentType;
    private final HostDescription host;
    private final Integer timeout;
    private final ConnectionProvider connectionProvider;
    private final HttpClient client;
    private final Long ttl;
    private final Boolean resendCookies;

    private final Scheduler scheduler;
    private final Map<Cookie, Long> cookies;

    private HttpConnection(final HostDescription host, final Integer timeout, final String user, final String password,
                           final Boolean useSsl, final SSLContext sslContext, final ArangoSerialization util, final Protocol contentType,
                           final Long ttl, final Boolean resendCookies) {
        super();
        this.host = host;
        this.timeout = timeout;
        this.user = user;
        this.password = password;
        this.useSsl = useSsl;
        this.sslContext = sslContext;
        this.util = util;
        this.contentType = contentType;
        this.ttl = ttl;
        this.resendCookies = resendCookies;

        this.scheduler = Schedulers.single();
        this.cookies = new HashMap<>();

        this.connectionProvider = initConnectionProvider();
        this.client = initClient();
    }

    private ConnectionProvider initConnectionProvider() {
        return ConnectionProvider.fixed(
                "http",
                1,  // FIXME: connection pooling should happen here, inside HttpConnection
                getAcquireTimeout(),
                ttl != null ? Duration.ofMillis(ttl) : null);
    }

    private long getAcquireTimeout() {
        return timeout != null && timeout >= 0 ? timeout : DEFAULT_POOL_ACQUIRE_TIMEOUT;
    }

    private HttpClient initClient() {
        return applySslContext(
                HttpClient
                        .create(connectionProvider)
                        .tcpConfiguration(tcpClient ->
                                timeout != null && timeout >= 0 ? tcpClient.option(CONNECT_TIMEOUT_MILLIS, timeout) : tcpClient)
                        .wiretap(true)
                        .protocol(HttpProtocol.HTTP11)
                        .keepAlive(true)
                        .baseUrl((Boolean.TRUE == useSsl ? "https://" : "http://") + host.getHost() + ":" + host.getPort())
                        .headers(headers -> {
                            if (user != null)
                                headers.set(AUTHORIZATION, buildBasicAuthentication(user, password));
                        })
        );
    }

    private HttpClient applySslContext(HttpClient httpClient) {
        if (Boolean.TRUE == useSsl && sslContext != null) {
            //noinspection deprecation
            return httpClient.secure(spec -> spec.sslContext(new JdkSslContext(sslContext, true, ClientAuth.NONE)));
        } else {
            return httpClient;
        }
    }

    private static String buildBasicAuthentication(final String principal, final String password) {
        final String plainAuth = principal + ":" + (password == null ? "" : password);
        String encodedAuth = Base64.getEncoder().encodeToString(plainAuth.getBytes());
        return "Basic " + encodedAuth;
    }

    @Override
    public void close() {
        connectionProvider.disposeLater().block();
    }

    private static String buildUrl(final Request request) {
        final StringBuilder sb = new StringBuilder();
        final String database = request.getDatabase();
        if (database != null && !database.isEmpty()) {
            sb.append("/_db/").append(database);
        }
        sb.append(request.getRequest());

        if (!request.getQueryParam().isEmpty()) {
            sb.append("?");
            final String paramString = request.getQueryParam().entrySet().stream()
                    .map(it -> it.getKey() + "=" + it.getValue())
                    .collect(Collectors.joining("&"));
            sb.append(paramString);
        }
        return sb.toString();
    }

    private HttpMethod requestTypeToHttpMethod(final RequestType requestType) {
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
                return HttpMethod.GET;
            default:
                throw new IllegalArgumentException();
        }
    }

    private String getContentType() {
        if (contentType == Protocol.HTTP_VPACK) {
            return CONTENT_TYPE_VPACK;
        } else if (contentType == Protocol.HTTP_JSON) {
            return CONTENT_TYPE_APPLICATION_JSON;
        } else {
            throw new IllegalArgumentException();
        }
    }

    private byte[] getBody(final Request request) {
        final VPackSlice body = request.getBody();
        if (body != null) {
            if (contentType == Protocol.HTTP_VPACK) {
                return Arrays.copyOfRange(body.getBuffer(), body.getStart(), body.getStart() + body.getByteSize());
            } else if (contentType == Protocol.HTTP_JSON) {
                return body.toString().getBytes();
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            return new byte[0];
        }
    }

    private HttpClient createHttpClient(final Request request, final int bodyLength) {
        return addCookies(client)
                .headers(headers -> {
                    headers.set(CONTENT_LENGTH, bodyLength);
                    headers.set(USER_AGENT, "Mozilla/5.0 (compatible; ArangoDB-JavaDriver/1.1; +http://mt.orz.at/)");
                    if (contentType == Protocol.HTTP_VPACK) {
                        headers.set(ACCEPT, "application/x-velocypack");
                    }
                    addHeaders(request, headers);
                    if (bodyLength > 0) {
                        headers.set(CONTENT_TYPE, getContentType());
                    }
                });
    }

    public Response execute(final Request request) throws ArangoDBException {
        byte[] body = getBody(request);
        final String url = buildUrl(request);

        if (LOGGER.isDebugEnabled()) {
            CURLLogger.log(
                    url,
                    request,
                    Optional.ofNullable(user),
                    Optional.ofNullable(password),
                    util
            );
        }

        return applyTimeout(
                Mono.defer(() ->
                        // this block runs on the single scheduler executor, so that cookies reads and writes are
                        // always performed by the same thread, thus w/o need for concurrency management
                        createHttpClient(request, body.length)
                                .request(requestTypeToHttpMethod(request.getRequestType())).uri(url)
                                .send(Mono.just(Unpooled.wrappedBuffer(body)))
                                .responseSingle(this::buildResponse))
                        .doOnNext(response -> ResponseUtils.checkError(util, response))
                        .subscribeOn(scheduler)
                        .doOnError(e -> !(e instanceof ArangoDBException), e -> connectionProvider.dispose())
        ).block();
    }

    private Mono<Response> applyTimeout(Mono<Response> client) {
        if (timeout != null && timeout > 0) {
            return client.timeout(Duration.ofMillis(timeout));
        } else {
            return client;
        }
    }

    private static void addHeaders(final Request request, final HttpHeaders headers) {
        for (final Entry<String, String> header : request.getHeaderParam().entrySet()) {
            headers.add(header.getKey(), header.getValue());
        }
    }

    private void removeExpiredCookies() {
        long now = new Date().getTime();
        boolean removed = cookies.entrySet().removeIf(entry -> entry.getKey().maxAge() >= 0 && entry.getValue() + entry.getKey().maxAge() * 1000 < now);
        if (removed) {
            LOGGER.debug("removed cookies");
        }
    }

    private HttpClient addCookies(final HttpClient httpClient) {
        removeExpiredCookies();
        HttpClient c = httpClient;
        for (Cookie cookie : cookies.keySet()) {
            LOGGER.debug("sending cookie: {}", cookie);
            c = c.cookie(cookie);
        }
        return c;
    }

    private void saveCookies(HttpClientResponse resp) {
        if (resendCookies != null && resendCookies) {
            resp.cookies().values().stream().flatMap(Collection::stream)
                    .forEach(cookie -> {
                        LOGGER.debug("saving cookie: {}", cookie);
                        cookies.put(cookie, new Date().getTime());
                    });
        }
    }

    private Mono<Response> buildResponse(HttpClientResponse resp, ByteBufMono bytes) {

        final Mono<VPackSlice> vPackSliceMono;

        if (resp.method() == HttpMethod.HEAD || "0".equals(resp.responseHeaders().get(CONTENT_LENGTH))) {
            vPackSliceMono = Mono.just(new VPackSlice(null));
        } else if (contentType == Protocol.HTTP_VPACK) {
            vPackSliceMono = bytes.asByteArray().map(VPackSlice::new);
        } else if (contentType == Protocol.HTTP_JSON) {
            vPackSliceMono = bytes.asInputStream()
                    .map(input -> {
                        try {
                            String content = IOUtils.toString(input);
                            return util.serialize(content, new ArangoSerializer.Options().stringAsJson(true).serializeNullValues(true));
                        } catch (IOException e) {
                            throw new ArangoDBException(e);
                        }
                    });
        } else {
            throw new IllegalArgumentException();
        }

        return vPackSliceMono
                .map(body -> {
                    final Response response = new Response();
                    response.setResponseCode(resp.status().code());
                    resp.responseHeaders().forEach(it -> response.getMeta().put(it.getKey(), it.getValue()));
                    if (body.getBuffer() != null && body.getBuffer().length > 0) {
                        response.setBody(body);
                    }
                    return response;
                })
                .publishOn(scheduler)
                .doOnNext(it -> saveCookies(resp));
    }

}
