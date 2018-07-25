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

import java.lang.reflect.Field;
import java.util.Date;

import com.arangodb.entity.ArangoDBVersion;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.entity.CollectionStatus;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.DocumentField;
import com.arangodb.entity.LogLevel;
import com.arangodb.entity.Permissions;
import com.arangodb.entity.QueryEntity;
import com.arangodb.entity.QueryExecutionState;
import com.arangodb.entity.ReplicationFactor;
import com.arangodb.entity.ViewType;
import com.arangodb.entity.arangosearch.ArangoSearchProperties;
import com.arangodb.entity.arangosearch.ArangoSearchPropertiesEntity;
import com.arangodb.internal.velocystream.internal.AuthenticationRequest;
import com.arangodb.model.TraversalOptions;
import com.arangodb.velocypack.VPackFieldNamingStrategy;
import com.arangodb.velocypack.VPackModule;
import com.arangodb.velocypack.VPackParserModule;
import com.arangodb.velocypack.VPackParserSetupContext;
import com.arangodb.velocypack.VPackSetupContext;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.Response;

/**
 * @author Mark Vollmary
 *
 */
public class VPackDriverModule implements VPackModule, VPackParserModule {

	@Override
	public <C extends VPackSetupContext<C>> void setup(final C context) {
		context.fieldNamingStrategy(new VPackFieldNamingStrategy() {
			@Override
			public String translateName(final Field field) {
				final DocumentField annotation = field.getAnnotation(DocumentField.class);
				if (annotation != null) {
					return annotation.value().getSerializeName();
				}
				return field.getName();
			}
		});
		context.registerSerializer(Request.class, VPackSerializers.REQUEST);
		context.registerSerializer(AuthenticationRequest.class, VPackSerializers.AUTH_REQUEST);
		context.registerSerializer(CollectionType.class, VPackSerializers.COLLECTION_TYPE);
		context.registerSerializer(BaseDocument.class, VPackSerializers.BASE_DOCUMENT);
		context.registerSerializer(BaseEdgeDocument.class, VPackSerializers.BASE_EDGE_DOCUMENT);
		context.registerSerializer(TraversalOptions.Order.class, VPackSerializers.TRAVERSAL_ORDER);
		context.registerSerializer(LogLevel.class, VPackSerializers.LOG_LEVEL);
		context.registerSerializer(Permissions.class, VPackSerializers.PERMISSIONS);
		context.registerSerializer(ReplicationFactor.class, VPackSerializers.REPLICATION_FACTOR);
		context.registerSerializer(ViewType.class, VPackSerializers.VIEW_TYPE);
		context.registerSerializer(ArangoSearchProperties.class, VPackSerializers.ARANGO_SEARCH_PROPERTIES);

		context.registerDeserializer(Response.class, VPackDeserializers.RESPONSE);
		context.registerDeserializer(CollectionType.class, VPackDeserializers.COLLECTION_TYPE);
		context.registerDeserializer(CollectionStatus.class, VPackDeserializers.COLLECTION_STATUS);
		context.registerDeserializer(BaseDocument.class, VPackDeserializers.BASE_DOCUMENT);
		context.registerDeserializer(BaseEdgeDocument.class, VPackDeserializers.BASE_EDGE_DOCUMENT);
		context.registerDeserializer(QueryEntity.PROPERTY_STARTED, Date.class, VPackDeserializers.DATE_STRING);
		context.registerDeserializer(LogLevel.class, VPackDeserializers.LOG_LEVEL);
		context.registerDeserializer(ArangoDBVersion.License.class, VPackDeserializers.LICENSE);
		context.registerDeserializer(Permissions.class, VPackDeserializers.PERMISSIONS);
		context.registerDeserializer(QueryExecutionState.class, VPackDeserializers.QUERY_EXECUTION_STATE);
		context.registerDeserializer(ReplicationFactor.class, VPackDeserializers.REPLICATION_FACTOR);
		context.registerDeserializer(ViewType.class, VPackDeserializers.VIEW_TYPE);
		context.registerDeserializer(ArangoSearchProperties.class, VPackDeserializers.ARANGO_SEARCH_PROPERTIES);
		context.registerDeserializer(ArangoSearchPropertiesEntity.class,
			VPackDeserializers.ARANGO_SEARCH_PROPERTIES_ENTITY);
	}

	@Override
	public <C extends VPackParserSetupContext<C>> void setup(final C context) {

	}

}
