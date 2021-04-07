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

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.entity.CollectionStatus;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.License;
import com.arangodb.entity.LogLevel;
import com.arangodb.entity.MinReplicationFactor;
import com.arangodb.entity.Permissions;
import com.arangodb.entity.QueryExecutionState;
import com.arangodb.entity.ReplicationFactor;
import com.arangodb.entity.ViewEntity;
import com.arangodb.entity.ViewType;
import com.arangodb.entity.arangosearch.AnalyzerType;
import com.arangodb.entity.arangosearch.ArangoSearchCompression;
import com.arangodb.entity.arangosearch.ArangoSearchProperties;
import com.arangodb.entity.arangosearch.ArangoSearchPropertiesEntity;
import com.arangodb.entity.arangosearch.CollectionLink;
import com.arangodb.entity.arangosearch.ConsolidationPolicy;
import com.arangodb.entity.arangosearch.ConsolidationType;
import com.arangodb.entity.arangosearch.FieldLink;
import com.arangodb.entity.arangosearch.PrimarySort;
import com.arangodb.entity.arangosearch.StoreValuesType;
import com.arangodb.entity.arangosearch.StoredValue;
import com.arangodb.entity.arangosearch.analyzer.AQLAnalyzer;
import com.arangodb.entity.arangosearch.analyzer.DelimiterAnalyzer;
import com.arangodb.entity.arangosearch.analyzer.GeoJSONAnalyzer;
import com.arangodb.entity.arangosearch.analyzer.GeoPointAnalyzer;
import com.arangodb.entity.arangosearch.analyzer.IdentityAnalyzer;
import com.arangodb.entity.arangosearch.analyzer.NGramAnalyzer;
import com.arangodb.entity.arangosearch.analyzer.NormAnalyzer;
import com.arangodb.entity.arangosearch.analyzer.PipelineAnalyzer;
import com.arangodb.entity.arangosearch.analyzer.SearchAnalyzer;
import com.arangodb.entity.arangosearch.analyzer.StemAnalyzer;
import com.arangodb.entity.arangosearch.analyzer.StopwordsAnalyzer;
import com.arangodb.entity.arangosearch.analyzer.TextAnalyzer;
import com.arangodb.model.CollectionSchema;
import com.arangodb.velocypack.VPackDeserializer;
import com.arangodb.velocypack.VPackParser;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocystream.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

    public static final VPackDeserializer<License> LICENSE = (parent, vpack, context) -> License.valueOf(vpack.getAsString().toUpperCase());

    public static final VPackDeserializer<Permissions> PERMISSIONS = (parent, vpack, context) -> Permissions.valueOf(vpack.getAsString().toUpperCase());

    public static final VPackDeserializer<QueryExecutionState> QUERY_EXECUTION_STATE = (parent, vpack, context) -> QueryExecutionState.valueOf(vpack.getAsString().toUpperCase().replaceAll(" ", "_"));

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

    public static final VPackDeserializer<ViewType> VIEW_TYPE = (parent, vpack, context) -> "arangosearch".equals(vpack.getAsString()) ? ViewType.ARANGO_SEARCH
            : ViewType.valueOf(vpack.getAsString().toUpperCase());

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
                    link.storeValues(StoreValuesType.valueOf(storeValues.getAsString().toUpperCase()));
                }
                final VPackSlice fields = value.get("fields");
                if (fields.isObject()) {
                    final Iterator<Entry<String, VPackSlice>> fieldsIterator = fields.objectIterator();
                    for (; fieldsIterator.hasNext(); ) {
                        link.fields(deserializeField(fieldsIterator.next()));
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
        if (storedValues.isArray()) {
            final Iterator<VPackSlice> storedValueIterator = storedValues.arrayIterator();
            for (; storedValueIterator.hasNext(); ) {
                final VPackSlice entry = storedValueIterator.next();
                if (entry.isObject()) {
                    VPackSlice fields = entry.get("fields");
                    VPackSlice compression = entry.get("compression");
                    if (fields.isArray() && compression.isString()) {
                        final Iterator<VPackSlice> fieldsIterator = fields.arrayIterator();
                        List<String> fieldsList = new ArrayList<>();
                        fieldsIterator.forEachRemaining(it -> fieldsList.add(it.getAsString()));
                        properties.addStoredValues(new StoredValue(fieldsList, ArangoSearchCompression.valueOf(compression.getAsString())));
                    }
                }
            }
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
            link.storeValues(StoreValuesType.valueOf(storeValues.getAsString().toUpperCase()));
        }
        final VPackSlice fields = value.get("fields");
        if (fields.isObject()) {
            final Iterator<Entry<String, VPackSlice>> fieldsIterator = fields.objectIterator();
            for (; fieldsIterator.hasNext(); ) {
                link.fields(deserializeField(fieldsIterator.next()));
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
        final VPackSlice type = vpack.get("type");
        if (type.isString()) {
            final ConsolidationPolicy consolidate = ConsolidationPolicy
                    .of(ConsolidationType.valueOf(type.getAsString().toUpperCase()));
            final VPackSlice threshold = vpack.get("threshold");
            if (threshold.isNumber()) {
                consolidate.threshold(threshold.getAsDouble());
            }
            final VPackSlice segmentThreshold = vpack.get("segmentThreshold");
            if (segmentThreshold.isInteger()) {
                consolidate.segmentThreshold(segmentThreshold.getAsLong());
            }
            return consolidate;
        }
        return null;
    };

    public static final VPackDeserializer<CollectionSchema> COLLECTION_VALIDATION = (parent, vpack, context) -> {
        VPackParser parser = new VPackParser.Builder().build();
        CollectionSchema collectionValidation = new CollectionSchema();
        collectionValidation.setLevel(CollectionSchema.Level.of(vpack.get("level").getAsString()));
        collectionValidation.setRule(parser.toJson(vpack.get("rule"), true));
        collectionValidation.setMessage(vpack.get("message").getAsString());
        return collectionValidation;
    };

}
