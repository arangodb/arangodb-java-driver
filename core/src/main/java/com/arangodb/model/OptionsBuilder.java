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

package com.arangodb.model;

import com.arangodb.entity.EdgeDefinition;
import com.arangodb.entity.Permissions;
import com.arangodb.entity.ViewType;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public final class OptionsBuilder {

    private OptionsBuilder() {
        super();
    }

    public static UserCreateOptions build(final UserCreateOptions options, final String user, final String passwd) {
        return options.user(user).passwd(passwd);
    }

    public static PersistentIndexOptions build(final PersistentIndexOptions options, final Iterable<String> fields) {
        return options.fields(fields);
    }

    public static GeoIndexOptions build(final GeoIndexOptions options, final Iterable<String> fields) {
        return options.fields(fields);
    }

    @Deprecated
    public static FulltextIndexOptions build(final FulltextIndexOptions options, final Iterable<String> fields) {
        return options.fields(fields);
    }

    public static TtlIndexOptions build(final TtlIndexOptions options, final Iterable<String> fields) {
        return options.fields(fields);
    }

    public static ZKDIndexOptions build(final ZKDIndexOptions options, final Iterable<String> fields) {
        return options.fields(fields);
    }

    public static AbstractMDIndexOptions<?> build(final AbstractMDIndexOptions<?> options, final Iterable<String> fields) {
        return options.fields(fields);
    }

    public static CollectionCreateOptions build(final CollectionCreateOptions options, final String name) {
        return options.name(name);
    }

    public static AqlQueryOptions build(final AqlQueryOptions options, final String query,
                                        final Map<String, Object> bindVars) {
        return options.query(query).bindVars(bindVars);
    }

    public static AqlQueryExplainOptions build(
            final AqlQueryExplainOptions options,
            final String query,
            final Map<String, Object> bindVars) {
        return options.query(query).bindVars(bindVars);
    }

    public static AqlQueryParseOptions build(final AqlQueryParseOptions options, final String query) {
        return options.query(query);
    }

    public static GraphCreateOptions build(
            final GraphCreateOptions options,
            final String name,
            final Iterable<EdgeDefinition> edgeDefinitions) {
        ArrayList<EdgeDefinition> edCol = new ArrayList<>();
        if (edgeDefinitions != null) {
            edgeDefinitions.forEach(edCol::add);
        }
        return options.name(name).edgeDefinitions(edCol);
    }

    public static TransactionOptions build(final TransactionOptions options, final String action) {
        return options.action(action);
    }

    public static CollectionRenameOptions build(final CollectionRenameOptions options, final String name) {
        return options.name(name);
    }

    public static UserAccessOptions build(final UserAccessOptions options, final Permissions grant) {
        return options.grant(grant);
    }

    public static AqlFunctionCreateOptions build(
            final AqlFunctionCreateOptions options,
            final String name,
            final String code) {
        return options.name(name).code(code);
    }

    public static VertexCollectionCreateOptions build(
            final VertexCollectionCreateOptions options,
            final String collection) {
        return options.collection(collection);
    }

    public static ViewCreateOptions build(final ViewCreateOptions options, final String name, final ViewType type) {
        return options.name(name).type(type);
    }

    public static ViewRenameOptions build(final ViewRenameOptions options, final String name) {
        return options.name(name);
    }

}
