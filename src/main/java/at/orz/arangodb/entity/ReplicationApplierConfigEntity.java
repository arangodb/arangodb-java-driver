/*
 * Copyright (C) 2012,2013 tamtam180
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

package at.orz.arangodb.entity;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ReplicationApplierConfigEntity extends BaseEntity {

	String endpoint;
	String database;
	String username;
	String password;
	Integer maxConnectRetries;
	Integer connectTimeout;
	Integer requestTimeout;
	Integer chunkSize;
	Boolean autoStart;
	Boolean adaptivePolling;
	
	public String getEndpoint() {
		return endpoint;
	}
	public String getDatabase() {
		return database;
	}
	public String getUsername() {
		return username;
	}
	public String getPassword() {
		return password;
	}
	public Integer getMaxConnectRetries() {
		return maxConnectRetries;
	}
	public Integer getConnectTimeout() {
		return connectTimeout;
	}
	public Integer getRequestTimeout() {
		return requestTimeout;
	}
	public Integer getChunkSize() {
		return chunkSize;
	}
	public Boolean getAutoStart() {
		return autoStart;
	}
	public Boolean getAdaptivePolling() {
		return adaptivePolling;
	}
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	public void setDatabase(String database) {
		this.database = database;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public void setMaxConnectRetries(Integer maxConnectRetries) {
		this.maxConnectRetries = maxConnectRetries;
	}
	public void setConnectTimeout(Integer connectTimeout) {
		this.connectTimeout = connectTimeout;
	}
	public void setRequestTimeout(Integer requestTimeout) {
		this.requestTimeout = requestTimeout;
	}
	public void setChunkSize(Integer chunkSize) {
		this.chunkSize = chunkSize;
	}
	public void setAutoStart(Boolean autoStart) {
		this.autoStart = autoStart;
	}
	public void setAdaptivePolling(Boolean adaptivePolling) {
		this.adaptivePolling = adaptivePolling;
	}
	
}
