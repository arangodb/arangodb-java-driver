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
import com.arangodb.entity.QueryCachePropertiesEntity.CacheMode;
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

	private static final String CREATED = "created";
	private static final String ERRORS = "errors";
	private static final String EMPTY = "empty";
	private static final String IGNORED = "ignored";
	private static final String DETAILS = "details";

	private static Logger logger = LoggerFactory.getLogger(EntityDeserializers.class);

	private static class ClassHolder {
		private final Class<?>[] clazz;
		private int idx;

		ClassHolder(final Class<?>... clazz) {
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

	public static void setParameterized(final Class<?>... clazz) {
		parameterizedBridger.set(new ClassHolder(clazz));
	}

	public static void removeParameterized() {
		parameterizedBridger.remove();
	}

	private static Class<?> getParameterized() {
		final ClassHolder holder = parameterizedBridger.get();
		if (holder == null) {
			return null;
		}
		return holder.get();
	}

	private static boolean hasNextParameterized() {
		final ClassHolder holder = parameterizedBridger.get();
		if (holder == null) {
			return false;
		}
		return holder.hasNext();
	}

	private static Class<?> nextParameterized() {
		final ClassHolder holder = parameterizedBridger.get();
		if (holder == null) {
			return null;
		}
		return holder.next();
	}

	private static <T extends BaseEntity> T deserializeBaseParameter(final JsonObject obj, final T entity) {
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

	private static <T extends DocumentHolder> T deserializeDocumentParameter(final JsonObject obj, final T entity) {

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
		public DefaultEntity deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {
			if (json.isJsonNull()) {
				return null;
			}
			return deserializeBaseParameter(json.getAsJsonObject(), new DefaultEntity());
		}
	}

	public static class VersionDeserializer implements JsonDeserializer<ArangoVersion> {

		@Override
		public ArangoVersion deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final ArangoVersion entity = deserializeBaseParameter(obj, new ArangoVersion());

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
		public ArangoUnixTime deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final ArangoUnixTime entity = deserializeBaseParameter(obj, new ArangoUnixTime());

			if (obj.has("time")) {
				entity.time = obj.getAsJsonPrimitive("time").getAsDouble();
				final String time = obj.getAsJsonPrimitive("time").getAsString(); // 実際はdoubleだけど精度の問題が心配なので文字列で処理する。
				entity.second = (int) entity.time;

				final int pos = time.indexOf('.');
				entity.microsecond = (pos >= 0 && pos + 1 != time.length()) ? Integer.parseInt(time.substring(pos + 1))
						: 0;
			}

			return entity;
		}
	}

	public static class FiguresDeserializer implements JsonDeserializer<Figures> {

		@Override
		public Figures deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final Figures entity = new Figures();

			if (obj.has(ALIVE)) {
				final JsonObject alive = obj.getAsJsonObject(ALIVE);
				entity.aliveCount = alive.getAsJsonPrimitive(COUNT).getAsLong();
				entity.aliveSize = alive.getAsJsonPrimitive(SIZE).getAsLong();
			}

			if (obj.has("dead")) {
				final JsonObject dead = obj.getAsJsonObject("dead");
				entity.deadCount = dead.getAsJsonPrimitive(COUNT).getAsLong();
				entity.deadSize = dead.getAsJsonPrimitive(SIZE).getAsLong();
				entity.deadDeletion = dead.getAsJsonPrimitive("deletion").getAsLong();
			}

			if (obj.has("datafiles")) {
				final JsonObject datafiles = obj.getAsJsonObject("datafiles");
				entity.datafileCount = datafiles.getAsJsonPrimitive(COUNT).getAsLong();
				entity.datafileFileSize = datafiles.getAsJsonPrimitive(FILE_SIZE).getAsLong();
			}

			if (obj.has("journals")) {
				final JsonObject journals = obj.getAsJsonObject("journals");
				entity.journalsCount = journals.getAsJsonPrimitive(COUNT).getAsLong();
				entity.journalsFileSize = journals.getAsJsonPrimitive(FILE_SIZE).getAsLong();
			}

			if (obj.has("compactors")) {
				final JsonObject compactors = obj.getAsJsonObject("compactors");
				entity.compactorsCount = compactors.getAsJsonPrimitive(COUNT).getAsLong();
				entity.compactorsFileSize = compactors.getAsJsonPrimitive(FILE_SIZE).getAsLong();
			}

			if (obj.has(INDEXES)) {
				final JsonObject indexes = obj.getAsJsonObject(INDEXES);
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
		public CollectionKeyOption deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final CollectionKeyOption entity = new CollectionKeyOption();

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
		public CollectionEntity deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final CollectionEntity entity = deserializeBaseParameter(obj, new CollectionEntity());

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
		private final Type collectionsType = new TypeToken<List<CollectionEntity>>() {
		}.getType();

		@SuppressWarnings("unchecked")
		@Override
		public CollectionsEntity deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final CollectionsEntity entity = deserializeBaseParameter(obj, new CollectionsEntity());

			if (obj.has(RESULT)) {
				entity.setCollections((List<CollectionEntity>) context.deserialize(obj.get(RESULT), collectionsType));
			} else {
				entity.setCollections(new ArrayList<CollectionEntity>());
			}

			return entity;
		}
	}

	public static class AqlfunctionsEntityDeserializer implements JsonDeserializer<AqlFunctionsEntity> {

		@Override
		public AqlFunctionsEntity deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonArray obj = json.getAsJsonArray();
			final Iterator<JsonElement> iterator = obj.iterator();
			final Map<String, String> functions = new HashMap<String, String>();
			while (iterator.hasNext()) {
				final JsonElement e = iterator.next();
				final JsonObject o = e.getAsJsonObject();
				functions.put(o.get("name").getAsString(), o.get(CODE).getAsString());
			}
			return new AqlFunctionsEntity(functions);
		}
	}

	public static class JobsEntityDeserializer implements JsonDeserializer<JobsEntity> {

		@Override
		public JobsEntity deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonArray obj = json.getAsJsonArray();
			final Iterator<JsonElement> iterator = obj.iterator();
			final List<String> jobs = new ArrayList<String>();
			while (iterator.hasNext()) {
				final JsonElement e = iterator.next();
				jobs.add(e.getAsString());
			}
			return new JobsEntity(jobs);
		}
	}

	public static class CursorEntityDeserializer implements JsonDeserializer<CursorEntity<?>> {

		private final Type bindVarsType = new TypeToken<List<String>>() {
		}.getType();

		private final Type extraType = new TypeToken<Map<String, Object>>() {
		}.getType();

		@Override
		public CursorEntity<?> deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final CursorEntity<Object> entity = deserializeBaseParameter(obj, new CursorEntity<Object>());

			if (obj.has(RESULT)) {
				final JsonArray array = obj.getAsJsonArray(RESULT);
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
			final JsonDeserializationContext context,
			final CursorEntity<Object> entity,
			final JsonArray array) {
			final Class<?> clazz = getParameterized();
			final boolean withDocument = DocumentEntity.class.isAssignableFrom(clazz);
			if (withDocument) {
				nextParameterized();
			}
			try {
				final List<Object> list = new ArrayList<Object>(array.size());
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

		private void getWarnings(final CursorEntity<Object> entity) {
			if (entity.extra.containsKey(WARNINGS)) {
				final Object object = entity.extra.get(WARNINGS);
				if (object instanceof List<?>) {
					final List<?> l = (List<?>) entity.extra.get(WARNINGS);
					getWarningsFromList(entity, l);
				}
			}
		}

		private void getWarningsFromList(final CursorEntity<Object> entity, final List<?> l) {
			for (final Object o : l) {
				if (o instanceof Map<?, ?>) {
					final Map<?, ?> m = (Map<?, ?>) o;
					if (m.containsKey(CODE) && m.get(CODE) instanceof Double && m.containsKey(MESSAGE)
							&& m.get(MESSAGE) instanceof String) {
						final Long code = ((Double) m.get(CODE)).longValue();
						final String message = (String) m.get(MESSAGE);
						entity.warnings.add(new WarningEntity(code, message));
					}
				}
			}
		}

		private void getFullCount(final CursorEntity<Object> entity) {
			if (entity.extra.containsKey("stats") && entity.extra.get("stats") instanceof Map<?, ?>) {
				final Map<?, ?> m = (Map<?, ?>) entity.extra.get("stats");
				if (m.containsKey("fullCount") && m.get("fullCount") instanceof Double) {
					final Double v = (Double) m.get("fullCount");
					entity.fullCount = v.intValue();
				}
			}
		}

		private Class<?> backParameterized() {
			final ClassHolder holder = parameterizedBridger.get();
			if (holder == null) {
				return null;
			}
			return holder.back();
		}

	}

	public static class DocumentEntityDeserializer implements JsonDeserializer<DocumentEntity<?>> {

		@Override
		public DocumentEntity<?> deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return new DocumentEntity<Object>();
			}

			if (json.isJsonPrimitive()) {
				return new DocumentEntity<Object>();
			}

			if (json.isJsonArray()) {
				return new DocumentEntity<Object>();
			}

			final JsonObject obj = json.getAsJsonObject();

			final DocumentEntity<Object> entity = new DocumentEntity<Object>();
			deserializeDocumentParameter(obj, entity);

			// 他のフィールドはリフレクションで。 (TODO: Annotationのサポートと上記パラメータを弾く)
			final Class<?> clazz = getParameterized();
			if (clazz != null) {
				entity.entity = context.deserialize(obj, clazz);

				if (clazz.getName().equalsIgnoreCase(BaseDocument.class.getName())) {
					// iterate all key/value pairs of the jsonObject and
					// determine its class(String, Number, Boolean, HashMap,
					// List)
					((BaseDocument) entity.entity).setProperties(DeserializeSingleEntry.deserializeJsonObject(obj));
				}
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
		public static Object deserializeJsonElement(final JsonElement jsonElement) {
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
		private static Map<String, Object> deserializeJsonObject(final JsonObject jsonObject) {
			final Map<String, Object> result = new HashMap<String, Object>();
			final Set<Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
			for (final Map.Entry<String, JsonElement> entry : entrySet) {
				if (!nonProperties.contains(entry.getKey())) {
					result.put(entry.getKey(), deserializeJsonElement(jsonObject.get(entry.getKey())));
				}
			}
			return result;
		}

		private static List<Object> deserializeJsonArray(final JsonArray jsonArray) {
			final List<Object> tmpObjectList = new ArrayList<Object>();
			final Iterator<JsonElement> iterator = jsonArray.iterator();
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
		private static Object deserializeJsonPrimitive(final JsonPrimitive jsonPrimitive) {
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
		private final Type documentsType = new TypeToken<List<String>>() {
		}.getType();

		@Override
		public DocumentsEntity deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final DocumentsEntity entity = deserializeBaseParameter(obj, new DocumentsEntity());

			if (obj.has("documents")) {
				entity.documents = context.deserialize(obj.get("documents"), documentsType);
			}

			return entity;
		}

	}

	public static class IndexEntityDeserializer implements JsonDeserializer<IndexEntity> {
		private final Type fieldsType = new TypeToken<List<String>>() {
		}.getType();

		@Override
		public IndexEntity deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final IndexEntity entity = deserializeBaseParameter(obj, new IndexEntity());

			if (obj.has(ID)) {
				entity.id = obj.getAsJsonPrimitive(ID).getAsString();
			}

			if (obj.has("type")) {
				final String type = obj.getAsJsonPrimitive("type").getAsString().toUpperCase(Locale.US);
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
		private final Type indexesType = new TypeToken<List<IndexEntity>>() {
		}.getType();
		private final Type identifiersType = new TypeToken<Map<String, IndexEntity>>() {
		}.getType();

		@Override
		public IndexesEntity deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final IndexesEntity entity = deserializeBaseParameter(obj, new IndexesEntity());

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
		public AdminLogEntity deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final AdminLogEntity entity = deserializeBaseParameter(obj, new AdminLogEntity());
			// 全ての要素は必ずあることが前提なのでhasチェックはしない
			final int[] lids = context.deserialize(obj.getAsJsonArray("lid"), int[].class);
			final int[] levels = context.deserialize(obj.getAsJsonArray("level"), int[].class);
			final long[] timestamps = context.deserialize(obj.getAsJsonArray("timestamp"), long[].class);
			final String[] texts = context.deserialize(obj.getAsJsonArray("text"), String[].class);

			// 配列のサイズが全て同じであること
			if (lids.length != levels.length || lids.length != timestamps.length || lids.length != texts.length) {
				throw new IllegalStateException("each parameters returns wrong length.");
			}

			entity.logs = new ArrayList<AdminLogEntity.LogEntry>(lids.length);
			for (int i = 0; i < lids.length; i++) {
				final AdminLogEntity.LogEntry entry = new AdminLogEntity.LogEntry();
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
		public StatisticsEntity deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final StatisticsEntity entity = deserializeBaseParameter(obj, new StatisticsEntity());

			deserializeSystem(obj, entity);

			deserializeClient(context, obj, entity);

			deserializeServer(obj, entity);

			return entity;

		}

		private void deserializeServer(final JsonObject obj, final StatisticsEntity entity) {
			if (obj.has(SERVER)) {
				final JsonObject svr = obj.getAsJsonObject(SERVER);
				entity.server = new StatisticsEntity.Server();

				if (svr.has("uptime")) {
					entity.server.uptime = svr.getAsJsonPrimitive("uptime").getAsDouble();
				}
			}
		}

		private void deserializeClient(
			final JsonDeserializationContext context,
			final JsonObject obj,
			final StatisticsEntity entity) {
			if (obj.has("client")) {
				final StatisticsEntity.Client cli = new StatisticsEntity.Client();
				cli.figures = new TreeMap<String, StatisticsEntity.FigureValue>();
				entity.client = cli;

				final JsonObject client = obj.getAsJsonObject("client");
				if (client.has("httpConnections")) {
					cli.httpConnections = client.getAsJsonPrimitive("httpConnections").getAsInt();
				}
				for (final Entry<String, JsonElement> ent : client.entrySet()) {
					if (!"httpConnections".equals(ent.getKey())) {
						final JsonObject f = ent.getValue().getAsJsonObject();
						final FigureValue fv = new FigureValue();
						fv.sum = f.getAsJsonPrimitive("sum").getAsDouble();
						fv.count = f.getAsJsonPrimitive(COUNT).getAsLong();
						fv.counts = context.deserialize(f.getAsJsonArray("counts"), countsType);
						cli.figures.put(ent.getKey(), fv);
					}
				}
			}
		}

		private void deserializeSystem(final JsonObject obj, final StatisticsEntity entity) {
			if (obj.has("system")) {
				final StatisticsEntity.System sys = new StatisticsEntity.System();
				entity.system = sys;

				final JsonObject system = obj.getAsJsonObject("system");
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
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final StatisticsDescriptionEntity entity = deserializeBaseParameter(obj, new StatisticsDescriptionEntity());

			if (obj.has("groups")) {
				final JsonArray groups = obj.getAsJsonArray("groups");
				entity.groups = new ArrayList<StatisticsDescriptionEntity.Group>(groups.size());
				for (int i = 0, imax = groups.size(); i < imax; i++) {
					final JsonObject g = groups.get(i).getAsJsonObject();

					final Group group = new Group();
					group.group = g.getAsJsonPrimitive("group").getAsString();
					group.name = g.getAsJsonPrimitive("name").getAsString();
					group.description = g.getAsJsonPrimitive("description").getAsString();

					entity.groups.add(group);
				}
			}

			if (obj.has(FIGURES)) {
				final JsonArray figures = obj.getAsJsonArray(FIGURES);
				entity.figures = new ArrayList<StatisticsDescriptionEntity.Figure>(figures.size());
				for (int i = 0, imax = figures.size(); i < imax; i++) {
					final JsonObject f = figures.get(i).getAsJsonObject();

					final Figure figure = new Figure();
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
		public ScalarExampleEntity<?> deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final ScalarExampleEntity<?> entity = deserializeBaseParameter(obj, new ScalarExampleEntity<Object>());

			if (obj.has("document")) {
				entity.document = context.deserialize(obj.get("document"), DocumentEntity.class);
			}

			return entity;
		}

	}

	public static class SimpleByResultEntityDeserializer implements JsonDeserializer<SimpleByResultEntity> {

		@Override
		public SimpleByResultEntity deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final SimpleByResultEntity entity = deserializeBaseParameter(obj, new SimpleByResultEntity());

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
		public TransactionResultEntity deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final TransactionResultEntity entity = deserializeBaseParameter(obj, new TransactionResultEntity());

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
		public UserEntity deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final UserEntity entity = deserializeBaseParameter(obj, new UserEntity());

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
				final JsonObject authData = obj.getAsJsonObject("authData");
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
		public ImportResultEntity deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final ImportResultEntity entity = deserializeBaseParameter(obj, new ImportResultEntity());

			if (obj.has(CREATED)) {
				entity.setCreated(obj.getAsJsonPrimitive(CREATED).getAsInt());
			}

			if (obj.has(ERRORS)) {
				entity.setErrors(obj.getAsJsonPrimitive(ERRORS).getAsInt());
			}

			if (obj.has(EMPTY)) {
				entity.setEmpty(obj.getAsJsonPrimitive(EMPTY).getAsInt());
			}

			if (obj.has(UPDATED)) {
				entity.setUpdated(obj.getAsJsonPrimitive(UPDATED).getAsInt());
			}

			if (obj.has(IGNORED)) {
				entity.setIgnored(obj.getAsJsonPrimitive(IGNORED).getAsInt());
			}

			if (obj.has(DETAILS)) {
				final JsonArray asJsonArray = obj.getAsJsonArray(DETAILS);
				for (JsonElement jsonElement : asJsonArray) {
					entity.getDetails().add(jsonElement.getAsString());
				}
			}

			return entity;
		}
	}

	public static class DatabaseEntityDeserializer implements JsonDeserializer<DatabaseEntity> {
		@Override
		public DatabaseEntity deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final DatabaseEntity entity = deserializeBaseParameter(obj, new DatabaseEntity());

			if (obj.has(RESULT)) {
				final JsonObject result = obj.getAsJsonObject(RESULT);
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
		public StringsResultEntity deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final StringsResultEntity entity = deserializeBaseParameter(obj, new StringsResultEntity());

			if (obj.has(RESULT)) {
				entity.result = context.deserialize(obj.get(RESULT), resultType);
			}

			return entity;
		}
	}

	public static class BooleanResultEntityDeserializer implements JsonDeserializer<BooleanResultEntity> {
		@Override
		public BooleanResultEntity deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final BooleanResultEntity entity = deserializeBaseParameter(obj, new BooleanResultEntity());

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
		public Endpoint deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();

			final Endpoint entity = new Endpoint();
			entity.setDatabases((List<String>) context.deserialize(obj.getAsJsonArray("databases"), databasesType));
			entity.setEndpoint(obj.getAsJsonPrimitive("endpoint").getAsString());

			return entity;
		}
	}

	public static class DocumentResultEntityDeserializer implements JsonDeserializer<DocumentResultEntity<?>> {
		Type documentsType = new TypeToken<List<DocumentEntity<?>>>() {
		}.getType();

		@Override
		public DocumentResultEntity<?> deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final DocumentResultEntity<Object> entity = deserializeBaseParameter(obj,
				new DocumentResultEntity<Object>());

			if (obj.has(RESULT)) {
				final JsonElement resultElem = obj.get(RESULT);
				if (resultElem.isJsonArray()) {
					entity.result = context.deserialize(resultElem, documentsType);
				} else if (resultElem.isJsonObject()) {
					final DocumentEntity<Object> doc = context.deserialize(resultElem, DocumentEntity.class);
					final List<DocumentEntity<Object>> list = new ArrayList<DocumentEntity<Object>>(1);
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
		public ReplicationState deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final ReplicationState entity = new ReplicationState();

			entity.running = obj.getAsJsonPrimitive("running").getAsBoolean();
			entity.lastLogTick = obj.getAsJsonPrimitive("lastLogTick").getAsLong();
			entity.totalEvents = obj.getAsJsonPrimitive("totalEvents").getAsLong();
			entity.time = DateUtils.parse(obj.getAsJsonPrimitive("time").getAsString());

			return entity;
		}
	}

	public static class ReplicationInventoryEntityDeserializer implements JsonDeserializer<ReplicationInventoryEntity> {

		private final Type indexesType = new TypeToken<List<IndexEntity>>() {
		}.getType();

		@Override
		public ReplicationInventoryEntity deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final ReplicationInventoryEntity entity = deserializeBaseParameter(obj, new ReplicationInventoryEntity());

			if (obj.has(COLLECTIONS)) {
				final JsonArray collections = obj.getAsJsonArray(COLLECTIONS);
				entity.collections = new ArrayList<ReplicationInventoryEntity.Collection>(collections.size());
				for (int i = 0, imax = collections.size(); i < imax; i++) {
					final JsonObject elem = collections.get(i).getAsJsonObject();
					final Collection col = new Collection();

					if (elem.has("parameters")) {
						final JsonObject parameters = elem.getAsJsonObject("parameters");
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

		private void addCollectionParameters(final Collection col, final JsonObject parameters) {
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
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final ReplicationDumpRecord<DocumentEntity<Object>> entity = new ReplicationDumpRecord<DocumentEntity<Object>>();

			if (obj.has("tick")) {
				entity.tick = obj.getAsJsonPrimitive("tick").getAsLong();
			}
			if (obj.has("type")) {
				final int type = obj.getAsJsonPrimitive("type").getAsInt();
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
		public ReplicationSyncEntity deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final ReplicationSyncEntity entity = deserializeBaseParameter(obj, new ReplicationSyncEntity());

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
		public MapAsEntity deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final MapAsEntity entity = deserializeBaseParameter(obj, new MapAsEntity());

			entity.map = context.deserialize(obj, mapType);

			return entity;
		}
	}

	public static class ReplicationLoggerConfigEntityDeserializer
			implements JsonDeserializer<ReplicationLoggerConfigEntity> {
		@Override
		public ReplicationLoggerConfigEntity deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final ReplicationLoggerConfigEntity entity = deserializeBaseParameter(obj,
				new ReplicationLoggerConfigEntity());

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
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final ReplicationApplierConfigEntity entity = deserializeBaseParameter(obj,
				new ReplicationApplierConfigEntity());

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
		public ReplicationApplierState deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final ReplicationApplierState state = new ReplicationApplierState();

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

		private void deserializeTicks(final JsonObject obj, final ReplicationApplierState state) {
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

		private void deserializeProgress(final JsonObject obj, final ReplicationApplierState state) {
			if (obj.has("progress")) {
				final JsonObject progress = obj.getAsJsonObject("progress");
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

		private void deserializeLastError(final JsonObject obj, final ReplicationApplierState state) {
			if (obj.has(LAST_ERROR) && !obj.get(LAST_ERROR).isJsonNull()) {
				final JsonObject lastError = obj.getAsJsonObject(LAST_ERROR);
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
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final ReplicationApplierStateEntity entity = deserializeBaseParameter(obj,
				new ReplicationApplierStateEntity());

			if (obj.has(ENDPOINT)) {
				entity.endpoint = obj.getAsJsonPrimitive(ENDPOINT).getAsString();
			}

			if (obj.has(DATABASE)) {
				entity.database = obj.getAsJsonPrimitive(DATABASE).getAsString();
			}

			if (obj.has(SERVER)) {
				final JsonObject server = obj.getAsJsonObject(SERVER);
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
		private final Type clientsType = new TypeToken<List<Client>>() {
		}.getType();

		@Override
		public ReplicationLoggerStateEntity deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final ReplicationLoggerStateEntity entity = deserializeBaseParameter(obj,
				new ReplicationLoggerStateEntity());

			if (obj.has(STATE)) {
				entity.state = context.deserialize(obj.get(STATE), ReplicationState.class);
			}

			if (obj.has(SERVER)) {
				final JsonObject server = obj.getAsJsonObject(SERVER);
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
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final Client client = new Client();

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

		private static final String COLLECTION = "collection";

		@Override
		public GraphEntity deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final GraphEntity entity = deserializeBaseParameter(obj, new GraphEntity());

			final JsonObject graph = obj.has("graph") ? obj.getAsJsonObject("graph") : obj;
			deserializeDocumentParameter(graph, entity);

			if (graph.has("name")) {
				entity.name = graph.get("name").getAsString();
			}

			if (graph.has("orphanCollections")) {
				final JsonArray orphanCollections = graph.getAsJsonArray("orphanCollections");
				entity.orphanCollections = new ArrayList<String>();
				if (orphanCollections != null) {
					entity.orphanCollections = new ArrayList<String>(orphanCollections.size());
					for (int i = 0, imax = orphanCollections.size(); i < imax; i++) {
						final String orphanCollection = orphanCollections.get(i).getAsString();

						entity.orphanCollections.add(orphanCollection);
					}
				}
			}

			if (graph.has("edgeDefinitions")) {
				final JsonArray edgeDefinitions = graph.getAsJsonArray("edgeDefinitions");
				entity.edgeDefinitionsEntity = new EdgeDefinitionsEntity();
				if (edgeDefinitions != null) {
					addEdgeDefinitions(entity, edgeDefinitions);
				}
			}

			return entity;

		}

		private void addEdgeDefinitions(final GraphEntity entity, final JsonArray edgeDefinitions) {
			for (int i = 0, imax = edgeDefinitions.size(); i < imax; i++) {
				final EdgeDefinitionEntity edgeDefinitionEntity = new EdgeDefinitionEntity();
				final JsonObject edgeDefinition = edgeDefinitions.get(i).getAsJsonObject();
				if (edgeDefinition.has(COLLECTION)) {
					edgeDefinitionEntity.setCollection(edgeDefinition.get(COLLECTION).getAsString());
				}
				if (edgeDefinition.has("from")) {
					final List<String> from = new ArrayList<String>();
					final JsonElement fromElem = edgeDefinition.get("from");
					final JsonArray fromArray = fromElem.getAsJsonArray();
					final Iterator<JsonElement> iterator = fromArray.iterator();
					while (iterator.hasNext()) {
						final JsonElement e = iterator.next();
						from.add(e.getAsString());
					}

					edgeDefinitionEntity.setFrom(from);
				}
				if (edgeDefinition.has("to")) {
					final List<String> to = new ArrayList<String>();
					final JsonElement toElem = edgeDefinition.get("to");
					final JsonArray toArray = toElem.getAsJsonArray();
					final Iterator<JsonElement> iterator = toArray.iterator();
					while (iterator.hasNext()) {
						final JsonElement e = iterator.next();
						to.add(e.getAsString());
					}
					edgeDefinitionEntity.setTo(to);
				}
				entity.edgeDefinitionsEntity.addEdgeDefinition(edgeDefinitionEntity);
			}
		}
	}

	public static class GraphsEntityDeserializer implements JsonDeserializer<GraphsEntity> {
		private final Type graphsType = new TypeToken<List<GraphEntity>>() {
		}.getType();

		@Override
		public GraphsEntity deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final GraphsEntity entity = deserializeBaseParameter(obj, new GraphsEntity());

			if (obj.has("graphs")) {
				entity.graphs = context.deserialize(obj.get("graphs"), graphsType);
			}

			return entity;

		}
	}

	public static class BaseDocumentDeserializer implements JsonDeserializer<BaseDocument> {
		@Override
		public BaseDocument deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final BaseDocument entity = deserializeDocumentParameter(obj, new BaseDocument());

			if (entity instanceof BaseDocument) {
				entity.setProperties(DeserializeSingleEntry.deserializeJsonObject(obj));
			}

			return entity;
		}
	}

	public static class DeleteEntityDeserializer implements JsonDeserializer<DeletedEntity> {
		@Override
		public DeletedEntity deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final DeletedEntity entity = deserializeBaseParameter(obj, new DeletedEntity());

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
		public VertexEntity<?> deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final VertexEntity<Object> entity = deserializeBaseParameter(obj, new VertexEntity<Object>());

			final JsonObject vertex = obj.has("vertex") ? obj.getAsJsonObject("vertex") : obj;
			deserializeDocumentParameter(vertex, entity);

			final Class<?> clazz = getParameterized();
			if (clazz != null) {
				entity.setEntity(context.deserialize(vertex, clazz));
			}

			return entity;
		}
	}

	public static class EdgeEntityDeserializer implements JsonDeserializer<EdgeEntity<?>> {
		@Override
		public EdgeEntity<?> deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final EdgeEntity<Object> entity = deserializeBaseParameter(obj, new EdgeEntity<Object>());

			final JsonObject edge = obj.has("edge") ? obj.getAsJsonObject("edge") : obj;
			deserializeDocumentParameter(edge, entity);

			if (edge.has("_from")) {
				entity.fromVertexHandle = edge.getAsJsonPrimitive("_from").getAsString();
			}
			if (edge.has("_to")) {
				entity.toVertexHandle = edge.getAsJsonPrimitive("_to").getAsString();
			}

			// 他のフィールドはリフレクションで。 (TODO: Annotationのサポートと上記パラメータを弾く)
			final Class<?> clazz = getParameterized();
			if (clazz != null) {
				entity.entity = context.deserialize(edge, clazz);
			}

			return entity;
		}

	}

	public static class TraversalEntityDeserializer implements JsonDeserializer<TraversalEntity<?, ?>> {

		@Override
		public TraversalEntity<?, ?> deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final TraversalEntity<Object, Object> entity = deserializeBaseParameter(obj,
				new TraversalEntity<Object, Object>());
			deserializeBaseParameter(obj, entity);

			final JsonObject result = getFirstResultAsJsonObject(obj);
			if (result != null && result.getAsJsonObject().has("visited")) {
				final JsonObject visited = result.getAsJsonObject().getAsJsonObject("visited");

				final Class<?> vertexClazz = getParameterized();
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
			final JsonDeserializationContext context,
			final JsonObject visited,
			final Class<?> vertexClazz,
			final Class<?> edgeClazz) {
			final List<PathEntity<Object, Object>> pathEntities = new ArrayList<PathEntity<Object, Object>>();
			final JsonArray paths = visited.getAsJsonArray(PATHS);
			if (paths != null) {
				for (int i = 0, imax = paths.size(); i < imax; i++) {
					final JsonObject path = paths.get(i).getAsJsonObject();
					final PathEntity<Object, Object> pathEntity = new PathEntity<Object, Object>();

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
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final ShortestPathEntity<Object, Object> entity = deserializeBaseParameter(obj,
				new ShortestPathEntity<Object, Object>());
			deserializeBaseParameter(obj, entity);

			final JsonObject result = getFirstResultAsJsonObject(obj);
			if (result != null) {
				final Class<?> vertexClazz = getParameterized();
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
			final JsonDeserializationContext context,
			final ShortestPathEntity<Object, Object> entity,
			final JsonObject result,
			final Class<?> vertexClazz,
			final Class<?> edgeClazz) {
			final JsonArray paths = result.getAsJsonArray(PATHS);
			if (paths != null && paths.size() > 0) {
				final JsonObject path = paths.get(0).getAsJsonObject();

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
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final QueryCachePropertiesEntity entity = deserializeBaseParameter(obj, new QueryCachePropertiesEntity());

			if (obj.has("mode")) {
				final String modeAsString = obj.getAsJsonPrimitive("mode").getAsString();
				entity.setMode(CacheMode.valueOf(modeAsString));
			}

			if (obj.has("maxResults")) {
				entity.setMaxResults(obj.getAsJsonPrimitive("maxResults").getAsLong());
			}

			return entity;
		}

	}

	public static class QueriesResultEntityDeserializer implements JsonDeserializer<QueriesResultEntity> {

		@Override
		public QueriesResultEntity deserialize(
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonArray array = json.getAsJsonArray();
			final Iterator<JsonElement> iterator = array.iterator();
			final List<QueryEntity> queries = new ArrayList<QueryEntity>();
			while (iterator.hasNext()) {
				final JsonElement element = iterator.next();
				final JsonObject obj = element.getAsJsonObject();
				final QueryEntity entity = new QueryEntity();

				if (obj.has(ID)) {
					entity.setId(obj.getAsJsonPrimitive(ID).getAsString());
					queries.add(entity);
				}

				if (obj.has("query")) {
					entity.setQuery(obj.getAsJsonPrimitive("query").getAsString());
				}

				if (obj.has("started")) {
					final String str = obj.getAsJsonPrimitive("started").getAsString();

					final SimpleDateFormat sdf = new SimpleDateFormat(ALT_DATE_TIME_FORMAT);
					try {
						entity.setStarted(sdf.parse(str));
					} catch (final ParseException e) {
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
			final JsonElement json,
			final Type typeOfT,
			final JsonDeserializationContext context) {

			if (json.isJsonNull()) {
				return null;
			}

			final JsonObject obj = json.getAsJsonObject();
			final QueryTrackingPropertiesEntity entity = deserializeBaseParameter(obj,
				new QueryTrackingPropertiesEntity());

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

	private static JsonObject getFirstResultAsJsonObject(final JsonObject obj) {
		if (obj.has(RESULT)) {
			if (obj.get(RESULT).isJsonArray()) {
				return getElementAsJsonObject(obj.getAsJsonArray(RESULT));
			} else if (obj.get(RESULT).isJsonObject()) {
				return obj.getAsJsonObject(RESULT);
			}
		}
		return null;
	}

	private static JsonObject getElementAsJsonObject(final JsonArray arr) {
		if (arr != null && arr.size() > 0) {
			final JsonElement jsonElement = arr.get(0);
			if (jsonElement.isJsonObject()) {
				return jsonElement.getAsJsonObject();
			}
		}
		return null;
	}

	private static List<VertexEntity<Object>> getVertices(
		final Class<?> vertexClazz,
		final JsonDeserializationContext context,
		final JsonArray vertices) {
		final List<VertexEntity<Object>> list = new ArrayList<VertexEntity<Object>>();
		if (vertices != null) {
			for (int i = 0, imax = vertices.size(); i < imax; i++) {
				final JsonObject vertex = vertices.get(i).getAsJsonObject();
				final VertexEntity<Object> ve = getVertex(context, vertex, vertexClazz);
				list.add(ve);
			}
		}
		return list;
	}

	private static VertexEntity<Object> getVertex(
		final JsonDeserializationContext context,
		final JsonObject vertex,
		final Class<?> vertexClazz) {
		final VertexEntity<Object> ve = deserializeBaseParameter(vertex, new VertexEntity<Object>());
		deserializeDocumentParameter(vertex, ve);
		if (vertexClazz != null) {
			ve.setEntity(context.deserialize(vertex, vertexClazz));
		} else {
			ve.setEntity(context.deserialize(vertex, Object.class));
		}
		return ve;
	}

	private static List<EdgeEntity<Object>> getEdges(
		final Class<?> edgeClazz,
		final JsonDeserializationContext context,
		final JsonArray edges) {
		final List<EdgeEntity<Object>> list = new ArrayList<EdgeEntity<Object>>();
		if (edges != null) {
			for (int i = 0, imax = edges.size(); i < imax; i++) {
				final JsonObject edge = edges.get(i).getAsJsonObject();
				final EdgeEntity<Object> ve = deserializeBaseParameter(edge, new EdgeEntity<Object>());
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
