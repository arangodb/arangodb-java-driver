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
public class IndexesEntity extends BaseEntity {

	List<IndexEntity> indexes;
	Map<String, IndexEntity> identifiers;
	
	public List<IndexEntity> getIndexes() {
		return indexes;
	}
	public Map<String, IndexEntity> getIdentifiers() {
		return identifiers;
	}
	public void setIndexes(List<IndexEntity> indexes) {
		this.indexes = indexes;
	}
	public void setIdentifiers(Map<String, IndexEntity> identifiers) {
		this.identifiers = identifiers;
	}
	
	
}
