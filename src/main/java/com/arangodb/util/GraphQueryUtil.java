package com.arangodb.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.Direction;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Mark - mark@arangodb.com
 *
 */
public class GraphQueryUtil {

	private static final String AND = " && ";
	private static final String OR = " || ";
	private static final String GRAPH_NAME = "graphName";
	private static final String VERTEX_EXAMPLE = "vertexExample";
	private static final String START_VERTEX_EXAMPLE = "startVertexExample";
	private static final String END_VERTEX_EXAMPLE = "endVertexExample";
	private static final String SOURCE = "source";
	private static final String TARGET = "target";

	public static String createEdgeQuery(
		final ArangoDriver driver,
		final String graphName,
		final Object vertexExample,
		final GraphEdgesOptions graphEdgesOptions,
		final MapBuilder bindVars) throws ArangoException {

		final StringBuilder sb = new StringBuilder();
		if (vertexExample != null && String.class.isAssignableFrom(vertexExample.getClass())) {
			sb.append("FOR v,e IN ");
			appendDepth(graphEdgesOptions, sb);
			appendDirection(graphEdgesOptions.getDirection(), sb);
			appendBindVar(VERTEX_EXAMPLE, vertexExample, bindVars, sb);
		} else {
			final List<String> startVertexCollectionRestriction = graphEdgesOptions
					.getStartVertexCollectionRestriction();
			final List<String> vertexCollections = startVertexCollectionRestriction != null
					&& startVertexCollectionRestriction.size() > 0 ? startVertexCollectionRestriction
							: driver.graphGetVertexCollections(graphName, true);
			appendFor("start", vertexExample, sb, vertexCollections);
			sb.append(" FOR v,e IN ");
			appendDepth(graphEdgesOptions, sb);
			appendDirection(graphEdgesOptions.getDirection(), sb);
			sb.append(" start");
		}
		final List<String> edgeCollectionRestriction = graphEdgesOptions.getEdgeCollectionRestriction();
		appendEdgeCollectionsOrGraph(graphName, bindVars, sb, edgeCollectionRestriction);
		appendFilter("e", graphEdgesOptions.getEdgeExamples(), sb);
		appendFilter("v", graphEdgesOptions.getNeighborExamples(), sb);
		{
			final List<String> endVertexCollectionRestriction = graphEdgesOptions.getEndVertexCollectionRestriction();
			if (endVertexCollectionRestriction != null && endVertexCollectionRestriction.size() > 0) {
				sb.append(" FILTER ");
				for (String endVertexCollection : endVertexCollectionRestriction) {
					sb.append("IS_SAME_COLLECTION(`");
					sb.append(endVertexCollection);
					sb.append("`,v)");
					sb.append(OR);
				}
				sb.delete(sb.length() - OR.length(), sb.length() - 1);
			}
		}
		final Integer limit = graphEdgesOptions.getLimit();
		if (limit != null) {
			sb.append(" LIMIT ");
			sb.append(limit.intValue());
		}
		sb.append(" RETURN distinct e");
		if (graphEdgesOptions.getIncludeData() != null && !graphEdgesOptions.getIncludeData().booleanValue()) {
			sb.append(".id");
		}

		final String query = sb.toString();
		return query;
	}

	private static void appendEdgeCollectionsOrGraph(
		final String graphName,
		final MapBuilder bindVars,
		final StringBuilder sb,
		final List<String> edgeCollectionRestriction) {
		sb.append(" ");
		if (edgeCollectionRestriction != null && edgeCollectionRestriction.size() > 0) {
			for (String edgeCollection : edgeCollectionRestriction) {
				sb.append("`");
				sb.append(edgeCollection);
				sb.append("`,");
			}
			// remove last ,
			sb.deleteCharAt(sb.length() - 1);
		} else {
			appendGraphName(graphName, bindVars, sb);
		}
	}

	private static void appendBindVar(
		final String param,
		final Object var,
		final MapBuilder bindVars,
		final StringBuilder sb) {
		sb.append(" @");
		sb.append(param);
		bindVars.put(param, var);
	}

