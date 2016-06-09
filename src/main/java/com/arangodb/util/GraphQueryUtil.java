package com.arangodb.util;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.Direction;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Mark - mark@arangodb.com
 *
 */
public class GraphQueryUtil {

	private static final String AND = " && ";
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
			sb.append("FOR v,i IN ");
			appendDepth(graphEdgesOptions, sb);
			appendDirection(graphEdgesOptions, sb);
			sb.append(" @");
			sb.append(VERTEX_EXAMPLE);
			bindVars.put(VERTEX_EXAMPLE, JsonUtils.convertNullToMap(vertexExample));
		} else {
			final List<String> vertexCollections = driver.graphGetVertexCollections(graphName, true);
			if (vertexCollections.size() == 1) {
				sb.append("FOR start IN `");
				sb.append(vertexCollections.get(0));
				sb.append("`");
				appendFilter(vertexExample, sb);
			} else {
				sb.append("FOR start IN UNION (");
				for (String vertexCollection : vertexCollections) {
					sb.append("(FOR start IN `");
					sb.append(vertexCollection);
					sb.append("`");
					appendFilter(vertexExample, sb);
					sb.append(" RETURN start),");
				}
				// remove last ,
				sb.deleteCharAt(sb.length() - 1);
				sb.append(")");
			}
			sb.append(" FOR v,i IN ");
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
		final Integer limit = graphEdgesOptions.getLimit();
		if (limit != null) {
			sb.append(" LIMIT ");
			sb.append(limit.intValue());
		}
		sb.append(" RETURN distinct i");
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

	private static void appendFilter(final Object vertexExample, final StringBuilder sb) {
		Gson gson = new Gson();
		final JsonElement json = gson.toJsonTree(vertexExample);
		if (json.isJsonObject()) {
			sb.append(" FILTER ");
			final JsonObject jsonObject = json.getAsJsonObject();
			final Set<Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
			for (Entry<String, JsonElement> entry : entrySet) {
				sb.append("start.`");
				sb.append(entry.getKey());
				sb.append("` == ");
				sb.append(entry.getValue().toString());
				sb.append(AND);
			}
			sb.delete(sb.length() - AND.length(), sb.length() - 1);
		}
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
