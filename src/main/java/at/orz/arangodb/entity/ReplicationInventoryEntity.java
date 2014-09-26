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
import java.util.List;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ReplicationInventoryEntity extends BaseEntity {
	
	List<Collection> collections;
	ReplicationState state;
	long tick;

	public List<Collection> getCollections() {
		return collections;
	}
	public ReplicationState getState() {
		return state;
	}
	public long getTick() {
		return tick;
	}
	public void setCollections(List<Collection> collections) {
		this.collections = collections;
	}
	public void setState(ReplicationState state) {
		this.state = state;
	}
	public void setTick(long tick) {
		this.tick = tick;
	}

	public static class CollectionParameter implements Serializable {
		int version;
		CollectionType type;
		long cid;
		boolean deleted;
		boolean doCompact;
		long maximalSize;
		String name;
		boolean isVolatile;
		boolean waitForSync;
		public int getVersion() {
			return version;
		}
		public CollectionType getType() {
			return type;
		}
		public long getCid() {
			return cid;
		}
		public boolean isDeleted() {
			return deleted;
		}
		public boolean isDoCompact() {
			return doCompact;
		}
		public long getMaximalSize() {
			return maximalSize;
		}
		public String getName() {
			return name;
		}
		public boolean isVolatile() {
			return isVolatile;
		}
		public boolean isWaitForSync() {
			return waitForSync;
		}
		public void setVersion(int version) {
			this.version = version;
		}
		public void setType(CollectionType type) {
			this.type = type;
		}
		public void setCid(long cid) {
			this.cid = cid;
		}
		public void setDeleted(boolean deleted) {
			this.deleted = deleted;
		}
		public void setDoCompact(boolean doCompact) {
			this.doCompact = doCompact;
		}
		public void setMaximalSize(long maximalSize) {
			this.maximalSize = maximalSize;
		}
		public void setName(String name) {
			this.name = name;
		}
		public void setVolatile(boolean isVolatile) {
			this.isVolatile = isVolatile;
		}
		public void setWaitForSync(boolean waitForSync) {
			this.waitForSync = waitForSync;
		}
	}
	
	public static class Collection implements Serializable {
		CollectionParameter parameter;
		List<IndexEntity> indexes;
		public CollectionParameter getParameter() {
			return parameter;
		}
		public List<IndexEntity> getIndexes() {
			return indexes;
		}
		public void setParameter(CollectionParameter parameter) {
			this.parameter = parameter;
		}
		public void setIndexes(List<IndexEntity> indexes) {
			this.indexes = indexes;
		}
		
	}
	
}
