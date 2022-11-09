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

import com.arangodb.DbName;
import com.arangodb.entity.LogLevelEntity;
import com.arangodb.entity.Permissions;
import com.arangodb.entity.ServerRole;
import com.arangodb.entity.UserEntity;
import com.arangodb.internal.ArangoExecutor.ResponseDeserializer;
import com.arangodb.internal.serde.InternalSerde;
import com.arangodb.model.*;
import com.arangodb.RequestType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import static com.arangodb.internal.serde.SerdeUtils.constructListType;

/**
 * @author Mark Vollmary
 * @author Heiko Kernbach
 */
public abstract class InternalArangoDB<E extends ArangoExecutor> extends ArangoExecuteable<E> {
    private static final String PATH_API_ADMIN_LOG_ENTRIES = "/_admin/log/entries";
    private static final String PATH_API_ADMIN_LOG_LEVEL = "/_admin/log/level";
    private static final String PATH_API_ROLE = "/_admin/server/role";
    private static final String PATH_API_SERVER_ID = "/_admin/server/id";
    private static final String PATH_API_USER = "/_api/user";
    private static final String PATH_API_QUERY_RULES = "/_api/query/rules";

    protected InternalArangoDB(final E executor, final InternalSerde util) {
        super(executor, util);
    }

    protected InternalRequest getRoleRequest() {
        return request(DbName.SYSTEM, RequestType.GET, PATH_API_ROLE);
    }

    protected InternalRequest getServerIdRequest() {
        return request(DbName.SYSTEM, RequestType.GET, PATH_API_SERVER_ID);
    }

    protected ResponseDeserializer<ServerRole> getRoleResponseDeserializer() {
        return response -> getSerde().deserialize(response.getBody(), "/role", ServerRole.class);
    }

    protected ResponseDeserializer<String> getServerIdResponseDeserializer() {
        return response -> getSerde().deserialize(response.getBody(), "/id", String.class);
    }

    protected InternalRequest createDatabaseRequest(final DBCreateOptions options) {
        final InternalRequest request = request(DbName.SYSTEM, RequestType.POST,
                InternalArangoDatabase.PATH_API_DATABASE);
        request.setBody(getSerde().serialize(options));
        return request;
    }

    protected ResponseDeserializer<Boolean> createDatabaseResponseDeserializer() {
        return response -> getSerde().deserialize(response.getBody(), ArangoResponseField.RESULT_JSON_POINTER,
                Boolean.class);
    }

    protected InternalRequest getDatabasesRequest(final DbName dbName) {
        return request(dbName, RequestType.GET, InternalArangoDatabase.PATH_API_DATABASE);
    }

    protected ResponseDeserializer<Collection<String>> getDatabaseResponseDeserializer() {
        return response -> getSerde().deserialize(response.getBody(), ArangoResponseField.RESULT_JSON_POINTER,
                constructListType(String.class));
    }

    protected InternalRequest getAccessibleDatabasesForRequest(final DbName dbName, final String user) {
        return request(dbName, RequestType.GET, PATH_API_USER, user, ArangoRequestParam.DATABASE);
    }

    protected ResponseDeserializer<Collection<String>> getAccessibleDatabasesForResponseDeserializer() {
        return response -> {
            Iterator<String> names =
                    getSerde().parse(response.getBody(), ArangoResponseField.RESULT_JSON_POINTER).fieldNames();
            final Collection<String> dbs = new ArrayList<>();
            while (names.hasNext()) {
                dbs.add(names.next());
            }
            return dbs;
        };
    }

    protected InternalRequest createUserRequest(
            final DbName dbName,
            final String user,
            final String passwd,
            final UserCreateOptions options) {
        final InternalRequest request;
        request = request(dbName, RequestType.POST, PATH_API_USER);
        request.setBody(
                getSerde().serialize(OptionsBuilder.build(options != null ? options : new UserCreateOptions(), user,
                        passwd)));
        return request;
    }

    protected InternalRequest deleteUserRequest(final DbName dbName, final String user) {
        return request(dbName, RequestType.DELETE, PATH_API_USER, user);
    }

    protected InternalRequest getUsersRequest(final DbName dbName) {
        return request(dbName, RequestType.GET, PATH_API_USER);
    }

    protected InternalRequest getUserRequest(final DbName dbName, final String user) {
        return request(dbName, RequestType.GET, PATH_API_USER, user);
    }

    protected ResponseDeserializer<Collection<UserEntity>> getUsersResponseDeserializer() {
        return response -> getSerde().deserialize(response.getBody(), ArangoResponseField.RESULT_JSON_POINTER,
                constructListType(UserEntity.class));
    }

    protected InternalRequest updateUserRequest(final DbName dbName, final String user, final UserUpdateOptions options) {
        final InternalRequest request;
        request = request(dbName, RequestType.PATCH, PATH_API_USER, user);
        request.setBody(getSerde().serialize(options != null ? options : new UserUpdateOptions()));
        return request;
    }

    protected InternalRequest replaceUserRequest(final DbName dbName, final String user, final UserUpdateOptions options) {
        final InternalRequest request;
        request = request(dbName, RequestType.PUT, PATH_API_USER, user);
        request.setBody(getSerde().serialize(options != null ? options : new UserUpdateOptions()));
        return request;
    }

    protected InternalRequest updateUserDefaultDatabaseAccessRequest(final String user, final Permissions permissions) {
        return request(DbName.SYSTEM, RequestType.PUT, PATH_API_USER, user, ArangoRequestParam.DATABASE,
                "*").setBody(getSerde().serialize(OptionsBuilder.build(new UserAccessOptions(), permissions)));
    }

    protected InternalRequest updateUserDefaultCollectionAccessRequest(final String user, final Permissions permissions) {
        return request(DbName.SYSTEM, RequestType.PUT, PATH_API_USER, user, ArangoRequestParam.DATABASE,
                "*", "*").setBody(getSerde().serialize(OptionsBuilder.build(new UserAccessOptions(), permissions)));
    }

    protected InternalRequest getLogEntriesRequest(final LogOptions options) {
        final LogOptions params = options != null ? options : new LogOptions();
        return request(DbName.SYSTEM, RequestType.GET, PATH_API_ADMIN_LOG_ENTRIES)
                .putQueryParam(LogOptions.PROPERTY_UPTO, params.getUpto())
                .putQueryParam(LogOptions.PROPERTY_LEVEL, params.getLevel())
                .putQueryParam(LogOptions.PROPERTY_START, params.getStart())
                .putQueryParam(LogOptions.PROPERTY_SIZE, params.getSize())
                .putQueryParam(LogOptions.PROPERTY_OFFSET, params.getOffset())
                .putQueryParam(LogOptions.PROPERTY_SEARCH, params.getSearch())
                .putQueryParam(LogOptions.PROPERTY_SORT, params.getSort());
    }

    protected InternalRequest getLogLevelRequest() {
        return request(DbName.SYSTEM, RequestType.GET, PATH_API_ADMIN_LOG_LEVEL);
    }

    protected InternalRequest setLogLevelRequest(final LogLevelEntity entity) {
        return request(DbName.SYSTEM, RequestType.PUT, PATH_API_ADMIN_LOG_LEVEL)
                .setBody(getSerde().serialize(entity));
    }

    protected InternalRequest getQueryOptimizerRulesRequest() {
        return request(DbName.SYSTEM, RequestType.GET, PATH_API_QUERY_RULES);
    }

}
