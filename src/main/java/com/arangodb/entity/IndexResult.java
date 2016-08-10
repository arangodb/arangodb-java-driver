package com.arangodb.entity;

import java.util.Collection;
import java.util.Optional;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class IndexResult {

	private String id;
	private IndexType type;
	private Collection<String> fields;
	private Integer selectivityEstimate;
	private Boolean unique;
	private Boolean sparse;
	private Integer minLength;
	private Boolean isNewlyCreated;
	private Boolean geoJson;
	private Boolean constraint;

	public IndexResult() {
		super();
	}

	public String getId() {
		return id;
	}

	public IndexType getType() {
		return type;
	}

	public Collection<String> getFields() {
		return fields;
	}

	public Optional<Integer> getSelectivityEstimate() {
		return Optional.ofNullable(selectivityEstimate);
	}

	public Optional<Boolean> getUnique() {
		return Optional.ofNullable(unique);
	}

	public Optional<Boolean> getSparse() {
		return Optional.ofNullable(sparse);
	}

	public Optional<Integer> getMinLength() {
		return Optional.ofNullable(minLength);
	}

	public Optional<Boolean> getIsNewlyCreated() {
		return Optional.ofNullable(isNewlyCreated);
	}

	public Optional<Boolean> getGeoJson() {
		return Optional.ofNullable(geoJson);
	}

	public Optional<Boolean> getConstraint() {
		return Optional.ofNullable(constraint);
	}

}
