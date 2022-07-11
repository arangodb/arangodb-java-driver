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
        if (vpack.isString() && vpack.getAsString().equals("satellite")) {
            return ReplicationFactor.ofSatellite();
        } else {
            return ReplicationFactor.of(vpack.getAsInt());
        }
    };

    public static final VPackDeserializer<ViewType> VIEW_TYPE = (parent, vpack, context) -> "arangosearch".equals(vpack.getAsString()) ? ViewType.ARANGO_SEARCH
            : ViewType.valueOf(vpack.getAsString().toUpperCase(Locale.ENGLISH));

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
        return link;
    }

    public static final VPackDeserializer<ConsolidationPolicy> CONSOLIDATE = (parent, vpack, context) -> {
        final VPackSlice type = vpack.get("type");
        if (type.isString()) {
            final ConsolidationPolicy consolidate = ConsolidationPolicy
                    .of(ConsolidationType.valueOf(type.getAsString().toUpperCase(Locale.ENGLISH)));
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

    public static final VPackDeserializer<ZKDIndexOptions.FieldValueTypes> ZKD_FIELD_VALUE_TYPES =
            (parent, vpack, context) -> ZKDIndexOptions.FieldValueTypes.valueOf(vpack.getAsString().toUpperCase(Locale.ENGLISH));


}
