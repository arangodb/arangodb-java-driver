package com.arangodb.util;

import java.util.List;
import java.util.Map;

import com.arangodb.Direction;

public class GraphVerticesOptions implements OptionsInterface {

	private Direction direction;
	private List<String> vertexCollectionRestriction;

	/**
	 * The direction of the edges as a string. Possible values are outbound,
	 * inbound and any (default).
	 * 
	 * @return the direction
	 */
	public Direction getDirection() {
		return direction;
	}

	/**
	 * The direction of the edges as a string. Possible values are outbound,
	 * inbound and any (default).
	 * 
	 * @param direction
	 */
	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	/**
	 * One or multiple vertex collections that should be considered.
	 * 
	 * @return One or multiple vertex collections that should be considered.
	 */
	public List<String> getVertexCollectionRestriction() {
		return vertexCollectionRestriction;
	}

	/**
	 * One or multiple vertex collections that should be considered.
	 * 
	 * @param vertexCollectionRestriction
	 */
	public void setVertexCollectionRestriction(List<String> vertexCollectionRestriction) {
		this.vertexCollectionRestriction = vertexCollectionRestriction;
	}

	/**
	 * Returns a map of the options
	 * 
	 * @return a map
	 */
	@Override
	public Map<String, Object> toMap() {
		MapBuilder mp = new MapBuilder();
		if (direction != null) {
			mp.put("direction", direction.toString().toLowerCase());
		}
		if (CollectionUtils.isNotEmpty(vertexCollectionRestriction)) {
			mp.put("vertexCollectionRestriction", vertexCollectionRestriction);
		}
		return mp.get();
	}

}
