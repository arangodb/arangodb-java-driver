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
import java.util.*;
import java.util.Map.Entry;

import com.arangodb.entity.CollectionEntity.Figures;
import com.arangodb.entity.ReplicationApplierState.LastError;
import com.arangodb.entity.ReplicationApplierState.Progress;
import com.arangodb.entity.ReplicationInventoryEntity.Collection;
import com.arangodb.entity.ReplicationInventoryEntity.CollectionParameter;
import com.arangodb.entity.ReplicationLoggerStateEntity.Client;
import com.arangodb.entity.StatisticsDescriptionEntity.Figure;
import com.arangodb.entity.StatisticsDescriptionEntity.Group;
import com.arangodb.entity.StatisticsEntity.FigureValue;
import com.arangodb.util.DateUtils;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

/**
 * Entity deserializer , internally used.
 *
 * @author tamtam180 - kirscheless at gmail.com
 * 
 */
public class EntityDeserializers {

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

  private static Class<?> backParameterized() {
    ClassHolder holder = parameterizedBridger.get();
    if (holder == null) {
      return null;
    }
    return holder.back();
  }

  private static <T extends BaseEntity> T deserializeBaseParameter(JsonObject obj, T entity) {
    if (obj.has("error")) {
      entity.error = obj.getAsJsonPrimitive("error").getAsBoolean();
    }
    if (obj.has("code")) {
      entity.code = obj.getAsJsonPrimitive("code").getAsInt();
    }
    if (obj.has("errorNum")) {
      entity.errorNumber = obj.getAsJsonPrimitive("errorNum").getAsInt();
    }
    if (obj.has("errorMessage")) {
      entity.errorMessage = obj.getAsJsonPrimitive("errorMessage").getAsString();
    }
    if (obj.has("etag")) {
      entity.etag = obj.getAsJsonPrimitive("etag").getAsLong();
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
    if (true) {

    }

    return entity;
  }

  public static class DefaultEntityDeserializer implements JsonDeserializer<DefaultEntity> {
    @Override
    public DefaultEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      if (json.isJsonNull()) {
        return null;
      }
      return deserializeBaseParameter(json.getAsJsonObject(), new DefaultEntity());
    }
  }

  public static class VersionDeserializer implements JsonDeserializer<ArangoVersion> {
    @Override
    public ArangoVersion deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

      if (json.isJsonNull()) {
        return null;
      }

      JsonObject obj = json.getAsJsonObject();
      ArangoVersion entity = deserializeBaseParameter(obj, new ArangoVersion());

      if (obj.has("server")) {
        entity.server = obj.getAsJsonPrimitive("server").getAsString();
      }

      if (obj.has("version")) {
        entity.version = obj.getAsJsonPrimitive("version").getAsString();
      }

