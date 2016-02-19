package com.arangodb.util;

import java.util.List;
import java.util.Map;

import com.arangodb.Direction;

public class ShortestPathOptions extends AbstractOptions implements OptionsInterface {

	private Direction direction;
	private List<String> edgeCollectionRestriction;
	private List<String> startVertexCollectionRestriction;
	private List<String> endVertexCollectionRestriction;
	private Object edgeExamples;
	private String algorithm;
	private String weight;
	private Long defaultWeight;
	private Boolean includeData = Boolean.TRUE;

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
	 * @return this
	 */
	public ShortestPathOptions setDirection(Direction direction) {
		this.direction = direction;
		return this;
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
	 * @return this
	 */
	public ShortestPathOptions setEdgeCollectionRestriction(List<String> edgeCollectionRestriction) {
		this.edgeCollectionRestriction = edgeCollectionRestriction;
		return this;
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
	 * @return this
	 */
	public ShortestPathOptions setStartVertexCollectionRestriction(List<String> startVertexCollectionRestriction) {
		this.startVertexCollectionRestriction = startVertexCollectionRestriction;
		return this;
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
	 * @return this
	 */
	public ShortestPathOptions setEndVertexCollectionRestriction(List<String> endVertexCollectionRestriction) {
		this.endVertexCollectionRestriction = endVertexCollectionRestriction;
		return this;
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
	 * @return this
	 */
	public ShortestPathOptions setEdgeExamples(Object edgeExamples) {
		this.edgeExamples = edgeExamples;
		return this;
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
	 * @return this
	 */
	public ShortestPathOptions setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
		return this;
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
	 * @return this
	 */
	public ShortestPathOptions setWeight(String weight) {
		this.weight = weight;
		return this;
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
	 * @return this
	 */
	public ShortestPathOptions setDefaultWeight(Long defaultWeight) {
		this.defaultWeight = defaultWeight;
		return this;
	}

	/**
	 * Returns a map of the options
	 * 
	 * @return a map
	 */
	@Override
	public Map<String, Object> toMap() {
		MapBuilder mp = new MapBuilder();

		putAttributeToLower(mp, "direction", direction);
		putAttributeCollection(mp, "edgeCollectionRestriction", edgeCollectionRestriction);
		putAttributeCollection(mp, "startVertexCollectionRestriction", startVertexCollectionRestriction);
		putAttributeCollection(mp, "endVertexCollectionRestriction", endVertexCollectionRestriction);
		putAttribute(mp, "edgeExamples", edgeExamples);
		putAttribute(mp, "algorithm", algorithm);
		putAttribute(mp, "weight", weight);
		putAttribute(mp, "defaultWeight", defaultWeight);

		if (includeData != null) {
			mp.put("includeData", includeData);
			MapBuilder mp2 = new MapBuilder();
			mp2.put("edges", true);
			mp2.put("vertices", true);
			mp.put("includePath", mp2.get());
		}
		return mp.get();
	}

}
