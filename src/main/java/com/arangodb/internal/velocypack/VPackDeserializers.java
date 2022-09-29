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

package com.arangodb.internal.velocypack;

import com.arangodb.entity.*;
import com.arangodb.entity.arangosearch.*;
import com.arangodb.entity.arangosearch.analyzer.*;
import com.arangodb.model.CollectionSchema;
import com.arangodb.model.ZKDIndexOptions;
import com.arangodb.velocypack.VPackDeserializer;
import com.arangodb.velocypack.VPackParser;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocystream.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author Mark Vollmary
 */
public class VPackDeserializers {

    private static final Logger LOGGER = LoggerFactory.getLogger(VPackDeserializers.class);
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    public static final VPackDeserializer<Response> RESPONSE = (parent, vpack, context) -> {
        final Response response = new Response();
        response.setVersion(vpack.get(0).getAsInt());
        response.setType(vpack.get(1).getAsInt());
        response.setResponseCode(vpack.get(2).getAsInt());
        if (vpack.size() > 3) {
            response.setMeta(context.deserialize(vpack.get(3), Map.class));
        }
        return response;
    };

    public static final VPackDeserializer<CollectionType> COLLECTION_TYPE = (parent, vpack, context) -> CollectionType.fromType(vpack.getAsInt());

    public static final VPackDeserializer<CollectionStatus> COLLECTION_STATUS = (parent, vpack, context) -> CollectionStatus.fromStatus(vpack.getAsInt());

    @SuppressWarnings("unchecked")
    public static final VPackDeserializer<BaseDocument> BASE_DOCUMENT = (parent, vpack, context) -> new BaseDocument((Map) context.deserialize(vpack, Map.class));

    public static final VPackDeserializer<SearchAnalyzer> SEARCH_ANALYZER = (parent, vpack, context) -> {
        AnalyzerType type = context.deserialize(vpack.get("type"), AnalyzerType.class);
        switch (type) {
            case identity:
                return context.deserialize(vpack, IdentityAnalyzer.class);
            case text:
                return context.deserialize(vpack, TextAnalyzer.class);
            case ngram:
                return context.deserialize(vpack, NGramAnalyzer.class);
            case delimiter:
                return context.deserialize(vpack, DelimiterAnalyzer.class);
            case stem:
                return context.deserialize(vpack, StemAnalyzer.class);
            case norm:
                return context.deserialize(vpack, NormAnalyzer.class);
            case pipeline:
                return context.deserialize(vpack, PipelineAnalyzer.class);
            case stopwords:
                return context.deserialize(vpack, StopwordsAnalyzer.class);
            case aql:
                return context.deserialize(vpack, AQLAnalyzer.class);
            case geojson:
                return context.deserialize(vpack, GeoJSONAnalyzer.class);
            case geopoint:
                return context.deserialize(vpack, GeoPointAnalyzer.class);
            case segmentation:
                return context.deserialize(vpack, SegmentationAnalyzer.class);
            case collation:
                return context.deserialize(vpack, CollationAnalyzer.class);
            case classification:
                return context.deserialize(vpack, ClassificationAnalyzer.class);
            case nearest_neighbors:
                return context.deserialize(vpack, NearestNeighborsAnalyzer.class);
            case minhash:
                return context.deserialize(vpack, MinHashAnalyzer.class);
            default:
                throw new IllegalArgumentException("Unknown analyzer type: " + type);
        }
    };

    @SuppressWarnings("unchecked")
    public static final VPackDeserializer<BaseEdgeDocument> BASE_EDGE_DOCUMENT = (parent, vpack, context) -> new BaseEdgeDocument((Map) context.deserialize(vpack, Map.class));

