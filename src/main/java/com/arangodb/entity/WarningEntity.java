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

package com.arangodb.entity;

/**
 * @author a-brandt
 */
public class WarningEntity {

	/**
	 * a warning code
	 */
	Long code;

	/**
	 * a warning message
	 */
	String message;

	public WarningEntity() {
	}

	public WarningEntity(Long code, String message) {
		this.code = code;
		this.message = message;
	}

	/**
	 * returns the warning code
	 * 
	 * @return a warning code
	 */
	public Long getCode() {
		return code;
	}

	public void setCode(Long code) {
		this.code = code;
	}

	/**
	 * returns a warning message
	 * 
	 * @return a warning message
	 */
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "WarningEntity [code=" + code + ", message=" + message + "]";
	}

}