	private static void appendFor(
		final String var,
		final Object vertexExample,
		final StringBuilder sb,
		final List<String> vertexCollections) throws ArangoException {
		if (vertexCollections.size() == 1) {
			sb.append("FOR ");
			sb.append(var);
			sb.append(" IN `");
			sb.append(vertexCollections.get(0));
			sb.append("`");
			appendFilter(var, vertexExample, sb);
		} else {
			sb.append("FOR ");
			sb.append(var);
			sb.append(" IN UNION (");
			for (String vertexCollection : vertexCollections) {
				sb.append("(FOR ");
				sb.append(var);
				sb.append(" IN `");
				sb.append(vertexCollection);
				sb.append("`");
				appendFilter(var, vertexExample, sb);
				sb.append(" RETURN ");
				sb.append(var);
				sb.append("),");
			}
			// remove last ,
			sb.deleteCharAt(sb.length() - 1);
			sb.append(")");
		}
		sb.append(" ");
	}

	private static void appendGraphName(final String graphName, final MapBuilder bindVars, final StringBuilder sb) {
		sb.append("GRAPH");
		appendBindVar(GRAPH_NAME, graphName, bindVars, sb);
	}

	private static void appendDepth(final GraphEdgesOptions graphEdgesOptions, final StringBuilder sb) {
		final Integer minDepth = graphEdgesOptions.getMinDepth();
		final Integer maxDepth = graphEdgesOptions.getMaxDepth();
		if (minDepth != null || maxDepth != null) {
			sb.append(minDepth != null ? minDepth : 1);
			sb.append("..");
			sb.append(maxDepth != null ? maxDepth : 1);
			sb.append(" ");
		}
	}

	private static void appendDirection(final Direction direction, final StringBuilder sb) {
		final String directionName = direction != null ? direction.name() : Direction.ANY.name();
		sb.append(directionName);
	}

	private static void appendFilter(final String var, final Object example, final StringBuilder sb)
			throws ArangoException {
		if (example != null) {
			final Gson gson = new Gson();
			final JsonElement json = gson.toJsonTree(example);
			if (json.isJsonObject()) {
				sb.append(" FILTER ");
				appendObjectinFilter(var, json.getAsJsonObject(), sb);
			} else if (json.isJsonArray()) {
				sb.append(" FILTER ");
				final JsonArray jsonArray = json.getAsJsonArray();
				if (jsonArray.size() > 0) {
					for (JsonElement jsonElement : jsonArray) {
						if (jsonElement.isJsonObject()) {
							sb.append("(");
							appendObjectinFilter(var, jsonElement.getAsJsonObject(), sb);
							sb.append(")");
							sb.append(OR);
						} else if (!jsonElement.isJsonNull()) {
							throw new ArangoException("invalide format of entry in array example: "
									+ example.getClass().getSimpleName() + ". only objects in array allowed.");
						}
					}
					sb.delete(sb.length() - OR.length(), sb.length() - 1);
				}
			} else {
				throw new ArangoException("invalide format of example: " + example.getClass().getSimpleName()
						+ ". only object or array allowed.");
			}
		}
	}

	private static void appendObjectinFilter(final String var, final JsonObject jsonObject, final StringBuilder sb) {
		final Set<Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
		for (Entry<String, JsonElement> entry : entrySet) {
			sb.append(var);
			sb.append(".`");
			sb.append(entry.getKey());
			sb.append("` == ");
			sb.append(entry.getValue().toString());
			sb.append(AND);
		}
		sb.delete(sb.length() - AND.length(), sb.length() - 1);
	}

	public static String createVerticesQuery(
		final ArangoDriver driver,
		final String graphName,
		final Object vertexExample,
		final GraphVerticesOptions graphVerticesOptions,
		final MapBuilder bindVars) throws ArangoException {

		StringBuilder sb = new StringBuilder();
		final boolean stringVertexExample = vertexExample != null
				&& String.class.isAssignableFrom(vertexExample.getClass());
		if (stringVertexExample) {
			sb.append("RETURN ");
			sb.append("DOCUMENT(");
			appendBindVar(VERTEX_EXAMPLE, vertexExample, bindVars, sb);
			sb.append(")");
		} else {
			final List<String> startVertexCollectionRestriction = graphVerticesOptions.getVertexCollectionRestriction();
			final List<String> vertexCollections = startVertexCollectionRestriction != null
					&& startVertexCollectionRestriction.size() > 0 ? startVertexCollectionRestriction
							: driver.graphGetVertexCollections(graphName, true);
			appendFor("start", vertexExample, sb, vertexCollections);
			sb.append(" RETURN start");
		}

		final String query = sb.toString();
		return query;
	}

