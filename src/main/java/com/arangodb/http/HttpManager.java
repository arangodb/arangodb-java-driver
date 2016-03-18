/*
 * Copyright (C) 2012 tamtam180
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arangodb.http;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoException;
import com.arangodb.http.HttpRequestEntity.RequestType;
import com.arangodb.util.IOUtils;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * @author a-brandt
 * 
 */
public class HttpManager {

	private static final ContentType APPLICATION_JSON_UTF8 = ContentType.create("application/json", "utf-8");

	private static Logger logger = LoggerFactory.getLogger(HttpManager.class);

	private PoolingHttpClientConnectionManager cm;
	private CloseableHttpClient client;

	private ArangoConfigure configure;

	private HttpResponseEntity preDefinedResponse;

	private HttpMode httpMode = HttpMode.SYNC;

	private List<String> jobIds = new ArrayList<String>();

	private Map<String, InvocationObject> jobs = new HashMap<String, InvocationObject>();

	public enum HttpMode {
		SYNC, ASYNC, FIREANDFORGET
	}

	public HttpManager(ArangoConfigure configure) {
		this.configure = configure;
	}

	public ArangoConfigure getConfiguration() {
		return this.configure;
	}

	public void init() {
		// socket factory for HTTP
		ConnectionSocketFactory plainsf = new PlainConnectionSocketFactory();

		// socket factory for HTTPS
		SSLConnectionSocketFactory sslsf = initSSLConnectionSocketFactory();

		// register socket factories
		Registry<ConnectionSocketFactory> r = RegistryBuilder.<ConnectionSocketFactory> create()
				.register("http", plainsf).register("https", sslsf).build();

		// ConnectionManager
		cm = new PoolingHttpClientConnectionManager(r);
		cm.setDefaultMaxPerRoute(configure.getMaxPerConnection());
		cm.setMaxTotal(configure.getMaxTotalConnection());

		Builder custom = RequestConfig.custom();

		// RequestConfig
		if (configure.getConnectionTimeout() >= 0) {
			custom.setConnectTimeout(configure.getConnectionTimeout());
		}
		if (configure.getTimeout() >= 0) {
			custom.setConnectionRequestTimeout(configure.getTimeout());
			custom.setSocketTimeout(configure.getTimeout());
		}
		custom.setStaleConnectionCheckEnabled(configure.isStaleConnectionCheck());

		RequestConfig requestConfig = custom.build();

		HttpClientBuilder builder = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig);
		builder.setConnectionManager(cm);

