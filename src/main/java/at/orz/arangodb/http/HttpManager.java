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

package at.orz.arangodb.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.orz.arangodb.ArangoConfigure;
import at.orz.arangodb.ArangoException;
import at.orz.arangodb.http.HttpRequestEntity.RequestType;
import at.orz.arangodb.util.IOUtils;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * 
 */
public class HttpManager {

	private static final ContentType APPLICATION_JSON_UTF8 = ContentType.create("application/json", "utf-8");

	private Logger logger = LoggerFactory.getLogger(HttpManager.class);

	private PoolingClientConnectionManager cm;
	private DefaultHttpClient client;

	private ArangoConfigure configure;

	public HttpManager(ArangoConfigure configure) {
		this.configure = configure;
	}

	public void init() {
		// ConnectionManager
		cm = new PoolingClientConnectionManager();
		cm.setDefaultMaxPerRoute(configure.getMaxPerConnection());
		cm.setMaxTotal(configure.getMaxTotalConnection());
		// Params
		HttpParams params = new BasicHttpParams();
		if (configure.getConnectionTimeout() >= 0) {
			HttpConnectionParams.setConnectionTimeout(params, configure.getConnectionTimeout());
		}
		if (configure.getTimeout() >= 0) {
			HttpConnectionParams.setSoTimeout(params, configure.getTimeout());
		}

		HttpConnectionParams.setStaleCheckingEnabled(params, configure.isStaleConnectionCheck());

		// Client
		client = new DefaultHttpClient(cm, params);
		// TODO KeepAlive Strategy

		// Proxy
		if (configure.getProxyHost() != null && configure.getProxyPort() != 0) {
			HttpHost proxy = new HttpHost(configure.getProxyHost(), configure.getProxyPort(), "http");
			client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		}

		// Basic Auth
		// if (configure.getUser() != null && configure.getPassword() != null) {
		// AuthScope scope = AuthScope.ANY; // TODO
		// this.credentials = new
		// UsernamePasswordCredentials(configure.getUser(),
		// configure.getPassword());
		// client.getCredentialsProvider().setCredentials(scope, credentials);
		// }

		// Retry Handler
		client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(configure.getRetryCount(), false));

	}

	public void destroy() {
		if (cm != null) {
			cm.shutdown();
		}
		configure = null;
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

	public HttpResponseEntity doGet(String url, Map<String, Object> headers, Map<String, Object> params,
			String username, String password) throws ArangoException {
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

	public HttpResponseEntity doHeadGetDelete(RequestType type, String url, Map<String, Object> headers,
			Map<String, Object> params) throws ArangoException {
		return doHeadGetDelete(type, url, headers, params, null, null);
	}

	public HttpResponseEntity doHeadGetDelete(RequestType type, String url, Map<String, Object> headers,
			Map<String, Object> params, String username, String password) throws ArangoException {
		HttpRequestEntity requestEntity = new HttpRequestEntity();
		requestEntity.type = type;
		requestEntity.url = url;
		requestEntity.headers = headers;
		requestEntity.parameters = params;
		requestEntity.username = username;
		requestEntity.password = password;
		return execute(requestEntity);
	}

	public HttpResponseEntity doPost(String url, Map<String, Object> headers, Map<String, Object> params,
			String bodyText) throws ArangoException {
		return doPostPutPatch(RequestType.POST, url, headers, params, bodyText, null);
	}

	public HttpResponseEntity doPost(String url, Map<String, Object> params, String bodyText) throws ArangoException {
		return doPostPutPatch(RequestType.POST, url, null, params, bodyText, null);
	}

	public HttpResponseEntity doPost(String url, Map<String, Object> params, HttpEntity entity) throws ArangoException {
		return doPostPutPatch(RequestType.POST, url, null, params, null, entity);
	}

  public HttpResponseEntity doPostWithHeaders(String url, Map<String, Object> params, HttpEntity entity,
                                              Map<String, Object> headers, String body) throws ArangoException {
    return doPostPutPatch(RequestType.POST, url, headers, params, body, entity);
  }

	public HttpResponseEntity doPut(String url, Map<String, Object> headers, Map<String, Object> params, String bodyText)
			throws ArangoException {
		return doPostPutPatch(RequestType.PUT, url, headers, params, bodyText, null);
	}

	public HttpResponseEntity doPut(String url, Map<String, Object> params, String bodyText) throws ArangoException {
		return doPostPutPatch(RequestType.PUT, url, null, params, bodyText, null);
	}

	public HttpResponseEntity doPatch(String url, Map<String, Object> headers, Map<String, Object> params,
			String bodyText) throws ArangoException {
		return doPostPutPatch(RequestType.PATCH, url, headers, params, bodyText, null);
	}

	public HttpResponseEntity doPatch(String url, Map<String, Object> params, String bodyText) throws ArangoException {
		return doPostPutPatch(RequestType.PATCH, url, null, params, bodyText, null);
	}

	private HttpResponseEntity doPostPutPatch(RequestType type, String url, Map<String, Object> headers,
			Map<String, Object> params, String bodyText, HttpEntity entity) throws ArangoException {
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
	 * 
	 * @param requestEntity
	 * @return
	 * @throws ArangoException
	 */
	public HttpResponseEntity execute(HttpRequestEntity requestEntity) throws ArangoException {

		String url = buildUrl(requestEntity);

		if (logger.isDebugEnabled()) {
			if (requestEntity.type == RequestType.POST || requestEntity.type == RequestType.PUT
					|| requestEntity.type == RequestType.PATCH) {
				logger.debug("[REQ]http-{}: url={}, headers={}, body={}", new Object[] { requestEntity.type, url,
						requestEntity.headers, requestEntity.bodyText });
			} else {
				logger.debug("[REQ]http-{}: url={}, headers={}", new Object[] { requestEntity.type, url,
						requestEntity.headers });
			}
		}

		HttpRequestBase request = null;
		switch (requestEntity.type) {
		case GET:
			request = new HttpGet(url);
			break;
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
		}

		// common-header
		String userAgent = "Mozilla/5.0 (compatible; ArangoDB-JavaDriver/1.0; +http://mt.orz.at/)"; // TODO:
																									// 定数化
		request.setHeader("User-Agent", userAgent);
		// request.setHeader("Content-Type", "binary/octet-stream");

		// optinal-headers
		if (requestEntity.headers != null) {
      for (Entry<String, Object> keyValue : requestEntity.headers.entrySet()) {
      	request.setHeader(keyValue.getKey(), keyValue.getValue().toString());
			}
		}

		// Basic Auth
		Credentials credentials = null;
		if (requestEntity.username != null && requestEntity.password != null) {
			credentials = new UsernamePasswordCredentials(requestEntity.username, requestEntity.password);
		} else if (configure.getUser() != null && configure.getPassword() != null) {
			credentials = new UsernamePasswordCredentials(configure.getUser(), configure.getPassword());
		}
    if (credentials != null) {
			request.addHeader(BasicScheme.authenticate(credentials, "US-ASCII", false));
		}

		// CURL/httpie Logger
		if (configure.isEnableCURLLogger()) {
			CURLLogger.log(url, requestEntity, userAgent, credentials);
		}
    HttpResponse response = null;
		try {
      response = client.execute(request);
			if (response == null) {
				return null;
			}

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

		} catch (ClientProtocolException e) {
			throw new ArangoException(e);
		} catch (IOException e) {
			throw new ArangoException(e);
		}

	}

	public static String buildUrl(HttpRequestEntity requestEntity) {
		if (requestEntity.parameters != null && !requestEntity.parameters.isEmpty()) {
			String paramString = URLEncodedUtils.format(toList(requestEntity.parameters), "utf-8");
			if (requestEntity.url.contains("?")) {
				return requestEntity.url + "&" + paramString;
			} else {
				return requestEntity.url + "?" + paramString;
			}
		}
		return requestEntity.url;
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

	public DefaultHttpClient getClient() {
		return client;
	}

}
