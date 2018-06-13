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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.entity.ArangoDBVersion;
import com.arangodb.entity.ArangoDBVersion.License;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.entity.CollectionStatus;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.LogLevel;
import com.arangodb.entity.Permissions;
import com.arangodb.entity.QueryExecutionState;
import com.arangodb.entity.ReplicationFactor;
import com.arangodb.velocypack.VPackDeserializationContext;
import com.arangodb.velocypack.VPackDeserializer;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocystream.Response;

/**
 * @author Mark Vollmary
 *
 */
public class VPackDeserializers {

	private static final Logger LOGGER = LoggerFactory.getLogger(VPackDeserializers.class);
	private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

	public static final VPackDeserializer<Response> RESPONSE = new VPackDeserializer<Response>() {
		@SuppressWarnings("unchecked")
		@Override
		public Response deserialize(
			final VPackSlice parent,
			final VPackSlice vpack,
			final VPackDeserializationContext context) throws VPackException {
			final Response response = new Response();
			response.setVersion(vpack.get(0).getAsInt());
			response.setType(vpack.get(1).getAsInt());
			response.setResponseCode(vpack.get(2).getAsInt());
			if (vpack.size() > 3) {
				response.setMeta(context.deserialize(vpack.get(3), Map.class));
			}
			return response;
		}
	};

	public static final VPackDeserializer<CollectionType> COLLECTION_TYPE = new VPackDeserializer<CollectionType>() {
		@Override
		public CollectionType deserialize(
			final VPackSlice parent,
			final VPackSlice vpack,
			final VPackDeserializationContext context) throws VPackException {
			return CollectionType.fromType(vpack.getAsInt());
		}
	};

	public static final VPackDeserializer<CollectionStatus> COLLECTION_STATUS = new VPackDeserializer<CollectionStatus>() {
		@Override
		public CollectionStatus deserialize(
			final VPackSlice parent,
			final VPackSlice vpack,
			final VPackDeserializationContext context) throws VPackException {
			return CollectionStatus.fromStatus(vpack.getAsInt());
		}
	};

	@SuppressWarnings("unchecked")
	public static final VPackDeserializer<BaseDocument> BASE_DOCUMENT = new VPackDeserializer<BaseDocument>() {
		@Override
		public BaseDocument deserialize(
			final VPackSlice parent,
			final VPackSlice vpack,
			final VPackDeserializationContext context) throws VPackException {
			return new BaseDocument(context.deserialize(vpack, Map.class));
		}
	};

	@SuppressWarnings("unchecked")
	public static final VPackDeserializer<BaseEdgeDocument> BASE_EDGE_DOCUMENT = new VPackDeserializer<BaseEdgeDocument>() {
		@Override
		public BaseEdgeDocument deserialize(
			final VPackSlice parent,
			final VPackSlice vpack,
			final VPackDeserializationContext context) throws VPackException {
			return new BaseEdgeDocument(context.deserialize(vpack, Map.class));
		}
	};

	public static final VPackDeserializer<Date> DATE_STRING = new VPackDeserializer<Date>() {
		@Override
		public Date deserialize(
			final VPackSlice parent,
			final VPackSlice vpack,
			final VPackDeserializationContext context) throws VPackException {
			try {
				return new SimpleDateFormat(DATE_TIME_FORMAT).parse(vpack.getAsString());
			} catch (final ParseException e) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("got ParseException for date string: " + vpack.getAsString());
				}
			}
			return null;
		}
	};

	public static final VPackDeserializer<LogLevel> LOG_LEVEL = new VPackDeserializer<LogLevel>() {
		@Override
		public LogLevel deserialize(
			final VPackSlice parent,
			final VPackSlice vpack,
			final VPackDeserializationContext context) throws VPackException {
			return LogLevel.fromLevel(vpack.getAsInt());
		}
	};

	public static final VPackDeserializer<ArangoDBVersion.License> LICENSE = new VPackDeserializer<ArangoDBVersion.License>() {
		@Override
		public License deserialize(
			final VPackSlice parent,
			final VPackSlice vpack,
			final VPackDeserializationContext context) throws VPackException {
			return License.valueOf(vpack.getAsString().toUpperCase());
		}
	};

	public static final VPackDeserializer<Permissions> PERMISSIONS = new VPackDeserializer<Permissions>() {
		@Override
		public Permissions deserialize(
			final VPackSlice parent,
			final VPackSlice vpack,
			final VPackDeserializationContext context) throws VPackException {
			return Permissions.valueOf(vpack.getAsString().toUpperCase());
		}
	};

	public static final VPackDeserializer<QueryExecutionState> QUERY_EXECUTION_STATE = new VPackDeserializer<QueryExecutionState>() {
		@Override
		public QueryExecutionState deserialize(
			final VPackSlice parent,
			final VPackSlice vpack,
			final VPackDeserializationContext context) throws VPackException {
			return QueryExecutionState.valueOf(vpack.getAsString().toUpperCase().replaceAll(" ", "_"));
		}
	};

	public static final VPackDeserializer<ReplicationFactor> REPLICATION_FACTOR = new VPackDeserializer<ReplicationFactor>() {
		@Override
		public ReplicationFactor deserialize(
			final VPackSlice parent,
			final VPackSlice vpack,
			final VPackDeserializationContext context) throws VPackException {
			final ReplicationFactor replicationFactor = new ReplicationFactor();
			if (vpack.isString() && vpack.getAsString().equals("satellite")) {
				replicationFactor.setSatellite(true);
			} else {
				replicationFactor.setReplicationFactor(vpack.getAsInt());
			}
			return replicationFactor;
		}
	};
}
