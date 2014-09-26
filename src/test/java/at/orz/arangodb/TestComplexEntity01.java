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

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class TestComplexEntity01 {

	private String user;// = "testUser01";
	private String desc;// = "テストユーザーです。";
	private int age;// = 18;
	public TestComplexEntity01() {
	}
	public TestComplexEntity01(String user, String desc, int age) {
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
	public int getAge() {
		return age;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public void setAge(int age) {
		this.age = age;
	}
	
	
}
