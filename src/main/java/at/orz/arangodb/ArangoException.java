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

package at.orz.arangodb;

import at.orz.arangodb.entity.BaseEntity;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ArangoException extends Exception {
	
	protected BaseEntity entity;
	
	public ArangoException() {
		super();
	}
	
	public ArangoException(BaseEntity entity) {
		super((entity.getErrorNumber() == 0 ? "" : "[" + entity.getErrorNumber() + "]") + entity.getErrorMessage());
		this.entity = entity;
	}
	
	public ArangoException(String message, Throwable cause) {
		super(message, cause);
	}

	public ArangoException(String message) {
		super(message);
	}

	public ArangoException(Throwable cause) {
		super(cause);
	}

	public int getErrorNumber() {
		return (entity == null) ? 0 : entity.getErrorNumber();
	}
	
	public int getCode() {
		return (entity == null) ? 0: entity.getCode();
	}
	
	public String getErrorMessage() {
		return (entity == null) ? getMessage() : entity.getErrorMessage();
	}
	
	public <T extends BaseEntity> T getEntity() {
		return (T) entity;
	}
	
	public boolean isUnauthorized() {
		return (entity != null && entity.isUnauthorized());
	}
	
	public boolean isNotFound() {
		return (entity != null && entity.isNotFound());
	}
	
}
