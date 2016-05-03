/*
 * Copyright (C) 2012 tamtam180
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arangodb.entity;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;

import com.arangodb.annotations.DocumentKey;
import com.arangodb.annotations.Exclude;
import com.arangodb.entity.CollectionEntity.Figures;
import com.arangodb.entity.EntityDeserializers.CollectionKeyOptionDeserializer;
import com.arangodb.entity.marker.VertexEntity;
import com.arangodb.http.JsonSequenceEntity;
import com.arangodb.util.BaseDocumentCollection;
import com.arangodb.util.JsonUtils;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Entity factory , internally used.
 *
 * @author tamtam180 - kirscheless at gmail.com
 */
public class EntityFactory {

	private static Gson gson;
	private static Gson gsonNull;

	private EntityFactory() {
		// this is a helper class
	}

	public static GsonBuilder getGsonBuilder() {
		return new GsonBuilder().addSerializationExclusionStrategy(new ExcludeExclusionStrategy(true))
				.addDeserializationExclusionStrategy(new ExcludeExclusionStrategy(false))
				.setFieldNamingStrategy(new ArangoFieldNamingStrategy())
				.registerTypeAdapter(CollectionStatus.class, new CollectionStatusTypeAdapter())
				.registerTypeAdapter(CollectionEntity.class, new EntityDeserializers.CollectionEntityDeserializer())
				.registerTypeAdapter(DocumentEntity.class, new EntityDeserializers.DocumentEntityDeserializer())
				.registerTypeAdapter(DocumentsEntity.class, new EntityDeserializers.DocumentsEntityDeserializer())
				.registerTypeAdapter(AqlFunctionsEntity.class, new EntityDeserializers.AqlfunctionsEntityDeserializer())
				.registerTypeAdapter(JobsEntity.class, new EntityDeserializers.JobsEntityDeserializer())
				.registerTypeAdapter(ArangoVersion.class, new EntityDeserializers.VersionDeserializer())
				.registerTypeAdapter(ArangoUnixTime.class, new EntityDeserializers.ArangoUnixTimeDeserializer())
				.registerTypeAdapter(DefaultEntity.class, new EntityDeserializers.DefaultEntityDeserializer())
				.registerTypeAdapter(Figures.class, new EntityDeserializers.FiguresDeserializer())
				.registerTypeAdapter(CursorEntity.class, new EntityDeserializers.CursorEntityDeserializer())
				.registerTypeAdapter(IndexEntity.class, new EntityDeserializers.IndexEntityDeserializer())
				.registerTypeAdapter(IndexesEntity.class, new EntityDeserializers.IndexesEntityDeserializer())
				.registerTypeAdapter(ScalarExampleEntity.class,
					new EntityDeserializers.ScalarExampleEntityDeserializer())
				.registerTypeAdapter(SimpleByResultEntity.class,
					new EntityDeserializers.SimpleByResultEntityDeserializer())
				.registerTypeAdapter(TransactionResultEntity.class,
					new EntityDeserializers.TransactionResultEntityDeserializer())
				.registerTypeAdapter(AdminLogEntity.class, new EntityDeserializers.AdminLogEntryEntityDeserializer())
				.registerTypeAdapter(StatisticsEntity.class, new EntityDeserializers.StatisticsEntityDeserializer())
				.registerTypeAdapter(StatisticsDescriptionEntity.class,
					new EntityDeserializers.StatisticsDescriptionEntityDeserializer())
				.registerTypeAdapter(UserEntity.class, new EntityDeserializers.UserEntityDeserializer())
				.registerTypeAdapter(ImportResultEntity.class, new EntityDeserializers.ImportResultEntityDeserializer())
				.registerTypeAdapter(DatabaseEntity.class, new EntityDeserializers.DatabaseEntityDeserializer())
				.registerTypeAdapter(StringsResultEntity.class,
					new EntityDeserializers.StringsResultEntityDeserializer())
				.registerTypeAdapter(BooleanResultEntity.class,
					new EntityDeserializers.BooleanResultEntityDeserializer())
				.registerTypeAdapter(Endpoint.class, new EntityDeserializers.EndpointDeserializer())
				.registerTypeAdapter(DocumentResultEntity.class,
					new EntityDeserializers.DocumentResultEntityDeserializer())
				.registerTypeAdapter(CollectionKeyOptionDeserializer.class,
					new EntityDeserializers.CollectionKeyOptionDeserializer())
				.registerTypeAdapter(ReplicationInventoryEntity.class,
					new EntityDeserializers.ReplicationInventoryEntityDeserializer())
				.registerTypeAdapter(ReplicationDumpRecord.class,
					new EntityDeserializers.ReplicationDumpRecordDeserializer())
				.registerTypeAdapter(ReplicationSyncEntity.class,
					new EntityDeserializers.ReplicationSyncEntityDeserializer())
				.registerTypeAdapter(MapAsEntity.class, new EntityDeserializers.MapAsEntityDeserializer())
				.registerTypeAdapter(ReplicationLoggerConfigEntity.class,
					new EntityDeserializers.ReplicationLoggerConfigEntityDeserializer())
				.registerTypeAdapter(ReplicationApplierConfigEntity.class,
					new EntityDeserializers.ReplicationApplierConfigEntityDeserializer())
				.registerTypeAdapter(ReplicationApplierState.class,
					new EntityDeserializers.ReplicationApplierStateDeserializer())
				.registerTypeAdapter(ReplicationApplierStateEntity.class,
					new EntityDeserializers.ReplicationApplierStateEntityDeserializer())
				.registerTypeAdapter(ReplicationLoggerStateEntity.class,
					new EntityDeserializers.ReplicationLoggerStateEntityDeserializer())
				.registerTypeAdapter(ReplicationLoggerStateEntity.Client.class,
					new EntityDeserializers.ReplicationLoggerStateEntityClientDeserializer())
				.registerTypeAdapter(GraphEntity.class, new EntityDeserializers.GraphEntityDeserializer())
				.registerTypeAdapter(GraphsEntity.class, new EntityDeserializers.GraphsEntityDeserializer())
				.registerTypeAdapter(DeletedEntity.class, new EntityDeserializers.DeleteEntityDeserializer())
				.registerTypeAdapter(VertexEntity.class, new EntityDeserializers.VertexEntityDeserializer())
				.registerTypeAdapter(EdgeEntity.class, new EntityDeserializers.EdgeEntityDeserializer())
				.registerTypeAdapter(TraversalEntity.class, new EntityDeserializers.TraversalEntityDeserializer())
				.registerTypeAdapter(ShortestPathEntity.class, new EntityDeserializers.ShortestPathEntityDeserializer())
				.registerTypeAdapter(QueryCachePropertiesEntity.class,
					new EntityDeserializers.QueryCachePropertiesEntityDeserializer())
				.registerTypeAdapter(QueriesResultEntity.class,
					new EntityDeserializers.QueriesResultEntityDeserializer())
				.registerTypeAdapter(QueryTrackingPropertiesEntity.class,
					new EntityDeserializers.QueryTrackingPropertiesEntityDeserializer())
				.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	}

