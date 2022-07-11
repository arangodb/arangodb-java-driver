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
import com.arangodb.model.ZKDIndexOptions;
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

    public static final VPackSerializer<LogLevel> LOG_LEVEL = (builder, attribute, value, context) -> builder.add(attribute, value.getLevel());

    public static final VPackSerializer<Permissions> PERMISSIONS = (builder, attribute, value, context) -> builder.add(attribute, value.toString().toLowerCase(Locale.ENGLISH));

    public static final VPackSerializer<ViewType> VIEW_TYPE = (builder, attribute, value, context) -> {
        final String type = value == ViewType.ARANGO_SEARCH ? "arangosearch" : value.name().toLowerCase(Locale.ENGLISH);
        builder.add(attribute, type);
    };

    private static void serializeFieldLinks(final VPackBuilder builder, final Collection<FieldLink> links) {
        if (!links.isEmpty()) {
            builder.add("fields", ValueType.OBJECT);
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
                serializeFieldLinks(builder, fieldLink.getFields());
                builder.close();
            }
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
