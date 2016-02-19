package com.arangodb.util;

import java.util.HashMap;
import java.util.Map;

import com.arangodb.Direction;
import com.arangodb.InternalTraversalDriver.ItemOrder;
import com.arangodb.InternalTraversalDriver.Order;
import com.arangodb.InternalTraversalDriver.Strategy;
import com.arangodb.InternalTraversalDriver.Uniqueness;

public class TraversalQueryOptions extends AbstractOptions implements OptionsInterface {

	// (optional) name of the graph that contains the edges. Either
	// edgeCollection or graphName has to be given. In case both values are set
	// the graphName is prefered.
	private String graphName;

	// (optional) name of the collection that contains the edges.
	private String edgeCollection;

	// id of the startVertex, e.g. "users/foo".
	private String startVertex;

	// (optional, default is to include all nodes): body (JavaScript code) of
	// custom filter function function
	private String filter;

	// (optional, ANDed with any existing filters): visits only nodes in at
	// least the given depth
	private Long minDepth;

	// (optional, ANDed with any existing filters): visits only nodes in at most
	// the given depth
	private Long maxDepth;

	// (optional): body (JavaScript) code of custom visitor function
	private String visitor;

	// (optional): direction for traversal
	private Direction direction;

	// (optional): body (JavaScript) code of custom result initialisation
	// function
	private String init;

	// (optional): body (JavaScript) code of custom expander function
	private String expander;

	// (optional): body (JavaScript) code of a custom comparison function
	private String sort;

	// (optional): traversal strategy
	private Strategy strategy;

	// (optional): traversal order
	private Order order;

	// (optional): item iteration order
	private ItemOrder itemOrder;

	// (optional): specifies uniqueness for vertices visited if set
	private Uniqueness verticesUniqueness;

	// (optional): specifies uniqueness for edges visited if set
	private Uniqueness edgesUniqueness;

	// (optional): Maximum number of iterations in each traversal
	private Long maxIterations;

	public String getGraphName() {
		return graphName;
	}

	/**
	 * Set the (optional) name of the graph that contains the edges. Either
	 * edgeCollection or graphName has to be given. In case both values are set
	 * the graphName is prefered.
	 * 
	 * @param graphName
	 * @return this
	 */
	public TraversalQueryOptions setGraphName(String graphName) {
		this.graphName = graphName;
		return this;
	}

	public String getEdgeCollection() {
		return edgeCollection;
	}

	/**
	 * Set (optional) name of the collection that contains the edges.
	 * 
	 * @param edgeCollection
	 * @return this
	 */
	public TraversalQueryOptions setEdgeCollection(String edgeCollection) {
		this.edgeCollection = edgeCollection;
		return this;
	}

	public String getStartVertex() {
		return startVertex;
	}

	/**
	 * Set id of the startVertex, e.g. "users/foo".
	 * 
	 * @param startVertex
	 * @return this
	 */
	public TraversalQueryOptions setStartVertex(String startVertex) {
		this.startVertex = startVertex;
		return this;
	}

	public String getFilter() {
		return filter;
	}

	/**
	 * Set a filter.
	 * 
	 * (optional, default is to include all nodes): body (JavaScript code) of
	 * custom filter function function
	 * 
	 * @param filter
	 * @return this
	 */
	public TraversalQueryOptions setFilter(String filter) {
		this.filter = filter;
		return this;
	}

	public Long getMinDepth() {
		return minDepth;
	}

	/**
	 * Set a minimal depth.
	 * 
	 * (optional, ANDed with any existing filters): visits only nodes in at
	 * least the given depth
	 * 
	 * @param minDepth
	 * @return this
	 */
	public TraversalQueryOptions setMinDepth(Long minDepth) {
		this.minDepth = minDepth;
		return this;
	}

	public Long getMaxDepth() {
		return maxDepth;
	}

	/**
	 * Set a maximal depth.
	 * 
	 * (optional, ANDed with any existing filters): visits only nodes in at most
	 * the given depth
	 * 
	 * @param maxDepth
	 * @return this
	 */
	public TraversalQueryOptions setMaxDepth(Long maxDepth) {
		this.maxDepth = maxDepth;
		return this;
	}

	public String getVisitor() {
		return visitor;
	}

	/**
	 * Set visitor function.
	 * 
	 * (optional): body (JavaScript) code of custom visitor function
	 * 
	 * @param visitor
	 * @return this
	 */
	public TraversalQueryOptions setVisitor(String visitor) {
		this.visitor = visitor;
		return this;
	}

