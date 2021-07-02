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
import com.arangodb.internal.util.IOUtils;
import com.arangodb.internal.util.ResponseUtils;
import com.arangodb.util.ArangoSerialization;
import com.arangodb.util.ArangoSerializer.Options;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.Response;
import org.apache.hc.client5.http.ConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.auth.BasicScheme;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.ssl.HttpsSupport;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.message.BasicHeaderElementIterator;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * @author Mark Vollmary
 */
public class HttpConnection implements Connection {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpCommunication.class);
    private static final ContentType CONTENT_TYPE_APPLICATION_JSON_UTF8 = ContentType.create("application/json",
            "utf-8");
    private static final ContentType CONTENT_TYPE_VPACK = ContentType.create("application/x-velocypack");

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
        private HostnameVerifier hostnameVerifier;
        private Integer timeout;
        private HttpRequestRetryStrategy httpRequestRetryHandler;

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

        public Builder hostnameVerifier(final HostnameVerifier hostnameVerifier) {
            this.hostnameVerifier = hostnameVerifier;
            return this;
        }

        public Builder timeout(final Integer timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder httpRequestRetryHandler(final HttpRequestRetryStrategy httpRequestRetryHandler) {
            this.httpRequestRetryHandler = httpRequestRetryHandler;
            return this;
        }

        public HttpConnection build() {
            return new HttpConnection(host, timeout, user, password, useSsl, sslContext, hostnameVerifier, util,
                    contentType, ttl, httpCookieSpec, httpRequestRetryHandler);
        }
    }

    private final PoolingHttpClientConnectionManager cm;
    private final CloseableHttpClient client;
    private final ArangoSerialization util;
    private final Boolean useSsl;
    private final Protocol contentType;
    private final HostDescription host;
    private final HttpClientContext authCtx;
    private final Credentials credentials;

    private HttpConnection(final HostDescription host, final Integer timeout, final String user, final String password,
                           final Boolean useSsl, final SSLContext sslContext, final HostnameVerifier hostnameVerifier, final ArangoSerialization util, final Protocol contentType,
                           final Long ttl, final String httpCookieSpec, final HttpRequestRetryStrategy httpRequestRetryHandler) {
        super();
        this.host = host;
        this.useSsl = useSsl;
        this.util = util;
        this.contentType = contentType;

        PoolingHttpClientConnectionManagerBuilder cmBuilder = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnPerRoute(1)
                .setMaxConnTotal(1);
        if (Boolean.TRUE == useSsl) {
            cmBuilder.setSSLSocketFactory(new SSLConnectionSocketFactory(
                    sslContext != null ? sslContext : SSLContexts.createSystemDefault(),
                    hostnameVerifier != null ? hostnameVerifier : HttpsSupport.getDefaultHostnameVerifier()
            ));
        }
        if (ttl != null) {
            cmBuilder.setConnectionTimeToLive(TimeValue.ofMilliseconds(ttl));
        }
        cm = cmBuilder.build();

        final RequestConfig.Builder requestConfig = RequestConfig.custom();
        if (timeout != null && timeout >= 0) {
            requestConfig.setConnectTimeout(Timeout.of(timeout, TimeUnit.MILLISECONDS));
            requestConfig.setConnectionRequestTimeout(Timeout.of(timeout, TimeUnit.MILLISECONDS));
            requestConfig.setResponseTimeout(Timeout.of(timeout, TimeUnit.MILLISECONDS));
        }

        if (httpCookieSpec != null && httpCookieSpec.length() > 1) {
            requestConfig.setCookieSpec(httpCookieSpec);
        }

        final ConnectionKeepAliveStrategy keepAliveStrategy = (response, context) -> TimeValue.ofSeconds(HttpConnection.this.getKeepAliveDuration(response));
        final HttpClientBuilder builder = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig.build())
                .setConnectionManager(cm).setKeepAliveStrategy(keepAliveStrategy)
                .setRetryStrategy(httpRequestRetryHandler != null ? httpRequestRetryHandler : new DefaultHttpRequestRetryStrategy());

        client = builder.build();
        String pwd = password != null ? password : "";
        credentials = user != null ? new UsernamePasswordCredentials(user, pwd.toCharArray()) : null;
        authCtx = createAuthCtx();
    }

    private long getKeepAliveDuration(final HttpResponse response) {
        final BasicHeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HeaderElements.KEEP_ALIVE));
        while (it.hasNext()) {
            final HeaderElement he = it.next();
            final String param = he.getName();
            final String value = he.getValue();
            if (value != null && "timeout".equalsIgnoreCase(param)) {
                try {
                    return Long.parseLong(value);
                } catch (final NumberFormatException ignore) {
                }
            }
        }
        return 30L;
    }

    @Override
    public void close() throws IOException {
        cm.close();
        client.close();
    }

    private static String buildUrl(final String baseUrl, final Request request) {
        final StringBuilder sb = new StringBuilder().append(baseUrl);
        final String database = request.getDatabase();
        if (database != null && !database.isEmpty()) {
            sb.append("/_db/").append(database);
        }
        sb.append(request.getRequest());
        if (!request.getQueryParam().isEmpty()) {
            if (request.getRequest().contains("?")) {
                sb.append("&");
            } else {
                sb.append("?");
            }
            final String paramString = URLEncodedUtils.format(toList(request.getQueryParam()), StandardCharsets.UTF_8);
            sb.append(paramString);
        }
        return sb.toString();
    }

    private BasicClassicHttpRequest buildHttpRequestBase(final Request request, final String url) {
        final BasicClassicHttpRequest httpRequest;
        switch (request.getRequestType()) {
            case POST:
                httpRequest = requestWithBody(new HttpPost(url), request);
                break;
            case PUT:
                httpRequest = requestWithBody(new HttpPut(url), request);
                break;
            case PATCH:
                httpRequest = requestWithBody(new HttpPatch(url), request);
                break;
            case DELETE:
                httpRequest = requestWithBody(new HttpDelete(url), request);
                break;
            case HEAD:
                httpRequest = new HttpHead(url);
                break;
            case GET:
            default:
                httpRequest = new HttpGet(url);
                break;
        }
        return httpRequest;
    }

    private BasicClassicHttpRequest requestWithBody(final BasicClassicHttpRequest httpRequest, final Request request) {
        final VPackSlice body = request.getBody();
        if (body != null) {
            if (contentType == Protocol.HTTP_VPACK) {
                httpRequest.setEntity(new ByteArrayEntity(
                        Arrays.copyOfRange(body.getBuffer(), body.getStart(), body.getStart() + body.getByteSize()),
                        CONTENT_TYPE_VPACK));
            } else {
                httpRequest.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_APPLICATION_JSON_UTF8));
            }
        }
        return httpRequest;
    }

    private String buildBaseUrl(final HostDescription host) {
        return (Boolean.TRUE == useSsl ? "https://" : "http://") + host.getHost() + ":" + host.getPort();
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
        final String url = buildUrl(buildBaseUrl(host), request);
        final BasicClassicHttpRequest httpRequest = buildHttpRequestBase(request, url);
        httpRequest.setHeader("User-Agent", "Mozilla/5.0 (compatible; ArangoDB-JavaDriver/1.1; +http://mt.orz.at/)");
        if (contentType == Protocol.HTTP_VPACK) {
            httpRequest.setHeader("Accept", "application/x-velocypack");
        }
        addHeader(request, httpRequest);
        if (LOGGER.isDebugEnabled()) {
            CURLLogger.log(url, request, credentials, util);
        }
        Response response;
        response = buildResponse(client.execute(httpRequest, authCtx));
        checkError(response);
        return response;
    }

    private static void addHeader(final Request request, final BasicClassicHttpRequest httpRequest) {
        for (final Entry<String, String> header : request.getHeaderParam().entrySet()) {
            httpRequest.addHeader(header.getKey(), header.getValue());
        }
    }

    public HttpClientContext createAuthCtx() {
        final HttpClientContext localContext = HttpClientContext.create();
        if (credentials != null) {
            BasicScheme auth = new BasicScheme(StandardCharsets.UTF_8);
            auth.initPreemptive(credentials);
            HttpHost target = new HttpHost(Boolean.TRUE == useSsl ? "https" : "http", host.getHost(), host.getPort());
            localContext.resetAuthExchange(target, auth);
        }
        return localContext;
    }

    public Response buildResponse(final CloseableHttpResponse httpResponse)
            throws UnsupportedOperationException, IOException {
        final Response response = new Response();
        response.setResponseCode(httpResponse.getCode());
        final HttpEntity entity = httpResponse.getEntity();
        if (entity != null && entity.getContent() != null) {
            if (contentType == Protocol.HTTP_VPACK) {
                final byte[] content = IOUtils.toByteArray(entity.getContent());
                if (content.length > 0) {
                    response.setBody(new VPackSlice(content));
                }
            } else {
                final String content = IOUtils.toString(entity.getContent());
                if (!content.isEmpty()) {
                    response.setBody(
                            util.serialize(content, new Options().stringAsJson(true).serializeNullValues(true)));
                }
            }
        }
        final Header[] headers = httpResponse.getHeaders();
        final Map<String, String> meta = response.getMeta();
        for (final Header header : headers) {
            meta.put(header.getName(), header.getValue());
        }
        return response;
    }

    protected void checkError(final Response response) throws ArangoDBException {
        ResponseUtils.checkError(util, response);
    }

}
