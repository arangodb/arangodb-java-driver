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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public abstract class BaseMapEntity<K, V> extends BaseEntity implements Map<K, V> {

	private final TreeMap<K, V> innerMap = new TreeMap<K, V>();

	public int size() {
		return innerMap.size();
	}

	public boolean isEmpty() {
		return innerMap.isEmpty();
	}

	public boolean containsKey(Object key) {
		return innerMap.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return innerMap.containsValue(value);
	}

	public V get(Object key) {
		return innerMap.get(key);
	}

	public V put(K key, V value) {
		return innerMap.put(key, value);
	}

	public V remove(Object key) {
		return innerMap.remove(key);
	}

	public void putAll(Map<? extends K, ? extends V> t) {
		innerMap.putAll(t);
	}

	public void clear() {
		innerMap.clear();
	}

	public Set<K> keySet() {
		return innerMap.keySet();
	}

	public Collection<V> values() {
		return innerMap.values();
	}

	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return innerMap.entrySet();
	}
	
}
