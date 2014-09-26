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

package at.orz.arangodb;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class TestComplexEntity02 {

	private Integer x;
	private Integer y;
	private Integer z;

	public TestComplexEntity02() {
	}

	public TestComplexEntity02(Integer x, Integer y, Integer z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Integer getX() {
		return x;
	}
	public Integer getY() {
		return y;
	}
	public Integer getZ() {
		return z;
	}
	public void setX(Integer x) {
		this.x = x;
	}
	public void setY(Integer y) {
		this.y = y;
	}
	public void setZ(Integer z) {
		this.z = z;
	}
	
}
