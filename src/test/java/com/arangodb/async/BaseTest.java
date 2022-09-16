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

package com.arangodb.async;

import com.arangodb.entity.ArangoDBEngine;
import com.arangodb.DbName;
import com.arangodb.entity.License;
import com.arangodb.entity.ServerRole;
import com.arangodb.mapping.ArangoJack;
import com.arangodb.util.TestUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.util.UUID;
import java.util.concurrent.ExecutionException;


/**
 * @author Mark Vollmary
 */
public abstract class BaseTest {

    static final DbName TEST_DB = DbName.of("java_driver_test_db");
    static ArangoDBAsync arangoDB;
    static ArangoDatabaseAsync db;

    @BeforeAll
    static void init() throws InterruptedException, ExecutionException {
        if (arangoDB == null) {
            arangoDB = new ArangoDBAsync.Builder().serializer(new ArangoJack()).build();
        }

        if (arangoDB.db(TEST_DB).exists().get()) {
            arangoDB.db(TEST_DB).drop().get();
        }

        arangoDB.createDatabase(TEST_DB).get();
        BaseTest.db = arangoDB.db(TEST_DB);
    }

    @AfterAll
    static void shutdown() throws InterruptedException, ExecutionException {
        arangoDB.db(TEST_DB).drop().get();
        arangoDB.shutdown();
        arangoDB = null;
    }

    static String rnd() {
        return UUID.randomUUID().toString();
    }

    protected static boolean isAtLeastVersion(final ArangoDBAsync arangoDB, final int major, final int minor, final int patch)
            throws InterruptedException, ExecutionException {
        return com.arangodb.util.TestUtils.isAtLeastVersion(arangoDB.getVersion().get().getVersion(), major, minor, patch);
    }

    protected static boolean isAtLeastVersion(final ArangoDBAsync arangoDB, final int major, final int minor)
            throws InterruptedException, ExecutionException {
        return isAtLeastVersion(arangoDB, major, minor, 0);
    }

    protected boolean isAtLeastVersion(final int major, final int minor, final int patch) throws InterruptedException, ExecutionException {
        return isAtLeastVersion(arangoDB, major, minor, patch);
    }

    protected boolean isAtLeastVersion(final int major, final int minor) throws InterruptedException, ExecutionException {
        return isAtLeastVersion(major, minor, 0);
    }

    boolean isLessThanVersion(final int major, final int minor) throws ExecutionException, InterruptedException {
        return isLessThanVersion(major, minor, 0);
    }

    boolean isLessThanVersion(final int major, final int minor, final int patch) throws ExecutionException, InterruptedException {
        return TestUtils.isLessThanVersion(db.getVersion().get().getVersion(), major, minor, patch);
    }

    boolean isStorageEngine(ArangoDBEngine.StorageEngineName name) throws ExecutionException, InterruptedException {
        return name.equals(db.getEngine().get().getName());
    }

    boolean isSingleServer() throws ExecutionException, InterruptedException {
        return (arangoDB.getRole().get() == ServerRole.SINGLE);
    }

    boolean isCluster() throws ExecutionException, InterruptedException {
        return arangoDB.getRole().get() == ServerRole.COORDINATOR;
    }

    boolean isEnterprise() throws ExecutionException, InterruptedException {
        return arangoDB.getVersion().get().getLicense() == License.ENTERPRISE;
    }

}
