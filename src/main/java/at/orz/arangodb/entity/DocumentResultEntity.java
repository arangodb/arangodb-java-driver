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

import java.util.List;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class DocumentResultEntity<T> extends BaseEntity {

	List<DocumentEntity<T>> result;
	
	public DocumentEntity<T> getOne() {
		if (result == null || result.isEmpty()) {
			return null;
		}
		return result.get(0);
	}
	
	public int size() {
		if (result == null) {
			return 0;
		}
		return result.size();
	}

	public List<DocumentEntity<T>> getResult() {
		return result;
	}

	public void setResult(List<DocumentEntity<T>> result) {
		this.result = result;
	}
	
}
