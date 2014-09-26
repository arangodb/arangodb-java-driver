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

import java.util.List;
import java.util.Map;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class CollectionsEntity extends BaseEntity {
	
	List<CollectionEntity> collections;
	Map<String, CollectionEntity> names;
	
	public List<CollectionEntity> getCollections() {
		return collections;
	}
	public Map<String, CollectionEntity> getNames() {
		return names;
	}
	public void setCollections(List<CollectionEntity> collections) {
		this.collections = collections;
	}
	public void setNames(Map<String, CollectionEntity> names) {
		this.names = names;
	}
	
}