      return entity;
    }
  }

  public static class ArangoUnixTimeDeserializer implements JsonDeserializer<ArangoUnixTime> {
    @Override
    public ArangoUnixTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

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
        entity.microsecond = (pos >= 0 && pos + 1 != time.length()) ? Integer.parseInt(time.substring(pos + 1)) : 0;
      }

      return entity;
    }
  }

  public static class FiguresDeserializer implements JsonDeserializer<Figures> {
    @Override
    public Figures deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

      if (json.isJsonNull()) {
        return null;
      }

      JsonObject obj = json.getAsJsonObject();
      Figures entity = new Figures();

      if (obj.has("alive")) {
        JsonObject alive = obj.getAsJsonObject("alive");
        entity.aliveCount = alive.getAsJsonPrimitive("count").getAsLong();
        entity.aliveSize = alive.getAsJsonPrimitive("size").getAsLong();
      }

      if (obj.has("dead")) {
        JsonObject dead = obj.getAsJsonObject("dead");
        entity.deadCount = dead.getAsJsonPrimitive("count").getAsLong();
        entity.deadSize = dead.getAsJsonPrimitive("size").getAsLong();
        entity.deadDeletion = dead.getAsJsonPrimitive("deletion").getAsLong();
      }

      if (obj.has("datafiles")) {
        JsonObject datafiles = obj.getAsJsonObject("datafiles");
        entity.datafileCount = datafiles.getAsJsonPrimitive("count").getAsLong();
        entity.datafileFileSize = datafiles.getAsJsonPrimitive("fileSize").getAsLong();
      }

      if (obj.has("journals")) {
        JsonObject journals = obj.getAsJsonObject("journals");
        entity.journalsCount = journals.getAsJsonPrimitive("count").getAsLong();
        entity.journalsFileSize = journals.getAsJsonPrimitive("fileSize").getAsLong();
      }

      if (obj.has("compactors")) {
        JsonObject compactors = obj.getAsJsonObject("compactors");
        entity.compactorsCount = compactors.getAsJsonPrimitive("count").getAsLong();
        entity.compactorsFileSize = compactors.getAsJsonPrimitive("fileSize").getAsLong();
      }

      if (obj.has("shapefiles")) {
        JsonObject shapefiles = obj.getAsJsonObject("shapefiles");
        entity.shapefilesCount = shapefiles.getAsJsonPrimitive("count").getAsLong();
        entity.shapefilesFileSize = shapefiles.getAsJsonPrimitive("fileSize").getAsLong();
      }

      if (obj.has("shapes")) {
        JsonObject shapes = obj.getAsJsonObject("shapes");
        entity.shapesCount = shapes.getAsJsonPrimitive("count").getAsLong();
      }

      if (obj.has("attributes")) {
        JsonObject attributes = obj.getAsJsonObject("attributes");
        entity.attributesCount = attributes.getAsJsonPrimitive("count").getAsLong();
      }

      if (obj.has("indexes")) {
        JsonObject indexes = obj.getAsJsonObject("indexes");
        entity.indexesCount = indexes.getAsJsonPrimitive("count").getAsLong();
        entity.indexesSize = indexes.getAsJsonPrimitive("size").getAsLong();
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
    public CollectionKeyOption deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

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
    public CollectionEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

      if (json.isJsonNull()) {
        return null;
      }

      JsonObject obj = json.getAsJsonObject();
      CollectionEntity entity = deserializeBaseParameter(obj, new CollectionEntity());

      if (obj.has("name")) {
        entity.name = obj.getAsJsonPrimitive("name").getAsString();
      }

      if (obj.has("id")) {
        entity.id = obj.getAsJsonPrimitive("id").getAsLong();
      }

      if (obj.has("status")) {
        entity.status = context.deserialize(obj.get("status"), CollectionStatus.class);
      }

      if (obj.has("waitForSync")) {
        entity.waitForSync = obj.getAsJsonPrimitive("waitForSync").getAsBoolean();
      }

      if (obj.has("isSystem")) {
        entity.isSystem = obj.getAsJsonPrimitive("isSystem").getAsBoolean();
      }

      if (obj.has("isVolatile")) {
        entity.isVolatile = obj.getAsJsonPrimitive("isVolatile").getAsBoolean();
      }

      if (obj.has("journalSize")) {
        entity.journalSize = obj.getAsJsonPrimitive("journalSize").getAsLong();
      }

      if (obj.has("count")) {
        entity.count = obj.getAsJsonPrimitive("count").getAsLong();
      }

      if (obj.has("revision")) {
        entity.revision = obj.getAsJsonPrimitive("revision").getAsLong();
      }

      if (obj.has("figures")) {
        entity.figures = context.deserialize(obj.get("figures"), Figures.class);
      }

      if (obj.has("type")) {
        entity.type = CollectionType.valueOf(obj.getAsJsonPrimitive("type").getAsInt());
      }

      if (obj.has("keyOptions")) {
        entity.keyOptions = context.deserialize(obj.get("keyOptions"), CollectionKeyOption.class);
      }

      if (obj.has("checksum")) {
        entity.checksum = obj.getAsJsonPrimitive("checksum").getAsLong();
      }

      if (obj.has("doCompact")) {
        entity.doCompact = obj.getAsJsonPrimitive("doCompact").getAsBoolean();
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
    public CollectionsEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

      if (json.isJsonNull()) {
        return null;
      }

      JsonObject obj = json.getAsJsonObject();
      CollectionsEntity entity = deserializeBaseParameter(obj, new CollectionsEntity());

      if (obj.has("collections")) {
        entity.collections = context.deserialize(obj.get("collections"), collectionsType);
      }
      if (obj.has("names")) {
        entity.names = context.deserialize(obj.get("names"), namesType);
      }

      return entity;
    }
  }

  public static class AqlfunctionsEntityDeserializer implements JsonDeserializer<AqlFunctionsEntity> {

    @Override
    public AqlFunctionsEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

      if (json.isJsonNull()) {
        return null;
      }

      JsonArray obj = json.getAsJsonArray();
      Iterator<JsonElement> iterator = obj.iterator();
      Map<String, String> functions = new HashMap<String, String>();
      while (iterator.hasNext()) {
        JsonElement e = iterator.next();
        JsonObject o = e.getAsJsonObject();
        functions.put(o.get("name").getAsString(), o.get("code").getAsString());
      }
      return new AqlFunctionsEntity(functions);
    }
  }


  public static class JobsEntityDeserializer implements JsonDeserializer<JobsEntity> {

    public JobsEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {

      if (json.isJsonNull()) {
        return null;
      }

      JsonArray obj = json.getAsJsonArray();
      Iterator<JsonElement> iterator = obj.iterator();
      List<String> jobs = new ArrayList<String>();
      while(iterator.hasNext()) {
        JsonElement e  = iterator.next();
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
    public CursorEntity<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

      if (json.isJsonNull()) {
        return null;
      }

      JsonObject obj = json.getAsJsonObject();
      CursorEntity<Object> entity = deserializeBaseParameter(obj, new CursorEntity<Object>());

      if (obj.has("result")) {
        JsonArray array = obj.getAsJsonArray("result");
        if (array == null || array.isJsonNull() || array.size() == 0) {
          entity.results = Collections.emptyList();
        } else {
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
      }

      if (obj.has("hasMore")) {
        entity.hasMore = obj.getAsJsonPrimitive("hasMore").getAsBoolean();
      }

      if (obj.has("count")) {
        entity.count = obj.getAsJsonPrimitive("count").getAsInt();
      }

      if (obj.has("id")) {
        entity.cursorId = obj.getAsJsonPrimitive("id").getAsLong();
      }

      if (obj.has("bindVars")) {
        entity.bindVars = context.deserialize(obj.get("bindVars"), bindVarsType);
      }
      
      if (obj.has("extra")) {
        entity.extra = context.deserialize(obj.get("extra"), extraType);

        if (entity.extra.containsKey("stats")) {
          if (entity.extra.get("stats") instanceof Map<?, ?>) {
            Map<String, Object> m = (Map<String, Object>) entity.extra.get("stats");
            if (m.containsKey("fullCount")) {
              try {
                if (m.get("fullCount") instanceof Double) {
                  Double v = (Double) m.get("fullCount");
                  entity.fullCount = v.intValue();
                }
              }
              catch (Exception e) { } 
            }
          }
        }
      }

      return entity;
    }
  }

  public static class DocumentEntityDeserializer implements JsonDeserializer<DocumentEntity<?>> {

    @Override
    public DocumentEntity<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

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
      DocumentEntity<Object> entity = deserializeBaseParameter(obj, new DocumentEntity<Object>());
      deserializeDocumentParameter(obj, entity);

      // 他のフィールドはリフレクションで。 (TODO: Annotationのサポートと上記パラメータを弾く)
      Class<?> clazz = getParameterized();
      if (clazz != null) {
        entity.entity = context.deserialize(obj, clazz);
        if (clazz.getName().equalsIgnoreCase(BaseDocument.class.getName())) {
          // iterate all key/value pairs of the jsonObject and determine its class(String, Number, Boolean, HashMap, List)
          ((BaseDocument) entity.entity).setProperties(DeserializeSingleEntry.deserializeJsonObject(obj));
        }
      }

      return entity;
    }
  }

  public static class DeserializeSingleEntry {

    private static final List<String> nonProperties = new ArrayList<String>()        {
      {
        add("_id");
        add("_rev");
        add("_key");
      }
    };

    /**
     * desirializes any jsonElement
     *
     * @param jsonElement
     * @return
     */
    public static Object deserializeJsonElement (JsonElement jsonElement) {
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
     * desirializes a JsonObject into a Map<String, Object>
     *
     * @param jsonObject a jsonObject
     * @return the deserialized jsonObject
     */
    private static Map<String, Object> deserializeJsonObject(JsonObject jsonObject) {
      Map<String, Object> result = new HashMap<String, Object>();
      Set<Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
      for(Map.Entry<String,JsonElement> entry : entrySet){
        if (! nonProperties.contains(entry.getKey())) {
          result.put(entry.getKey(), deserializeJsonElement((JsonElement) jsonObject.get(entry.getKey())));
        }
      }
      return result;
    }

    private static List<Object> deserializeJsonArray (JsonArray jsonArray) {
      List<Object> tmpObjectList = new ArrayList<Object>();
      Iterator iterator = (jsonArray.iterator());
      while(iterator.hasNext()) {
        tmpObjectList.add(deserializeJsonElement((JsonElement) iterator.next()));
      }
      return tmpObjectList;
    }

    /**
     * deserializes a jsonPrimitiv into the equivalent java primitive
     *
     * @param jsonPrimitive
     * @return null|String|Double|Boolean
     */
    private static Object deserializeJsonPrimitive (JsonPrimitive jsonPrimitive) {
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
    public DocumentsEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

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
    public IndexEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

      if (json.isJsonNull()) {
        return null;
      }

      JsonObject obj = json.getAsJsonObject();
      IndexEntity entity = deserializeBaseParameter(obj, new IndexEntity());

      if (obj.has("id")) {
        entity.id = obj.getAsJsonPrimitive("id").getAsString();
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

      if (obj.has("size")) {
        entity.size = obj.getAsJsonPrimitive("size").getAsInt();
      }

      if (obj.has("minLength")) {
        entity.minLength = obj.getAsJsonPrimitive("minLength").getAsInt();
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
    public IndexesEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

      if (json.isJsonNull()) {
        return null;
      }

      JsonObject obj = json.getAsJsonObject();
      IndexesEntity entity = deserializeBaseParameter(obj, new IndexesEntity());

      if (obj.has("indexes")) {
        entity.indexes = context.deserialize(obj.get("indexes"), indexesType);
      }

      if (obj.has("identifiers")) {
        entity.identifiers = context.deserialize(obj.get("identifiers"), identifiersType);
      }

      return entity;
    }

  }

  public static class AdminLogEntryEntityDeserializer implements JsonDeserializer<AdminLogEntity> {
    @Override
    public AdminLogEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

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
    public StatisticsEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

      if (json.isJsonNull()) {
        return null;
      }

      JsonObject obj = json.getAsJsonObject();
      StatisticsEntity entity = deserializeBaseParameter(obj, new StatisticsEntity());

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

      if (obj.has("client")) {
        StatisticsEntity.Client cli = new StatisticsEntity.Client();
        cli.figures = new TreeMap<String, StatisticsEntity.FigureValue>();
        entity.client = cli;

        JsonObject client = obj.getAsJsonObject("client");
        if (client.has("httpConnections")) {
          cli.httpConnections = client.getAsJsonPrimitive("httpConnections").getAsInt();
        }
        for (Entry<String, JsonElement> ent : client.entrySet()) {
          if (!ent.getKey().equals("httpConnections")) {
            JsonObject f = ent.getValue().getAsJsonObject();
            FigureValue fv = new FigureValue();
            fv.sum = f.getAsJsonPrimitive("sum").getAsDouble();
            fv.count = f.getAsJsonPrimitive("count").getAsLong();
            fv.counts = context.deserialize(f.getAsJsonArray("counts"), countsType);
            cli.figures.put(ent.getKey(), fv);
          }
        }
      }

      if (obj.has("server")) {
        JsonObject svr = obj.getAsJsonObject("server");
        entity.server = new StatisticsEntity.Server();

        if (svr.has("uptime")) {
          entity.server.uptime = svr.getAsJsonPrimitive("uptime").getAsDouble();
        }
      }

      return entity;

    }
  }

  public static class StatisticsDescriptionEntityDeserializer implements JsonDeserializer<StatisticsDescriptionEntity> {
    Type cutsTypes = new TypeToken<BigDecimal[]>() {
    }.getType();

    @Override
    public StatisticsDescriptionEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

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

      if (obj.has("figures")) {
        JsonArray figures = obj.getAsJsonArray("figures");
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
    public ScalarExampleEntity<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

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
    public SimpleByResultEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

      if (json.isJsonNull()) {
        return null;
      }

      JsonObject obj = json.getAsJsonObject();
      SimpleByResultEntity entity = deserializeBaseParameter(obj, new SimpleByResultEntity());

      if (obj.has("deleted")) {
        entity.count = entity.deleted = obj.getAsJsonPrimitive("deleted").getAsInt();
      }

      if (obj.has("replaced")) {
        entity.count = entity.replaced = obj.getAsJsonPrimitive("replaced").getAsInt();
      }

      if (obj.has("updated")) {
        entity.count = entity.updated = obj.getAsJsonPrimitive("updated").getAsInt();
      }

      return entity;
    }

  }

  public static class TransactionResultEntityDeserializer implements JsonDeserializer<TransactionResultEntity> {

    @Override
    public TransactionResultEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {

      if (json.isJsonNull()) {
        return null;
      }

      JsonObject obj = json.getAsJsonObject();
      TransactionResultEntity entity = deserializeBaseParameter(obj, new TransactionResultEntity());

      if (obj.has("result")) { // MEMO:
        if (obj.get("result") instanceof JsonObject) {
          entity.setResult((Object) obj.get("result"));
        } else if (obj.getAsJsonPrimitive("result").isBoolean()) {
          entity.setResult((Boolean) (obj.getAsJsonPrimitive("result").getAsBoolean()));
        } else if (obj.getAsJsonPrimitive("result").isNumber()) {
          entity.setResult(obj.getAsJsonPrimitive("result").getAsNumber());
        } else  if (obj.getAsJsonPrimitive("result").isString()) {
          entity.setResult((String) (obj.getAsJsonPrimitive("result").getAsString()));
        }
      }


      return entity;
    }

  }

  public static class UserEntityDeserializer implements JsonDeserializer<UserEntity> {

    @Override
    public UserEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

      if (json.isJsonNull()) {
        return null;
      }

      JsonObject obj = json.getAsJsonObject();
      UserEntity entity = deserializeBaseParameter(obj, new UserEntity());

      if (obj.has("user")) { // MEMO:
        // RequestはusernameなのにResponseは何故userなのか。。
        entity.username = obj.getAsJsonPrimitive("user").getAsString();
      }

      if (obj.has("password")) {
        entity.password = obj.getAsJsonPrimitive("password").getAsString();
      }

      if (obj.has("active")) {
        entity.active = obj.getAsJsonPrimitive("active").getAsBoolean();
      }

      if (obj.has("extra")) {
        entity.extra = context.deserialize(obj.getAsJsonObject("extra"), Map.class);
      }

      return entity;
    }

  }

  public static class ImportResultEntityDeserializer implements JsonDeserializer<ImportResultEntity> {
    @Override
    public ImportResultEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

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
    public DatabaseEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

      if (json.isJsonNull()) {
        return null;
      }

      JsonObject obj = json.getAsJsonObject();
      DatabaseEntity entity = deserializeBaseParameter(obj, new DatabaseEntity());

      if (obj.has("result")) {
        JsonObject result = obj.getAsJsonObject("result");
        if (result.has("name")) {
          entity.name = result.getAsJsonPrimitive("name").getAsString();
        }
        if (result.has("id")) {
          entity.id = result.getAsJsonPrimitive("id").getAsString();
        }
        if (result.has("path")) {
          entity.path = result.getAsJsonPrimitive("path").getAsString();
        }
        if (result.has("isSystem")) {
          entity.isSystem = result.getAsJsonPrimitive("isSystem").getAsBoolean();
        }
      }

      return entity;
    }
  }

  public static class StringsResultEntityDeserializer implements JsonDeserializer<StringsResultEntity> {
    Type resultType = new TypeToken<ArrayList<String>>() {
    }.getType();

    @Override
    public StringsResultEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

      if (json.isJsonNull()) {
        return null;
      }

      JsonObject obj = json.getAsJsonObject();
      StringsResultEntity entity = deserializeBaseParameter(obj, new StringsResultEntity());

      if (obj.has("result")) {
        entity.result = context.deserialize(obj.get("result"), resultType);
      }

      return entity;
    }
  }

  public static class BooleanResultEntityDeserializer implements JsonDeserializer<BooleanResultEntity> {
    @Override
    public BooleanResultEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

      if (json.isJsonNull()) {
        return null;
      }

      JsonObject obj = json.getAsJsonObject();
      BooleanResultEntity entity = deserializeBaseParameter(obj, new BooleanResultEntity());

      if (obj.has("result")) {
        entity.result = obj.getAsJsonPrimitive("result").getAsBoolean();
      }

      return entity;
    }
  }

  public static class EndpointDeserializer implements JsonDeserializer<Endpoint> {
    Type databasesType = new TypeToken<List<String>>() {
    }.getType();

    @Override
    public Endpoint deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

      if (json.isJsonNull()) {
        return null;
      }

      JsonObject obj = json.getAsJsonObject();

      Endpoint entity = new Endpoint();
      entity.databases = context.deserialize(obj.getAsJsonArray("databases"), databasesType);
      entity.endpoint = obj.getAsJsonPrimitive("endpoint").getAsString();

      return entity;
    }
  }

  public static class DocumentResultEntityDeserializer implements JsonDeserializer<DocumentResultEntity<?>> {
    Type documentsType = new TypeToken<List<DocumentEntity<?>>>() {
    }.getType();

    @Override
    public DocumentResultEntity<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

      if (json.isJsonNull()) {
        return null;
      }

      JsonObject obj = json.getAsJsonObject();
      DocumentResultEntity<Object> entity = deserializeBaseParameter(obj, new DocumentResultEntity<Object>());

      if (obj.has("result")) {
        JsonElement resultElem = obj.get("result");
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
    public ReplicationState deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

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
    public ReplicationInventoryEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

      if (json.isJsonNull()) {
        return null;
      }

      JsonObject obj = json.getAsJsonObject();
      ReplicationInventoryEntity entity = deserializeBaseParameter(obj, new ReplicationInventoryEntity());

      if (obj.has("collections")) {
        JsonArray collections = obj.getAsJsonArray("collections");
        entity.collections = new ArrayList<ReplicationInventoryEntity.Collection>(collections.size());
        for (int i = 0, imax = collections.size(); i < imax; i++) {
          JsonObject elem = collections.get(i).getAsJsonObject();
          Collection col = new Collection();

          if (elem.has("parameters")) {
            JsonObject parameters = elem.getAsJsonObject("parameters");

            col.parameter = new CollectionParameter();
            if (parameters.has("version")) {
              col.parameter.version = parameters.getAsJsonPrimitive("version").getAsInt();
            }
            if (parameters.has("type")) {
              col.parameter.type = CollectionType.valueOf(parameters.getAsJsonPrimitive("type").getAsInt());
            }
            if (parameters.has("cid")) {
              col.parameter.cid = parameters.getAsJsonPrimitive("cid").getAsLong();
            }
            if (parameters.has("deleted")) {
              col.parameter.deleted = parameters.getAsJsonPrimitive("deleted").getAsBoolean();
            }
            if (parameters.has("doCompact")) {
              col.parameter.doCompact = parameters.getAsJsonPrimitive("doCompact").getAsBoolean();
            }
            if (parameters.has("maximalSize")) {
              col.parameter.maximalSize = parameters.getAsJsonPrimitive("maximalSize").getAsLong();
            }
            if (parameters.has("name")) {
              col.parameter.name = parameters.getAsJsonPrimitive("name").getAsString();
            }
            if (parameters.has("isVolatile")) {
              col.parameter.isVolatile = parameters.getAsJsonPrimitive("isVolatile").getAsBoolean();
            }
            if (parameters.has("waitForSync")) {
              col.parameter.waitForSync = parameters.getAsJsonPrimitive("waitForSync").getAsBoolean();
            }
          }

          if (elem.has("indexes")) {
            col.indexes = context.deserialize(elem.getAsJsonArray("indexes"), indexesType);
          }

          entity.collections.add(col);
        }
      }

      if (obj.has("state")) {
        entity.state = context.deserialize(obj.getAsJsonObject("state"), ReplicationState.class);
      }

      if (obj.has("tick")) {
        entity.tick = obj.getAsJsonPrimitive("tick").getAsLong();
      }

      return entity;
    }
  }

  public static class ReplicationDumpRecordDeserializer implements JsonDeserializer<ReplicationDumpRecord<?>> {
    Type documentsType = new TypeToken<List<DocumentEntity<?>>>() {
    }.getType();

    @Override
    public ReplicationDumpRecord<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

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
    public ReplicationSyncEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

      if (json.isJsonNull()) {
        return null;
      }

      JsonObject obj = json.getAsJsonObject();
      ReplicationSyncEntity entity = deserializeBaseParameter(obj, new ReplicationSyncEntity());

      if (obj.has("collections")) {
        entity.collections = context.deserialize(obj.getAsJsonArray("collections"), collectionsType);
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
    public MapAsEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

      if (json.isJsonNull()) {
        return null;
      }

      JsonObject obj = json.getAsJsonObject();
      MapAsEntity entity = deserializeBaseParameter(obj, new MapAsEntity());

      entity.map = context.deserialize(obj, mapType);

      return entity;
    }
  }

  public static class ReplicationLoggerConfigEntityDeserializer implements
      JsonDeserializer<ReplicationLoggerConfigEntity> {
    @Override
    public ReplicationLoggerConfigEntity
        deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

      if (json.isJsonNull()) {
        return null;
      }

      JsonObject obj = json.getAsJsonObject();
      ReplicationLoggerConfigEntity entity = deserializeBaseParameter(obj, new ReplicationLoggerConfigEntity());

      if (obj.has("autoStart")) {
        entity.autoStart = obj.getAsJsonPrimitive("autoStart").getAsBoolean();
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

  public static class ReplicationApplierConfigEntityDeserializer implements
      JsonDeserializer<ReplicationApplierConfigEntity> {
    @Override
    public ReplicationApplierConfigEntity
        deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

      if (json.isJsonNull()) {
        return null;
      }

      JsonObject obj = json.getAsJsonObject();
      ReplicationApplierConfigEntity entity = deserializeBaseParameter(obj, new ReplicationApplierConfigEntity());

      if (obj.has("endpoint")) {
        entity.endpoint = obj.getAsJsonPrimitive("endpoint").getAsString();
      }

      if (obj.has("database")) {
        entity.database = obj.getAsJsonPrimitive("database").getAsString();
      }

      if (obj.has("username")) {
        entity.username = obj.getAsJsonPrimitive("username").getAsString();
      }

      if (obj.has("password")) {
        entity.password = obj.getAsJsonPrimitive("password").getAsString();
      }

      if (obj.has("maxConnectRetries")) {
        entity.maxConnectRetries = obj.getAsJsonPrimitive("maxConnectRetries").getAsInt();
      }

      if (obj.has("connectTimeout")) {
        entity.connectTimeout = obj.getAsJsonPrimitive("connectTimeout").getAsInt();
      }

      if (obj.has("requestTimeout")) {
        entity.requestTimeout = obj.getAsJsonPrimitive("requestTimeout").getAsInt();
      }

      if (obj.has("chunkSize")) {
        entity.chunkSize = obj.getAsJsonPrimitive("chunkSize").getAsInt();
      }

      if (obj.has("autoStart")) {
        entity.autoStart = obj.getAsJsonPrimitive("autoStart").getAsBoolean();
      }

      if (obj.has("adaptivePolling")) {
        entity.adaptivePolling = obj.getAsJsonPrimitive("adaptivePolling").getAsBoolean();
      }

      return entity;
    }
  }

  public static class ReplicationApplierStateDeserializer implements JsonDeserializer<ReplicationApplierState> {
    @Override
    public ReplicationApplierState deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

      if (json.isJsonNull()) {
        return null;
      }

      JsonObject obj = json.getAsJsonObject();
      ReplicationApplierState state = new ReplicationApplierState();

      if (obj.has("running")) {
        state.running = obj.getAsJsonPrimitive("running").getAsBoolean();
      }
      if (obj.has("lastAppliedContinuousTick") && !obj.get("lastAppliedContinuousTick").isJsonNull()) {
        state.lastAppliedContinuousTick = obj.getAsJsonPrimitive("lastAppliedContinuousTick").getAsLong();
      }
      if (obj.has("lastProcessedContinuousTick") && !obj.get("lastProcessedContinuousTick").isJsonNull()) {
        state.lastProcessedContinuousTick = obj.getAsJsonPrimitive("lastProcessedContinuousTick").getAsLong();
      }
      if (obj.has("lastAvailableContinuousTick") && !obj.get("lastAvailableContinuousTick").isJsonNull()) {
        state.lastAvailableContinuousTick = obj.getAsJsonPrimitive("lastAvailableContinuousTick").getAsLong();
      }
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

      if (obj.has("lastError") && !obj.get("lastError").isJsonNull()) {
        JsonObject lastError = obj.getAsJsonObject("lastError");
        state.lastError = new LastError();
        if (lastError.has("time")) {
          state.lastError.time = DateUtils.parse(lastError.getAsJsonPrimitive("time").getAsString());
        }
        if (lastError.has("errorNum")) {
          state.lastError.errorNum = lastError.getAsJsonPrimitive("errorNum").getAsInt();
        }
        if (lastError.has("errorMessage")) {
          state.lastError.errorMessage = lastError.getAsJsonPrimitive("errorMessage").getAsString();
        }
      }

      if (obj.has("progress")) {
        JsonObject progress = obj.getAsJsonObject("progress");
        state.progress = new Progress();
        if (progress.has("failedConnects")) {
          state.progress.failedConnects = progress.getAsJsonPrimitive("failedConnects").getAsLong();
        }
        if (progress.has("message")) {
          state.progress.message = progress.getAsJsonPrimitive("message").getAsString();
        }
        if (progress.has("time")) {
          state.progress.time = DateUtils.parse(progress.getAsJsonPrimitive("time").getAsString());
        }
      }

      return state;
    }
  }

  public static class ReplicationApplierStateEntityDeserializer implements
      JsonDeserializer<ReplicationApplierStateEntity> {
    @Override
    public ReplicationApplierStateEntity
        deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

      if (json.isJsonNull()) {
        return null;
      }

      JsonObject obj = json.getAsJsonObject();
      ReplicationApplierStateEntity entity = deserializeBaseParameter(obj, new ReplicationApplierStateEntity());

      if (obj.has("endpoint")) {
        entity.endpoint = obj.getAsJsonPrimitive("endpoint").getAsString();
      }

      if (obj.has("database")) {
        entity.database = obj.getAsJsonPrimitive("database").getAsString();
      }

      if (obj.has("server")) {
        JsonObject server = obj.getAsJsonObject("server");
        entity.serverVersion = server.getAsJsonPrimitive("version").getAsString();
        entity.serverId = server.getAsJsonPrimitive("serverId").getAsString();
      }

      if (obj.has("state")) {
        entity.state = context.deserialize(obj.get("state"), ReplicationApplierState.class);
      }

      return entity;
    }
  }

  public static class ReplicationLoggerStateEntityDeserializer implements
      JsonDeserializer<ReplicationLoggerStateEntity> {
    private Type clientsType = new TypeToken<List<Client>>() {
    }.getType();

    @Override
    public ReplicationLoggerStateEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

      if (json.isJsonNull()) {
        return null;
      }

      JsonObject obj = json.getAsJsonObject();
      ReplicationLoggerStateEntity entity = deserializeBaseParameter(obj, new ReplicationLoggerStateEntity());

      if (obj.has("state")) {
        entity.state = context.deserialize(obj.get("state"), ReplicationState.class);
      }

      if (obj.has("server")) {
        JsonObject server = obj.getAsJsonObject("server");
        entity.serverVersion = server.getAsJsonPrimitive("version").getAsString();
        entity.serverId = server.getAsJsonPrimitive("serverId").getAsString();
      }

      if (obj.has("clients")) {
        entity.clients = context.deserialize(obj.getAsJsonArray("clients"), clientsType);
      }

      return entity;
    }
  }

  public static class ReplicationLoggerStateEntityClientDeserializer implements
      JsonDeserializer<ReplicationLoggerStateEntity.Client> {
    @Override
    public ReplicationLoggerStateEntity.Client deserialize(
      JsonElement json,
      Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {

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
    public GraphEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

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
        if (!orphanCollections.equals(null)) {
          entity.orphanCollections = new ArrayList<String>(orphanCollections.size());
          for (int i = 0, imax = orphanCollections.size(); i < imax; i++) {
            String orphanCollection = orphanCollections.get(i).getAsString();

            entity.orphanCollections.add(orphanCollection);
          }
        }
      }

      if (graph.has("edgeDefinitions")) {
        JsonArray edgeDefinitions = graph.getAsJsonArray("edgeDefinitions");
        entity.edgeDefinitions = new ArrayList<EdgeDefinitionEntity>();
        if (!edgeDefinitions.equals(null)) {
          for (int i = 0, imax = edgeDefinitions.size(); i < imax; i++) {
            EdgeDefinitionEntity edgeDefinitionEntity = new EdgeDefinitionEntity();
            JsonObject edgeDefinition = edgeDefinitions.get(i).getAsJsonObject();
            if (edgeDefinition.has("collection")) {
              edgeDefinitionEntity.setCollection(edgeDefinition.get("collection").getAsString());
            }
            if (edgeDefinition.has("from")) {
              edgeDefinitionEntity.setFrom(new ArrayList<String>());
            }
            if (edgeDefinition.has("to")) {
              edgeDefinitionEntity.setTo(new ArrayList<String>());
            }
            entity.edgeDefinitions.add(edgeDefinitionEntity);
          }
        }
      }

      return entity;

    }
  }

  public static class GraphsEntityDeserializer implements JsonDeserializer<GraphsEntity> {
    private Type graphsType = new TypeToken<List<GraphEntity>>() {
    }.getType();

    @Override
    public GraphsEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

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
    public DeletedEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

      if (json.isJsonNull()) {
        return null;
      }

      JsonObject obj = json.getAsJsonObject();
      DeletedEntity entity = deserializeBaseParameter(obj, new DeletedEntity());

      if (obj.has("deleted")) {
        entity.deleted = obj.getAsJsonPrimitive("deleted").getAsBoolean();
      }
      if (obj.has("removed")) {
        entity.deleted = obj.getAsJsonPrimitive("removed").getAsBoolean();
      }

      return entity;

    }
  }

  public static class VertexEntityDeserializer implements JsonDeserializer<DocumentEntity<?>> {
    @Override
    public DocumentEntity<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

      if (json.isJsonNull()) {
        return null;
      }

      JsonObject obj = json.getAsJsonObject();

      DocumentEntity<?> entity;
      if (obj.has("vertex")) {
        entity = context.deserialize(obj.get("vertex"), DocumentEntity.class);
      } else {
        entity = new DocumentEntity<Object>();
      }

      entity = deserializeBaseParameter(obj, entity);

      return entity;
    }
  }

  public static class EdgeEntityDeserializer implements JsonDeserializer<EdgeEntity<?>> {
    @Override
    public EdgeEntity<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

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

}
