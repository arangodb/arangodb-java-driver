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

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ReplicationDumpHeader implements Serializable {

	Long lasttick;
	Boolean checkmore;
	Long lastincluded;
	Boolean active;
	
	public Long getLasttick() {
		return lasttick;
	}
	public Boolean getCheckmore() {
		return checkmore;
	}
	public Long getLastincluded() {
		return lastincluded;
	}
	public Boolean getActive() {
		return active;
	}
	public void setLasttick(Long lasttick) {
		this.lasttick = lasttick;
	}
	public void setCheckmore(Boolean checkmore) {
		this.checkmore = checkmore;
	}
	public void setLastincluded(Long lastincluded) {
		this.lastincluded = lastincluded;
	}
	public void setActive(Boolean active) {
		this.active = active;
	}
	
}
