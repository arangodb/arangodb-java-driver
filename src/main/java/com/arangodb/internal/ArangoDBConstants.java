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

package com.arangodb.internal;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ArangoDBConstants {

	public static final String DEFAULT_HOST = "127.0.0.1";
	public static final Integer DEFAULT_PORT = 8529;
	public static final Integer DEFAULT_TIMEOUT = 0;
	public static final Boolean DEFAULT_USE_SSL = false;

	public static final int CHUNK_MIN_HEADER_SIZE = Integer.BYTES + Integer.BYTES + Long.BYTES;
	public static final int CHUNK_MAX_HEADER_SIZE = CHUNK_MIN_HEADER_SIZE + Long.BYTES;
	public static final int CHUNK_DEFAULT_CONTENT_SIZE = 30000;

	public static final String PATH_API_DOCUMENT = "/_api/document";
	public static final String PATH_API_COLLECTION = "/_api/collection";
	public static final String PATH_API_DATABASE = "/_api/database";
	public static final String PATH_API_VERSION = "/_api/version";
	public static final String PATH_API_INDEX = "/_api/index";
	public static final String PATH_API_USER = "/_api/user";
	public static final String PATH_API_CURSOR = "/_api/cursor";
	public static final String PATH_API_GHARIAL = "/_api/gharial";
	public static final String PATH_API_TRANSACTION = "/_api/transaction";
	public static final String PATH_API_AQLFUNCTION = "/_api/aqlfunction";
	public static final String PATH_API_EXPLAIN = "/_api/explain";
	public static final String PATH_API_QUERY = "/_api/query";
	public static final String PATH_API_QUERY_CACHE = "/_api/query-cache";
	public static final String PATH_API_QUERY_CACHE_PROPERTIES = "/_api/query-cache/properties";
	public static final String PATH_API_QUERY_PROPERTIES = "/_api/query/properties";
	public static final String PATH_API_QUERY_CURRENT = "/_api/query/current";
	public static final String PATH_API_QUERY_SLOW = "/_api/query/slow";
	public static final String PATH_API_TRAVERSAL = "/_api/traversal";
	public static final String PATH_API_ADMIN_LOG = "/_admin/log";
	public static final String PATH_API_ADMIN_ROUTING_RELOAD = "/_admin/routing/reload";

	public static final String ENCRYPTION_PLAIN = "plain";

	public static final String SYSTEM = "_system";
	public static final String ID = "id";
	public static final String RESULT = "result";
	public static final String VISITED = "visited";
	public static final String VERTICES = "vertices";
	public static final String EDGES = "edges";
	public static final String WAIT_FOR_SYNC = "waitForSync";
	public static final String IF_NONE_MATCH = "If-None-Match";
	public static final String IF_MATCH = "If-Match";
	public static final String KEEP_NULL = "keepNull";
	public static final String MERGE_OBJECTS = "mergeObjects";
	public static final String IGNORE_REVS = "ignoreRevs";
	public static final String RETURN_NEW = "returnNew";
	public static final String NEW = "new";
	public static final String RETURN_OLD = "returnOld";
	public static final String OLD = "old";
	public static final String COLLECTION = "collection";
	public static final String COLLECTIONS = "collections";
	public static final String EXCLUDE_SYSTEM = "excludeSystem";
	public static final String USER = "user";
	public static final String RW = "rw";
	public static final String NONE = "none";
	public static final String DATABASE = "database";
	public static final String CURRENT = "current";
	public static final String INDEXES = "indexes";
	public static final String TRUNCATE = "truncate";
	public static final String COUNT = "count";
	public static final String LOAD = "load";
	public static final String UNLOAD = "unload";
	public static final String PROPERTIES = "properties";
	public static final String RENAME = "rename";
	public static final String REVISION = "revision";
	public static final String FULLCOUNT = "fullCount";
	public static final String GROUP = "group";
	public static final String NAMESPACE = "namespace";
	public static final String GRAPH = "graph";
	public static final String GRAPHS = "graphs";
	public static final String VERTEX = "vertex";
	public static final String EDGE = "edge";
	public static final String ERROR = "error";
}
