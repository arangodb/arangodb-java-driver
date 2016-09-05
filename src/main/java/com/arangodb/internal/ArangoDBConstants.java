package com.arangodb.internal;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ArangoDBConstants {

	public static final String DEFAULT_HOST = "127.0.0.1";
	public static final Integer DEFAULT_PORT = 8529;
	public static final Integer DEFAULT_TIMEOUT = 0;

	public static final int CHUNK_MIN_HEADER_SIZE = Integer.BYTES + Integer.BYTES + Long.BYTES;
	public static final int CHUNK_MAX_HEADER_SIZE = CHUNK_MIN_HEADER_SIZE + Long.BYTES;
	public static final int CHUNK_BODY_SIZE = 1500;

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

	public static final String SYSTEM = "_system";
	public static final String ID = "id";
	public static final String RESULT = "result";
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
}