	static {
		configure(getGsonBuilder());
	}

	/**
	 * Configures instances of Gson used by this factory.
	 * 
	 * @param builders
	 *            one or two GsonBuilder instances. If only one is provided it
	 *            will be used for initializing both <code>gson</code> and
	 *            <code>gsonNull</code> fields (latter with
	 *            <code>serializeNulls()</code> called prior to creating). If
	 *            two are given - first initializes <code>gson</code> field,
	 *            second initializes <code>gsonNull</code> (used when
	 *            serialization of nulls is requested).
	 */
	public static void configure(GsonBuilder... builders) {
		if (builders.length < 1) {
			throw new IllegalArgumentException("builders");
		}

		gson = builders[0].create();

		if (builders.length > 1) {
			gsonNull = builders[1].create();
		} else {
			// use the first one again, but with nulls serialization turned on
			gsonNull = builders[0].serializeNulls().create();
		}
	}

	public static <T> T createEntity(String jsonText, Type type) {
		return gson.fromJson(jsonText, type);
	}

	public static <T> String toJsonString(T obj) {
		return toJsonString(obj, false);
	}

	public static <T> JsonSequenceEntity toJsonSequenceEntity(Iterator<T> itr) {
		return new JsonSequenceEntity(itr, gson);
	}

	public static String toImportHeaderValues(Collection<? extends Collection<?>> headerValues) {
		StringWriter writer = new StringWriter();
		for (Collection<?> array : headerValues) {
			gson.toJson(array, writer);
			writer.write('\n');
		}
		writer.flush();
		return writer.toString();
	}

	public static <T> String toJsonString(T obj, boolean includeNullValue) {
		if (obj != null && ((obj instanceof BaseDocument) || (obj instanceof BaseDocumentCollection))) {
			String tmp = includeNullValue ? gsonNull.toJson(obj) : gson.toJson(obj);

			JsonParser jsonParser = new JsonParser();
			JsonElement jsonElement = jsonParser.parse(tmp);

			if (jsonElement.isJsonArray()) {
				JsonArray jsonArray = jsonElement.getAsJsonArray();

				StringBuilder builder = new StringBuilder();
				builder.append("[");
				if (0 != jsonArray.size()) {

					builder.append(JsonUtils.convertBaseDocumentToJson(jsonArray.get(0).getAsJsonObject()));
					for (int i = 1; i < jsonArray.size(); ++i) {
						builder.append(",");
						builder.append(JsonUtils.convertBaseDocumentToJson(jsonArray.get(0).getAsJsonObject()));
					}

				}
				builder.append("]");

				return builder.toString();
			} else {
				return JsonUtils.convertBaseDocumentToJson(jsonElement.getAsJsonObject());
			}
		}

		return includeNullValue ? gsonNull.toJson(obj) : gson.toJson(obj);
	}

	/**
	 * @param <T>
	 * @param obj
	 * @param includeNullValue
	 * @return a JsonElement object
	 * @since 1.4.0
	 */
	public static <T> JsonElement toJsonElement(T obj, boolean includeNullValue) {
		return includeNullValue ? gsonNull.toJsonTree(obj) : gson.toJsonTree(obj);
	}

	/**
	 * @author tamtam180 - kirscheless at gmail.com
	 * @since 1.4.0
	 */
	private static class ExcludeExclusionStrategy implements ExclusionStrategy {
		private final boolean serialize;

		public ExcludeExclusionStrategy(boolean serialize) {
			this.serialize = serialize;
		}

		@Override
		public boolean shouldSkipField(FieldAttributes f) {
			Exclude annotation = f.getAnnotation(Exclude.class);
			return annotation != null && (serialize ? annotation.serialize() : annotation.deserialize());
		}

		@Override
		public boolean shouldSkipClass(Class<?> clazz) {
			return false;
		}
	}

	private static class ArangoFieldNamingStrategy implements FieldNamingStrategy {
		private static final String KEY = "_key";

		@Override
		public String translateName(Field f) {
			DocumentKey key = f.getAnnotation(DocumentKey.class);
			if (key == null) {
				return FieldNamingPolicy.IDENTITY.translateName(f);
			}
			return KEY;
		}
	}
}
