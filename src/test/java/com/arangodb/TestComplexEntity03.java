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

package com.arangodb;

import com.arangodb.entity.BaseDocument;
import com.google.gson.annotations.SerializedName;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * @author gschwab
 *
 */
public class TestComplexEntity03 {

	@SerializedName(BaseDocument.KEY)
	private String documentKey;
	@SerializedName(BaseDocument.REV)
	private String documentRevision;
	@SerializedName(BaseDocument.ID)
	private String documentHandle;

	private String user;// = "testUser01";
	private String desc;// = "This is a test user";
	private Integer age;// = 18;

	public TestComplexEntity03() {
	}

	public TestComplexEntity03(final String user, final String desc, final Integer age) {
		this.user = user;
		this.desc = desc;
		this.age = age;
	}

	public String getUser() {
		return user;
	}

	public String getDesc() {
		return desc;
	}

	public Integer getAge() {
		return age;
	}

	public void setUser(final String user) {
		this.user = user;
	}

	public void setDesc(final String desc) {
		this.desc = desc;
	}

	public void setAge(final Integer age) {
		this.age = age;
	}

	public String getDocumentKey() {
		return documentKey;
	}

	public void setDocumentKey(final String documentKey) {
		this.documentKey = documentKey;
	}

	public String getDocumentHandle() {
		return documentHandle;
	}

	public void setDocumentHandle(final String documentHandle) {
		this.documentHandle = documentHandle;
	}

	public String getDocumentRevision() {
		return documentRevision;
	}

	public void setDocumentRevision(final String documentRevision) {
		this.documentRevision = documentRevision;
	}

}
