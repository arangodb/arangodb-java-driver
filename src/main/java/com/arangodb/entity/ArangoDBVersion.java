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

package com.arangodb.entity;

/**
 * @author Mark Vollmary
 * 
 * @see <a href="https://docs.arangodb.com/current/HTTP/MiscellaneousFunctions/index.html#return-server-version">API
 *      Documentation</a>
 */
public class ArangoDBVersion implements Entity {

	private String server;
	private String version;
	private License license;

	public ArangoDBVersion() {
		super();
	}

	/**
	 * @return will always contain arango
	 */
	public String getServer() {
		return server;
	}

	/**
	 * @return the server version string. The string has the format "major.minor.sub". major and minor will be numeric,
	 *         and sub may contain a number or a textual version.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @return the license
	 */
	public License getLicense() {
		return license;
	}

}