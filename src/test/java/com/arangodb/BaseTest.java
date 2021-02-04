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

package com.arangodb;

import com.arangodb.entity.ArangoDBEngine;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.EdgeDefinition;
import com.arangodb.entity.License;
import com.arangodb.entity.ServerRole;
import com.arangodb.mapping.ArangoJack;
import com.arangodb.model.CollectionCreateOptions;
import com.arangodb.model.GraphCreateOptions;
import com.arangodb.util.TestUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public abstract class BaseTest {

    static final String TEST_DB = "java_driver_test_db";

    // TODO: make configurable
    static final List<ArangoDB> arangos = Arrays.asList(
            new ArangoDB.Builder().useProtocol(Protocol.VST).build(),
            new ArangoDB.Builder().useProtocol(Protocol.HTTP_JSON).build(),
            new ArangoDB.Builder().useProtocol(Protocol.HTTP_VPACK).build(),
            new ArangoDB.Builder().serializer(new ArangoJack()).build()
    );

    @Parameters
    public static List<ArangoDB> builders() {
        return arangos;
    }

    protected final ArangoDB arangoDB;
    protected final ArangoDatabase db;

    BaseTest(final ArangoDB arangoDB) {
        this.arangoDB = arangoDB;
        db = arangoDB.db(TEST_DB);
    }

    static ArangoDatabase initDB() {
        ArangoDatabase database = arangos.get(0).db(TEST_DB);
        if (!database.exists())
            database.create();
        return database;
    }

    static void initGraph(String name, Collection<EdgeDefinition> edgeDefinitions, GraphCreateOptions options) {
        ArangoDatabase db = initDB();
        db.createGraph(name, edgeDefinitions, options);
    }

    static void initCollections(String... collections) {
        ArangoDatabase db = initDB();
        for (String collection : collections) {
            if (db.collection(collection).exists())
                db.collection(collection).drop();
            db.createCollection(collection, null);
        }
    }

    static void initEdgeCollections(String... collections) {
        ArangoDatabase db = initDB();
        for (String collection : collections) {
            if (db.collection(collection).exists())
                db.collection(collection).drop();
            db.createCollection(collection, new CollectionCreateOptions().type(CollectionType.EDGES));
        }
    }

    @BeforeClass
    public static void init() {
        ArangoDatabase database = arangos.get(0).db(TEST_DB);
        if (database.exists())
            database.drop();
    }

    @AfterClass
    public static void shutdown() {
        ArangoDatabase database = arangos.get(0).db(TEST_DB);
        if (database.exists())
            database.drop();
        arangos.forEach(ArangoDB::shutdown);
    }

    static String rnd() {
        return UUID.randomUUID().toString();
    }

    boolean isAtLeastVersion(final int major, final int minor) {
        return isAtLeastVersion(major, minor, 0);
    }

    boolean isAtLeastVersion(final int major, final int minor, final int patch) {
        return TestUtils.isAtLeastVersion(arangoDB.getVersion().getVersion(), major, minor, patch);
    }

    boolean isStorageEngine(ArangoDBEngine.StorageEngineName name) {
        return name.equals(arangoDB.getEngine().getName());
    }

    boolean isSingleServer() {
        return arangoDB.getRole() == ServerRole.SINGLE;
    }

    boolean isCluster() {
        return arangoDB.getRole() == ServerRole.COORDINATOR;
    }

    boolean isEnterprise() {
        return arangoDB.getVersion().getLicense() == License.ENTERPRISE;
    }

}
