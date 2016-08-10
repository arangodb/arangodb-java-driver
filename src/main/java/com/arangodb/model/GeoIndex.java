package com.arangodb.model;

import java.util.Collection;

import com.arangodb.entity.IndexType;

/**
 * @author Mark - mark at arangodb.com
 *
 */
@SuppressWarnings("unused")
public class GeoIndex {

	private final Collection<String> fields;
	private final IndexType type;
	private final Boolean geoJson;

	private GeoIndex(final Collection<String> fields, final IndexType type, final Boolean geoJson) {
		super();
		this.fields = fields;
		this.type = type;
		this.geoJson = geoJson;
	}

	public static class Options {

		private Boolean geoJson;

		public Options geoJson(final Boolean geoJson) {
			this.geoJson = geoJson;
			return this;
		}

		public GeoIndex build(final Collection<String> fields) {
			return new GeoIndex(fields, IndexType.GEO, geoJson);
		}
	}
}
