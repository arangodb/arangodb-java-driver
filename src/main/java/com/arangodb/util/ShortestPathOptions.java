package com.arangodb.util;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

import com.arangodb.Direction;

public class ShortestPathOptions implements OptionsInterface {

	private Direction direction;
	private List<String> edgeCollectionRestriction;
	private List<String> startVertexCollectionRestriction;
	private List<String> endVertexCollectionRestriction;
	private Object edgeExamples;
	private String algorithm;
	private String weight;
	private Long defaultWeight;

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
	 * One or multiple edge collection names. Only edges from these collections
	 * will be considered for the path.
	 * 
	 * @return One or multiple edge collection names.
	 */
	public List<String> getEdgeCollectionRestriction() {
		return edgeCollectionRestriction;
	}

	/**
	 * One or multiple edge collection names. Only edges from these collections
	 * will be considered for the path.
	 * 
	 * @param edgeCollectionRestriction
	 */
	public void setEdgeCollectionRestriction(List<String> edgeCollectionRestriction) {
		this.edgeCollectionRestriction = edgeCollectionRestriction;
	}

	/**
	 * One or multiple vertex collection names. Only vertices from these
	 * collections will be considered as start vertex of a path.
	 * 
	 * @return One or multiple vertex collection names.
	 */
	public List<String> getStartVertexCollectionRestriction() {
		return startVertexCollectionRestriction;
	}

	/**
	 * One or multiple vertex collection names. Only vertices from these
	 * collections will be considered as start vertex of a path.
	 * 
	 * @param startVertexCollectionRestriction
	 */
	public void setStartVertexCollectionRestriction(List<String> startVertexCollectionRestriction) {
		this.startVertexCollectionRestriction = startVertexCollectionRestriction;
	}

	/**
	 * One or multiple vertex collection names. Only vertices from these
	 * collections will be considered as end vertex of a path.
	 * 
	 * @return One or multiple vertex collection names.
	 */
	public List<String> getEndVertexCollectionRestriction() {
		return endVertexCollectionRestriction;
	}

	/**
	 * One or multiple vertex collection names. Only vertices from these
	 * collections will be considered as end vertex of a path.
	 * 
	 * @param endVertexCollectionRestriction
	 */
	public void setEndVertexCollectionRestriction(List<String> endVertexCollectionRestriction) {
		this.endVertexCollectionRestriction = endVertexCollectionRestriction;
	}

	/**
	 * A filter example for the edges
	 * 
	 * @return A filter example for the edges
	 */
	public Object getEdgeExamples() {
		return edgeExamples;
	}

	/**
	 * A filter example for the edges
	 * 
	 * @param edgeExamples
	 */
	public void setEdgeExamples(Object edgeExamples) {
		this.edgeExamples = edgeExamples;
	}

	/**
	 * The algorithm to calculate the shortest paths. If both start and end
	 * vertex examples are empty Floyd-Warshall is used, otherwise the default
	 * is Dijkstra.
	 * 
	 * @return The algorithm to calculate the shortest paths.
	 */
	public String getAlgorithm() {
		return algorithm;
	}

	/**
	 * The algorithm to calculate the shortest paths. If both start and end
	 * vertex examples are empty Floyd-Warshall is used, otherwise the default
	 * is Dijkstra.
	 * 
	 * @param algorithm
	 */
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	/**
	 * The name of the attribute of the edges containing the length as a string.
	 * 
	 * @return The name of the attribute
	 */
	public String getWeight() {
		return weight;
	}

	/**
	 * The name of the attribute of the edges containing the length as a string.
	 * 
	 * @param weight
	 */
	public void setWeight(String weight) {
		this.weight = weight;
	}

	/**
	 * Only used with the option weight. If an edge does not have the attribute
	 * named as defined in option weight this default is used as length. If no
	 * default is supplied the default would be positive Infinity so the path
	 * could not be calculated.
	 * 
	 * @return a default weight
	 */
	public Long getDefaultWeight() {
		return defaultWeight;
	}

	/**
	 * Only used with the option weight. If an edge does not have the attribute
	 * named as defined in option weight this default is used as length. If no
	 * default is supplied the default would be positive Infinity so the path
	 * could not be calculated.
	 * 
	 * @param defaultWeight
	 */
	public void setDefaultWeight(Long defaultWeight) {
		this.defaultWeight = defaultWeight;
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
		if (CollectionUtils.isNotEmpty(edgeCollectionRestriction)) {
			mp.put("edgeCollectionRestriction", edgeCollectionRestriction);
		}
		if (CollectionUtils.isNotEmpty(startVertexCollectionRestriction)) {
			mp.put("startVertexCollectionRestriction", startVertexCollectionRestriction);
		}
		if (CollectionUtils.isNotEmpty(endVertexCollectionRestriction)) {
			mp.put("endVertexCollectionRestriction", endVertexCollectionRestriction);
		}
		if (edgeExamples != null) {
			mp.put("edgeExamples", edgeExamples);
		}
		if (algorithm != null) {
			mp.put("algorithm", algorithm);
		}
		if (weight != null) {
			mp.put("weight", weight);
		}
		if (defaultWeight != null) {
			mp.put("defaultWeight", defaultWeight);
		}
		return mp.get();
	}

}
