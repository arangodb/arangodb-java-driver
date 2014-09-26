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

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * @since 1.4.0
 */
public class ReplicationLoggerStateEntity extends BaseEntity {

	ReplicationState state;
	String serverVersion;
	String serverId;
	List<Client> clients;

	public ReplicationState getState() {
		return state;
	}

	public String getServerVersion() {
		return serverVersion;
	}

	public String getServerId() {
		return serverId;
	}

	public List<Client> getClients() {
		return clients;
	}

	public void setState(ReplicationState state) {
		this.state = state;
	}

	public void setServerVersion(String serverVersion) {
		this.serverVersion = serverVersion;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public void setClients(List<Client> clients) {
		this.clients = clients;
	}

	public static class Client implements Serializable {
		String serverId;
		Long lastServedTick;
		Date time;
		public String getServerId() {
			return serverId;
		}
		public Long getLastServedTick() {
			return lastServedTick;
		}
		public Date getTime() {
			return time;
		}
		public void setServerId(String serverId) {
			this.serverId = serverId;
		}
		public void setLastServedTick(Long lastServedTick) {
			this.lastServedTick = lastServedTick;
		}
		public void setTime(Date time) {
			this.time = time;
		}
	}
	
}
