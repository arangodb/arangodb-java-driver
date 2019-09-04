/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb.model;

/**
 * @author Mark Vollmary
 * 
 * @see <a href="https://docs.arangodb.com/current/HTTP/Traversal/index.html">API Documentation</a>
 */
public class TraversalOptions {

	public enum Direction {
		outbound, inbound, any
	}

	public enum ItemOrder {
		forward, backward
	}

	public enum Strategy {
		depthfirst, breadthfirst
	}

	public enum UniquenessType {
		none, global, path
	}

	public enum Order {
		preorder, postorder, preorder_expander
	}

	private String sort;
	private Direction direction;
	private Integer minDepth;
	private String startVertex;
	private String visitor;
	private ItemOrder itemOrder;
	private Strategy strategy;
	private String filter;
	private String init;
	private Integer maxIterations;
	private Integer maxDepth;
	private Uniqueness uniqueness;
	private Order order;
	private String graphName;
	private String expander;
	private String edgeCollection;

	public String getSort() {
		return sort;
	}

	/**
	 * 
	 * @param sort
	 *            JavaScript code of a custom comparison function for the edges. The signature of this function is (l,
	 *            r) -> integer (where l and r are edges) and must return -1 if l is smaller than, +1 if l is greater
	 *            than, and 0 if l and r are equal. The reason for this is the following: The order of edges returned
	 *            for a certain vertex is undefined. This is because there is no natural order of edges for a vertex
	 *            with multiple connected edges. To explicitly define the order in which edges on the vertex are
	 *            followed, you can specify an edge comparator function with this attribute. Note that the value here
	 *            has to be a string to conform to the JSON standard, which in turn is parsed as function body on the
	 *            server side. Furthermore note that this attribute is only used for the standard expanders. If you use
	 *            your custom expander you have to do the sorting yourself within the expander code.
	 * @return options
	 */
	public TraversalOptions sort(final String sort) {
		this.sort = sort;
		return this;
	}

	public Direction getDirection() {
		return direction;
	}

	/**
	 * 
	 * @param direction
	 *            direction for traversal
	 * 
	 *            if set, must be either "outbound", "inbound", or "any"
	 * 
	 *            if not set, the expander attribute must be specified
	 * @return options
	 */
	public TraversalOptions direction(final Direction direction) {
		this.direction = direction;
		return this;
	}

	public Integer getMinDepth() {
		return minDepth;
	}

	/**
	 * 
	 * @param minDepth
	 *            ANDed with any existing filters): visits only nodes in at least the given depth
	 * @return options
	 */
	public TraversalOptions minDepth(final Integer minDepth) {
		this.minDepth = minDepth;
		return this;
	}

	public String getStartVertex() {
		return startVertex;
	}

	/**
	 * 
	 * @param startVertex
	 *            The id of the startVertex, e.g. "users/foo".
	 * @return options
	 */
	public TraversalOptions startVertex(final String startVertex) {
		this.startVertex = startVertex;
		return this;
	}

	public String getVisitor() {
		return visitor;
	}

	/**
	 * 
	 * @param visitor
	 *            JavaScript code of custom visitor function function signature: (config, result, vertex, path,
	 *            connected) -> void The visitor function can do anything, but its return value is ignored. To populate
	 *            a result, use the result variable by reference. Note that the connected argument is only populated
	 *            when the order attribute is set to "preorder-expander".
	 * @return options
	 */
	public TraversalOptions visitor(final String visitor) {
		this.visitor = visitor;
		return this;
	}

	public ItemOrder getItemOrder() {
		return itemOrder;
	}

	/**
	 * 
	 * @param itemOrder
	 *            The item iteration order can be "forward" or "backward"
	 * @return options
	 */
	public TraversalOptions itemOrder(final ItemOrder itemOrder) {
		this.itemOrder = itemOrder;
		return this;
	}

	public Strategy getStrategy() {
		return strategy;
	}

	/**
	 * 
	 * @param strategy
	 *            The traversal strategy can be "depthfirst" or "breadthfirst"
	 * @return options
	 */
	public TraversalOptions strategy(final Strategy strategy) {
		this.strategy = strategy;
		return this;
	}

	public String getFilter() {
		return filter;
	}

