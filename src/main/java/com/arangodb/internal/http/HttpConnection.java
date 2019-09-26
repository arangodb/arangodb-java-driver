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
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientResponse;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.util.CharsetUtil.UTF_8;

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
        private String httpCookieSpec;
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

        public Builder timeout(final Integer timeout) {
            this.timeout = timeout;
            return this;
        }

        public HttpConnection build() {
            return new HttpConnection(host, timeout, user, password, useSsl, sslContext, util, contentType, ttl, httpCookieSpec);
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

    private HttpConnection(final HostDescription host, final Integer timeout, final String user, final String password,
                           final Boolean useSsl, final SSLContext sslContext, final ArangoSerialization util, final Protocol contentType,
                           // FIXME: setup ttl and httpCookieSpec
                           final Long ttl, final String httpCookieSpec) {
        super();
        this.host = host;
        this.user = user;
        this.password = password;
        this.useSsl = useSsl;
        this.sslContext = sslContext;
        this.util = util;
        this.contentType = contentType;
        this.timeout = timeout;

        // FIXME
//        if (httpCookieSpec != null && httpCookieSpec.length() > 1) {
//            requestConfig.setCookieSpec(httpCookieSpec);
//        }
    }

    private HttpClient getClient() {
        // TODO: build client using reactor.netty.resources.ConnectionProvider
        return HttpClient.create()
                .protocol(HttpProtocol.HTTP11)
                .wiretap(true)
                .baseUrl(buildBaseUrl())
                .tcpConfiguration(tcpClient ->
                        timeout != null && timeout >= 0 ? tcpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout) : tcpClient)
                // FIXME
//                .tcpConfiguration(tcpClient ->
//                        Boolean.TRUE == useSsl && sslContext != null ? tcpClient.secure(buildSslContext()) : tcpClient)
                .keepAlive(true)
                .headers(headers -> {
                    headers.set(CONTENT_TYPE, getContentType());
                    if (user != null)
                        headers.set(AUTHORIZATION, buildBasicAuthentication(user, password));
                });
    }

    private String buildBaseUrl() {
        return (Boolean.TRUE == useSsl ? "https://" : "http://") + host.getHost() + ":" + host.getPort();
    }

    private SslContext buildSslContext() {
        try {
            return SslContextBuilder.forClient().sslContextProvider(sslContext.getProvider()).build();
        } catch (SSLException e) {
            e.printStackTrace();
            throw new ArangoDBException(e);
        }
    }

    static String buildBasicAuthentication(final String principal, final String password) {
        final String tmp = principal + ":" + (password == null ? "" : password);
        String encoded = Base64.getEncoder().encodeToString(tmp.getBytes());
        return "Basic " + encoded;
    }

    @Override
    public void close() throws IOException {
        // TODO
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
            final String paramString = URLEncodedUtils.format(toList(request.getQueryParam()), UTF_8);
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

    private HttpClient.RequestSender buildMethod(final HttpClient client, final Request request) {
        return client.request(requestTypeToHttpMethod(request.getRequestType()));
    }

    private HttpClient.RequestSender buildUri(final HttpClient.RequestSender sender, final String url) {
        return sender.uri(url);
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

    private static List<NameValuePair> toList(final Map<String, String> parameters) {
        final ArrayList<NameValuePair> paramList = new ArrayList<>(parameters.size());
        for (final Entry<String, String> param : parameters.entrySet()) {
            if (param.getValue() != null) {
                paramList.add(new BasicNameValuePair(param.getKey(), param.getValue()));
            }
        }
        return paramList;
    }

    public Response execute(final Request request) throws ArangoDBException, IOException {
        final String url = buildUrl(request);
        byte[] body = getBody(request);

        if (LOGGER.isDebugEnabled()) {
            CURLLogger.log(
                    url,
                    request,
                    Optional.ofNullable(user),
                    Optional.ofNullable(password),
                    util
            );
        }

        HttpClient c = getClient()
                .headers(headers -> {
                    headers.set(CONTENT_LENGTH, body.length);
                    headers.set(USER_AGENT, "Mozilla/5.0 (compatible; ArangoDB-JavaDriver/1.1; +http://mt.orz.at/)");
                    if (contentType == Protocol.HTTP_VPACK) {
                        headers.set(ACCEPT, "application/x-velocypack");
                    }
                    addHeader(request, headers);
                });

        HttpClient.RequestSender sender = buildUri(buildMethod(c, request), url);
        HttpClient.ResponseReceiver<?> receiver = sender.send(Mono.just(Unpooled.wrappedBuffer(body)));

        return receiver
                .responseSingle(this::buildResponse)
                .doOnNext(this::checkError)
                .block();
    }

    private static void addHeader(final Request request, final HttpHeaders headers) {
        for (final Entry<String, String> header : request.getHeaderParam().entrySet()) {
            headers.add(header.getKey(), header.getValue());
        }
    }

    private Mono<Response> buildResponse(HttpClientResponse resp, ByteBufMono bytes) {
        final Response response = new Response();
        response.setResponseCode(resp.status().code());

        final Map<String, String> meta = response.getMeta();
        resp.responseHeaders().forEach(it -> meta.put(it.getKey(), it.getValue()));

        final Mono<VPackSlice> vPackSliceMono;
        if (contentType == Protocol.HTTP_VPACK) {
            vPackSliceMono = bytes.asByteArray().map(VPackSlice::new);
        } else {
            vPackSliceMono = bytes.asInputStream()
                    .map(input -> {
                        try {
                            String content = IOUtils.toString(input);
                            return util.serialize(content, new ArangoSerializer.Options().stringAsJson(true).serializeNullValues(true));
                        } catch (IOException e) {
                            throw new ArangoDBException(e);
                        }
                    });
        }
        return vPackSliceMono.map(body -> {
            response.setBody(body);
            return response;
        });
    }

    private void checkError(final Response response) throws ArangoDBException {
        ResponseUtils.checkError(util, response);
    }

}
