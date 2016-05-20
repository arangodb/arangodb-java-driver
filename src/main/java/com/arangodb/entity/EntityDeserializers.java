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

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.entity.CollectionEntity.Figures;
import com.arangodb.entity.ReplicationApplierState.LastError;
import com.arangodb.entity.ReplicationApplierState.Progress;
import com.arangodb.entity.ReplicationInventoryEntity.Collection;
import com.arangodb.entity.ReplicationInventoryEntity.CollectionParameter;
import com.arangodb.entity.ReplicationLoggerStateEntity.Client;
import com.arangodb.entity.StatisticsDescriptionEntity.Figure;
import com.arangodb.entity.StatisticsDescriptionEntity.Group;
import com.arangodb.entity.StatisticsEntity.FigureValue;
import com.arangodb.entity.marker.VertexEntity;
import com.arangodb.util.DateUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

/**
 * Entity deserializer , internally used.
 *
 * @author tamtam180 - kirscheless at gmail.com
 * 
 */
public class EntityDeserializers {

	private static final String ADAPTIVE_POLLING = "adaptivePolling";

	private static final String AUTO_START = "autoStart";

	private static final String CHUNK_SIZE = "chunkSize";

	private static final String REQUEST_TIMEOUT = "requestTimeout";

	private static final String CONNECT_TIMEOUT = "connectTimeout";

	private static final String MAX_CONNECT_RETRIES = "maxConnectRetries";

	private static final String PASSWORD = "password";

	private static final String USERNAME = "username";

	private static final String DATABASE = "database";

	private static final String ENDPOINT = "endpoint";

	private static final String SERVER = "server";

	private static final String ETAG = "etag";

	private static final String ERROR_MESSAGE = "errorMessage";

	private static final String ERROR_NUM = "errorNum";

	private static final String CODE = "code";

	private static final String ERROR = "error";

	private static final String RESULT = "result";

	private static final String VERSION = "version";

	private static final String PATHS = "paths";

	private static final String VERTICES = "vertices";

	private static final String SIZE = "size";

	private static final String COUNT = "count";

	private static final String ALIVE = "alive";

	private static final String WARNINGS = "warnings";

	private static final String ALT_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

	private static final String DO_COMPACT = "doCompact";

	private static final String CHECKSUM = "checksum";

	private static final String KEY_OPTIONS = "keyOptions";

	private static final String TYPE = "type";

	private static final String FIGURES = "figures";

	private static final String REVISION = "revision";

	private static final String JOURNAL_SIZE = "journalSize";

	private static final String IS_VOLATILE = "isVolatile";

	private static final String IS_SYSTEM = "isSystem";

	private static final String WAIT_FOR_SYNC = "waitForSync";

	private static final String STATUS = "status";

	private static final String ID = "id";

	private static final String NAME = "name";

	private static final String EXTRA = "extra";

	private static final String ACTIVE = "active";

	private static final String COLLECTIONS = "collections";

	private static final String LAST_ERROR = "lastError";

	private static final String LAST_AVAILABLE_CONTINUOUS_TICK = "lastAvailableContinuousTick";

	private static final String LAST_PROCESSED_CONTINUOUS_TICK = "lastProcessedContinuousTick";

	private static final String LAST_APPLIED_CONTINUOUS_TICK = "lastAppliedContinuousTick";

	private static final String INDEXES = "indexes";

	private static final String EDGES = "edges";

	private static final String STATE = "state";

	private static final String FILE_SIZE = "fileSize";

	private static final String UPDATED = "updated";

	private static final String REPLACED = "replaced";

	private static final String DELETED = "deleted";

	private static final String MESSAGE = "message";

	private static Logger logger = LoggerFactory.getLogger(EntityDeserializers.class);

	private static class ClassHolder {
		private Class<?>[] clazz;
		private int idx;

		ClassHolder(Class<?>... clazz) {
			this.clazz = clazz;
			this.idx = 0;
		}

		public boolean isEmpty() {
			return clazz == null || clazz.length == 0;
		}

		public Class<?> get() {
			if (isEmpty()) {
				return null;
			}
			return clazz[idx];
		}

		public Class<?> next() {
			if (isEmpty()) {
				return null;
			}
			if (idx + 1 >= clazz.length) {
				throw new IllegalStateException("idx max-over!! idx=" + (idx + 1));
			}
			return clazz[++idx];
		}

		public boolean hasNext() {
			if (isEmpty()) {
				return false;
			}
			if (idx + 1 >= clazz.length) {
				return false;
			}
			return true;
		}

		public Class<?> back() {
			if (isEmpty()) {
				return null;
			}
			if (idx - 1 < 0) {
				throw new IllegalStateException("idx min-over!! idx=" + (idx - 1));
			}
			return clazz[--idx];
		}
	}

	private static ThreadLocal<ClassHolder> parameterizedBridger = new ThreadLocal<ClassHolder>();

	private EntityDeserializers() {
		// this is a helper class
	}

	public static void setParameterized(Class<?>... clazz) {
		parameterizedBridger.set(new ClassHolder(clazz));
	}

	public static void removeParameterized() {
		parameterizedBridger.remove();
	}

	private static Class<?> getParameterized() {
		ClassHolder holder = parameterizedBridger.get();
		if (holder == null) {
			return null;
		}
		return holder.get();
	}

	private static boolean hasNextParameterized() {
		ClassHolder holder = parameterizedBridger.get();
		if (holder == null) {
			return false;
		}
		return holder.hasNext();
	}

	private static Class<?> nextParameterized() {
		ClassHolder holder = parameterizedBridger.get();
		if (holder == null) {
			return null;
		}
		return holder.next();
	}

	private static <T extends BaseEntity> T deserializeBaseParameter(JsonObject obj, T entity) {
		if (obj.has(ERROR) && obj.getAsJsonPrimitive(ERROR).isBoolean()) {
			entity.error = obj.getAsJsonPrimitive(ERROR).getAsBoolean();
		}
		if (obj.has(CODE) && obj.getAsJsonPrimitive(CODE).isNumber()) {
			entity.code = obj.getAsJsonPrimitive(CODE).getAsInt();
		}
		if (obj.has(ERROR_NUM) && obj.getAsJsonPrimitive(ERROR_NUM).isNumber()) {
			entity.errorNumber = obj.getAsJsonPrimitive(ERROR_NUM).getAsInt();
		}
		if (obj.has(ERROR_MESSAGE)) {
			entity.errorMessage = obj.getAsJsonPrimitive(ERROR_MESSAGE).getAsString();
		}
		if (obj.has(ETAG) && obj.getAsJsonPrimitive(ERROR_NUM).isNumber()) {
			entity.etag = obj.getAsJsonPrimitive(ETAG).getAsLong();
		}

		return entity;
	}

	private static <T extends DocumentHolder> T deserializeDocumentParameter(JsonObject obj, T entity) {

		if (obj.has("_rev")) {
			entity.setDocumentRevision(obj.getAsJsonPrimitive("_rev").getAsLong());
		}
		if (obj.has("_id")) {
			entity.setDocumentHandle(obj.getAsJsonPrimitive("_id").getAsString());
		}
		if (obj.has("_key")) {
			entity.setDocumentKey(obj.getAsJsonPrimitive("_key").getAsString());
		}

		return entity;
	}