	/**
	 * 
	 * @param filter
	 *            default is to include all nodes: body (JavaScript code) of custom filter function function signature:
	 *            (config, vertex, path) -> mixed can return four different string values:
	 * 
	 *            "exclude" -> this vertex will not be visited.
	 * 
	 *            "prune" -> the edges of this vertex will not be followed.
	 * 
	 *            "" or undefined -> visit the vertex and follow it's edges.
	 * 
	 *            Array -> containing any combination of the above.
	 * 
	 *            If there is at least one "exclude" or "prune" respectivly is contained, it's effect will occur.
	 * @return options
	 */
	public TraversalOptions filter(final String filter) {
		this.filter = filter;
		return this;
	}

	public String getInit() {
		return init;
	}

	/**
	 * 
	 * @param init
	 *            JavaScript code of custom result initialization function function signature: (config, result) -> void
	 *            initialize any values in result with what is required
	 * @return options
	 */
	public TraversalOptions init(final String init) {
		this.init = init;
		return this;
	}

	public Integer getMaxIterations() {
		return maxIterations;
	}

	/**
	 * 
	 * @param maxIterations
	 *            Maximum number of iterations in each traversal. This number can be set to prevent endless loops in
	 *            traversal of cyclic graphs. When a traversal performs as many iterations as the maxIterations value,
	 *            the traversal will abort with an error. If maxIterations is not set, a server-defined value may be
	 *            used.
	 * @return options
	 */
	public TraversalOptions maxIterations(final Integer maxIterations) {
		this.maxIterations = maxIterations;
		return this;
	}

	public Integer getMaxDepth() {
		return maxDepth;
	}

	/**
	 * 
	 * @param maxDepth
	 *            ANDed with any existing filters visits only nodes in at most the given depth.
	 * @return options
	 */
	public TraversalOptions maxDepth(final Integer maxDepth) {
		this.maxDepth = maxDepth;
		return this;
	}

	public UniquenessType getVerticesUniqueness() {
		return uniqueness != null ? uniqueness.vertices : null;
	}

	/**
	 * 
	 * @param vertices
	 *            Specifies uniqueness for vertices can be "none", "global" or "path"
	 * @return options
	 */
	public TraversalOptions verticesUniqueness(final UniquenessType vertices) {
		getUniqueness().setVertices(vertices);
		return this;
	}

	public UniquenessType getEdgesUniqueness() {
		return uniqueness != null ? uniqueness.edges : null;
	}

	/**
	 * 
	 * @param edges
	 *            Specifies uniqueness for edges can be "none", "global" or "path"
	 * @return options
	 */
	public TraversalOptions edgesUniqueness(final UniquenessType edges) {
		getUniqueness().setEdges(edges);
		return this;
	}

	public Order getOrder() {
		return order;
	}

	/**
	 * 
	 * @param order
	 *            The traversal order can be "preorder", "postorder" or "preorder-expander"
	 * @return options
	 */
	public TraversalOptions order(final Order order) {
		this.order = order;
		return this;
	}

	public String getGraphName() {
		return graphName;
	}

	/**
	 * 
	 * @param graphName
	 *            The name of the graph that contains the edges. Either edgeCollection or graphName has to be given. In
	 *            case both values are set the graphName is prefered.
	 * @return options
	 */
	public TraversalOptions graphName(final String graphName) {
		this.graphName = graphName;
		return this;
	}

	public String getExpander() {
		return expander;
	}

	/**
	 * 
	 * @param expander
	 *            JavaScript code of custom expander function must be set if direction attribute is not set function
	 *            signature: (config, vertex, path) -> array expander must return an array of the connections for vertex
	 *            each connection is an object with the attributes edge and vertex
	 * @return options
	 */
	public TraversalOptions expander(final String expander) {
		this.expander = expander;
		return this;
	}

	public String getEdgeCollection() {
		return edgeCollection;
	}

	/**
	 * 
	 * @param edgeCollection
	 *            The name of the collection that contains the edges.
	 * @return options
	 */
	public TraversalOptions edgeCollection(final String edgeCollection) {
		this.edgeCollection = edgeCollection;
		return this;
	}

	public static class Uniqueness {

		private UniquenessType vertices;
		private UniquenessType edges;

		public UniquenessType getVertices() {
			return vertices;
		}

		public void setVertices(final UniquenessType vertices) {
			this.vertices = vertices;
		}

		public UniquenessType getEdges() {
			return edges;
		}

		public void setEdges(final UniquenessType edges) {
			this.edges = edges;
		}
	}

	private Uniqueness getUniqueness() {
		if (uniqueness == null) {
			uniqueness = new Uniqueness();
			uniqueness.vertices = UniquenessType.none;
			uniqueness.edges = UniquenessType.none;
		}

		return uniqueness;
	}

}
