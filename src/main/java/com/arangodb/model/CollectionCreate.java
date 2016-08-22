package com.arangodb.model;

import com.arangodb.entity.CollectionType;
import com.arangodb.entity.KeyOptions;
import com.arangodb.entity.KeyType;

/**
 * @author Mark - mark at arangodb.com
 *
 */
@SuppressWarnings("unused")
public class CollectionCreate {

	private final String name;
	private final Long journalSize;
	private final KeyOptions keyOptions;
	private final Boolean waitForSync;
	private final Boolean doCompact;
	private final Boolean isVolatile;
	private final String[] shardKeys;
	private final Integer numberOfShards;
	private final Boolean isSystem;
	private final CollectionType type;
	private final Integer indexBuckets;

	private CollectionCreate(final String name, final Long journalSize, final KeyOptions keyOptions,
		final Boolean waitForSync, final Boolean doCompact, final Boolean isVolatile, final String[] shardKeys,
		final Integer numberOfShards, final Boolean isSystem, final CollectionType type, final Integer indexBuckets) {
		super();
		this.name = name;
		this.journalSize = journalSize;
		this.keyOptions = keyOptions;
		this.waitForSync = waitForSync;
		this.doCompact = doCompact;
		this.isVolatile = isVolatile;
		this.shardKeys = shardKeys;
		this.numberOfShards = numberOfShards;
		this.isSystem = isSystem;
		this.type = type;
		this.indexBuckets = indexBuckets;
	}

	public static class Options {

		private Long journalSize;
		private KeyOptions keyOptions;
		private Boolean waitForSync;
		private Boolean doCompact;
		private Boolean isVolatile;
		private String[] shardKeys;
		private Integer numberOfShards;
		private Boolean isSystem;
		private CollectionType type;
		private Integer indexBuckets;

		public Options journalSize(final Long journalSize) {
			this.journalSize = journalSize;
			return this;
		}

		public Options keyOptions(
			final Boolean allowUserKeys,
			final KeyType type,
			final Integer increment,
			final Integer offset) {
			this.keyOptions = new KeyOptions(allowUserKeys, type, increment, offset);
			return this;
		}

		public Options waitForSync(final Boolean waitForSync) {
			this.waitForSync = waitForSync;
			return this;
		}

		public Options doCompact(final Boolean doCompact) {
			this.doCompact = doCompact;
			return this;
		}

		public Options isVolatile(final Boolean isVolatile) {
			this.isVolatile = isVolatile;
			return this;
		}

		public Options shardKeys(final String[] shardKeys) {
			this.shardKeys = shardKeys;
			return this;
		}

		public Options numberOfShards(final Integer numberOfShards) {
			this.numberOfShards = numberOfShards;
			return this;
		}

		public Options isSystem(final Boolean isSystem) {
			this.isSystem = isSystem;
			return this;
		}

		public Options type(final CollectionType type) {
			this.type = type;
			return this;
		}

		public Options indexBuckets(final Integer indexBuckets) {
			this.indexBuckets = indexBuckets;
			return this;
		}

		protected CollectionCreate build(final String name) {
			return new CollectionCreate(name, journalSize, keyOptions, waitForSync, doCompact, isVolatile, shardKeys,
					numberOfShards, isSystem, type, indexBuckets);
		}
	}

}
