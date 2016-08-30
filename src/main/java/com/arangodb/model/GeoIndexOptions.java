package com.arangodb.model;

import java.util.Collection;

import com.arangodb.entity.IndexType;

/**
 * @author Mark - mark at arangodb.com
 *
 * @see <a href="https://docs.arangodb.com/current/HTTP/Indexes/Geo.html#create-geospatial-index">API Documentation</a>
 */
public class GeoIndexOptions {

	private Collection<String> fields;
	private final IndexType type = IndexType.geo;
	private Boolean geoJson;

	public GeoIndexOptions() {
		super();
	}

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

	/**
	 * @param geoJson
	 *            If a geo-spatial index on a location is constructed and geoJson is true, then the order within the
	 *            array is longitude followed by latitude. This corresponds to the format described in
	 * @return options
	 */
	public GeoIndexOptions geoJson(final Boolean geoJson) {
		this.geoJson = geoJson;
		return this;
	}

}