	public Direction getDirection() {
		return direction;
	}

	/**
	 * Set (optional) direction for traversal
	 * 
	 * @param direction
	 * @return this
	 */
	public TraversalQueryOptions setDirection(Direction direction) {
		this.direction = direction;
		return this;
	}

	public String getInit() {
		return init;
	}

	/**
	 * Set an initialisation function.
	 * 
	 * (optional): body (JavaScript) code of custom result initialisation
	 * function
	 * 
	 * @param init
	 * @return this
	 */
	public TraversalQueryOptions setInit(String init) {
		this.init = init;
		return this;
	}

	public String getExpander() {
		return expander;
	}

	/**
	 * Set an expander function.
	 * 
	 * (optional): body (JavaScript) code of custom expander function
	 * 
	 * @param expander
	 * @return this
	 */
	public TraversalQueryOptions setExpander(String expander) {
		this.expander = expander;
		return this;
	}

	public String getSort() {
		return sort;
	}

	/**
	 * Set a comparison function.
	 * 
	 * (optional): body (JavaScript) code of a custom comparison function
	 * 
	 * @param sort
	 * @return this
	 */
	public TraversalQueryOptions setSort(String sort) {
		this.sort = sort;
		return this;
	}

	public Strategy getStrategy() {
		return strategy;
	}

	/**
	 * Set (optional) traversal strategy.
	 * 
	 * @param strategy
	 * @return this
	 */
	public TraversalQueryOptions setStrategy(Strategy strategy) {
		this.strategy = strategy;
		return this;
	}

	public Order getOrder() {
		return order;
	}

	/**
	 * Set the (optional) traversal order
	 * 
	 * @param order
	 * @return this
	 */
	public TraversalQueryOptions setOrder(Order order) {
		this.order = order;
		return this;
	}

	public ItemOrder getItemOrder() {
		return itemOrder;
	}

	/**
	 * Set the (optional) item iteration order
	 * 
	 * @param itemOrder
	 * @return this
	 */
	public TraversalQueryOptions setItemOrder(ItemOrder itemOrder) {
		this.itemOrder = itemOrder;
		return this;
	}

	public Uniqueness getVerticesUniqueness() {
		return verticesUniqueness;
	}

	/**
	 * Set the (optional) uniqueness type for vertices visited if set
	 * 
	 * @param verticesUniqueness
	 * @return this
	 */
	public TraversalQueryOptions setVerticesUniqueness(Uniqueness verticesUniqueness) {
		this.verticesUniqueness = verticesUniqueness;
		return this;
	}

	public Uniqueness getEdgesUniqueness() {
		return edgesUniqueness;
	}

	/**
	 * Set the (optional) uniqueness type for edges visited if set
	 * 
	 * @param edgesUniqueness
	 * @return this
	 */
	public TraversalQueryOptions setEdgesUniqueness(Uniqueness edgesUniqueness) {
		this.edgesUniqueness = edgesUniqueness;
		return this;
	}

	public Long getMaxIterations() {
		return maxIterations;
	}

	/**
	 * Set the (optional) maximum number of iterations in each traversal
	 * 
	 * @param maxIterations
	 * @return this
	 */
	public TraversalQueryOptions setMaxIterations(Long maxIterations) {
		this.maxIterations = maxIterations;
		return this;
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> object = new HashMap<String, Object>();

		putAttribute(object, "graphName", graphName);
		putAttribute(object, "edgeCollection", edgeCollection);
		putAttribute(object, "startVertex", startVertex);
		putAttribute(object, "filter", filter);

		putAttribute(object, "minDepth", minDepth);
		putAttribute(object, "maxDepth", maxDepth);
		putAttribute(object, "visitor", visitor);
		putAttributeToLower(object, "direction", direction);

		putAttribute(object, "init", init);
		putAttribute(object, "expander", expander);
		putAttribute(object, "sort", sort);

		putAttributeToLower(object, "strategy", strategy);
		putAttributeToLower(object, "order", order);
		putAttributeToLower(object, "itemOrder", itemOrder);

		if (verticesUniqueness != null || edgesUniqueness != null) {
			Map<String, Object> uniqueness = new HashMap<String, Object>();

			putAttributeToLower(uniqueness, "vertices", verticesUniqueness);
			putAttributeToLower(uniqueness, "edges", edgesUniqueness);

			object.put("uniqueness", uniqueness);
		}

		putAttribute(object, "maxIterations", maxIterations);

		return object;
	}

}