    public static final VPackDeserializer<Date> DATE_STRING = (parent, vpack, context) -> {
        try {
            return new SimpleDateFormat(DATE_TIME_FORMAT).parse(vpack.getAsString());
        } catch (final ParseException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("got ParseException for date string: " + vpack.getAsString());
            }
        }
        return null;
    };

    public static final VPackDeserializer<LogLevel> LOG_LEVEL = (parent, vpack, context) -> LogLevel.fromLevel(vpack.getAsInt());

    public static final VPackDeserializer<License> LICENSE = (parent, vpack, context) -> License.valueOf(vpack.getAsString().toUpperCase(Locale.ENGLISH));

    public static final VPackDeserializer<Permissions> PERMISSIONS = (parent, vpack, context) -> Permissions.valueOf(vpack.getAsString().toUpperCase(Locale.ENGLISH));

    public static final VPackDeserializer<QueryExecutionState> QUERY_EXECUTION_STATE = (parent, vpack, context) -> QueryExecutionState.valueOf(vpack.getAsString().toUpperCase(Locale.ENGLISH).replaceAll(" ", "_"));

    public static final VPackDeserializer<ReplicationFactor> REPLICATION_FACTOR = (parent, vpack, context) -> {
        final ReplicationFactor replicationFactor = new ReplicationFactor();
        if (vpack.isString() && vpack.getAsString().equals("satellite")) {
            replicationFactor.setSatellite(true);
        } else {
            replicationFactor.setReplicationFactor(vpack.getAsInt());
        }
        return replicationFactor;
    };

    public static final VPackDeserializer<MinReplicationFactor> MIN_REPLICATION_FACTOR = (parent, vpack, context) -> {
        final MinReplicationFactor minReplicationFactor = new MinReplicationFactor();
        minReplicationFactor.setMinReplicationFactor(vpack.getAsInt());
        return minReplicationFactor;
    };

    public static final VPackDeserializer<ViewType> VIEW_TYPE = (parent, vpack, context) -> {
        String value = vpack.getAsString();
        switch (value) {
            case "arangosearch":
                return ViewType.ARANGO_SEARCH;
            case "search-alias":
                return ViewType.SEARCH_ALIAS;
            default:
                throw new IllegalArgumentException("Unknown view type: " + value);
        }
    };

    public static final VPackDeserializer<StoredValue> STORED_VALUE = (parent, vpack, context) -> {
        VPackSlice fields = vpack.get("fields");
        VPackSlice compression = vpack.get("compression");
        final Iterator<VPackSlice> fieldsIterator = fields.arrayIterator();
        List<String> fieldsList = new ArrayList<>();
        while (fieldsIterator.hasNext()) {
            fieldsList.add(fieldsIterator.next().getAsString());
        }
        return new StoredValue(fieldsList, ArangoSearchCompression.valueOf(compression.getAsString()));
    };

    public static final VPackDeserializer<ArangoSearchProperties> ARANGO_SEARCH_PROPERTIES = (parent, vpack, context) -> {
        final ArangoSearchProperties properties = new ArangoSearchProperties();
        final VPackSlice consolidationIntervalMsec = vpack.get("consolidationIntervalMsec");
        if (consolidationIntervalMsec.isInteger()) {
            properties.setConsolidationIntervalMsec(consolidationIntervalMsec.getAsLong());
        }

        final VPackSlice commitIntervalMsec = vpack.get("commitIntervalMsec");
        if (commitIntervalMsec.isInteger()) {
            properties.setCommitIntervalMsec(commitIntervalMsec.getAsLong());
        }

        final VPackSlice cleanupIntervalStep = vpack.get("cleanupIntervalStep");
        if (cleanupIntervalStep.isInteger()) {
            properties.setCleanupIntervalStep(cleanupIntervalStep.getAsLong());
        }

        final VPackSlice consolidationPolicy = vpack.get("consolidationPolicy");
        if (consolidationPolicy.isObject()) {
            properties.setConsolidationPolicy(
                    context.deserialize(consolidationPolicy, ConsolidationPolicy.class));
        }

        final VPackSlice links = vpack.get("links");
        if (links.isObject()) {
            final Iterator<Entry<String, VPackSlice>> collectionIterator = links.objectIterator();
            for (; collectionIterator.hasNext(); ) {
                final Entry<String, VPackSlice> entry = collectionIterator.next();
                final VPackSlice value = entry.getValue();
                final CollectionLink link = CollectionLink.on(entry.getKey());
                final VPackSlice analyzers = value.get("analyzers");
                if (analyzers.isArray()) {
                    final Iterator<VPackSlice> analyzerIterator = analyzers.arrayIterator();
                    for (; analyzerIterator.hasNext(); ) {
                        link.analyzers(analyzerIterator.next().getAsString());
                    }
                }
                final VPackSlice includeAllFields = value.get("includeAllFields");
                if (includeAllFields.isBoolean()) {
                    link.includeAllFields(includeAllFields.getAsBoolean());
                }
                final VPackSlice trackListPositions = value.get("trackListPositions");
                if (trackListPositions.isBoolean()) {
                    link.trackListPositions(trackListPositions.getAsBoolean());
                }
                final VPackSlice storeValues = value.get("storeValues");
                if (storeValues.isString()) {
                    link.storeValues(StoreValuesType.valueOf(storeValues.getAsString().toUpperCase(Locale.ENGLISH)));
                }
                final VPackSlice fields = value.get("fields");
                if (fields.isObject()) {
                    final Iterator<Entry<String, VPackSlice>> fieldsIterator = fields.objectIterator();
                    for (; fieldsIterator.hasNext(); ) {
                        link.fields(deserializeField(fieldsIterator.next()));
                    }
                }
                final VPackSlice nested = value.get("nested");
                if (nested.isObject()) {
                    final Iterator<Entry<String, VPackSlice>> fieldsIterator = nested.objectIterator();
                    while (fieldsIterator.hasNext()) {
                        link.nested(deserializeField(fieldsIterator.next()));
                    }
                }
                properties.addLink(link);
            }
        }

        final VPackSlice primarySorts = vpack.get("primarySort");
        if (primarySorts.isArray()) {
            final Iterator<VPackSlice> primarySortsIterator = primarySorts.arrayIterator();
            for (; primarySortsIterator.hasNext(); ) {
                final VPackSlice entry = primarySortsIterator.next();
                if (entry.isObject()) {
                    if (entry.get("field").isString() && entry.get("asc").isBoolean()) {
                        final PrimarySort primarySort = PrimarySort.on(entry.get("field").getAsString());
                        primarySort.ascending(entry.get("asc").getAsBoolean());
                        properties.addPrimarySort(primarySort);
                    }
                }
            }
        }

        final VPackSlice primarySortCompression = vpack.get("primarySortCompression");
        if (primarySortCompression.isString()) {
            properties.setPrimarySortCompression(ArangoSearchCompression.valueOf(primarySortCompression.getAsString()));
        }

        final VPackSlice storedValues = vpack.get("storedValues");
        final Iterator<VPackSlice> storedValueIterator = storedValues.arrayIterator();
        while (storedValueIterator.hasNext()) {
            StoredValue sv = context.deserialize(storedValueIterator.next(), StoredValue.class);
            properties.addStoredValues(sv);
        }

        return properties;
    };

    protected static FieldLink deserializeField(final Entry<String, VPackSlice> field) {
        final VPackSlice value = field.getValue();
        final FieldLink link = FieldLink.on(field.getKey());
        final VPackSlice analyzers = value.get("analyzers");
        if (analyzers.isArray()) {
            final Iterator<VPackSlice> analyzerIterator = analyzers.arrayIterator();
            for (; analyzerIterator.hasNext(); ) {
                link.analyzers(analyzerIterator.next().getAsString());
            }
        }
        final VPackSlice includeAllFields = value.get("includeAllFields");
        if (includeAllFields.isBoolean()) {
            link.includeAllFields(includeAllFields.getAsBoolean());
        }
        final VPackSlice trackListPositions = value.get("trackListPositions");
        if (trackListPositions.isBoolean()) {
            link.trackListPositions(trackListPositions.getAsBoolean());
        }
        final VPackSlice storeValues = value.get("storeValues");
        if (storeValues.isString()) {
            link.storeValues(StoreValuesType.valueOf(storeValues.getAsString().toUpperCase(Locale.ENGLISH)));
        }
        final VPackSlice fields = value.get("fields");
        if (fields.isObject()) {
            final Iterator<Entry<String, VPackSlice>> fieldsIterator = fields.objectIterator();
            for (; fieldsIterator.hasNext(); ) {
                link.fields(deserializeField(fieldsIterator.next()));
            }
        }
        final VPackSlice nested = value.get("nested");
        if (nested.isObject()) {
            final Iterator<Entry<String, VPackSlice>> fieldsIterator = nested.objectIterator();
            while (fieldsIterator.hasNext()) {
                link.nested(deserializeField(fieldsIterator.next()));
            }
        }
        return link;
    }

    public static final VPackDeserializer<ArangoSearchPropertiesEntity> ARANGO_SEARCH_PROPERTIES_ENTITY = (parent, vpack, context) -> {
        final ViewEntity entity = context.deserialize(vpack, ViewEntity.class);
        final ArangoSearchProperties properties = context.deserialize(vpack, ArangoSearchProperties.class);
        return new ArangoSearchPropertiesEntity(entity.getId(),
                entity.getName(), entity.getType(), properties);
    };

    public static final VPackDeserializer<ConsolidationPolicy> CONSOLIDATE = (parent, vpack, context) -> {
        ConsolidationType type = ConsolidationType.valueOf(vpack.get("type").getAsString().toUpperCase(Locale.ENGLISH));
        final ConsolidationPolicy consolidate = ConsolidationPolicy.of(type);
        if (ConsolidationType.BYTES_ACCUM.equals(type)) {
            consolidate.threshold(vpack.get("threshold").getAsDouble());
        } else {
            consolidate.segmentsMin(vpack.get("segmentsMin").getAsLong());
            consolidate.segmentsMax(vpack.get("segmentsMax").getAsLong());
            consolidate.segmentsBytesMax(vpack.get("segmentsBytesMax").getAsLong());
            consolidate.segmentsBytesFloor(vpack.get("segmentsBytesFloor").getAsLong());
            consolidate.minScore(vpack.get("minScore").getAsLong());
        }
        return consolidate;
    };

    public static final VPackDeserializer<CollectionSchema> COLLECTION_VALIDATION = (parent, vpack, context) -> {
        VPackParser parser = new VPackParser.Builder().build();
        CollectionSchema collectionValidation = new CollectionSchema();
        collectionValidation.setLevel(CollectionSchema.Level.of(vpack.get("level").getAsString()));
        collectionValidation.setRule(parser.toJson(vpack.get("rule"), true));
        collectionValidation.setMessage(vpack.get("message").getAsString());
        return collectionValidation;
    };

    public static final VPackDeserializer<ZKDIndexOptions.FieldValueTypes> ZKD_FIELD_VALUE_TYPES =
            (parent, vpack, context) -> ZKDIndexOptions.FieldValueTypes.valueOf(vpack.getAsString().toUpperCase(Locale.ENGLISH));


    public static final VPackDeserializer<InvertedIndexPrimarySort.Field> INVERTED_INDEX_PRIMARY_SORT_FIELD = (parent, vpack, context) -> {
        InvertedIndexPrimarySort.Field.Direction dir = vpack.get("asc").getAsBoolean() ?
                InvertedIndexPrimarySort.Field.Direction.asc : InvertedIndexPrimarySort.Field.Direction.desc;
        return new InvertedIndexPrimarySort.Field(vpack.get("field").getAsString(), dir);
    };

    public static final VPackDeserializer<SearchAliasPropertiesEntity> SEARCH_ALIAS_PROPERTIES_ENTITY = (parent, vpack, context) -> {
        String id = vpack.get("id").getAsString();
        String name = vpack.get("name").getAsString();
        ViewType type = context.deserialize(vpack.get("type"), ViewType.class);
        SearchAliasProperties properties = context.deserialize(vpack, SearchAliasProperties.class);
        return new SearchAliasPropertiesEntity(id, name, type, properties);
    };

    public static final VPackDeserializer<SearchAliasIndex> SEARCH_ALIAS_INDEX = (parent, vpack, context) -> {
        String collection = vpack.get("collection").getAsString();
        String index = vpack.get("index").getAsString();
        return new SearchAliasIndex(collection, index);
    };
}
