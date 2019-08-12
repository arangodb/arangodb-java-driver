/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
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

package com.arangodb.model;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 * @see <a href= "https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#read-document-header">API
 * Documentation</a>
 */
public class DocumentExistsOptions {

	private String ifNoneMatch;
	private String ifMatch;
	private boolean catchException;
	private String streamTransactionId;

	public DocumentExistsOptions() {
		super();
		catchException = true;
	}

	public String getIfNoneMatch() {
		return ifNoneMatch;
	}

	/**
	 * @param ifNoneMatch document revision must not contain If-None-Match
	 * @return options
	 */
	public DocumentExistsOptions ifNoneMatch(final String ifNoneMatch) {
		this.ifNoneMatch = ifNoneMatch;
		return this;
	}

	public String getIfMatch() {
		return ifMatch;
	}

	/**
	 * @param ifMatch document revision must contain If-Match
	 * @return options
	 */
	public DocumentExistsOptions ifMatch(final String ifMatch) {
		this.ifMatch = ifMatch;
		return this;
	}

	public boolean isCatchException() {
		return catchException;
	}

	/**
	 * @param catchException whether or not catch possible thrown exceptions
	 * @return options
	 */
	public DocumentExistsOptions catchException(final boolean catchException) {
		this.catchException = catchException;
		return this;
	}

	public String getStreamTransactionId() {
		return streamTransactionId;
	}

	/**
	 * @param streamTransactionId If set, the operation will be executed within the transaction.
	 * @return options
	 * @since ArangoDB 3.5.0
	 */
	public DocumentExistsOptions streamTransactionId(final String streamTransactionId) {
		this.streamTransactionId = streamTransactionId;
		return this;
	}

}
