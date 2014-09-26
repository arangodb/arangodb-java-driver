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

import java.util.TreeMap;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * @since 1.4.0
 * @see http://www.arangodb.org/manuals/current/RefManualReplication.html#RefManualReplicationEventTypes
 */
public enum ReplicationEventType {

	LOGGER_STOPPED(1000),
	LOGGER_STARTED(1001),
	
	COLLECTION_CREATED(2000),
	COLLECTION_DROPPED(2001),
	COLLECTION_RENAMED(2002),
	COLLECTION_PROP_CHANGED(2003),
	
	INDEX_CREATED(2100),
	INDEX_DROPPED(2101),
	
	TRANSACTION_STARTED(2200),
	TRANSACTION_COMMITED(2201),
	
	DOCUMENT_UPSERT(2300),
	EDGE_UPSERT(2301),
	DELETION(2302)
	;

	private static TreeMap<Integer, ReplicationEventType> lookup = new TreeMap<Integer, ReplicationEventType>();
	static {
		for (ReplicationEventType type: ReplicationEventType.values()) {
			lookup.put(type.getType(), type);
		}
	}
	
	private final int type;
	private ReplicationEventType(int type) {
		this.type = type;
		// ここでMapに入れた方がスマートだが、初期化エラーが出る
	}
	public int getType() {
		return type;
	}
	public static ReplicationEventType valueOf(int type) {
		return lookup.get(type);
	}

}
