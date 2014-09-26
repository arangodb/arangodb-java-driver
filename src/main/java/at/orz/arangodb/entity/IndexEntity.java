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

import java.util.List;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class IndexEntity extends BaseEntity {

	String id;
	IndexType type;
	List<String> fields;
	boolean getJson;
	boolean isNewlyCreated;
	boolean unique;
	int size;
	int minLength;
	
	public String getId() {
		return id;
	}
	public IndexType getType() {
		return type;
	}
	public List<String> getFields() {
		return fields;
	}
	public boolean isGetJson() {
		return getJson;
	}
	public boolean isNewlyCreated() {
		return isNewlyCreated;
	}
	public boolean isUnique() {
		return unique;
	}
	public int getSize() {
		return size;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setType(IndexType type) {
		this.type = type;
	}
	public void setFields(List<String> fields) {
		this.fields = fields;
	}
	public void setGetJson(boolean getJson) {
		this.getJson = getJson;
	}
	public void setNewlyCreated(boolean isNewlyCreated) {
		this.isNewlyCreated = isNewlyCreated;
	}
	public void setUnique(boolean unique) {
		this.unique = unique;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public int getMinLength() {
		return minLength;
	}
	public void setMinLength(int minLength) {
		this.minLength = minLength;
	}
	
}
