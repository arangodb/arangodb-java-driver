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
import com.arangodb.entity.License;
import com.arangodb.entity.ServerRole;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.TestRule;

import java.util.concurrent.ExecutionException;


/**
 * @author Mark Vollmary
 */
public abstract class BaseTest {

    static final String TEST_DB = "java_driver_test_db";
    static ArangoDBAsync arangoDB;
    static ArangoDatabaseAsync db;

    @ClassRule
    public static TestRule acquireHostListRule = TestUtils.acquireHostListRule;

    @BeforeClass
    public static void init() throws InterruptedException, ExecutionException {
        if (arangoDB == null) {
            arangoDB = new ArangoDBAsync.Builder().build();
        }

        if (arangoDB.db(TEST_DB).exists().get()) {
            arangoDB.db(TEST_DB).drop().get();
        }

        arangoDB.createDatabase(TEST_DB).get();
        BaseTest.db = arangoDB.db(TEST_DB);
    }

    @AfterClass
    public static void shutdown() throws InterruptedException, ExecutionException {
        arangoDB.db(TEST_DB).drop().get();
        arangoDB.shutdown();
        arangoDB = null;
    }

    protected static boolean isAtLeastVersion(final ArangoDBAsync arangoDB, final int major, final int minor)
            throws InterruptedException, ExecutionException {
        final String[] split = arangoDB.getVersion().get().getVersion().split("\\.");
        return Integer.parseInt(split[0]) >= major && Integer.parseInt(split[1]) >= minor;
    }

    protected boolean isAtLeastVersion(final int major, final int minor) throws InterruptedException, ExecutionException {
        return isAtLeastVersion(arangoDB, major, minor);
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
