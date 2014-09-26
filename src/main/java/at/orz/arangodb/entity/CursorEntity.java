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

import java.util.Iterator;
import java.util.List;

import at.orz.arangodb.util.CollectionUtils;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class CursorEntity<T> extends BaseEntity implements Iterable<T> {

	boolean hasMore;
	int count = -1;
	long cursorId = -1;
	List<String> bindVars;
	List<? extends T> results;
	
	public Iterator<T> iterator() {
		return (Iterator<T>) CollectionUtils.safetyIterator(results);
	}
	public int size() {
		if (results == null) {
			return 0;
		}
		return results.size();
	}
	public T get(int index) {
		rangeCheck(index);
		return results.get(index);
	}

	private void rangeCheck(int index) {
		int size = size();
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
		}
	}	
	
	public List<? extends T> getResults() {
		return results;
	}

	public boolean isHasMore() {
		return hasMore;
	}
	public boolean hasMore() {
		return hasMore;
	}

	public int getCount() {
		return count;
	}

	public long getCursorId() {
		return cursorId;
	}

	public List<String> getBindVars() {
		return bindVars;
	}

	public void setResults(List<T> results) {
		this.results = results;
	}

	public void setHasMore(boolean hasMore) {
		this.hasMore = hasMore;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void setCursorId(long cursorId) {
		this.cursorId = cursorId;
	}

	public void setBindVars(List<String> bindVars) {
		this.bindVars = bindVars;
	}
	
}
