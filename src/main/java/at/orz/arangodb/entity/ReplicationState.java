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

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ReplicationState implements Serializable {

	boolean running;
	long lastLogTick;
	long totalEvents;
	Date time;

	public boolean isRunning() {
		return running;
	}
	public long getLastLogTick() {
		return lastLogTick;
	}
	public long getTotalEvents() {
		return totalEvents;
	}
	public Date getTime() {
		return time;
	}
	public void setRunning(boolean running) {
		this.running = running;
	}
	public void setLastLogTick(long lastLogTick) {
		this.lastLogTick = lastLogTick;
	}
	public void setTotalEvents(long totalEvents) {
		this.totalEvents = totalEvents;
	}
	public void setTime(Date time) {
		this.time = time;
	}

}
