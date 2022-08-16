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
import com.arangodb.internal.DocumentFields;
import com.arangodb.internal.velocystream.internal.AuthenticationRequest;
import com.arangodb.internal.velocystream.internal.JwtAuthenticationRequest;
import com.arangodb.model.CollectionSchema;
import com.arangodb.model.TraversalOptions;
import com.arangodb.model.TraversalOptions.Order;
import com.arangodb.model.ZKDIndexOptions;
import com.arangodb.model.arangosearch.ArangoSearchPropertiesOptions;
import com.arangodb.model.arangosearch.SearchAliasCreateOptions;
import com.arangodb.velocypack.*;
import com.arangodb.velocystream.Request;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Mark Vollmary
 */
public class VPackSerializers {

    public static final VPackSerializer<Request> REQUEST = (builder, attribute, value, context) -> {
        builder.add(attribute, ValueType.ARRAY);
        builder.add(value.getVersion());
        builder.add(value.getType());
        builder.add(value.getDbName().get());
        builder.add(value.getRequestType().getType());
        builder.add(value.getRequest());
        builder.add(ValueType.OBJECT);
        for (final Entry<String, String> entry : value.getQueryParam().entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        builder.close();
        builder.add(ValueType.OBJECT);
        for (final Entry<String, String> entry : value.getHeaderParam().entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        builder.close();
        builder.close();
    };

    public static final VPackSerializer<AuthenticationRequest> AUTH_REQUEST = (builder, attribute, value, context) -> {
        builder.add(attribute, ValueType.ARRAY);
        builder.add(value.getVersion());
        builder.add(value.getType());
        builder.add(value.getEncryption());
        builder.add(value.getUser());
        builder.add(value.getPassword());
        builder.close();
    };

    public static final VPackSerializer<JwtAuthenticationRequest> JWT_AUTH_REQUEST = (builder, attribute, value, context) -> {
        builder.add(attribute, ValueType.ARRAY);
        builder.add(value.getVersion());
        builder.add(value.getType());
        builder.add(value.getEncryption());
        builder.add(value.getToken());
        builder.close();
    };

    public static final VPackSerializer<CollectionType> COLLECTION_TYPE = (builder, attribute, value, context) -> builder.add(attribute, value.getType());

    public static final VPackSerializer<BaseDocument> BASE_DOCUMENT = (builder, attribute, value, context) -> {
        final Map<String, Object> doc = new HashMap<>(value.getProperties());
        doc.put(DocumentFields.ID, value.getId());
        doc.put(DocumentFields.KEY, value.getKey());
        doc.put(DocumentFields.REV, value.getRevision());
        context.serialize(builder, attribute, doc);
    };

    public static final VPackSerializer<BaseEdgeDocument> BASE_EDGE_DOCUMENT = (builder, attribute, value, context) -> {
        final Map<String, Object> doc = new HashMap<>(value.getProperties());
        doc.put(DocumentFields.ID, value.getId());
        doc.put(DocumentFields.KEY, value.getKey());
        doc.put(DocumentFields.REV, value.getRevision());
        doc.put(DocumentFields.FROM, value.getFrom());
        doc.put(DocumentFields.TO, value.getTo());
        context.serialize(builder, attribute, doc);
    };

    public static final VPackSerializer<TraversalOptions.Order> TRAVERSAL_ORDER = (builder, attribute, value, context) -> {
        if (Order.preorder_expander == value) {
            builder.add(attribute, "preorder-expander");
        } else {
            builder.add(attribute, value.name());
        }
    };

    public static final VPackSerializer<LogLevel> LOG_LEVEL = (builder, attribute, value, context) -> builder.add(attribute, value.getLevel());

    public static final VPackSerializer<Permissions> PERMISSIONS = (builder, attribute, value, context) -> builder.add(attribute, value.toString().toLowerCase(Locale.ENGLISH));

    public static final VPackSerializer<ReplicationFactor> REPLICATION_FACTOR = (builder, attribute, value, context) -> {
        final Boolean satellite = value.getSatellite();
        if (Boolean.TRUE == satellite) {
            builder.add(attribute, "satellite");
        } else if (value.getReplicationFactor() != null) {
            builder.add(attribute, value.getReplicationFactor());
        }
    };

    public static final VPackSerializer<MinReplicationFactor> MIN_REPLICATION_FACTOR = (builder, attribute, value, context) -> {
        if (value.getMinReplicationFactor() != null) {
            builder.add(attribute, value.getMinReplicationFactor());
        }
    };

    public static final VPackSerializer<ViewType> VIEW_TYPE = (builder, attribute, value, context) -> {
        if (value == ViewType.ARANGO_SEARCH) {
            builder.add(attribute, "arangosearch");
        } else if (value == ViewType.SEARCH_ALIAS) {
            builder.add(attribute, "search-alias");
        } else {
            throw new IllegalArgumentException();
        }
    };

    public static final VPackSerializer<ArangoSearchPropertiesOptions> ARANGO_SEARCH_PROPERTIES_OPTIONS = (builder, attribute, value, context) -> {
        builder.add(ValueType.OBJECT);
        context.serialize(builder, attribute, value.getProperties());
        builder.close();
    };

    public static final VPackSerializer<ArangoSearchProperties> ARANGO_SEARCH_PROPERTIES = (builder, attribute, value, context) -> {
        final Long consolidationIntervalMsec = value.getConsolidationIntervalMsec();
        if (consolidationIntervalMsec != null) {
            builder.add("consolidationIntervalMsec", consolidationIntervalMsec);
        }

        final Long commitIntervalMsec = value.getCommitIntervalMsec();
        if (commitIntervalMsec != null) {
            builder.add("commitIntervalMsec", commitIntervalMsec);
        }

        final Long cleanupIntervalStep = value.getCleanupIntervalStep();
        if (cleanupIntervalStep != null) {
            builder.add("cleanupIntervalStep", cleanupIntervalStep);
        }
        context.serialize(builder, "consolidationPolicy", value.getConsolidationPolicy());

        final Collection<CollectionLink> links = value.getLinks();
        if (!links.isEmpty()) {
            builder.add("links", ValueType.OBJECT);
            for (final CollectionLink collectionLink : links) {
                builder.add(collectionLink.getName(), ValueType.OBJECT);
                final Collection<String> analyzers = collectionLink.getAnalyzers();
                if (!analyzers.isEmpty()) {
                    builder.add("analyzers", ValueType.ARRAY);
                    for (final String analyzer : analyzers) {
                        builder.add(analyzer);
                    }
                    builder.close();
                }
                final Boolean includeAllFields = collectionLink.getIncludeAllFields();
                if (includeAllFields != null) {
                    builder.add("includeAllFields", includeAllFields);
                }
                final Boolean trackListPositions = collectionLink.getTrackListPositions();
                if (trackListPositions != null) {
                    builder.add("trackListPositions", trackListPositions);
                }
                final StoreValuesType storeValues = collectionLink.getStoreValues();
                if (storeValues != null) {
                    builder.add("storeValues", storeValues.name().toLowerCase(Locale.ENGLISH));
                }
                Boolean inBackground = collectionLink.getInBackground();
                if (inBackground != null) {
                    builder.add("inBackground", inBackground);
                }
                serializeFieldLinks(builder, collectionLink.getFields());
                serializeNested(builder, collectionLink.getNested());
                builder.close();
            }
            builder.close();
        }

        final Collection<PrimarySort> primarySorts = value.getPrimarySort();
        if (!primarySorts.isEmpty()) {
            builder.add("primarySort", ValueType.ARRAY); // open array
            for (final PrimarySort primarySort : primarySorts) {
                builder.add(ValueType.OBJECT); // open object
                builder.add("field", primarySort.getFieldName());
                builder.add("asc", primarySort.getAscending());
                builder.close(); // close object
            }
            builder.close(); // close array
        }

        final ArangoSearchCompression primarySortCompression = value.getPrimarySortCompression();
        if (primarySortCompression != null) {
            builder.add("primarySortCompression", primarySortCompression.getValue());
        }

        final Collection<StoredValue> storedValues = value.getStoredValues();
        if (!storedValues.isEmpty()) {
            builder.add("storedValues", ValueType.ARRAY); // open array
            for (final StoredValue storedValue : storedValues) {
                context.serialize(builder, null, storedValue);
            }
            builder.close(); // close array
        }

    };

    public static final VPackSerializer<SearchAliasProperties> SEARCH_ALIAS_PROPERTIES = (builder, attribute, value, context) -> {
        Collection<SearchAliasIndex> indexes = value.getIndexes();
        builder.add("indexes", ValueType.ARRAY);
        for (SearchAliasIndex index : indexes) {
            context.serialize(builder, null, index);
        }
        builder.close();
    };

    public static final VPackSerializer<SearchAliasIndex> SEARCH_ALIAS_INDEX = (builder, attribute, value, context) -> {
        builder.add(ValueType.OBJECT);
        builder.add("collection", value.getCollection());
        builder.add("index", value.getIndex());
        context.serialize(builder, "operation", value.getOperation());
        builder.close();
    };

    public static final VPackSerializer<StoredValue> STORED_VALUE = (builder, attribute, value, context) -> {
        builder.add(ValueType.OBJECT); // open object
        builder.add("fields", ValueType.ARRAY);
        for (final String field : value.getFields()) {
            builder.add(field);
        }
        builder.close();
        if (value.getCompression() != null) {
            builder.add("compression", value.getCompression().getValue());
        }
        builder.close(); // close object
    };

    public static final VPackSerializer<InvertedIndexPrimarySort.Field> PRIMARY_SORT_FIELD = (builder, attribute, value, context) -> {
        builder.add(ValueType.OBJECT);
        builder.add("field", value.getField());
        builder.add("direction", value.getDirection().toString());
        builder.close();
    };

    private static void serializeFieldLinks(final VPackBuilder builder, final Collection<FieldLink> links) {
        if (!links.isEmpty()) {
            builder.add("fields", ValueType.OBJECT);
            serializeFields(builder, links);
            builder.close();
        }
    }

    private static void serializeNested(final VPackBuilder builder, final Collection<FieldLink> nested) {
        if (!nested.isEmpty()) {
            builder.add("nested", ValueType.OBJECT);
            serializeFields(builder, nested);
            builder.close();
        }
    }

    private static void serializeFields(final VPackBuilder builder, final Collection<FieldLink> links){
        for (final FieldLink fieldLink : links) {
            builder.add(fieldLink.getName(), ValueType.OBJECT);
            final Collection<String> analyzers = fieldLink.getAnalyzers();
            if (!analyzers.isEmpty()) {
                builder.add("analyzers", ValueType.ARRAY);
                for (final String analyzer : analyzers) {
                    builder.add(analyzer);
                }
                builder.close();
            }
            final Boolean includeAllFields = fieldLink.getIncludeAllFields();
            if (includeAllFields != null) {
                builder.add("includeAllFields", includeAllFields);
            }
            final Boolean trackListPositions = fieldLink.getTrackListPositions();
            if (trackListPositions != null) {
                builder.add("trackListPositions", trackListPositions);
            }
            final StoreValuesType storeValues = fieldLink.getStoreValues();
            if (storeValues != null) {
                builder.add("storeValues", storeValues.name().toLowerCase(Locale.ENGLISH));
            }
            Boolean inBackground = fieldLink.getInBackground();
            if (inBackground != null) {
                builder.add("inBackground", inBackground);
            }
            serializeFieldLinks(builder, fieldLink.getFields());
            serializeNested(builder, fieldLink.getNested());
            builder.close();
        }
    }

    public static final VPackSerializer<ConsolidationType> CONSOLIDATE_TYPE = (builder, attribute, value, context) -> builder.add(attribute, value.toString().toLowerCase(Locale.ENGLISH));

    public static final VPackSerializer<CollectionSchema> COLLECTION_VALIDATION = (builder, attribute, value, context) -> {
        VPackParser parser = new VPackParser.Builder().build();
        VPackSlice rule = value.getRule() != null ? parser.fromJson(value.getRule(), true) : null;
        final Map<String, Object> doc = new HashMap<>();
        doc.put("message", value.getMessage());
        doc.put("level", value.getLevel() != null ? value.getLevel().getValue() : null);
        doc.put("rule", rule);
        context.serialize(builder, attribute, doc);
    };

    public static final VPackSerializer<ZKDIndexOptions.FieldValueTypes> ZKD_FIELD_VALUE_TYPES =
            (builder, attribute, value, context) -> builder.add(attribute, value.name().toLowerCase(Locale.ENGLISH));

}
