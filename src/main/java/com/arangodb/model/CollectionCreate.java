package com.arangodb.model;

import java.util.Optional;
import java.util.concurrent.Future;

import com.arangodb.entity.CollectionEntity;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class CollectionCreate implements Executeable<CollectionEntity> {

	private final DB db;
	private final String name;
	private final Options options;

	public static class Options {
		private Optional<Boolean> waitForSync = Optional.empty();

		public Options waitForSync(final Boolean waitForSync) {
			this.waitForSync = Optional.of(waitForSync);
			return this;
		}
	}

	protected CollectionCreate(final DB db, final String name, final Options options) {
		this.db = db;
		this.name = name;
		this.options = options;
	}

	@Override
	public Future<CollectionEntity> execute(final ExecuteCallback<CollectionEntity> callback) {
		return null;
	}
}