		// KeepAlive Strategy
		ConnectionKeepAliveStrategy keepAliveStrategy = new ConnectionKeepAliveStrategy() {

			@Override
			public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
				return HttpManager.this.getKeepAliveDuration(response);
			}

		};
		builder.setKeepAliveStrategy(keepAliveStrategy);

		// Retry Handler
		builder.setRetryHandler(new DefaultHttpRequestRetryHandler(configure.getRetryCount(), false));

		// Proxy
		addProxyToBuilder(builder);

		// Client
		client = builder.build();
	}

	private long getKeepAliveDuration(HttpResponse response) {
		// Honor 'keep-alive' header
		HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
		while (it.hasNext()) {
			HeaderElement he = it.nextElement();
			String param = he.getName();
			String value = he.getValue();
			if (value != null && "timeout".equalsIgnoreCase(param)) {
				try {
					return Long.parseLong(value) * 1000L;
				} catch (NumberFormatException ignore) {
					// ignore this exception
				}
			}
		}
		// otherwise keep alive for 30 seconds
		return 30L * 1000L;
	}

	private void addProxyToBuilder(HttpClientBuilder builder) {
		if (configure.getProxyHost() != null && configure.getProxyPort() != 0) {
			HttpHost proxy = new HttpHost(configure.getProxyHost(), configure.getProxyPort(), "http");

			DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
			builder.setRoutePlanner(routePlanner);
		}
	}

	public void destroy() {
		if (cm != null) {
			cm.shutdown();
		}
		configure = null;
	}

	public HttpMode getHttpMode() {
		return httpMode;
	}

	public void setHttpMode(HttpMode httpMode) {
		this.httpMode = httpMode;
	}

	public HttpResponseEntity doGet(String url) throws ArangoException {
		return doGet(url, null);
	}

	public HttpResponseEntity doGet(String url, Map<String, Object> params) throws ArangoException {
		return doHeadGetDelete(RequestType.GET, url, null, params);
	}

	public HttpResponseEntity doGet(String url, Map<String, Object> headers, Map<String, Object> params)
			throws ArangoException {
		return doHeadGetDelete(RequestType.GET, url, headers, params);
	}

	public HttpResponseEntity doGet(
		String url,
		Map<String, Object> headers,
		Map<String, Object> params,
		String username,
		String password) throws ArangoException {
		return doHeadGetDelete(RequestType.GET, url, headers, params, username, password);
	}

	public HttpResponseEntity doHead(String url, Map<String, Object> params) throws ArangoException {
		return doHeadGetDelete(RequestType.HEAD, url, null, params);
	}

	public HttpResponseEntity doDelete(String url, Map<String, Object> params) throws ArangoException {
		return doHeadGetDelete(RequestType.DELETE, url, null, params);
	}

	public HttpResponseEntity doDelete(String url, Map<String, Object> headers, Map<String, Object> params)
			throws ArangoException {
		return doHeadGetDelete(RequestType.DELETE, url, headers, params);
	}

	public HttpResponseEntity doHeadGetDelete(
		RequestType type,
		String url,
		Map<String, Object> headers,
		Map<String, Object> params) throws ArangoException {
		return doHeadGetDelete(type, url, headers, params, null, null);
	}

	public HttpResponseEntity doHeadGetDelete(
		RequestType type,
		String url,
		Map<String, Object> headers,
		Map<String, Object> params,
		String username,
		String password) throws ArangoException {
		HttpRequestEntity requestEntity = new HttpRequestEntity();
		requestEntity.type = type;
		requestEntity.url = url;
		requestEntity.headers = headers;
		requestEntity.parameters = params;
		requestEntity.username = username;
		requestEntity.password = password;
		return execute(requestEntity);
	}

	public HttpResponseEntity doPost(
		String url,
		Map<String, Object> headers,
		Map<String, Object> params,
		String bodyText) throws ArangoException {
		return doPostPutPatch(RequestType.POST, url, headers, params, bodyText, null);
	}

	public HttpResponseEntity doPost(String url, Map<String, Object> params, String bodyText) throws ArangoException {
		return doPostPutPatch(RequestType.POST, url, null, params, bodyText, null);
	}

	public HttpResponseEntity doPost(String url, Map<String, Object> params, HttpEntity entity) throws ArangoException {
		return doPostPutPatch(RequestType.POST, url, null, params, null, entity);
	}

	public HttpResponseEntity doPostWithHeaders(
		String url,
		Map<String, Object> params,
		HttpEntity entity,
		Map<String, Object> headers,
		String body) throws ArangoException {
		return doPostPutPatch(RequestType.POST, url, headers, params, body, entity);
	}

	public HttpResponseEntity doPut(
		String url,
		Map<String, Object> headers,
		Map<String, Object> params,
		String bodyText) throws ArangoException {
		return doPostPutPatch(RequestType.PUT, url, headers, params, bodyText, null);
	}

	public HttpResponseEntity doPut(String url, Map<String, Object> params, String bodyText) throws ArangoException {
		return doPostPutPatch(RequestType.PUT, url, null, params, bodyText, null);
	}

	public HttpResponseEntity doPatch(
		String url,
		Map<String, Object> headers,
		Map<String, Object> params,
		String bodyText) throws ArangoException {
		return doPostPutPatch(RequestType.PATCH, url, headers, params, bodyText, null);
	}

	public HttpResponseEntity doPatch(String url, Map<String, Object> params, String bodyText) throws ArangoException {
		return doPostPutPatch(RequestType.PATCH, url, null, params, bodyText, null);
	}

	private HttpResponseEntity doPostPutPatch(
		RequestType type,
		String url,
		Map<String, Object> headers,
		Map<String, Object> params,
		String bodyText,
		HttpEntity entity) throws ArangoException {
		HttpRequestEntity requestEntity = new HttpRequestEntity();
		requestEntity.type = type;
		requestEntity.url = url;
		requestEntity.headers = headers;
		requestEntity.parameters = params;
		requestEntity.bodyText = bodyText;
		requestEntity.entity = entity;
		return execute(requestEntity);
	}

	/**
	 * Executes the request and handles connect exceptions
	 * 
	 * @param requestEntity
	 *            the request
	 * @return the response of the request
	 * 
	 * @throws ArangoException
	 */
	public HttpResponseEntity execute(HttpRequestEntity requestEntity) throws ArangoException {
		int retries = 0;
		int connectRetryCount = configure.getConnectRetryCount();

		while (true) {
			try {
				return executeInternal(configure.getBaseUrl(), requestEntity);
			} catch (SocketException ex) {
				retries++;
				if (connectRetryCount > 0 && retries > connectRetryCount) {
					logger.error(ex.getMessage(), ex);
					throw new ArangoException(ex);
				}

				if (configure.hasFallbackHost()) {
					configure.changeCurrentHost();
				}

				logger.warn(ex.getMessage(), ex);
				try {
					// 1000 milliseconds is one second.
					Thread.sleep(configure.getConnectRetryWait());
				} catch (InterruptedException iex) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	/**
	 * Executes the request
	 * 
	 * @param requestEntity
	 *            the request
	 * @return the response of the request
	 * @throws ArangoException
	 */
	private HttpResponseEntity executeInternal(String baseUrl, HttpRequestEntity requestEntity)
			throws ArangoException, SocketException {

		String url = buildUrl(baseUrl, requestEntity);

		logRequest(requestEntity, url);

		HttpRequestBase request = buildHttpRequestBase(requestEntity, url);

		// common-header
		String userAgent = "Mozilla/5.0 (compatible; ArangoDB-JavaDriver/1.1; +http://mt.orz.at/)";
		request.setHeader("User-Agent", userAgent);

		addOptionalHeaders(requestEntity, request);

		addHttpModeHeader(request);

		// Basic Auth
		Credentials credentials = addCredentials(requestEntity, request);

		// CURL/HTTP Logger
		if (configure.isEnableCURLLogger()) {
			CURLLogger.log(url, requestEntity, credentials);
		}

		HttpResponseEntity responseEntity = null;
		if (preDefinedResponse != null) {
			responseEntity = preDefinedResponse;
		} else {
			HttpResponse response = executeRequest(request);
			if (response != null) {
				try {
					responseEntity = buildHttpResponseEntity(requestEntity, response);
				} catch (IOException e) {
					throw new ArangoException(e);
				}

				if (this.getHttpMode().equals(HttpMode.ASYNC)) {
					Map<String, String> map = responseEntity.getHeaders();
					this.addJob(map.get("X-Arango-Async-Id"), this.getCurrentObject());
				} else if (this.getHttpMode().equals(HttpMode.FIREANDFORGET)) {
					responseEntity = null;
				}
			}
		}

		return responseEntity;
	}

	private HttpResponse executeRequest(HttpRequestBase request) throws SocketException, ArangoException {
		try {
			return client.execute(request);
		} catch (SocketException ex) {
			// catch SocketException before IOException
			throw ex;
		} catch (ClientProtocolException e) {
			throw new ArangoException(e);
		} catch (IOException e) {
			throw new ArangoException(e);
		}
	}

	private HttpResponseEntity buildHttpResponseEntity(HttpRequestEntity requestEntity, HttpResponse response)
			throws IOException {
		HttpResponseEntity responseEntity = new HttpResponseEntity();

		// http status
		StatusLine status = response.getStatusLine();
		responseEntity.statusCode = status.getStatusCode();
		responseEntity.statusPhrase = status.getReasonPhrase();

		logger.debug("[RES]http-{}: statusCode={}", requestEntity.type, responseEntity.statusCode);

		// ヘッダの処理
		// // TODO etag特殊処理は削除する。
		Header etagHeader = response.getLastHeader("etag");
		if (etagHeader != null) {
			responseEntity.etag = Long.parseLong(etagHeader.getValue().replace("\"", ""));
		}
		// ヘッダをMapに変換する
		responseEntity.headers = new TreeMap<String, String>();
		for (Header header : response.getAllHeaders()) {
			responseEntity.headers.put(header.getName(), header.getValue());
		}

		// レスポンスの取得
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			Header contentType = entity.getContentType();
			if (contentType != null) {
				responseEntity.contentType = contentType.getValue();
				if (responseEntity.isDumpResponse()) {
					responseEntity.stream = entity.getContent();
					logger.debug("[RES]http-{}: stream, {}", requestEntity.type, contentType.getValue());
				}
			}
			// Close stream in this method.
			if (responseEntity.stream == null) {
				responseEntity.text = IOUtils.toString(entity.getContent());
				logger.debug("[RES]http-{}: text={}", requestEntity.type, responseEntity.text);
			}
		}
		return responseEntity;
	}

	private void addHttpModeHeader(HttpRequestBase request) {
		if (this.getHttpMode().equals(HttpMode.ASYNC)) {
			request.addHeader("x-arango-async", "store");
		} else if (this.getHttpMode().equals(HttpMode.FIREANDFORGET)) {
			request.addHeader("x-arango-async", "true");
		}
	}

	private Credentials addCredentials(HttpRequestEntity requestEntity, HttpRequestBase request)
			throws ArangoException {
		Credentials credentials = null;
		if (requestEntity.username != null && requestEntity.password != null) {
			credentials = new UsernamePasswordCredentials(requestEntity.username, requestEntity.password);
		} else if (configure.getUser() != null && configure.getPassword() != null) {
			credentials = new UsernamePasswordCredentials(configure.getUser(), configure.getPassword());
		}
		if (credentials != null) {
			BasicScheme basicScheme = new BasicScheme();
			try {
				request.addHeader(basicScheme.authenticate(credentials, request, null));
			} catch (AuthenticationException e) {
				throw new ArangoException(e);
			}
		}
		return credentials;
	}

	private void addOptionalHeaders(HttpRequestEntity requestEntity, HttpRequestBase request) {
		if (requestEntity.headers != null) {
			for (Entry<String, Object> keyValue : requestEntity.headers.entrySet()) {
				request.setHeader(keyValue.getKey(), keyValue.getValue().toString());
			}
		}
	}

	private HttpRequestBase buildHttpRequestBase(HttpRequestEntity requestEntity, String url) {
		HttpRequestBase request;
		switch (requestEntity.type) {
		case POST:
			HttpPost post = new HttpPost(url);
			configureBodyParams(requestEntity, post);
			request = post;
			break;
		case PUT:
			HttpPut put = new HttpPut(url);
			configureBodyParams(requestEntity, put);
			request = put;
			break;
		case PATCH:
			HttpPatch patch = new HttpPatch(url);
			configureBodyParams(requestEntity, patch);
			request = patch;
			break;
		case HEAD:
			request = new HttpHead(url);
			break;
		case DELETE:
			request = new HttpDelete(url);
			break;
		case GET:
		default:
			request = new HttpGet(url);
			break;
		}
		return request;
	}

	private void logRequest(HttpRequestEntity requestEntity, String url) {
		if (logger.isDebugEnabled()) {
			if (requestEntity.type == RequestType.POST || requestEntity.type == RequestType.PUT
					|| requestEntity.type == RequestType.PATCH) {
				logger.debug("[REQ]http-{}: url={}, headers={}, body={}",
					new Object[] { requestEntity.type, url, requestEntity.headers, requestEntity.bodyText });
			} else {
				logger.debug("[REQ]http-{}: url={}, headers={}",
					new Object[] { requestEntity.type, url, requestEntity.headers });
			}
		}
	}

	public static String buildUrl(String baseUrl, HttpRequestEntity requestEntity) {
		if (requestEntity.parameters != null && !requestEntity.parameters.isEmpty()) {
			String paramString = URLEncodedUtils.format(toList(requestEntity.parameters), "utf-8");
			if (requestEntity.url.contains("?")) {
				return baseUrl + requestEntity.url + "&" + paramString;
			} else {
				return baseUrl + requestEntity.url + "?" + paramString;
			}
		}
		return baseUrl + requestEntity.url;
	}

	private static List<NameValuePair> toList(Map<String, Object> parameters) {
		ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>(parameters.size());
		for (Entry<String, Object> param : parameters.entrySet()) {
			if (param.getValue() != null) {
				paramList.add(new BasicNameValuePair(param.getKey(), param.getValue().toString()));
			}
		}
		return paramList;
	}

	public static void configureBodyParams(HttpRequestEntity requestEntity, HttpEntityEnclosingRequestBase request) {

		if (requestEntity.entity != null) {
			request.setEntity(requestEntity.entity);
		} else if (requestEntity.bodyText != null) {
			request.setEntity(new StringEntity(requestEntity.bodyText, APPLICATION_JSON_UTF8));
		}

	}

	public static boolean is400Error(ArangoException e) {
		return e.getCode() == HttpStatus.SC_BAD_REQUEST;
	}

	public static boolean is404Error(ArangoException e) {
		return e.getCode() == HttpStatus.SC_NOT_FOUND;
	}

	public static boolean is412Error(ArangoException e) {
		return e.getCode() == HttpStatus.SC_PRECONDITION_FAILED;
	}

	public static boolean is200(HttpResponseEntity res) {
		return res.getStatusCode() == HttpStatus.SC_OK;
	}

	public static boolean is400Error(HttpResponseEntity res) {
		return res.getStatusCode() == HttpStatus.SC_BAD_REQUEST;
	}

	public static boolean is404Error(HttpResponseEntity res) {
		return res.getStatusCode() == HttpStatus.SC_NOT_FOUND;
	}

	public static boolean is412Error(HttpResponseEntity res) {
		return res.getStatusCode() == HttpStatus.SC_PRECONDITION_FAILED;
	}

	public CloseableHttpClient getClient() {
		return client;
	}

	public InvocationObject getCurrentObject() {
		// do nothing here (used in BatchHttpManager)
		return null;
	}

	public void setCurrentObject(InvocationObject currentObject) {
		// do nothing here (used in BatchHttpManager)
	}

	public void setPreDefinedResponse(HttpResponseEntity preDefinedResponse) {
		this.preDefinedResponse = preDefinedResponse;
	}

	public List<String> getJobIds() {
		return jobIds;
	}

	public Map<String, InvocationObject> getJobs() {
		return jobs;
	}

	public void addJob(String jobId, InvocationObject invocationObject) {
		jobIds.add(jobId);
		jobs.put(jobId, invocationObject);
	}

	public String getLastJobId() {
		return jobIds.isEmpty() ? null : jobIds.get(jobIds.size() - 1);
	}

	public void resetJobs() {
		this.jobIds = new ArrayList<String>();
		this.jobs.clear();

	}

	private SSLConnectionSocketFactory initSSLConnectionSocketFactory() {
		SSLConnectionSocketFactory sslsf;
		if (configure.getSslContext() != null) {
			sslsf = new SSLConnectionSocketFactory(configure.getSslContext());
		} else {
			sslsf = new SSLConnectionSocketFactory(SSLContexts.createSystemDefault());
		}
		return sslsf;
	}

}
