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

import java.util.Map;

import com.google.gson.annotations.SerializedName;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class UserEntity extends BaseEntity {

	@SerializedName("username")
	String username;
	@SerializedName("passwd")
	String password;
	Boolean active;
	Map<String, Object> extra;
	
	public UserEntity() {
	}
	public UserEntity(String username, String password, Boolean active, Map<String, Object> extra) {
		this.username = username;
		this.password = password;
		this.active = active;
		this.extra = extra;
	}


	public String getUsername() {
		return username;
	}
	public String getPassword() {
		return password;
	}
	public Boolean isActive() {
		return active;
	}
	public Map<String, Object> getExtra() {
		return extra;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public void setActive(Boolean active) {
		this.active = active;
	}
	public void setExtra(Map<String, Object> extra) {
		this.extra = extra;
	}
	
}
