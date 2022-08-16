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

import com.arangodb.SearchAlias;
import com.arangodb.entity.*;
import com.arangodb.entity.arangosearch.*;
import com.arangodb.entity.arangosearch.analyzer.SearchAnalyzer;
import com.arangodb.internal.DocumentFields;
import com.arangodb.internal.velocystream.internal.AuthenticationRequest;
import com.arangodb.internal.velocystream.internal.JwtAuthenticationRequest;
import com.arangodb.model.CollectionSchema;
import com.arangodb.model.TraversalOptions;
import com.arangodb.model.ZKDIndexOptions;
import com.arangodb.model.arangosearch.ArangoSearchPropertiesOptions;
import com.arangodb.velocypack.VPackModule;
import com.arangodb.velocypack.VPackParserModule;
import com.arangodb.velocypack.VPackParserSetupContext;
import com.arangodb.velocypack.VPackSetupContext;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.Response;

import java.lang.annotation.Annotation;
import java.util.Date;

/**
 * @author Mark Vollmary
 */
public class VPackDriverModule implements VPackModule, VPackParserModule {

    @Override
    public <C extends VPackSetupContext<C>> void setup(final C context) {
        context.fieldNamingStrategy(field -> {
            for (Annotation annotation : field.getAnnotations()) {
                if(annotation instanceof DocumentField) {
                    return ((DocumentField) annotation).value().getSerializeName();
                } else if (annotation instanceof Id) {
                    return DocumentFields.ID;
                } else if (annotation instanceof Key) {
                    return DocumentFields.KEY;
                } else if (annotation instanceof Rev) {
                    return DocumentFields.REV;
                } else if (annotation instanceof From) {
                    return DocumentFields.FROM;
                } else if (annotation instanceof To) {
                    return DocumentFields.TO;
                }
            }
            return field.getName();
        });
        context.registerSerializer(Request.class, VPackSerializers.REQUEST);
        context.registerSerializer(AuthenticationRequest.class, VPackSerializers.AUTH_REQUEST);
        context.registerSerializer(JwtAuthenticationRequest.class, VPackSerializers.JWT_AUTH_REQUEST);
        context.registerSerializer(CollectionType.class, VPackSerializers.COLLECTION_TYPE);
        context.registerSerializer(BaseDocument.class, VPackSerializers.BASE_DOCUMENT);
        context.registerSerializer(BaseEdgeDocument.class, VPackSerializers.BASE_EDGE_DOCUMENT);
        context.registerSerializer(TraversalOptions.Order.class, VPackSerializers.TRAVERSAL_ORDER);
        context.registerSerializer(LogLevel.class, VPackSerializers.LOG_LEVEL);
        context.registerSerializer(Permissions.class, VPackSerializers.PERMISSIONS);
        context.registerSerializer(ReplicationFactor.class, VPackSerializers.REPLICATION_FACTOR);
        context.registerSerializer(MinReplicationFactor.class, VPackSerializers.MIN_REPLICATION_FACTOR);
        context.registerSerializer(ViewType.class, VPackSerializers.VIEW_TYPE);
        context.registerSerializer(ArangoSearchPropertiesOptions.class, VPackSerializers.ARANGO_SEARCH_PROPERTIES_OPTIONS);
        context.registerSerializer(ArangoSearchProperties.class, VPackSerializers.ARANGO_SEARCH_PROPERTIES);
        context.registerSerializer(SearchAliasProperties.class, VPackSerializers.SEARCH_ALIAS_PROPERTIES);
        context.registerSerializer(SearchAliasIndex.class, VPackSerializers.SEARCH_ALIAS_INDEX);
        context.registerSerializer(StoredValue.class, VPackSerializers.STORED_VALUE);
        context.registerSerializer(InvertedIndexPrimarySort.Field.class, VPackSerializers.PRIMARY_SORT_FIELD);
        context.registerSerializer(ConsolidationType.class, VPackSerializers.CONSOLIDATE_TYPE);
        context.registerSerializer(CollectionSchema.class, VPackSerializers.COLLECTION_VALIDATION);
        context.registerSerializer(ZKDIndexOptions.FieldValueTypes.class, VPackSerializers.ZKD_FIELD_VALUE_TYPES);

        context.registerDeserializer(Response.class, VPackDeserializers.RESPONSE);
        context.registerDeserializer(CollectionType.class, VPackDeserializers.COLLECTION_TYPE);
        context.registerDeserializer(CollectionStatus.class, VPackDeserializers.COLLECTION_STATUS);
        context.registerDeserializer(BaseDocument.class, VPackDeserializers.BASE_DOCUMENT);
        context.registerDeserializer(SearchAnalyzer.class, VPackDeserializers.SEARCH_ANALYZER);
        context.registerDeserializer(BaseEdgeDocument.class, VPackDeserializers.BASE_EDGE_DOCUMENT);
        context.registerDeserializer(QueryEntity.PROPERTY_STARTED, Date.class, VPackDeserializers.DATE_STRING);
        context.registerDeserializer(LogLevel.class, VPackDeserializers.LOG_LEVEL);
        context.registerDeserializer(License.class, VPackDeserializers.LICENSE);
        context.registerDeserializer(Permissions.class, VPackDeserializers.PERMISSIONS);
        context.registerDeserializer(QueryExecutionState.class, VPackDeserializers.QUERY_EXECUTION_STATE);
        context.registerDeserializer(ReplicationFactor.class, VPackDeserializers.REPLICATION_FACTOR);
        context.registerDeserializer(MinReplicationFactor.class, VPackDeserializers.MIN_REPLICATION_FACTOR);
        context.registerDeserializer(ViewType.class, VPackDeserializers.VIEW_TYPE);
        context.registerDeserializer(StoredValue.class, VPackDeserializers.STORED_VALUE);
        context.registerDeserializer(ArangoSearchProperties.class, VPackDeserializers.ARANGO_SEARCH_PROPERTIES);
        context.registerDeserializer(ArangoSearchPropertiesEntity.class, VPackDeserializers.ARANGO_SEARCH_PROPERTIES_ENTITY);
        context.registerDeserializer(ConsolidationPolicy.class, VPackDeserializers.CONSOLIDATE);
        context.registerDeserializer(CollectionSchema.class, VPackDeserializers.COLLECTION_VALIDATION);
        context.registerDeserializer(ZKDIndexOptions.FieldValueTypes.class, VPackDeserializers.ZKD_FIELD_VALUE_TYPES);
        context.registerDeserializer(InvertedIndexPrimarySort.Field.class, VPackDeserializers.INVERTED_INDEX_PRIMARY_SORT_FIELD);
        context.registerDeserializer(SearchAliasPropertiesEntity.class, VPackDeserializers.SEARCH_ALIAS_PROPERTIES_ENTITY);
        context.registerDeserializer(SearchAliasIndex.class, VPackDeserializers.SEARCH_ALIAS_INDEX);
    }

    @Override
    public <C extends VPackParserSetupContext<C>> void setup(final C context) {

    }

}