	public static String createShortestPathQuery(
		final ArangoDriver driver,
		final String database,
		final String graphName,
		final Object startVertexExample,
		final Object endVertexExample,
		final ShortestPathOptions shortestPathOptions,
		final Class<?> vertexClass,
		final Class<?> edgeClass,
		final MapBuilder bindVars) throws ArangoException {
		/*
		 * 
		 * final String query =
		 * "for i in graph_shortest_path(@graphName, @startVertexExample, @endVertexExample, @options) return i"
		 * ; final Map<String, Object> bindVars = mapBuilder.put("graphName",
		 * graphName) .put("startVertexExample",
		 * startVertexExample).put("endVertexExample", endVertexExample)
		 * .put("options", options).get();
		 */
		final StringBuilder sb = new StringBuilder();
		final boolean notStringStartVertexExample = startVertexExample != null
				&& !String.class.isAssignableFrom(startVertexExample.getClass());
		boolean notStringEndVertexExample = endVertexExample != null
				&& !String.class.isAssignableFrom(endVertexExample.getClass());
		if (notStringStartVertexExample || notStringEndVertexExample) {
			final List<String> startVertexCollectionRestriction = shortestPathOptions
					.getStartVertexCollectionRestriction();
			final List<String> endVertexCollectionRestriction = shortestPathOptions.getEndVertexCollectionRestriction();
			final boolean startVertexCollectionRestrictionNotEmpty = startVertexCollectionRestriction != null
					&& startVertexCollectionRestriction.size() > 0;
			final boolean endVertexCollectionRestrictionNotEmpty = endVertexCollectionRestriction != null
					&& endVertexCollectionRestriction.size() > 0;
			final List<String> vertexCollections = (!startVertexCollectionRestrictionNotEmpty
					|| !endVertexCollectionRestrictionNotEmpty) ? driver.graphGetVertexCollections(graphName, true)
							: new ArrayList<String>();

			if (notStringStartVertexExample) {
				final List<String> tmpStartVertexCollectionRestriction = startVertexCollectionRestrictionNotEmpty
						? startVertexCollectionRestriction : vertexCollections;
				appendFor(SOURCE, startVertexExample, sb, tmpStartVertexCollectionRestriction);
			}
			if (notStringEndVertexExample) {
				final List<String> tmpEndVertexCollectionRestriction = endVertexCollectionRestrictionNotEmpty
						? endVertexCollectionRestriction : vertexCollections;
				appendFor(TARGET, endVertexExample, sb, tmpEndVertexCollectionRestriction);
			}
			if (notStringStartVertexExample && notStringEndVertexExample) {
				sb.append("FILTER target != source ");
			}
		}
		{// p
			sb.append("LET p = ( FOR v, e IN ");
			appendDirection(shortestPathOptions.getDirection(), sb);
			sb.append(" SHORTEST_PATH ");
			if (notStringStartVertexExample) {
				sb.append(SOURCE);
			} else {
				appendBindVar(START_VERTEX_EXAMPLE, startVertexExample, bindVars, sb);
			}
			sb.append(" TO ");
			if (notStringEndVertexExample) {
				sb.append(TARGET);
			} else {
				appendBindVar(END_VERTEX_EXAMPLE, endVertexExample, bindVars, sb);
			}
			List<String> edgeCollectionRestriction = shortestPathOptions.getEdgeCollectionRestriction();
			appendEdgeCollectionsOrGraph(graphName, bindVars, sb, edgeCollectionRestriction);

			final String weight = shortestPathOptions.getWeight();
			if (weight != null) {
				sb.append(" OPTIONS {weightAttribute: @attribute, defaultWeight: @default} ");
				sb.append(
					" RETURN { v: v, e: e, d: IS_NULL(e) ? 0 : (IS_NUMBER(e[@attribute]) ? e[@attribute] : @default)}) ");
				bindVars.put("attribute", weight);
				final Long defaultWeight = shortestPathOptions.getDefaultWeight();
				bindVars.put("default", defaultWeight != null ? defaultWeight : 1);
			} else {
				sb.append(" RETURN {v: v, e: e, d: IS_NULL(e) ? 0 : 1}) ");
			}
		}
		sb.append("FILTER LENGTH(p) > 0 ");
		if (shortestPathOptions.getIncludeData() != null && !shortestPathOptions.getIncludeData().booleanValue()) {
			sb.append(
				"RETURN { vertices: p[*].v._id, edges: p[* FILTER CURRENT.e != null].e._id, distance: SUM(p[*].d)}");
		} else {
			sb.append("RETURN { vertices: p[*].v, edges: p[* FILTER CURRENT.e != null].e, distance: SUM(p[*].d)}");
		}

		final String query = sb.toString();
		return query;
	}
}
