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

package at.orz.arangodb.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class AdminLogEntity extends BaseEntity {

	int totalAmount;
	List<LogEntry> logs;
	
	public int getTotalAmount() {
		return totalAmount;
	}
	public List<LogEntry> getLogs() {
		return logs;
	}
	public void setTotalAmount(int totalAmount) {
		this.totalAmount = totalAmount;
	}
	public void setLogs(List<LogEntry> logs) {
		this.logs = logs;
	}

	public static class LogEntry implements Serializable {
		int lid;
		int level;
		Date timestamp;
		String text;
		public int getLid() {
			return lid;
		}
		public int getLevel() {
			return level;
		}
		public Date getTimestamp() {
			return timestamp;
		}
		public String getText() {
			return text;
		}
		public void setLid(int lid) {
			this.lid = lid;
		}
		public void setLevel(int level) {
			this.level = level;
		}
		public void setTimestamp(Date timestamp) {
			this.timestamp = timestamp;
		}
		public void setText(String text) {
			this.text = text;
		}
	}

}
