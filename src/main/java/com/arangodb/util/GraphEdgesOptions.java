package com.arangodb.util;

import java.util.List;
import java.util.Map;

import com.arangodb.Direction;

public class GraphEdgesOptions implements OptionsInterface {

	private Direction direction;
	private List<String> edgeCollectionRestriction;
	private List<String> startVertexCollectionRestriction;
	private List<String> endVertexCollectionRestriction;
	private Object edgeExamples;
	private Object neighborExamples;
	private Integer minDepth;
	private Integer maxDepth;
	private Integer maxIterations;
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
	public GraphEdgesOptions setDirection(Direction direction) {
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
	public GraphEdgesOptions setEdgeCollectionRestriction(List<String> edgeCollectionRestriction) {
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
	public GraphEdgesOptions setStartVertexCollectionRestriction(List<String> startVertexCollectionRestriction) {
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
	public GraphEdgesOptions setEndVertexCollectionRestriction(List<String> endVertexCollectionRestriction) {
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
	public GraphEdgesOptions setEdgeExamples(Object edgeExamples) {
		this.edgeExamples = edgeExamples;
		return this;
	}

	/**
	 * An example for the desired neighbors
	 * 
	 * @return An example for the desired neighbors
	 */
	public Object getNeighborExamples() {
		return neighborExamples;
	}

	/**
	 * An example for the desired neighbors
	 * 
	 * @param neighborExamples
	 * @return this
	 */
	public GraphEdgesOptions setNeighborExamples(Object neighborExamples) {
		this.neighborExamples = neighborExamples;
		return this;
	}

	/**
	 * Defines the minimal length of a path from an edge to a vertex (default is
	 * 1, which means only the edges directly connected to a vertex would be
	 * returned).
	 * 
	 * @return efines the minimal length of a path
	 */
	public Integer getMinDepth() {
		return minDepth;
	}

	/**
	 * Defines the minimal length of a path from an edge to a vertex (default is
	 * 1, which means only the edges directly connected to a vertex would be
	 * returned).
	 * 
	 * @param minDepth
	 * @return this
	 */
	public GraphEdgesOptions setMinDepth(Integer minDepth) {
		this.minDepth = minDepth;
		return this;
	}

	/**
	 * Defines the maximal length of a path from an edge to a vertex (default is
	 * 1, which means only the edges directly connected to a vertex would be
	 * returned).
	 * 
	 * @return Defines the maximal length of a path
	 */
	public Integer getMaxDepth() {
		return maxDepth;
	}

	/**
	 * Defines the maximal length of a path from an edge to a vertex (default is
	 * 1, which means only the edges directly connected to a vertex would be
	 * returned).
	 * 
	 * @param maxDepth
	 * @return this
	 */
	public GraphEdgesOptions setMaxDepth(Integer maxDepth) {
		this.maxDepth = maxDepth;
		return this;
	}

	/**
	 * the maximum number of iterations that the traversal is allowed to
	 * perform. It is sensible to set this number so unbounded traversals
	 * 
	 * @return the maximum number of iterations
	 */
	public Integer getMaxIterations() {
		return maxIterations;
	}

	/**
	 * the maximum number of iterations that the traversal is allowed to
	 * perform. It is sensible to set this number so unbounded traversals
	 * 
	 * @return this
	 * @param maxIterations
	 */
	public GraphEdgesOptions setMaxIterations(Integer maxIterations) {
		this.maxIterations = maxIterations;
		return this;
	}

	/**
	 * Get include data
	 * 
	 * @return
	 */
	public Boolean getIncludeData() {
		return includeData;
	}

	/**
	 * set include data to be compatible with older versions of AnrangoDB
	 * 
	 * @param includeData
	 * 
	 * @since ArangoDB 2.6
	 */
	public void setIncludeData(Boolean includeData) {
		this.includeData = includeData;
	}

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
		if (neighborExamples != null) {
			mp.put("neighborExamples", neighborExamples);
		}
		if (minDepth != null) {
			mp.put("minDepth", minDepth);
		}
		if (maxDepth != null) {
			mp.put("maxDepth", maxDepth);
		}
		if (maxIterations != null) {
			mp.put("maxIterations", maxIterations);
		}
		if (includeData != null) {
			mp.put("includeData", includeData);
		}

		return mp.get();
	}

}
