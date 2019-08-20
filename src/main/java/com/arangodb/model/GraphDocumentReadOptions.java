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

import com.arangodb.velocypack.annotations.Expose;

/**
 * @author Mark Vollmary
 */
public class GraphDocumentReadOptions {

	private String ifNoneMatch;
	private String ifMatch;
	private boolean catchException;
	@Expose(serialize = false)
	private Boolean allowDirtyRead;

	public GraphDocumentReadOptions() {
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
	public GraphDocumentReadOptions ifNoneMatch(final String ifNoneMatch) {
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
	public GraphDocumentReadOptions ifMatch(final String ifMatch) {
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
	public GraphDocumentReadOptions catchException(final boolean catchException) {
		this.catchException = catchException;
		return this;
	}

	/**
	 * @param allowDirtyRead Set to {@code true} allows reading from followers in an active-failover setup.
	 * @return options
	 * @see <a href="https://docs.arangodb.com/current/Manual/Administration/ActiveFailover/#reading-from-follower">API
	 * Documentation</a>
	 * @since ArangoDB 3.4.0
	 */
	public GraphDocumentReadOptions allowDirtyRead(final Boolean allowDirtyRead) {
		this.allowDirtyRead = allowDirtyRead;
		return this;
	}

	public Boolean getAllowDirtyRead() {
		return allowDirtyRead;
	}

}
