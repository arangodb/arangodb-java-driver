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
public class CollectionKeyOption implements Serializable {

	String type;
	boolean allowUserKeys;
	long increment;
	long offset;
	
	public static CollectionKeyOption createIncrementOption(boolean allowUserKeys, long increment, long offset) {
		CollectionKeyOption option = new CollectionKeyOption();
		option.setType("autoincrement");
		option.setAllowUserKeys(allowUserKeys);
		option.setIncrement(increment);
		option.setOffset(offset);
		return option;
	}
	
	public String getType() {
		return type;
	}
	public boolean isAllowUserKeys() {
		return allowUserKeys;
	}
	public long getIncrement() {
		return increment;
	}
	public long getOffset() {
		return offset;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setAllowUserKeys(boolean allowUserKeys) {
		this.allowUserKeys = allowUserKeys;
	}
	public void setIncrement(long increment) {
		this.increment = increment;
	}
	public void setOffset(long offset) {
		this.offset = offset;
	}
	
}
