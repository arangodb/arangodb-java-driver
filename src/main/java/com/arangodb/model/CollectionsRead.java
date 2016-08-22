package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class CollectionsRead {

	private final Boolean excludeSystem;

	private CollectionsRead(final Boolean excludeSystem) {
		super();
		this.excludeSystem = excludeSystem;
	}

	public Boolean getExcludeSystem() {
		return excludeSystem;
	}

	public static class Options {
		private Boolean excludeSystem;

		public Options excludeSystem(final Boolean excludeSystem) {
			this.excludeSystem = excludeSystem;
			return this;
		}

		protected CollectionsRead build() {
			return new CollectionsRead(excludeSystem);
		}
	}

}
