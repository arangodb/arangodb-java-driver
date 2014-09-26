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
public class SimpleByResultEntity extends BaseEntity {

	int count;
	
	int updated;
	int replaced;
	int deleted;
	
	public int getCount() {
		return count;
	}
	public int getUpdated() {
		return updated;
	}
	public int getReplaced() {
		return replaced;
	}
	public int getDeleted() {
		return deleted;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public void setUpdated(int updated) {
		this.updated = updated;
	}
	public void setReplaced(int replaced) {
		this.replaced = replaced;
	}
	public void setDeleted(int deleted) {
		this.deleted = deleted;
	}
	
}
