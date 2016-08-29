package com.arangodb.internal;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.CollectionResult;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class CollectionCache {

	private static final Logger LOGGER = LoggerFactory.getLogger(CollectionCache.class);
	private static final long MAX_CACHE_TIME = 600000;

	private static class CollectionInfo {
		private final String name;
		private final long time;

		public CollectionInfo(final String name, final long time) {
			super();
			this.name = name;
			this.time = time;
		}
	}

	public static interface DBAccess {
		ArangoDatabase db(final String name);
	}

	private final Map<String, Map<Long, CollectionInfo>> cache;
	private DBAccess access;
	private String db;

	public CollectionCache() {
		super();
		cache = new HashMap<>();
	}

	public void init(final DBAccess access) {
		this.access = access;
	}

	public void setDb(final String db) {
		this.db = db;
	}

	public String getCollectionName(final long id) {
		final CollectionInfo info = getInfo(id);
		return info != null ? info.name : null;
	}

	private CollectionInfo getInfo(final long id) {
		Map<Long, CollectionInfo> dbCache = cache.get(db);
		if (dbCache == null) {
			dbCache = new HashMap<>();
			cache.put(db, dbCache);
		}
		CollectionInfo info = dbCache.get(id);
		if (info == null || isExpired(info.time)) {
			try {
				final String name = execute(id);
				info = new CollectionInfo(name, new Date().getTime());
				dbCache.put(id, info);
			} catch (final ArangoDBException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		return info;
	}

	private String execute(final long id) throws ArangoDBException {
		final CollectionResult result = access.db(db).getCollectionInfo(String.valueOf(id));
		return result.getName();
	}

	private boolean isExpired(final long time) {
		return new Date().getTime() > time + MAX_CACHE_TIME;
	}

}
