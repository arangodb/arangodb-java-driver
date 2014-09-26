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

import java.io.InputStream;
import java.util.Map;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class HttpResponseEntity {
	
	int statusCode;
	String statusPhrase;
	String text;
	InputStream stream;
	long etag = -1;
	Map<String, String> headers;
	
	String contentType;

	/**
	 * @return
	 * @since 1.4.0
	 */
	public boolean isJsonResponse() {
		return (contentType != null && contentType.startsWith("application/json"));
	}
	
	/**
	 * @return
	 * @since 1.4.0
	 */
	public boolean isDumpResponse() {
		return (contentType != null && contentType.startsWith("application/x-arango-dump"));
	}
	
	/**
	 * @return
	 * @since 1.4.0
	 */
	public boolean isTextResponse() {
		return (contentType != null && contentType.startsWith("text/plain"));
	}
	
	public int getStatusCode() {
		return statusCode;
	}
	public InputStream getStream() {
		return stream;
	}
	public void setStream(InputStream stream) {
		this.stream = stream;
	}
	public String getStatusPhrase() {
		return statusPhrase;
	}
	public String getText() {
		return text;
	}
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	public void setStatusPhrase(String statusPhrase) {
		this.statusPhrase = statusPhrase;
	}
	public void setText(String text) {
		this.text = text;
	}
	public long getEtag() {
		return etag;
	}
	public void setEtag(long etag) {
		this.etag = etag;
	}
	public Map<String, String> getHeaders() {
		return headers;
	}
	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
}
