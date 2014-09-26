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
public class ReplicationApplierStateEntity extends BaseEntity {

	ReplicationApplierState state;
	String serverVersion;
	String serverId;
	String endpoint;
	String database;
	
	public ReplicationApplierState getState() {
		return state;
	}
	public String getServerVersion() {
		return serverVersion;
	}
	public String getServerId() {
		return serverId;
	}
	public String getEndpoint() {
		return endpoint;
	}
	public String getDatabase() {
		return database;
	}
	public void setState(ReplicationApplierState state) {
		this.state = state;
	}
	public void setServerVersion(String serverVersion) {
		this.serverVersion = serverVersion;
	}
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	public void setDatabase(String database) {
		this.database = database;
	}
	
}
