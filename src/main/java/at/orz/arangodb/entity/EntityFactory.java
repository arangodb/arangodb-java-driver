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

package at.orz.arangodb.entity;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;

import at.orz.arangodb.annotations.DocumentKey;
import at.orz.arangodb.annotations.Exclude;
import at.orz.arangodb.entity.CollectionEntity.Figures;
import at.orz.arangodb.entity.EntityDeserializers.CollectionKeyOptionDeserializer;
import at.orz.arangodb.entity.marker.VertexEntity;
import at.orz.arangodb.http.JsonSequenceEntity;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class EntityFactory {

	private static Gson gson;
	private static Gson gsonNull;
	private static GsonBuilder getBuilder() {
		return new GsonBuilder()
			.addSerializationExclusionStrategy(new ExcludeExclusionStrategy(true))
			.addDeserializationExclusionStrategy(new ExcludeExclusionStrategy(false))
			.setFieldNamingStrategy(new ArangoFieldNamingStrategy())
			.registerTypeAdapter(CollectionStatus.class, new CollectionStatusTypeAdapter())
			.registerTypeAdapter(CollectionEntity.class, new EntityDeserializers.CollectionEntityDeserializer())
			.registerTypeAdapter(DocumentEntity.class, new EntityDeserializers.DocumentEntityDeserializer())
			.registerTypeAdapter(DocumentsEntity.class, new EntityDeserializers.DocumentsEntityDeserializer())
			.registerTypeAdapter(ArangoVersion.class, new EntityDeserializers.VersionDeserializer())
			.registerTypeAdapter(ArangoUnixTime.class, new EntityDeserializers.ArangoUnixTimeDeserializer())
			.registerTypeAdapter(DefaultEntity.class, new EntityDeserializers.DefaultEntityDeserializer())
			.registerTypeAdapter(Figures.class, new EntityDeserializers.FiguresDeserializer())
			.registerTypeAdapter(CursorEntity.class, new EntityDeserializers.CursorEntityDeserializer())
			.registerTypeAdapter(IndexEntity.class, new EntityDeserializers.IndexEntityDeserializer())
			.registerTypeAdapter(IndexesEntity.class, new EntityDeserializers.IndexesEntityDeserializer())
			.registerTypeAdapter(ScalarExampleEntity.class, new EntityDeserializers.ScalarExampleEntityDeserializer())
			.registerTypeAdapter(SimpleByResultEntity.class, new EntityDeserializers.SimpleByResultEntityDeserializer())
			.registerTypeAdapter(AdminLogEntity.class, new EntityDeserializers.AdminLogEntryEntityDeserializer())
			.registerTypeAdapter(StatisticsEntity.class, new EntityDeserializers.StatisticsEntityDeserializer())
			.registerTypeAdapter(StatisticsDescriptionEntity.class, new EntityDeserializers.StatisticsDescriptionEntityDeserializer())
			.registerTypeAdapter(ExplainEntity.class, new EntityDeserializers.ExplainEntityDeserializer())
			.registerTypeAdapter(UserEntity.class, new EntityDeserializers.UserEntityDeserializer())
			.registerTypeAdapter(ImportResultEntity.class, new EntityDeserializers.ImportResultEntityDeserializer())
			.registerTypeAdapter(DatabaseEntity.class, new EntityDeserializers.DatabaseEntityDeserializer())
			.registerTypeAdapter(StringsResultEntity.class, new EntityDeserializers.StringsResultEntityDeserializer())
			.registerTypeAdapter(BooleanResultEntity.class, new EntityDeserializers.BooleanResultEntityDeserializer())
			.registerTypeAdapter(Endpoint.class, new EntityDeserializers.EndpointDeserializer())
			.registerTypeAdapter(DocumentResultEntity.class, new EntityDeserializers.DocumentResultEntityDeserializer())
			.registerTypeAdapter(CollectionKeyOptionDeserializer.class, new EntityDeserializers.CollectionKeyOptionDeserializer())
			.registerTypeAdapter(ReplicationInventoryEntity.class, new EntityDeserializers.ReplicationInventoryEntityDeserializer())
			.registerTypeAdapter(ReplicationDumpRecord.class, new EntityDeserializers.ReplicationDumpRecordDeserializer())
			.registerTypeAdapter(ReplicationSyncEntity.class, new EntityDeserializers.ReplicationSyncEntityDeserializer())
			.registerTypeAdapter(MapAsEntity.class, new EntityDeserializers.MapAsEntityDeserializer())
			.registerTypeAdapter(ReplicationLoggerConfigEntity.class, new EntityDeserializers.ReplicationLoggerConfigEntityDeserializer())
			.registerTypeAdapter(ReplicationApplierConfigEntity.class, new EntityDeserializers.ReplicationApplierConfigEntityDeserializer())
			.registerTypeAdapter(ReplicationApplierState.class, new EntityDeserializers.ReplicationApplierStateDeserializer())
			.registerTypeAdapter(ReplicationApplierStateEntity.class, new EntityDeserializers.ReplicationApplierStateEntityDeserializer())
			.registerTypeAdapter(ReplicationLoggerStateEntity.class, new EntityDeserializers.ReplicationLoggerStateEntityDeserializer())
			.registerTypeAdapter(ReplicationLoggerStateEntity.Client.class, new EntityDeserializers.ReplicationLoggerStateEntityClientDeserializer())
			.registerTypeAdapter(GraphEntity.class, new EntityDeserializers.GraphEntityDeserializer())
			.registerTypeAdapter(GraphsEntity.class, new EntityDeserializers.GraphsEntityDeserializer())
			.registerTypeAdapter(DeletedEntity.class, new EntityDeserializers.DeleteEntityDeserializer())
			.registerTypeAdapter(VertexEntity.class, new EntityDeserializers.VertexEntityDeserializer())
			.registerTypeAdapter(EdgeEntity.class, new EntityDeserializers.EdgeEntityDeserializer())
			;
	}
	static {
		gson = getBuilder().create();
		gsonNull = getBuilder().serializeNulls().create();
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
	
	public static <T> String toImportHeaderValues(Collection<? extends Collection<?>> headerValues) {
		StringWriter writer = new StringWriter();
		for (Collection<?> array : headerValues) {
			gson.toJson(array, writer);
			writer.write('\n');
		}
		writer.flush();
		return writer.toString();
	}

	public static <T> String toJsonString(T obj, boolean includeNullValue) {
		return includeNullValue ? gsonNull.toJson(obj) : gson.toJson(obj);
	}

	/**
	 * 
	 * @param obj
	 * @param includeNullValue
	 * @return
	 * @since 1.4.0
	 */
	public static <T> JsonElement toJsonElement(T obj, boolean includeNullValue) {
		return includeNullValue ? gsonNull.toJsonTree(obj) : gson.toJsonTree(obj);
	}
	
	/**
	 * 
	 * @author tamtam180 - kirscheless at gmail.com
	 * @since 1.4.0
	 */
	private static class ExcludeExclusionStrategy implements ExclusionStrategy {
		private boolean serialize;
		public ExcludeExclusionStrategy(boolean serialize) {
			this.serialize = serialize;
		}
		public boolean shouldSkipField(FieldAttributes f) {
			Exclude annotation = f.getAnnotation(Exclude.class);
			if (annotation != null && (serialize ? annotation.serialize() : annotation.deserialize())) {
				return true;
			}
			return false;
		}
		public boolean shouldSkipClass(Class<?> clazz) {
			return false;
		}
	}
	
	private static class ArangoFieldNamingStrategy implements FieldNamingStrategy {
		private static final String KEY = "_key";
		public String translateName(Field f) {
			DocumentKey key = f.getAnnotation(DocumentKey.class);
			if (key == null) {
				return FieldNamingPolicy.IDENTITY.translateName(f);
			}
			return KEY;
		}
	}
}