	public static class DefaultEntityDeserializer implements JsonDeserializer<DefaultEntity> {
		@Override
		public DefaultEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
			if (json.isJsonNull()) {
				return null;
			}
			return deserializeBaseParameter(json.getAsJsonObject(), new DefaultEntity());
		}
	}

	public static class VersionDeserializer implements JsonDeserializer<ArangoVersion> {

		@Override
		public ArangoVersion deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			ArangoVersion entity = deserializeBaseParameter(obj, new ArangoVersion());

			if (obj.has(SERVER)) {
				entity.server = obj.getAsJsonPrimitive(SERVER).getAsString();
			}

			if (obj.has(VERSION)) {
				entity.version = obj.getAsJsonPrimitive(VERSION).getAsString();
			}

			return entity;
		}
	}

	public static class ArangoUnixTimeDeserializer implements JsonDeserializer<ArangoUnixTime> {
		@Override
		public ArangoUnixTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			ArangoUnixTime entity = deserializeBaseParameter(obj, new ArangoUnixTime());

			if (obj.has("time")) {
				entity.time = obj.getAsJsonPrimitive("time").getAsDouble();
				String time = obj.getAsJsonPrimitive("time").getAsString(); // 実際はdoubleだけど精度の問題が心配なので文字列で処理する。
				entity.second = (int) entity.time;

				int pos = time.indexOf('.');
				entity.microsecond = (pos >= 0 && pos + 1 != time.length()) ? Integer.parseInt(time.substring(pos + 1))
						: 0;
			}

			return entity;
		}
	}

	public static class FiguresDeserializer implements JsonDeserializer<Figures> {

		@Override
		public Figures deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			Figures entity = new Figures();

			if (obj.has(ALIVE)) {
				JsonObject alive = obj.getAsJsonObject(ALIVE);
				entity.aliveCount = alive.getAsJsonPrimitive(COUNT).getAsLong();
				entity.aliveSize = alive.getAsJsonPrimitive(SIZE).getAsLong();
			}

			if (obj.has("dead")) {
				JsonObject dead = obj.getAsJsonObject("dead");
				entity.deadCount = dead.getAsJsonPrimitive(COUNT).getAsLong();
				entity.deadSize = dead.getAsJsonPrimitive(SIZE).getAsLong();
				entity.deadDeletion = dead.getAsJsonPrimitive("deletion").getAsLong();
			}

			if (obj.has("datafiles")) {
				JsonObject datafiles = obj.getAsJsonObject("datafiles");
				entity.datafileCount = datafiles.getAsJsonPrimitive(COUNT).getAsLong();
				entity.datafileFileSize = datafiles.getAsJsonPrimitive(FILE_SIZE).getAsLong();
			}

			if (obj.has("journals")) {
				JsonObject journals = obj.getAsJsonObject("journals");
				entity.journalsCount = journals.getAsJsonPrimitive(COUNT).getAsLong();
				entity.journalsFileSize = journals.getAsJsonPrimitive(FILE_SIZE).getAsLong();
			}

			if (obj.has("compactors")) {
				JsonObject compactors = obj.getAsJsonObject("compactors");
				entity.compactorsCount = compactors.getAsJsonPrimitive(COUNT).getAsLong();
				entity.compactorsFileSize = compactors.getAsJsonPrimitive(FILE_SIZE).getAsLong();
			}

			if (obj.has("shapefiles")) {
				JsonObject shapefiles = obj.getAsJsonObject("shapefiles");
				entity.shapefilesCount = shapefiles.getAsJsonPrimitive(COUNT).getAsLong();
				entity.shapefilesFileSize = shapefiles.getAsJsonPrimitive(FILE_SIZE).getAsLong();
			}

			if (obj.has("shapes")) {
				JsonObject shapes = obj.getAsJsonObject("shapes");
				entity.shapesCount = shapes.getAsJsonPrimitive(COUNT).getAsLong();
			}

			if (obj.has("attributes")) {
				JsonObject attributes = obj.getAsJsonObject("attributes");
				entity.attributesCount = attributes.getAsJsonPrimitive(COUNT).getAsLong();
			}

			if (obj.has(INDEXES)) {
				JsonObject indexes = obj.getAsJsonObject(INDEXES);
				entity.indexesCount = indexes.getAsJsonPrimitive(COUNT).getAsLong();
				entity.indexesSize = indexes.getAsJsonPrimitive(SIZE).getAsLong();
			}

			if (obj.has("lastTick")) {
				entity.lastTick = obj.getAsJsonPrimitive("lastTick").getAsLong();
			}

			if (obj.has("uncollectedLogfileEntries")) {
				entity.uncollectedLogfileEntries = obj.getAsJsonPrimitive("uncollectedLogfileEntries").getAsLong();
			}

			return entity;
		}
	}

	public static class CollectionKeyOptionDeserializer implements JsonDeserializer<CollectionKeyOption> {
		@Override
		public CollectionKeyOption deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			CollectionKeyOption entity = new CollectionKeyOption();

			if (obj.has("type")) {
				entity.type = obj.getAsJsonPrimitive("type").getAsString();
			}

			if (obj.has("allowUserKeys")) {
				entity.allowUserKeys = obj.getAsJsonPrimitive("allowUserKeys").getAsBoolean();
			}

			if (obj.has("increment")) {
				entity.increment = obj.getAsJsonPrimitive("increment").getAsLong();
			}

			if (obj.has("offset")) {
				entity.offset = obj.getAsJsonPrimitive("offset").getAsLong();
			}

			return entity;
		}
	}

	public static class CollectionEntityDeserializer implements JsonDeserializer<CollectionEntity> {

		@Override
		public CollectionEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			CollectionEntity entity = deserializeBaseParameter(obj, new CollectionEntity());

			if (obj.has(NAME)) {
				entity.name = obj.getAsJsonPrimitive(NAME).getAsString();
			}

			if (obj.has(ID)) {
				entity.id = obj.getAsJsonPrimitive(ID).getAsLong();
			}

			if (obj.has(STATUS)) {
				entity.status = context.deserialize(obj.get(STATUS), CollectionStatus.class);
			}

			if (obj.has(WAIT_FOR_SYNC)) {
				entity.waitForSync = obj.getAsJsonPrimitive(WAIT_FOR_SYNC).getAsBoolean();
			}

			if (obj.has(IS_SYSTEM)) {
				entity.isSystem = obj.getAsJsonPrimitive(IS_SYSTEM).getAsBoolean();
			}

			if (obj.has(IS_VOLATILE)) {
				entity.isVolatile = obj.getAsJsonPrimitive(IS_VOLATILE).getAsBoolean();
			}

			if (obj.has(JOURNAL_SIZE)) {
				entity.journalSize = obj.getAsJsonPrimitive(JOURNAL_SIZE).getAsLong();
			}

			if (obj.has(COUNT)) {
				entity.count = obj.getAsJsonPrimitive(COUNT).getAsLong();
			}

			if (obj.has(REVISION)) {
				entity.revision = obj.getAsJsonPrimitive(REVISION).getAsLong();
			}

			if (obj.has(FIGURES)) {
				entity.figures = context.deserialize(obj.get(FIGURES), Figures.class);
			}

			if (obj.has(TYPE)) {
				entity.type = CollectionType.valueOf(obj.getAsJsonPrimitive(TYPE).getAsInt());
			}

			if (obj.has(KEY_OPTIONS)) {
				entity.keyOptions = context.deserialize(obj.get(KEY_OPTIONS), CollectionKeyOption.class);
			}

			if (obj.has(CHECKSUM)) {
				entity.checksum = obj.getAsJsonPrimitive(CHECKSUM).getAsLong();
			}

			if (obj.has(DO_COMPACT)) {
				entity.doCompact = obj.getAsJsonPrimitive(DO_COMPACT).getAsBoolean();
			}

			return entity;
		}
	}

	public static class CollectionsEntityDeserializer implements JsonDeserializer<CollectionsEntity> {
		private Type collectionsType = new TypeToken<List<CollectionEntity>>() {
		}.getType();
		private Type namesType = new TypeToken<Map<String, CollectionEntity>>() {
		}.getType();

		@Override
		public CollectionsEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			CollectionsEntity entity = deserializeBaseParameter(obj, new CollectionsEntity());

			if (obj.has(COLLECTIONS)) {
				entity.collections = context.deserialize(obj.get(COLLECTIONS), collectionsType);
			}
			if (obj.has("names")) {
				entity.names = context.deserialize(obj.get("names"), namesType);
			}

			return entity;
		}
	}

	public static class AqlfunctionsEntityDeserializer implements JsonDeserializer<AqlFunctionsEntity> {

		@Override
		public AqlFunctionsEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonArray obj = json.getAsJsonArray();
			Iterator<JsonElement> iterator = obj.iterator();
			Map<String, String> functions = new HashMap<String, String>();
			while (iterator.hasNext()) {
				JsonElement e = iterator.next();
				JsonObject o = e.getAsJsonObject();
				functions.put(o.get("name").getAsString(), o.get(CODE).getAsString());
			}
			return new AqlFunctionsEntity(functions);
		}
	}

	public static class JobsEntityDeserializer implements JsonDeserializer<JobsEntity> {

		@Override
		public JobsEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonArray obj = json.getAsJsonArray();
			Iterator<JsonElement> iterator = obj.iterator();
			List<String> jobs = new ArrayList<String>();
			while (iterator.hasNext()) {
				JsonElement e = iterator.next();
				jobs.add(e.getAsString());
			}
			return new JobsEntity(jobs);
		}
	}

	public static class CursorEntityDeserializer implements JsonDeserializer<CursorEntity<?>> {

		private Type bindVarsType = new TypeToken<List<String>>() {
		}.getType();

		private Type extraType = new TypeToken<Map<String, Object>>() {
		}.getType();

		@Override
		public CursorEntity<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			CursorEntity<Object> entity = deserializeBaseParameter(obj, new CursorEntity<Object>());

			if (obj.has(RESULT)) {
				JsonArray array = obj.getAsJsonArray(RESULT);
				if (array == null || array.isJsonNull() || array.size() == 0) {
					entity.results = Collections.emptyList();
				} else {
					getResultObjects(context, entity, array);
				}
			}

			if (obj.has("hasMore")) {
				entity.hasMore = obj.getAsJsonPrimitive("hasMore").getAsBoolean();
			}

			if (obj.has(COUNT)) {
				entity.count = obj.getAsJsonPrimitive(COUNT).getAsInt();
			}

			if (obj.has(ID)) {
				entity.cursorId = obj.getAsJsonPrimitive(ID).getAsLong();
			}

			if (obj.has("cached")) {
				entity.cached = obj.getAsJsonPrimitive("cached").getAsBoolean();
			}

			if (obj.has("bindVars")) {
				entity.bindVars = context.deserialize(obj.get("bindVars"), bindVarsType);
			}

			entity.warnings = new ArrayList<WarningEntity>();
			if (obj.has(EXTRA)) {
				entity.extra = context.deserialize(obj.get(EXTRA), extraType);
				getFullCount(entity);
				getWarnings(entity);
			}

			return entity;
		}

		private void getResultObjects(
			JsonDeserializationContext context,
			CursorEntity<Object> entity,
			JsonArray array) {
			Class<?> clazz = getParameterized();
			boolean withDocument = DocumentEntity.class.isAssignableFrom(clazz);
			if (withDocument) {
				nextParameterized();
			}
			try {
				List<Object> list = new ArrayList<Object>(array.size());
				for (int i = 0, imax = array.size(); i < imax; i++) {
					list.add(context.deserialize(array.get(i), clazz));
				}
				entity.results = list;
			} finally {
				if (withDocument) {
					backParameterized();
				}
			}
		}

		private void getWarnings(CursorEntity<Object> entity) {
			if (entity.extra.containsKey(WARNINGS)) {
				Object object = entity.extra.get(WARNINGS);
				if (object instanceof List<?>) {
					List<?> l = (List<?>) entity.extra.get(WARNINGS);
					getWarningsFromList(entity, l);
				}
			}
		}

		private void getWarningsFromList(CursorEntity<Object> entity, List<?> l) {
			for (Object o : l) {
				if (o instanceof Map<?, ?>) {
					Map<?, ?> m = (Map<?, ?>) o;
					if (m.containsKey(CODE) && m.get(CODE) instanceof Double && m.containsKey(MESSAGE)
							&& m.get(MESSAGE) instanceof String) {
						Long code = ((Double) m.get(CODE)).longValue();
						String message = (String) m.get(MESSAGE);
						entity.warnings.add(new WarningEntity(code, message));
					}
				}
			}
		}

		private void getFullCount(CursorEntity<Object> entity) {
			if (entity.extra.containsKey("stats") && entity.extra.get("stats") instanceof Map<?, ?>) {
				Map<?, ?> m = (Map<?, ?>) entity.extra.get("stats");
				if (m.containsKey("fullCount") && m.get("fullCount") instanceof Double) {
					Double v = (Double) m.get("fullCount");
					entity.fullCount = v.intValue();
				}
			}
		}

		private Class<?> backParameterized() {
			ClassHolder holder = parameterizedBridger.get();
			if (holder == null) {
				return null;
			}
			return holder.back();
		}

	}

	public static class DocumentEntityDeserializer implements JsonDeserializer<DocumentEntity<?>> {

		@Override
		public DocumentEntity<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return new DocumentEntity<Object>();
			}

			if (json.isJsonPrimitive()) {
				return new DocumentEntity<Object>();
			}

			if (json.isJsonArray()) {
				return new DocumentEntity<Object>();
			}

			JsonObject obj = json.getAsJsonObject();

			DocumentEntity<Object> entity = new DocumentEntity<Object>();
			deserializeDocumentParameter(obj, entity);

			// 他のフィールドはリフレクションで。 (TODO: Annotationのサポートと上記パラメータを弾く)
			Class<?> clazz = getParameterized();
			if (clazz != null) {
				entity.entity = context.deserialize(obj, clazz);
				// if
				// (clazz.getName().equalsIgnoreCase(BaseDocument.class.getName()))
				// {
				// // iterate all key/value pairs of the jsonObject and
				// // determine its class(String, Number, Boolean, HashMap,
				// // List)
				// ((BaseDocument)
				// entity.entity).setProperties(DeserializeSingleEntry.deserializeJsonObject(obj));
				// }
			}

			return entity;
		}
	}

	public static class DeserializeSingleEntry {

		private static final List<String> nonProperties = Arrays.asList("_id", "_rev", "_key");

		private DeserializeSingleEntry() {
			// this is a helper class
		}

		/**
		 * deserialize any jsonElement
		 *
		 * @param jsonElement
		 * @return a object
		 */
		public static Object deserializeJsonElement(JsonElement jsonElement) {
			if (jsonElement.getClass() == JsonPrimitive.class) {
				return deserializeJsonPrimitive((JsonPrimitive) jsonElement);
			} else if (jsonElement.getClass() == JsonArray.class) {
				return deserializeJsonArray((JsonArray) jsonElement);
			} else if (jsonElement.getClass() == JsonObject.class) {
				return deserializeJsonObject((JsonObject) jsonElement);
			}
			return null;
		}

		/**
		 * deserializes a JsonObject into a Map<String, Object>
		 *
		 * @param jsonObject
		 *            a jsonObject
		 * @return the deserialized jsonObject
		 */
		private static Map<String, Object> deserializeJsonObject(JsonObject jsonObject) {
			Map<String, Object> result = new HashMap<String, Object>();
			Set<Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
			for (Map.Entry<String, JsonElement> entry : entrySet) {
				if (!nonProperties.contains(entry.getKey())) {
					result.put(entry.getKey(), deserializeJsonElement(jsonObject.get(entry.getKey())));
				}
			}
			return result;
		}

		private static List<Object> deserializeJsonArray(JsonArray jsonArray) {
			List<Object> tmpObjectList = new ArrayList<Object>();
			Iterator<JsonElement> iterator = jsonArray.iterator();
			while (iterator.hasNext()) {
				tmpObjectList.add(deserializeJsonElement(iterator.next()));
			}
			return tmpObjectList;
		}

		/**
		 * deserializes a jsonPrimitiv into the equivalent java primitive
		 *
		 * @param jsonPrimitive
		 * @return null|String|Double|Boolean
		 */
		private static Object deserializeJsonPrimitive(JsonPrimitive jsonPrimitive) {
			if (jsonPrimitive.isBoolean()) {
				return jsonPrimitive.getAsBoolean();
			} else if (jsonPrimitive.isNumber()) {
				return jsonPrimitive.getAsDouble();
			} else if (jsonPrimitive.isString()) {
				return jsonPrimitive.getAsString();
			}
			return null;
		}

	}

	public static class DocumentsEntityDeserializer implements JsonDeserializer<DocumentsEntity> {
		private Type documentsType = new TypeToken<List<String>>() {
		}.getType();

		@Override
		public DocumentsEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			DocumentsEntity entity = deserializeBaseParameter(obj, new DocumentsEntity());

			if (obj.has("documents")) {
				entity.documents = context.deserialize(obj.get("documents"), documentsType);
			}

			return entity;
		}

	}

	public static class IndexEntityDeserializer implements JsonDeserializer<IndexEntity> {
		private Type fieldsType = new TypeToken<List<String>>() {
		}.getType();

		@Override
		public IndexEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			IndexEntity entity = deserializeBaseParameter(obj, new IndexEntity());

			if (obj.has(ID)) {
				entity.id = obj.getAsJsonPrimitive(ID).getAsString();
			}

			if (obj.has("type")) {
				String type = obj.getAsJsonPrimitive("type").getAsString().toUpperCase(Locale.US);
				if (type.startsWith(IndexType.GEO.name())) {
					entity.type = IndexType.GEO;
				} else {
					entity.type = IndexType.valueOf(type);
				}
			}

			if (obj.has("fields")) {
				entity.fields = context.deserialize(obj.getAsJsonArray("fields"), fieldsType);
			}

			if (obj.has("geoJson")) {
				entity.geoJson = obj.getAsJsonPrimitive("geoJson").getAsBoolean();
			}

			if (obj.has("isNewlyCreated")) {
				entity.isNewlyCreated = obj.getAsJsonPrimitive("isNewlyCreated").getAsBoolean();
			}

			if (obj.has("unique")) {
				entity.unique = obj.getAsJsonPrimitive("unique").getAsBoolean();
			}

			if (obj.has("sparse")) {
				entity.sparse = obj.getAsJsonPrimitive("sparse").getAsBoolean();
			}

			if (obj.has("size")) {
				entity.size = obj.getAsJsonPrimitive("size").getAsInt();
			}

			if (obj.has("minLength")) {
				entity.minLength = obj.getAsJsonPrimitive("minLength").getAsInt();
			}

			if (obj.has("selectivityEstimate")) {
				entity.selectivityEstimate = obj.getAsJsonPrimitive("selectivityEstimate").getAsDouble();
			}

			return entity;
		}
	}

	public static class IndexesEntityDeserializer implements JsonDeserializer<IndexesEntity> {
		private Type indexesType = new TypeToken<List<IndexEntity>>() {
		}.getType();
		private Type identifiersType = new TypeToken<Map<String, IndexEntity>>() {
		}.getType();

		@Override
		public IndexesEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			IndexesEntity entity = deserializeBaseParameter(obj, new IndexesEntity());

			if (obj.has(INDEXES)) {
				entity.indexes = context.deserialize(obj.get(INDEXES), indexesType);
			}

			if (obj.has("identifiers")) {
				entity.identifiers = context.deserialize(obj.get("identifiers"), identifiersType);
			}

			return entity;
		}

	}

	public static class AdminLogEntryEntityDeserializer implements JsonDeserializer<AdminLogEntity> {
		@Override
		public AdminLogEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			AdminLogEntity entity = deserializeBaseParameter(obj, new AdminLogEntity());
			// 全ての要素は必ずあることが前提なのでhasチェックはしない
			int[] lids = context.deserialize(obj.getAsJsonArray("lid"), int[].class);
			int[] levels = context.deserialize(obj.getAsJsonArray("level"), int[].class);
			long[] timestamps = context.deserialize(obj.getAsJsonArray("timestamp"), long[].class);
			String[] texts = context.deserialize(obj.getAsJsonArray("text"), String[].class);

			// 配列のサイズが全て同じであること
			if (lids.length != levels.length || lids.length != timestamps.length || lids.length != texts.length) {
				throw new IllegalStateException("each parameters returns wrong length.");
			}

			entity.logs = new ArrayList<AdminLogEntity.LogEntry>(lids.length);
			for (int i = 0; i < lids.length; i++) {
				AdminLogEntity.LogEntry entry = new AdminLogEntity.LogEntry();
				entry.lid = lids[i];
				entry.level = levels[i];
				entry.timestamp = new Date(timestamps[i] * 1000L);
				entry.text = texts[i];
				entity.logs.add(entry);
			}

			if (obj.has("totalAmount")) {
				entity.totalAmount = obj.getAsJsonPrimitive("totalAmount").getAsInt();
			}

			return entity;
		}
	}

	public static class StatisticsEntityDeserializer implements JsonDeserializer<StatisticsEntity> {
		Type countsType = new TypeToken<long[]>() {
		}.getType();

		@Override
		public StatisticsEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			StatisticsEntity entity = deserializeBaseParameter(obj, new StatisticsEntity());

			deserializeSystem(obj, entity);

			deserializeClient(context, obj, entity);

			deserializeServer(obj, entity);

			return entity;

		}

		private void deserializeServer(JsonObject obj, StatisticsEntity entity) {
			if (obj.has(SERVER)) {
				JsonObject svr = obj.getAsJsonObject(SERVER);
				entity.server = new StatisticsEntity.Server();

				if (svr.has("uptime")) {
					entity.server.uptime = svr.getAsJsonPrimitive("uptime").getAsDouble();
				}
			}
		}

		private void deserializeClient(JsonDeserializationContext context, JsonObject obj, StatisticsEntity entity) {
			if (obj.has("client")) {
				StatisticsEntity.Client cli = new StatisticsEntity.Client();
				cli.figures = new TreeMap<String, StatisticsEntity.FigureValue>();
				entity.client = cli;

				JsonObject client = obj.getAsJsonObject("client");
				if (client.has("httpConnections")) {
					cli.httpConnections = client.getAsJsonPrimitive("httpConnections").getAsInt();
				}
				for (Entry<String, JsonElement> ent : client.entrySet()) {
					if (!"httpConnections".equals(ent.getKey())) {
						JsonObject f = ent.getValue().getAsJsonObject();
						FigureValue fv = new FigureValue();
						fv.sum = f.getAsJsonPrimitive("sum").getAsDouble();
						fv.count = f.getAsJsonPrimitive(COUNT).getAsLong();
						fv.counts = context.deserialize(f.getAsJsonArray("counts"), countsType);
						cli.figures.put(ent.getKey(), fv);
					}
				}
			}
		}

		private void deserializeSystem(JsonObject obj, StatisticsEntity entity) {
			if (obj.has("system")) {
				StatisticsEntity.System sys = new StatisticsEntity.System();
				entity.system = sys;

				JsonObject system = obj.getAsJsonObject("system");
				if (system.has("minorPageFaults")) {
					sys.minorPageFaults = system.getAsJsonPrimitive("minorPageFaults").getAsLong();
				}
				if (system.has("majorPageFaults")) {
					sys.majorPageFaults = system.getAsJsonPrimitive("majorPageFaults").getAsLong();
				}
				if (system.has("userTime")) {
					sys.userTime = system.getAsJsonPrimitive("userTime").getAsDouble();
				}
				if (system.has("systemTime")) {
					sys.systemTime = system.getAsJsonPrimitive("systemTime").getAsDouble();
				}
				if (system.has("numberOfThreads")) {
					sys.numberOfThreads = system.getAsJsonPrimitive("numberOfThreads").getAsInt();
				}
				if (system.has("residentSize")) {
					sys.residentSize = system.getAsJsonPrimitive("residentSize").getAsLong();
				}
				if (system.has("virtualSize")) {
					sys.virtualSize = system.getAsJsonPrimitive("virtualSize").getAsLong();
				}
			}
		}
	}

	public static class StatisticsDescriptionEntityDeserializer
			implements JsonDeserializer<StatisticsDescriptionEntity> {
		Type cutsTypes = new TypeToken<BigDecimal[]>() {
		}.getType();

		@Override
		public StatisticsDescriptionEntity deserialize(
			JsonElement json,
			Type typeOfT,
			JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			StatisticsDescriptionEntity entity = deserializeBaseParameter(obj, new StatisticsDescriptionEntity());

			if (obj.has("groups")) {
				JsonArray groups = obj.getAsJsonArray("groups");
				entity.groups = new ArrayList<StatisticsDescriptionEntity.Group>(groups.size());
				for (int i = 0, imax = groups.size(); i < imax; i++) {
					JsonObject g = groups.get(i).getAsJsonObject();

					Group group = new Group();
					group.group = g.getAsJsonPrimitive("group").getAsString();
					group.name = g.getAsJsonPrimitive("name").getAsString();
					group.description = g.getAsJsonPrimitive("description").getAsString();

					entity.groups.add(group);
				}
			}

			if (obj.has(FIGURES)) {
				JsonArray figures = obj.getAsJsonArray(FIGURES);
				entity.figures = new ArrayList<StatisticsDescriptionEntity.Figure>(figures.size());
				for (int i = 0, imax = figures.size(); i < imax; i++) {
					JsonObject f = figures.get(i).getAsJsonObject();

					Figure figure = new Figure();
					figure.group = f.getAsJsonPrimitive("group").getAsString();
					figure.identifier = f.getAsJsonPrimitive("identifier").getAsString();
					figure.name = f.getAsJsonPrimitive("name").getAsString();
					figure.description = f.getAsJsonPrimitive("description").getAsString();
					figure.type = f.getAsJsonPrimitive("type").getAsString();
					figure.units = f.getAsJsonPrimitive("units").getAsString();
					if (f.has("cuts")) {
						figure.cuts = context.deserialize(f.getAsJsonArray("cuts"), cutsTypes);
					}

					entity.figures.add(figure);

				}
			}

			return entity;
		}
	}

	public static class ScalarExampleEntityDeserializer implements JsonDeserializer<ScalarExampleEntity<?>> {

		@Override
		public ScalarExampleEntity<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			ScalarExampleEntity<?> entity = deserializeBaseParameter(obj, new ScalarExampleEntity<Object>());

			if (obj.has("document")) {
				entity.document = context.deserialize(obj.get("document"), DocumentEntity.class);
			}

			return entity;
		}

	}

	public static class SimpleByResultEntityDeserializer implements JsonDeserializer<SimpleByResultEntity> {

		@Override
		public SimpleByResultEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			SimpleByResultEntity entity = deserializeBaseParameter(obj, new SimpleByResultEntity());

			if (obj.has(DELETED)) {
				entity.count = entity.deleted = obj.getAsJsonPrimitive(DELETED).getAsInt();
			}

			if (obj.has(REPLACED)) {
				entity.count = entity.replaced = obj.getAsJsonPrimitive(REPLACED).getAsInt();
			}

			if (obj.has(UPDATED)) {
				entity.count = entity.updated = obj.getAsJsonPrimitive(UPDATED).getAsInt();
			}

			return entity;
		}

	}

	public static class TransactionResultEntityDeserializer implements JsonDeserializer<TransactionResultEntity> {

		@Override
		public TransactionResultEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			TransactionResultEntity entity = deserializeBaseParameter(obj, new TransactionResultEntity());

			if (obj.has(RESULT)) { // MEMO:
				if (obj.get(RESULT) instanceof JsonObject) {
					entity.setResult(obj.get(RESULT));
				} else if (obj.getAsJsonPrimitive(RESULT).isBoolean()) {
					entity.setResult(obj.getAsJsonPrimitive(RESULT).getAsBoolean());
				} else if (obj.getAsJsonPrimitive(RESULT).isNumber()) {
					entity.setResult(obj.getAsJsonPrimitive(RESULT).getAsNumber());
				} else if (obj.getAsJsonPrimitive(RESULT).isString()) {
					entity.setResult(obj.getAsJsonPrimitive(RESULT).getAsString());
				}
			}

			return entity;
		}

	}

	public static class UserEntityDeserializer implements JsonDeserializer<UserEntity> {

		@Override
		public UserEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			UserEntity entity = deserializeBaseParameter(obj, new UserEntity());

			if (obj.has("user")) { // MEMO:
				// RequestはusernameなのにResponseは何故userなのか。。
				entity.username = obj.getAsJsonPrimitive("user").getAsString();
			}

			if (obj.has(PASSWORD)) {
				entity.password = obj.getAsJsonPrimitive(PASSWORD).getAsString();
			}

			if (obj.has(ACTIVE)) {
				entity.active = obj.getAsJsonPrimitive(ACTIVE).getAsBoolean();
			} else if (obj.has("authData")) {
				// for simple/all requsts
				JsonObject authData = obj.getAsJsonObject("authData");
				if (authData.has(ACTIVE)) {
					entity.active = authData.getAsJsonPrimitive(ACTIVE).getAsBoolean();
				}
			}

			if (obj.has(EXTRA)) {
				entity.extra = context.deserialize(obj.getAsJsonObject(EXTRA), Map.class);
			} else if (obj.has("userData")) {
				// for simple/all requsts
				entity.extra = context.deserialize(obj.getAsJsonObject("userData"), Map.class);
			} else {
				entity.extra = new HashMap<String, Object>();
			}

			return entity;
		}

	}

	public static class ImportResultEntityDeserializer implements JsonDeserializer<ImportResultEntity> {
		@Override
		public ImportResultEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			ImportResultEntity entity = deserializeBaseParameter(obj, new ImportResultEntity());

			if (obj.has("created")) {
				entity.created = obj.getAsJsonPrimitive("created").getAsInt();
			}

			if (obj.has("errors")) {
				entity.errors = obj.getAsJsonPrimitive("errors").getAsInt();
			}

			if (obj.has("empty")) {
				entity.empty = obj.getAsJsonPrimitive("empty").getAsInt();
			}

			return entity;
		}
	}

	public static class DatabaseEntityDeserializer implements JsonDeserializer<DatabaseEntity> {
		@Override
		public DatabaseEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			DatabaseEntity entity = deserializeBaseParameter(obj, new DatabaseEntity());

			if (obj.has(RESULT)) {
				JsonObject result = obj.getAsJsonObject(RESULT);
				if (result.has("name")) {
					entity.name = result.getAsJsonPrimitive("name").getAsString();
				}
				if (result.has(ID)) {
					entity.id = result.getAsJsonPrimitive(ID).getAsString();
				}
				if (result.has("path")) {
					entity.path = result.getAsJsonPrimitive("path").getAsString();
				}
				if (result.has(IS_SYSTEM)) {
					entity.isSystem = result.getAsJsonPrimitive(IS_SYSTEM).getAsBoolean();
				}
			}

			return entity;
		}
	}

	public static class StringsResultEntityDeserializer implements JsonDeserializer<StringsResultEntity> {
		Type resultType = new TypeToken<ArrayList<String>>() {
		}.getType();

		@Override
		public StringsResultEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			StringsResultEntity entity = deserializeBaseParameter(obj, new StringsResultEntity());

			if (obj.has(RESULT)) {
				entity.result = context.deserialize(obj.get(RESULT), resultType);
			}

			return entity;
		}
	}

	public static class BooleanResultEntityDeserializer implements JsonDeserializer<BooleanResultEntity> {
		@Override
		public BooleanResultEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			BooleanResultEntity entity = deserializeBaseParameter(obj, new BooleanResultEntity());

			if (obj.has(RESULT)) {
				entity.result = obj.getAsJsonPrimitive(RESULT).getAsBoolean();
			}

			return entity;
		}
	}

	public static class EndpointDeserializer implements JsonDeserializer<Endpoint> {
		Type databasesType = new TypeToken<List<String>>() {
		}.getType();

		@SuppressWarnings("unchecked")
		@Override
		public Endpoint deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();

			Endpoint entity = new Endpoint();
			entity.setDatabases((List<String>) context.deserialize(obj.getAsJsonArray("databases"), databasesType));
			entity.setEndpoint(obj.getAsJsonPrimitive("endpoint").getAsString());

			return entity;
		}
	}

	public static class DocumentResultEntityDeserializer implements JsonDeserializer<DocumentResultEntity<?>> {
		Type documentsType = new TypeToken<List<DocumentEntity<?>>>() {
		}.getType();

		@Override
		public DocumentResultEntity<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			DocumentResultEntity<Object> entity = deserializeBaseParameter(obj, new DocumentResultEntity<Object>());

			if (obj.has(RESULT)) {
				JsonElement resultElem = obj.get(RESULT);
				if (resultElem.isJsonArray()) {
					entity.result = context.deserialize(resultElem, documentsType);
				} else if (resultElem.isJsonObject()) {
					DocumentEntity<Object> doc = context.deserialize(resultElem, DocumentEntity.class);
					List<DocumentEntity<Object>> list = new ArrayList<DocumentEntity<Object>>(1);
					list.add(doc);
					entity.result = list;
				} else {
					throw new IllegalStateException("result type is not array or object:" + resultElem);
				}
			}

			return entity;
		}
	}

	public static class ReplicationStateDeserializer implements JsonDeserializer<ReplicationState> {
		@Override
		public ReplicationState deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			ReplicationState entity = new ReplicationState();

			entity.running = obj.getAsJsonPrimitive("running").getAsBoolean();
			entity.lastLogTick = obj.getAsJsonPrimitive("lastLogTick").getAsLong();
			entity.totalEvents = obj.getAsJsonPrimitive("totalEvents").getAsLong();
			entity.time = DateUtils.parse(obj.getAsJsonPrimitive("time").getAsString());

			return entity;
		}
	}

	public static class ReplicationInventoryEntityDeserializer implements JsonDeserializer<ReplicationInventoryEntity> {

		private Type indexesType = new TypeToken<List<IndexEntity>>() {
		}.getType();

		@Override
		public ReplicationInventoryEntity deserialize(
			JsonElement json,
			Type typeOfT,
			JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			ReplicationInventoryEntity entity = deserializeBaseParameter(obj, new ReplicationInventoryEntity());

			if (obj.has(COLLECTIONS)) {
				JsonArray collections = obj.getAsJsonArray(COLLECTIONS);
				entity.collections = new ArrayList<ReplicationInventoryEntity.Collection>(collections.size());
				for (int i = 0, imax = collections.size(); i < imax; i++) {
					JsonObject elem = collections.get(i).getAsJsonObject();
					Collection col = new Collection();

					if (elem.has("parameters")) {
						JsonObject parameters = elem.getAsJsonObject("parameters");
						addCollectionParameters(col, parameters);
					}

					if (elem.has(INDEXES)) {
						col.indexes = context.deserialize(elem.getAsJsonArray(INDEXES), indexesType);
					}

					entity.collections.add(col);
				}
			}

			if (obj.has(STATE)) {
				entity.state = context.deserialize(obj.getAsJsonObject(STATE), ReplicationState.class);
			}

			if (obj.has("tick")) {
				entity.tick = obj.getAsJsonPrimitive("tick").getAsLong();
			}

			return entity;
		}

		private void addCollectionParameters(Collection col, JsonObject parameters) {
			col.parameter = new CollectionParameter();
			if (parameters.has(VERSION)) {
				col.parameter.version = parameters.getAsJsonPrimitive(VERSION).getAsInt();
			}
			if (parameters.has("type")) {
				col.parameter.type = CollectionType.valueOf(parameters.getAsJsonPrimitive("type").getAsInt());
			}
			if (parameters.has("cid")) {
				col.parameter.cid = parameters.getAsJsonPrimitive("cid").getAsLong();
			}
			if (parameters.has(DELETED)) {
				col.parameter.deleted = parameters.getAsJsonPrimitive(DELETED).getAsBoolean();
			}
			if (parameters.has(DO_COMPACT)) {
				col.parameter.doCompact = parameters.getAsJsonPrimitive(DO_COMPACT).getAsBoolean();
			}
			if (parameters.has("maximalSize")) {
				col.parameter.maximalSize = parameters.getAsJsonPrimitive("maximalSize").getAsLong();
			}
			if (parameters.has("name")) {
				col.parameter.name = parameters.getAsJsonPrimitive("name").getAsString();
			}
			if (parameters.has(IS_VOLATILE)) {
				col.parameter.isVolatile = parameters.getAsJsonPrimitive(IS_VOLATILE).getAsBoolean();
			}
			if (parameters.has(WAIT_FOR_SYNC)) {
				col.parameter.waitForSync = parameters.getAsJsonPrimitive(WAIT_FOR_SYNC).getAsBoolean();
			}
		}
	}

	public static class ReplicationDumpRecordDeserializer implements JsonDeserializer<ReplicationDumpRecord<?>> {
		Type documentsType = new TypeToken<List<DocumentEntity<?>>>() {
		}.getType();

		@Override
		public ReplicationDumpRecord<?> deserialize(
			JsonElement json,
			Type typeOfT,
			JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			ReplicationDumpRecord<DocumentEntity<Object>> entity = new ReplicationDumpRecord<DocumentEntity<Object>>();

			if (obj.has("tick")) {
				entity.tick = obj.getAsJsonPrimitive("tick").getAsLong();
			}
			if (obj.has("type")) {
				int type = obj.getAsJsonPrimitive("type").getAsInt();
				entity.type = ReplicationEventType.valueOf(type);
			}
			if (obj.has("key")) {
				entity.key = obj.getAsJsonPrimitive("key").getAsString();
			}
			if (obj.has("rev")) {
				entity.rev = obj.getAsJsonPrimitive("rev").getAsLong();
			}
			if (obj.has("data")) {
				entity.data = context.deserialize(obj.getAsJsonObject("data"), DocumentEntity.class);
			}

			return entity;
		}
	}

	public static class ReplicationSyncEntityDeserializer implements JsonDeserializer<ReplicationSyncEntity> {
		Type collectionsType = new TypeToken<List<CollectionEntity>>() {
		}.getType();

		@Override
		public ReplicationSyncEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			ReplicationSyncEntity entity = deserializeBaseParameter(obj, new ReplicationSyncEntity());

			if (obj.has(COLLECTIONS)) {
				entity.collections = context.deserialize(obj.getAsJsonArray(COLLECTIONS), collectionsType);
			}
			if (obj.has("lastLogTick")) {
				entity.lastLogTick = obj.getAsJsonPrimitive("lastLogTick").getAsLong();
			}

			return entity;
		}
	}

	public static class MapAsEntityDeserializer implements JsonDeserializer<MapAsEntity> {
		Type mapType = new TypeToken<Map<String, Object>>() {
		}.getType();

		@Override
		public MapAsEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			MapAsEntity entity = deserializeBaseParameter(obj, new MapAsEntity());

			entity.map = context.deserialize(obj, mapType);

			return entity;
		}
	}

	public static class ReplicationLoggerConfigEntityDeserializer
			implements JsonDeserializer<ReplicationLoggerConfigEntity> {
		@Override
		public ReplicationLoggerConfigEntity deserialize(
			JsonElement json,
			Type typeOfT,
			JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			ReplicationLoggerConfigEntity entity = deserializeBaseParameter(obj, new ReplicationLoggerConfigEntity());

			if (obj.has(AUTO_START)) {
				entity.autoStart = obj.getAsJsonPrimitive(AUTO_START).getAsBoolean();
			}
			if (obj.has("logRemoteChanges")) {
				entity.logRemoteChanges = obj.getAsJsonPrimitive("logRemoteChanges").getAsBoolean();
			}
			if (obj.has("maxEvents")) {
				entity.maxEvents = obj.getAsJsonPrimitive("maxEvents").getAsLong();
			}
			if (obj.has("maxEventsSize")) {
				entity.maxEventsSize = obj.getAsJsonPrimitive("maxEventsSize").getAsLong();
			}

			return entity;
		}
	}

	public static class ReplicationApplierConfigEntityDeserializer
			implements JsonDeserializer<ReplicationApplierConfigEntity> {

		@Override
		public ReplicationApplierConfigEntity deserialize(
			JsonElement json,
			Type typeOfT,
			JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			ReplicationApplierConfigEntity entity = deserializeBaseParameter(obj, new ReplicationApplierConfigEntity());

			if (obj.has(ENDPOINT)) {
				entity.endpoint = obj.getAsJsonPrimitive(ENDPOINT).getAsString();
			}

			if (obj.has(DATABASE)) {
				entity.database = obj.getAsJsonPrimitive(DATABASE).getAsString();
			}

			if (obj.has(USERNAME)) {
				entity.username = obj.getAsJsonPrimitive(USERNAME).getAsString();
			}

			if (obj.has(PASSWORD)) {
				entity.password = obj.getAsJsonPrimitive(PASSWORD).getAsString();
			}

			if (obj.has(MAX_CONNECT_RETRIES)) {
				entity.maxConnectRetries = obj.getAsJsonPrimitive(MAX_CONNECT_RETRIES).getAsInt();
			}

			if (obj.has(CONNECT_TIMEOUT)) {
				entity.connectTimeout = obj.getAsJsonPrimitive(CONNECT_TIMEOUT).getAsInt();
			}

			if (obj.has(REQUEST_TIMEOUT)) {
				entity.requestTimeout = obj.getAsJsonPrimitive(REQUEST_TIMEOUT).getAsInt();
			}

			if (obj.has(CHUNK_SIZE)) {
				entity.chunkSize = obj.getAsJsonPrimitive(CHUNK_SIZE).getAsInt();
			}

			if (obj.has(AUTO_START)) {
				entity.autoStart = obj.getAsJsonPrimitive(AUTO_START).getAsBoolean();
			}

			if (obj.has(ADAPTIVE_POLLING)) {
				entity.adaptivePolling = obj.getAsJsonPrimitive(ADAPTIVE_POLLING).getAsBoolean();
			}

			return entity;
		}
	}

	public static class ReplicationApplierStateDeserializer implements JsonDeserializer<ReplicationApplierState> {

		@Override
		public ReplicationApplierState deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			ReplicationApplierState state = new ReplicationApplierState();

			if (obj.has("running")) {
				state.running = obj.getAsJsonPrimitive("running").getAsBoolean();
			}

			deserializeTicks(obj, state);

			if (obj.has("time")) {
				state.time = DateUtils.parse(obj.getAsJsonPrimitive("time").getAsString());
			}
			if (obj.has("totalRequests")) {
				state.totalRequests = obj.getAsJsonPrimitive("totalRequests").getAsLong();
			}
			if (obj.has("totalFailedConnects")) {
				state.totalFailedConnects = obj.getAsJsonPrimitive("totalFailedConnects").getAsLong();
			}
			if (obj.has("totalEvents")) {
				state.totalEvents = obj.getAsJsonPrimitive("totalEvents").getAsLong();
			}

			deserializeLastError(obj, state);

			deserializeProgress(obj, state);

			return state;
		}

		private void deserializeTicks(JsonObject obj, ReplicationApplierState state) {
			if (obj.has(LAST_APPLIED_CONTINUOUS_TICK) && !obj.get(LAST_APPLIED_CONTINUOUS_TICK).isJsonNull()) {
				state.lastAppliedContinuousTick = obj.getAsJsonPrimitive(LAST_APPLIED_CONTINUOUS_TICK).getAsLong();
			}
			if (obj.has(LAST_PROCESSED_CONTINUOUS_TICK) && !obj.get(LAST_PROCESSED_CONTINUOUS_TICK).isJsonNull()) {
				state.lastProcessedContinuousTick = obj.getAsJsonPrimitive(LAST_PROCESSED_CONTINUOUS_TICK).getAsLong();
			}
			if (obj.has(LAST_AVAILABLE_CONTINUOUS_TICK) && !obj.get(LAST_AVAILABLE_CONTINUOUS_TICK).isJsonNull()) {
				state.lastAvailableContinuousTick = obj.getAsJsonPrimitive(LAST_AVAILABLE_CONTINUOUS_TICK).getAsLong();
			}
		}

		private void deserializeProgress(JsonObject obj, ReplicationApplierState state) {
			if (obj.has("progress")) {
				JsonObject progress = obj.getAsJsonObject("progress");
				state.progress = new Progress();
				if (progress.has("failedConnects")) {
					state.progress.failedConnects = progress.getAsJsonPrimitive("failedConnects").getAsLong();
				}
				if (progress.has(MESSAGE)) {
					state.progress.message = progress.getAsJsonPrimitive(MESSAGE).getAsString();
				}
				if (progress.has("time")) {
					state.progress.time = DateUtils.parse(progress.getAsJsonPrimitive("time").getAsString());
				}
			}
		}

		private void deserializeLastError(JsonObject obj, ReplicationApplierState state) {
			if (obj.has(LAST_ERROR) && !obj.get(LAST_ERROR).isJsonNull()) {
				JsonObject lastError = obj.getAsJsonObject(LAST_ERROR);
				state.lastError = new LastError();
				if (lastError.has("time")) {
					state.lastError.setTime(DateUtils.parse(lastError.getAsJsonPrimitive("time").getAsString()));
				}
				if (lastError.has(ERROR_NUM)) {
					state.lastError.setErrorNum(lastError.getAsJsonPrimitive(ERROR_NUM).getAsInt());
				}
				if (lastError.has(ERROR_MESSAGE)) {
					state.lastError.setErrorMessage(lastError.getAsJsonPrimitive(ERROR_MESSAGE).getAsString());
				}
			}
		}
	}

	public static class ReplicationApplierStateEntityDeserializer
			implements JsonDeserializer<ReplicationApplierStateEntity> {
		@Override
		public ReplicationApplierStateEntity deserialize(
			JsonElement json,
			Type typeOfT,
			JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			ReplicationApplierStateEntity entity = deserializeBaseParameter(obj, new ReplicationApplierStateEntity());

			if (obj.has(ENDPOINT)) {
				entity.endpoint = obj.getAsJsonPrimitive(ENDPOINT).getAsString();
			}

			if (obj.has(DATABASE)) {
				entity.database = obj.getAsJsonPrimitive(DATABASE).getAsString();
			}

			if (obj.has(SERVER)) {
				JsonObject server = obj.getAsJsonObject(SERVER);
				entity.serverVersion = server.getAsJsonPrimitive(VERSION).getAsString();
				entity.serverId = server.getAsJsonPrimitive("serverId").getAsString();
			}

			if (obj.has(STATE)) {
				entity.state = context.deserialize(obj.get(STATE), ReplicationApplierState.class);
			}

			return entity;
		}
	}

	public static class ReplicationLoggerStateEntityDeserializer
			implements JsonDeserializer<ReplicationLoggerStateEntity> {
		private Type clientsType = new TypeToken<List<Client>>() {
		}.getType();

		@Override
		public ReplicationLoggerStateEntity deserialize(
			JsonElement json,
			Type typeOfT,
			JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			ReplicationLoggerStateEntity entity = deserializeBaseParameter(obj, new ReplicationLoggerStateEntity());

			if (obj.has(STATE)) {
				entity.state = context.deserialize(obj.get(STATE), ReplicationState.class);
			}

			if (obj.has(SERVER)) {
				JsonObject server = obj.getAsJsonObject(SERVER);
				entity.serverVersion = server.getAsJsonPrimitive(VERSION).getAsString();
				entity.serverId = server.getAsJsonPrimitive("serverId").getAsString();
			}

			if (obj.has("clients")) {
				entity.clients = context.deserialize(obj.getAsJsonArray("clients"), clientsType);
			}

			return entity;
		}
	}

	public static class ReplicationLoggerStateEntityClientDeserializer
			implements JsonDeserializer<ReplicationLoggerStateEntity.Client> {
		@Override
		public ReplicationLoggerStateEntity.Client deserialize(
			JsonElement json,
			Type typeOfT,
			JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			Client client = new Client();

			if (obj.has("serverId")) {
				client.serverId = obj.getAsJsonPrimitive("serverId").getAsString();
			}

			if (obj.has("lastServedTick")) {
				client.lastServedTick = obj.getAsJsonPrimitive("lastServedTick").getAsLong();
			}

			if (obj.has("time")) {
				client.time = DateUtils.parse(obj.getAsJsonPrimitive("time").getAsString());
			}

			return client;
		}
	}

	public static class GraphEntityDeserializer implements JsonDeserializer<GraphEntity> {
		@Override
		public GraphEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			GraphEntity entity = deserializeBaseParameter(obj, new GraphEntity());

			JsonObject graph = obj.has("graph") ? obj.getAsJsonObject("graph") : obj;
			deserializeDocumentParameter(graph, entity);

			if (graph.has("name")) {
				entity.name = graph.get("name").getAsString();
			}

			if (graph.has("orphanCollections")) {
				JsonArray orphanCollections = graph.getAsJsonArray("orphanCollections");
				entity.orphanCollections = new ArrayList<String>();
				if (orphanCollections != null) {
					entity.orphanCollections = new ArrayList<String>(orphanCollections.size());
					for (int i = 0, imax = orphanCollections.size(); i < imax; i++) {
						String orphanCollection = orphanCollections.get(i).getAsString();

						entity.orphanCollections.add(orphanCollection);
					}
				}
			}

			if (graph.has("edgeDefinitions")) {
				JsonArray edgeDefinitions = graph.getAsJsonArray("edgeDefinitions");
				entity.edgeDefinitionsEntity = new EdgeDefinitionsEntity();
				if (edgeDefinitions != null) {
					addEdgeDefinitions(entity, edgeDefinitions);
				}
			}

			return entity;

		}

		private void addEdgeDefinitions(GraphEntity entity, JsonArray edgeDefinitions) {
			for (int i = 0, imax = edgeDefinitions.size(); i < imax; i++) {
				EdgeDefinitionEntity edgeDefinitionEntity = new EdgeDefinitionEntity();
				JsonObject edgeDefinition = edgeDefinitions.get(i).getAsJsonObject();
				if (edgeDefinition.has("collection")) {
					edgeDefinitionEntity.setCollection(edgeDefinition.get("collection").getAsString());
				}
				if (edgeDefinition.has("from")) {
					List<String> from = new ArrayList<String>();
					JsonElement fromElem = edgeDefinition.get("from");
					JsonArray fromArray = fromElem.getAsJsonArray();
					Iterator<JsonElement> iterator = fromArray.iterator();
					while (iterator.hasNext()) {
						JsonElement e = iterator.next();
						from.add(e.getAsString());
					}

					edgeDefinitionEntity.setFrom(from);
				}
				if (edgeDefinition.has("to")) {
					List<String> to = new ArrayList<String>();
					JsonElement toElem = edgeDefinition.get("to");
					JsonArray toArray = toElem.getAsJsonArray();
					Iterator<JsonElement> iterator = toArray.iterator();
					while (iterator.hasNext()) {
						JsonElement e = iterator.next();
						to.add(e.getAsString());
					}
					edgeDefinitionEntity.setTo(to);
				}
				entity.edgeDefinitionsEntity.addEdgeDefinition(edgeDefinitionEntity);
			}
		}
	}

	public static class GraphsEntityDeserializer implements JsonDeserializer<GraphsEntity> {
		private Type graphsType = new TypeToken<List<GraphEntity>>() {
		}.getType();

		@Override
		public GraphsEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			GraphsEntity entity = deserializeBaseParameter(obj, new GraphsEntity());

			if (obj.has("graphs")) {
				entity.graphs = context.deserialize(obj.get("graphs"), graphsType);
			}

			return entity;

		}
	}

	public static class DeleteEntityDeserializer implements JsonDeserializer<DeletedEntity> {
		@Override
		public DeletedEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			DeletedEntity entity = deserializeBaseParameter(obj, new DeletedEntity());

			if (obj.has(DELETED)) {
				entity.deleted = obj.getAsJsonPrimitive(DELETED).getAsBoolean();
			}
			if (obj.has("removed")) {
				entity.deleted = obj.getAsJsonPrimitive("removed").getAsBoolean();
			}

			return entity;

		}
	}

	public static class VertexEntityDeserializer implements JsonDeserializer<VertexEntity<?>> {
		@Override
		public VertexEntity<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			VertexEntity<Object> entity = deserializeBaseParameter(obj, new VertexEntity<Object>());

			JsonObject vertex = obj.has("vertex") ? obj.getAsJsonObject("vertex") : obj;
			deserializeDocumentParameter(vertex, entity);

			Class<?> clazz = getParameterized();
			if (clazz != null) {
				entity.setEntity(context.deserialize(vertex, clazz));
			}

			return entity;
		}
	}

	public static class EdgeEntityDeserializer implements JsonDeserializer<EdgeEntity<?>> {
		@Override
		public EdgeEntity<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			EdgeEntity<Object> entity = deserializeBaseParameter(obj, new EdgeEntity<Object>());

			JsonObject edge = obj.has("edge") ? obj.getAsJsonObject("edge") : obj;
			deserializeDocumentParameter(edge, entity);

			if (edge.has("_from")) {
				entity.fromVertexHandle = edge.getAsJsonPrimitive("_from").getAsString();
			}
			if (edge.has("_to")) {
				entity.toVertexHandle = edge.getAsJsonPrimitive("_to").getAsString();
			}

			// 他のフィールドはリフレクションで。 (TODO: Annotationのサポートと上記パラメータを弾く)
			Class<?> clazz = getParameterized();
			if (clazz != null) {
				entity.entity = context.deserialize(edge, clazz);
			}

			return entity;
		}

	}

	public static class TraversalEntityDeserializer implements JsonDeserializer<TraversalEntity<?, ?>> {

		@Override
		public TraversalEntity<?, ?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			TraversalEntity<Object, Object> entity = deserializeBaseParameter(obj,
				new TraversalEntity<Object, Object>());
			deserializeBaseParameter(obj, entity);

			JsonObject result = getFirstResultAsJsonObject(obj);
			if (result != null && result.getAsJsonObject().has("visited")) {
				JsonObject visited = result.getAsJsonObject().getAsJsonObject("visited");

				Class<?> vertexClazz = getParameterized();
				Class<?> edgeClazz = null;

				if (hasNextParameterized()) {
					edgeClazz = nextParameterized();
				}

				if (visited.has(VERTICES)) {
					entity.setVertices(getVertices(vertexClazz, context, visited.getAsJsonArray(VERTICES)));
				}
				if (visited.has(PATHS)) {
					entity.setPaths(getPaths(context, visited, vertexClazz, edgeClazz));
				}
			}

			return entity;
		}

		private List<PathEntity<Object, Object>> getPaths(
			JsonDeserializationContext context,
			JsonObject visited,
			Class<?> vertexClazz,
			Class<?> edgeClazz) {
			List<PathEntity<Object, Object>> pathEntities = new ArrayList<PathEntity<Object, Object>>();
			JsonArray paths = visited.getAsJsonArray(PATHS);
			if (paths != null) {
				for (int i = 0, imax = paths.size(); i < imax; i++) {
					JsonObject path = paths.get(i).getAsJsonObject();
					PathEntity<Object, Object> pathEntity = new PathEntity<Object, Object>();

					if (path.has(EDGES)) {
						pathEntity.setEdges(getEdges(edgeClazz, context, path.getAsJsonArray(EDGES)));
					}
					if (path.has(VERTICES)) {
						pathEntity.setVertices(getVertices(vertexClazz, context, path.getAsJsonArray(VERTICES)));
					}

					pathEntities.add(pathEntity);
				}
			}
			return pathEntities;
		}

	}

	public static class ShortestPathEntityDeserializer implements JsonDeserializer<ShortestPathEntity<?, ?>> {
		@Override
		public ShortestPathEntity<?, ?> deserialize(
			JsonElement json,
			Type typeOfT,
			JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			ShortestPathEntity<Object, Object> entity = deserializeBaseParameter(obj,
				new ShortestPathEntity<Object, Object>());
			deserializeBaseParameter(obj, entity);

			JsonObject result = getFirstResultAsJsonObject(obj);
			if (result != null) {
				Class<?> vertexClazz = getParameterized();
				Class<?> edgeClazz = null;

				if (hasNextParameterized()) {
					edgeClazz = nextParameterized();
				}

				if (result.has("distance")) {
					entity.setDistance(result.get("distance").getAsLong());
				} else {
					entity.setDistance(-1L);
				}
				if (result.has(EDGES)) {
					// new version >= 2.6
					entity.setEdges(getEdges(edgeClazz, context, result.getAsJsonArray(EDGES)));
				}
				if (result.has(VERTICES)) {
					// new version >= 2.6
					entity.setVertices(getVertices(vertexClazz, context, result.getAsJsonArray(VERTICES)));
				}
				if (result.has(PATHS)) {
					// old version < 2.6
					addOldPath(context, entity, result, vertexClazz, edgeClazz);
				}
			} else {
				entity.setDistance(-1L);
			}

			return entity;
		}

		private void addOldPath(
			JsonDeserializationContext context,
			ShortestPathEntity<Object, Object> entity,
			JsonObject result,
			Class<?> vertexClazz,
			Class<?> edgeClazz) {
			JsonArray paths = result.getAsJsonArray(PATHS);
			if (paths != null && paths.size() > 0) {
				JsonObject path = paths.get(0).getAsJsonObject();

				if (path.has(EDGES)) {
					entity.setEdges(getEdges(edgeClazz, context, path.getAsJsonArray(EDGES)));
				}
				if (path.has(VERTICES)) {
					entity.setVertices(getVertices(vertexClazz, context, path.getAsJsonArray(VERTICES)));
				}
			}
		}
	}

	public static class QueryCachePropertiesEntityDeserializer implements JsonDeserializer<QueryCachePropertiesEntity> {

		@Override
		public QueryCachePropertiesEntity deserialize(
			JsonElement json,
			Type typeOfT,
			JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			QueryCachePropertiesEntity entity = deserializeBaseParameter(obj, new QueryCachePropertiesEntity());

			if (obj.has("mode")) {
				entity.setMode(obj.getAsJsonPrimitive("mode").getAsString());
			}

			if (obj.has("maxResults")) {
				entity.setMaxResults(obj.getAsJsonPrimitive("maxResults").getAsLong());
			}

			return entity;
		}

	}

	public static class QueriesResultEntityDeserializer implements JsonDeserializer<QueriesResultEntity> {

		@Override
		public QueriesResultEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonArray array = json.getAsJsonArray();
			Iterator<JsonElement> iterator = array.iterator();
			List<QueryEntity> queries = new ArrayList<QueryEntity>();
			while (iterator.hasNext()) {
				JsonElement element = iterator.next();
				JsonObject obj = element.getAsJsonObject();
				QueryEntity entity = new QueryEntity();

				if (obj.has(ID)) {
					entity.setId(obj.getAsJsonPrimitive(ID).getAsString());
					queries.add(entity);
				}

				if (obj.has("query")) {
					entity.setQuery(obj.getAsJsonPrimitive("query").getAsString());
				}

				if (obj.has("started")) {
					String str = obj.getAsJsonPrimitive("started").getAsString();

					SimpleDateFormat sdf = new SimpleDateFormat(ALT_DATE_TIME_FORMAT);
					try {
						entity.setStarted(sdf.parse(str));
					} catch (ParseException e) {
						logger.debug("got ParseException for date string: " + str);
					}
				}

				if (obj.has("runTime")) {
					entity.setRunTime(obj.getAsJsonPrimitive("runTime").getAsDouble());
				}

			}

			return new QueriesResultEntity(queries);
		}
	}

	public static class QueryTrackingPropertiesEntityDeserializer
			implements JsonDeserializer<QueryTrackingPropertiesEntity> {

		@Override
		public QueryTrackingPropertiesEntity deserialize(
			JsonElement json,
			Type typeOfT,
			JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			JsonObject obj = json.getAsJsonObject();
			QueryTrackingPropertiesEntity entity = deserializeBaseParameter(obj, new QueryTrackingPropertiesEntity());

			if (obj.has("enabled")) {
				entity.setEnabled(obj.getAsJsonPrimitive("enabled").getAsBoolean());
			}

			if (obj.has("trackSlowQueries")) {
				entity.setTrackSlowQueries(obj.getAsJsonPrimitive("trackSlowQueries").getAsBoolean());
			}

			if (obj.has("maxSlowQueries")) {
				entity.setMaxSlowQueries(obj.getAsJsonPrimitive("maxSlowQueries").getAsLong());
			}

			if (obj.has("slowQueryThreshold")) {
				entity.setSlowQueryThreshold(obj.getAsJsonPrimitive("slowQueryThreshold").getAsLong());
			}

			if (obj.has("maxQueryStringLength")) {
				entity.setMaxQueryStringLength(obj.getAsJsonPrimitive("maxQueryStringLength").getAsLong());
			}

			return entity;
		}

	}

	private static JsonObject getFirstResultAsJsonObject(JsonObject obj) {
		if (obj.has(RESULT)) {
			if (obj.get(RESULT).isJsonArray()) {
				return getElementAsJsonObject(obj.getAsJsonArray(RESULT));
			} else if (obj.get(RESULT).isJsonObject()) {
				return obj.getAsJsonObject(RESULT);
			}
		}
		return null;
	}

	private static JsonObject getElementAsJsonObject(JsonArray arr) {
		if (arr != null && arr.size() > 0) {
			JsonElement jsonElement = arr.get(0);
			if (jsonElement.isJsonObject()) {
				return jsonElement.getAsJsonObject();
			}
		}
		return null;
	}

	private static List<VertexEntity<Object>> getVertices(
		Class<?> vertexClazz,
		JsonDeserializationContext context,
		JsonArray vertices) {
		List<VertexEntity<Object>> list = new ArrayList<VertexEntity<Object>>();
		if (vertices != null) {
			for (int i = 0, imax = vertices.size(); i < imax; i++) {
				JsonObject vertex = vertices.get(i).getAsJsonObject();
				VertexEntity<Object> ve = getVertex(context, vertex, vertexClazz);
				list.add(ve);
			}
		}
		return list;
	}

	private static VertexEntity<Object> getVertex(
		JsonDeserializationContext context,
		JsonObject vertex,
		Class<?> vertexClazz) {
		VertexEntity<Object> ve = deserializeBaseParameter(vertex, new VertexEntity<Object>());
		deserializeDocumentParameter(vertex, ve);
		if (vertexClazz != null) {
			ve.setEntity(context.deserialize(vertex, vertexClazz));
		} else {
			ve.setEntity(context.deserialize(vertex, Object.class));
		}
		return ve;
	}

	private static List<EdgeEntity<Object>> getEdges(
		Class<?> edgeClazz,
		JsonDeserializationContext context,
		JsonArray edges) {
		List<EdgeEntity<Object>> list = new ArrayList<EdgeEntity<Object>>();
		if (edges != null) {
			for (int i = 0, imax = edges.size(); i < imax; i++) {
				JsonObject edge = edges.get(i).getAsJsonObject();
				EdgeEntity<Object> ve = deserializeBaseParameter(edge, new EdgeEntity<Object>());
				deserializeDocumentParameter(edge, ve);
				if (edgeClazz != null) {
					ve.setEntity(context.deserialize(edge, edgeClazz));
				} else {
					ve.setEntity(context.deserialize(edge, Object.class));
				}
				list.add(ve);
			}
		}
		return list;
	}
}
