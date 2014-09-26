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

package at.orz.arangodb.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class CollectionUtils {

	public static class EmptyIterator<E> implements Iterator<E> {
		public boolean hasNext() {
			return false;
		}
		public E next() {
			throw new NoSuchElementException();
		}
		public void remove() {
			throw new IllegalStateException();
		}
	}
	
	private static final EmptyIterator<Object> EMPTY_ITERATOR = new EmptyIterator<Object>();
	
	@SuppressWarnings("unchecked")
	public static <T> Iterator<T> emptyIterator() {
		return (Iterator<T>) EMPTY_ITERATOR;
	}
	
	public static <T> Iterator<T> safetyIterator(Collection<T> collection) {
		if (collection == null) {
			return emptyIterator();
		}
		return collection.iterator();
	}
	
	public static <T> List<T> safety(List<T> list) {
		if (list == null) {
			return new ArrayList<T>(0); // mutable list
		}
		return list;
	}
	
	public static <T> String join(T[] array, String separator) {
		if (array == null || array.length == 0) {
			return "";
		}
		
		StringBuilder buffer = new StringBuilder();
		buffer.append(array[0]);
		
		for (int i = 1; i < array.length; i++) {
			buffer.append(separator);
			buffer.append(array[i]);
		}
		
		return buffer.toString();
	}
	
}
