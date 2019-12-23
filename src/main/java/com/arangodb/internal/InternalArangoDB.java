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

package com.arangodb.internal;

import com.arangodb.entity.LogLevelEntity;
import com.arangodb.entity.Permissions;
import com.arangodb.entity.ServerRole;
import com.arangodb.entity.UserEntity;
import com.arangodb.internal.ArangoExecutor.ResponseDeserializer;
import com.arangodb.internal.util.ArangoSerializationFactory;
import com.arangodb.model.*;
import com.arangodb.velocypack.Type;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * @author Mark Vollmary
 * @author Heiko Kernbach
 */
public abstract class InternalArangoDB<E extends ArangoExecutor> extends ArangoExecuteable<E> {

    private static final String PATH_API_ADMIN_LOG = "/_admin/log";
    private static final String PATH_API_ADMIN_LOG_LEVEL = "/_admin/log/level";
    private static final String PATH_API_ROLE = "/_admin/server/role";
    private static final String PATH_ENDPOINTS = "/_api/cluster/endpoints";
    private static final String PATH_API_USER = "/_api/user";

    protected InternalArangoDB(final E executor, final ArangoSerializationFactory util, final ArangoContext context) {
        super(executor, util, context);
    }

    protected Request getRoleRequest() {
        return request(ArangoRequestParam.SYSTEM, RequestType.GET, PATH_API_ROLE);
    }

    protected ResponseDeserializer<ServerRole> getRoleResponseDeserializer() {
        return response -> util().deserialize(response.getBody().get("role"), ServerRole.class);
    }

    protected Request createDatabaseRequest(final DBCreateOptions options) {
        final Request request = request(ArangoRequestParam.SYSTEM, RequestType.POST,
                InternalArangoDatabase.PATH_API_DATABASE);
        request.setBody(util().serialize(options));
        return request;
    }

    protected ResponseDeserializer<Boolean> createDatabaseResponseDeserializer() {
        return response -> response.getBody().get(ArangoResponseField.RESULT).getAsBoolean();
    }

    protected Request getDatabasesRequest(final String database) {
        return request(database, RequestType.GET, InternalArangoDatabase.PATH_API_DATABASE);
    }

    protected ResponseDeserializer<Collection<String>> getDatabaseResponseDeserializer() {
        return response -> {
            final VPackSlice result = response.getBody().get(ArangoResponseField.RESULT);
            return util().deserialize(result, new Type<Collection<String>>() {
            }.getType());
        };
    }

    protected Request getAccessibleDatabasesForRequest(final String database, final String user) {
        return request(database, RequestType.GET, PATH_API_USER, user, ArangoRequestParam.DATABASE);
    }

    protected ResponseDeserializer<Collection<String>> getAccessibleDatabasesForResponseDeserializer() {
        return response -> {
            final VPackSlice result = response.getBody().get(ArangoResponseField.RESULT);
            final Collection<String> dbs = new ArrayList<>();
            for (final Iterator<Entry<String, VPackSlice>> iterator = result.objectIterator(); iterator
                    .hasNext(); ) {
                dbs.add(iterator.next().getKey());
            }
            return dbs;
        };
    }

    protected Request createUserRequest(
            final String database,
            final String user,
            final String passwd,
            final UserCreateOptions options) {
        final Request request;
        request = request(database, RequestType.POST, PATH_API_USER);
        request.setBody(
                util().serialize(OptionsBuilder.build(options != null ? options : new UserCreateOptions(), user, passwd)));
        return request;
    }

    protected Request deleteUserRequest(final String database, final String user) {
        return request(database, RequestType.DELETE, PATH_API_USER, user);
    }

    protected Request getUsersRequest(final String database) {
        return request(database, RequestType.GET, PATH_API_USER);
    }

    protected Request getUserRequest(final String database, final String user) {
        return request(database, RequestType.GET, PATH_API_USER, user);
    }

    protected ResponseDeserializer<Collection<UserEntity>> getUsersResponseDeserializer() {
        return response -> {
            final VPackSlice result = response.getBody().get(ArangoResponseField.RESULT);
            return util().deserialize(result, new Type<Collection<UserEntity>>() {
            }.getType());
        };
    }

    protected Request updateUserRequest(final String database, final String user, final UserUpdateOptions options) {
        final Request request;
        request = request(database, RequestType.PATCH, PATH_API_USER, user);
        request.setBody(util().serialize(options != null ? options : new UserUpdateOptions()));
        return request;
    }

    protected Request replaceUserRequest(final String database, final String user, final UserUpdateOptions options) {
        final Request request;
        request = request(database, RequestType.PUT, PATH_API_USER, user);
        request.setBody(util().serialize(options != null ? options : new UserUpdateOptions()));
        return request;
    }

    protected Request updateUserDefaultDatabaseAccessRequest(final String user, final Permissions permissions) {
        return request(ArangoRequestParam.SYSTEM, RequestType.PUT, PATH_API_USER, user, ArangoRequestParam.DATABASE,
                "*").setBody(util().serialize(OptionsBuilder.build(new UserAccessOptions(), permissions)));
    }

    protected Request updateUserDefaultCollectionAccessRequest(final String user, final Permissions permissions) {
        return request(ArangoRequestParam.SYSTEM, RequestType.PUT, PATH_API_USER, user, ArangoRequestParam.DATABASE,
                "*", "*").setBody(util().serialize(OptionsBuilder.build(new UserAccessOptions(), permissions)));
    }

    protected Request getLogsRequest(final LogOptions options) {
        final LogOptions params = options != null ? options : new LogOptions();
        return request(ArangoRequestParam.SYSTEM, RequestType.GET, PATH_API_ADMIN_LOG)
                .putQueryParam(LogOptions.PROPERTY_UPTO, params.getUpto())
                .putQueryParam(LogOptions.PROPERTY_LEVEL, params.getLevel())
                .putQueryParam(LogOptions.PROPERTY_START, params.getStart())
                .putQueryParam(LogOptions.PROPERTY_SIZE, params.getSize())
                .putQueryParam(LogOptions.PROPERTY_OFFSET, params.getOffset())
                .putQueryParam(LogOptions.PROPERTY_SEARCH, params.getSearch())
                .putQueryParam(LogOptions.PROPERTY_SORT, params.getSort());
    }

    protected Request getLogLevelRequest() {
        return request(ArangoRequestParam.SYSTEM, RequestType.GET, PATH_API_ADMIN_LOG_LEVEL);
    }

    protected Request setLogLevelRequest(final LogLevelEntity entity) {
        return request(ArangoRequestParam.SYSTEM, RequestType.PUT, PATH_API_ADMIN_LOG_LEVEL)
                .setBody(util().serialize(entity));
    }

}
