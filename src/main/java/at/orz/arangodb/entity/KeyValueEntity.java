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

import java.util.Date;
import java.util.Map;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class KeyValueEntity extends BaseEntity {

	long id;
	boolean saved;
	
	Date created;
	Date expires;
	Map<String, Object> attributes;
	public long getId() {
		return id;
	}
	public boolean isSaved() {
		return saved;
	}
	public Date getCreated() {
		return created;
	}
	public Date getExpires() {
		return expires;
	}
	public Map<String, Object> getAttributes() {
		return attributes;
	}
	public void setId(long id) {
		this.id = id;
	}
	public void setSaved(boolean saved) {
		this.saved = saved;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public void setExpires(Date expires) {
		this.expires = expires;
	}
	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

}
