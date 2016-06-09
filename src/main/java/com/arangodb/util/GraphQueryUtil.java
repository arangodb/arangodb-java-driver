package com.arangodb.util;

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

	public static String createEdgeQuery(
		final ArangoDriver driver,
		final String graphName,
		final Class<?> clazz,
		final Object vertexExample,
		final GraphEdgesOptions graphEdgesOptions,
		final MapBuilder bindVars) throws ArangoException {

		final StringBuilder sb = new StringBuilder();
		if (vertexExample != null && String.class.isAssignableFrom(vertexExample.getClass())) {
			sb.append("FOR v,e IN ");
			appendDepth(graphEdgesOptions, sb);
			appendDirection(graphEdgesOptions, sb);
			sb.append(" @");
			sb.append(VERTEX_EXAMPLE);
			bindVars.put(VERTEX_EXAMPLE, JsonUtils.convertNullToMap(vertexExample));
		} else {
			final List<String> startVertexCollectionRestriction = graphEdgesOptions
					.getStartVertexCollectionRestriction();
			final List<String> vertexCollections = startVertexCollectionRestriction != null
					&& startVertexCollectionRestriction.size() > 0 ? startVertexCollectionRestriction
							: driver.graphGetVertexCollections(graphName, true);
			if (vertexCollections.size() == 1) {
				sb.append("FOR start IN `");
				sb.append(vertexCollections.get(0));
				sb.append("`");
				appendFilter("start", vertexExample, sb);
			} else {
				sb.append("FOR start IN UNION (");
				for (String vertexCollection : vertexCollections) {
					sb.append("(FOR start IN `");
					sb.append(vertexCollection);
					sb.append("`");
					appendFilter("start", vertexExample, sb);
					sb.append(" RETURN start),");
				}
				// remove last ,
				sb.deleteCharAt(sb.length() - 1);
				sb.append(")");
			}
			sb.append(" FOR v,e IN ");
			appendDepth(graphEdgesOptions, sb);
			appendDirection(graphEdgesOptions, sb);
			sb.append(" start");
		}
		sb.append(" ");
		final List<String> edgeCollectionRestriction = graphEdgesOptions.getEdgeCollectionRestriction();
		if (edgeCollectionRestriction != null && edgeCollectionRestriction.size() > 0) {
			for (String edgeCollection : edgeCollectionRestriction) {
				sb.append(edgeCollection);
				sb.append(",");
			}
			// remove last ,
			sb.deleteCharAt(sb.length() - 1);
		} else {
			sb.append("GRAPH @");
			sb.append(GRAPH_NAME);
			bindVars.put(GRAPH_NAME, graphName);
		}
		appendFilter("e", graphEdgesOptions.getEdgeExamples(), sb);
		appendFilter("v", graphEdgesOptions.getNeighborExamples(), sb);
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

	private static void appendDirection(final GraphEdgesOptions graphEdgesOptions, final StringBuilder sb) {
		final String direction = graphEdgesOptions.getDirection() != null ? graphEdgesOptions.getDirection().name()
				: Direction.ANY.name();
		sb.append(direction);
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
		final Class<?> clazz,
		final Object vertexExample,
		final GraphVerticesOptions graphVerticesOptions,
		final MapBuilder bindVars) throws ArangoException {
		return null;
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
		final MapBuilder bindVars) {
		return null;
	}
}
