/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb;

import java.io.Closeable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.arangodb.entity.CursorEntity.Stats;
import com.arangodb.entity.CursorEntity.Warning;

/**
 * @author Mark Vollmary
 *
 */
public interface ArangoCursor<T> extends Iterable<T>, Iterator<T>, Closeable {

	/**
	 * @return id of temporary cursor created on the server
	 */
	String getId();

	Class<T> getType();

	/**
	 * @return the total number of result documents available (only available if the query was executed with the count
	 *         attribute set)
	 */
	Integer getCount();

	Stats getStats();

	Collection<Warning> getWarnings();

	/**
	 * @return indicating whether the query result was served from the query cache or not
	 */
	boolean isCached();

	List<T> asListRemaining();

}
