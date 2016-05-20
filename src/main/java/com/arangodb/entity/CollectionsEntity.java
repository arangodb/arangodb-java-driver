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

package com.arangodb.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.arangodb.util.CollectionUtils;

/**
 * A entity representing a list of ArangoDB collections
 *
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class CollectionsEntity extends BaseEntity {

	/**
	 * The list of collections
	 */
	private List<CollectionEntity> collections;

	public List<CollectionEntity> getCollections() {
		return collections;
	}

	public Map<String, CollectionEntity> getNames() {
		Map<String, CollectionEntity> names = new HashMap<String, CollectionEntity>();

		if (CollectionUtils.isNotEmpty(collections)) {
			for (CollectionEntity collectionEntity : collections) {
				names.put(collectionEntity.getName(), collectionEntity);
			}
		}

		return names;
	}

	public void setCollections(List<CollectionEntity> collections) {
		this.collections = collections;
	}

	@Override
	public String toString() {
		return "CollectionsEntity [collections=" + collections + "]";
	}

}
