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

import com.arangodb.DbName;
import com.arangodb.Protocol;
import com.arangodb.internal.net.Connection;
import com.arangodb.internal.net.HostDescription;
import com.arangodb.internal.serde.InternalSerde;
import com.arangodb.internal.util.ResponseUtils;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;
import com.arangodb.velocystream.Response;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import static org.apache.http.HttpHeaders.AUTHORIZATION;

/**
 * @author Mark Vollmary
 */
public class HttpConnection implements Connection {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpCommunication.class);
    private static final ContentType CONTENT_TYPE_APPLICATION_JSON_UTF8 = ContentType.create("application/json", "utf-8");
    private static final ContentType CONTENT_TYPE_VPACK = ContentType.create("application/x-velocypack");
    private final String user;
    private final String password;
    private final InternalSerde util;
    private final Protocol contentType;
    private volatile String jwt = null;
    private final WebClient client;

    private HttpConnection(final HostDescription host, final Integer timeout, final String user, final String password,
                           final Boolean useSsl, final SSLContext sslContext, final HostnameVerifier hostnameVerifier
            , final InternalSerde util, final Protocol contentType,
                           final Long ttl, final String httpCookieSpec) {
        super();
        this.user = user;
        this.password = password;
        this.util = util;
        this.contentType = contentType;
        Vertx vertx = Vertx.vertx(new VertxOptions().setEventLoopPoolSize(1));
        // TODO: name threads
        client = WebClient.create(vertx, new WebClientOptions()
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
                .setProtocolVersion(HttpVersion.HTTP_2)
                .setDefaultHost(host.getHost())
                .setDefaultPort(host.getPort()));

//        final RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder
//                .create();
//        if (Boolean.TRUE.equals(useSsl)) {
//            registryBuilder.register("https", new SSLConnectionSocketFactory(
//                    sslContext != null ? sslContext : SSLContexts.createSystemDefault(),
//                    hostnameVerifier != null ? hostnameVerifier :
//                            SSLConnectionSocketFactory.getDefaultHostnameVerifier()
//            ));
//        } else {
//            registryBuilder.register("http", new PlainConnectionSocketFactory());
//        }
//        cm = new PoolingHttpClientConnectionManager(registryBuilder.build());
//        cm.setDefaultMaxPerRoute(1);
//        cm.setMaxTotal(1);
//        final RequestConfig.Builder requestConfig = RequestConfig.custom();
//        if (timeout != null && timeout >= 0) {
//            requestConfig.setConnectTimeout(timeout);
//            requestConfig.setConnectionRequestTimeout(timeout);
//            requestConfig.setSocketTimeout(timeout);
//        }
//
//        if (httpCookieSpec != null && httpCookieSpec.length() > 1) {
//            requestConfig.setCookieSpec(httpCookieSpec);
//        }
//
//        final ConnectionKeepAliveStrategy keepAliveStrategy =
//                (response, context) -> HttpConnection.this.getKeepAliveDuration(response);
//        final HttpClientBuilder builder = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig.build())
//                .setConnectionManager(cm).setKeepAliveStrategy(keepAliveStrategy)
//                .setRetryHandler(httpRequestRetryHandler != null ? httpRequestRetryHandler :
//                        new DefaultHttpRequestRetryHandler());
//        if (ttl != null) {
//            builder.setConnectionTimeToLive(ttl, TimeUnit.MILLISECONDS);
//        }
//        client = builder.build();
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

//    private long getKeepAliveDuration(final HttpResponse response) {
//        final HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
//        while (it.hasNext()) {
//            final HeaderElement he = it.nextElement();
//            final String param = he.getName();
//            final String value = he.getValue();
//            if (value != null && "timeout".equalsIgnoreCase(param)) {
//                try {
//                    return Long.parseLong(value) * 1000L;
//                } catch (final NumberFormatException ignore) {
//                }
//            }
//        }
//        return 30L * 1000L;
//    }

    @Override
    public void close() throws IOException {
//        cm.shutdown();
//        client.close();
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

//    private HttpRequestBase buildHttpRequestBase(final Request request, final String url) {
//        final HttpRequestBase httpRequest;
//        switch (request.getRequestType()) {
//            case POST:
//                httpRequest = requestWithBody(new HttpPost(url), request);
//                break;
//            case PUT:
//                httpRequest = requestWithBody(new HttpPut(url), request);
//                break;
//            case PATCH:
//                httpRequest = requestWithBody(new HttpPatch(url), request);
//                break;
//            case DELETE:
//                httpRequest = requestWithBody(new HttpDeleteWithBody(url), request);
//                break;
//            case HEAD:
//                httpRequest = new HttpHead(url);
//                break;
//            case GET:
//            default:
//                httpRequest = new HttpGet(url);
//                break;
//        }
//        return httpRequest;
//    }

//    private HttpRequestBase requestWithBody(final HttpEntityEnclosingRequestBase httpRequest, final Request request) {
//        final byte[] body = request.getBody();
//        if (body != null) {
//            if (contentType == Protocol.HTTP_VPACK) {
//                httpRequest.setEntity(new ByteArrayEntity(body, CONTENT_TYPE_VPACK));
//            } else {
//                httpRequest.setEntity(new ByteArrayEntity(body, CONTENT_TYPE_APPLICATION_JSON_UTF8));
//            }
//        }
//        return httpRequest;
//    }

    public Response execute(final Request request) throws IOException {
        String url = buildUrl(request);
        HttpRequest<Buffer> httpRequest = client.request(requestTypeToHttpMethod(request.getRequestType()), url);
        if (contentType == Protocol.HTTP_VPACK) {
            httpRequest.putHeader("Accept", "application/x-velocypack");
        }
        addHeader(request, httpRequest);
        if (jwt != null) {
            httpRequest.putHeader(AUTHORIZATION, "Bearer " + jwt);
            if (LOGGER.isDebugEnabled()) {
                CURLLogger.log(url, request, null, jwt, util);
            }
        } else if (user != null) {
            // FIXME: credentials as class field
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user, password != null ? password : "");
            httpRequest.authentication(credentials);
            if (LOGGER.isDebugEnabled()) {
                CURLLogger.log(url, request, user, jwt, util);
            }
        }

        byte[] reqBody = request.getBody();
        Buffer buffer;
        if (reqBody != null) {
            buffer = Buffer.buffer(reqBody);
            if (contentType == Protocol.HTTP_VPACK) {
                httpRequest.putHeader(HttpHeaders.CONTENT_TYPE.toString(), CONTENT_TYPE_VPACK.toString());
            } else {
                httpRequest.putHeader(HttpHeaders.CONTENT_TYPE.toString(), CONTENT_TYPE_APPLICATION_JSON_UTF8.toString());
            }
        } else {
            buffer = Buffer.buffer();
        }
        HttpResponse<Buffer> bufferResponse;
        try {
            // FIXME: make async API
            bufferResponse = httpRequest.sendBuffer(buffer).toCompletionStage().toCompletableFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
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
        this.jwt = jwt;
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
        private HostnameVerifier hostnameVerifier;
        private Integer timeout;
//        private HttpRequestRetryHandler httpRequestRetryHandler;

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

        public Builder hostnameVerifier(final HostnameVerifier hostnameVerifier) {
            this.hostnameVerifier = hostnameVerifier;
            return this;
        }

        public Builder timeout(final Integer timeout) {
            this.timeout = timeout;
            return this;
        }

//        public Builder httpRequestRetryHandler(final HttpRequestRetryHandler httpRequestRetryHandler) {
//            this.httpRequestRetryHandler = httpRequestRetryHandler;
//            return this;
//        }

        public HttpConnection build() {
            return new HttpConnection(host, timeout, user, password, useSsl, sslContext, hostnameVerifier, util,
                    contentType, ttl, httpCookieSpec);
        }
    }

}
