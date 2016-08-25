package com.arangodb.model;

import java.util.Collection;

import com.arangodb.entity.IndexType;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class GeoIndexOptions {

	private Collection<String> fields;
	private final IndexType type = IndexType.geo;
	private Boolean geoJson;

	protected Collection<String> getFields() {
		return fields;
	}

	protected GeoIndexOptions fields(final Collection<String> fields) {
		this.fields = fields;
		return this;
	}

	protected IndexType getType() {
		return type;
	}

	public Boolean getGeoJson() {
		return geoJson;
	}

	public GeoIndexOptions geoJson(final Boolean geoJson) {
		this.geoJson = geoJson;
		return this;
	}

}
