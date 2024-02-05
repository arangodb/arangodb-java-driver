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

import com.arangodb.ArangoDB;
import com.arangodb.DbName;
import com.arangodb.Protocol;
import com.arangodb.entity.ArangoDBEngine;
import com.arangodb.entity.ArangoDBVersion;
import com.arangodb.entity.License;
import com.arangodb.entity.ServerRole;
import com.arangodb.mapping.ArangoJack;
import com.arangodb.util.TestUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.util.UUID;

import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * @author Mark Vollmary
 */
public abstract class BaseTest {

    static final DbName TEST_DB = DbName.of("java_driver_test_db");
    static ArangoDBAsync arangoDB;
    static ArangoDatabaseAsync db;
    private static final ArangoDB adb = new ArangoDB.Builder()
            .useProtocol(Protocol.HTTP_JSON)
            .serializer(new ArangoJack())
            .build();
    private static final ArangoDBVersion version = adb.getVersion();

    @BeforeAll
    public static void init() {
        assumeTrue(isLessThanVersion(3, 12), "VST not supported");

        if (arangoDB == null) {
            arangoDB = new ArangoDBAsync.Builder().serializer(new ArangoJack()).build();
        }

        if (adb.db(TEST_DB).exists()) {
            adb.db(TEST_DB).drop();
        }

        adb.createDatabase(TEST_DB);
        BaseTest.db = arangoDB.db(TEST_DB);
    }

    @AfterAll
    public static void shutdown() {
        if (arangoDB != null) { // test not skipped
            adb.db(TEST_DB).drop();
            arangoDB.shutdown();
            arangoDB = null;
        }
        adb.shutdown();
    }

    static String rnd() {
        return UUID.randomUUID().toString();
    }

    protected static boolean isAtLeastVersion(final int major, final int minor, final int patch) {
        return TestUtils.isAtLeastVersion(version.getVersion(), major, minor, patch);
    }

    protected static boolean isAtLeastVersion(final int major, final int minor) {
        return isAtLeastVersion(major, minor, 0);
    }

    protected static boolean isMinorVersionAndAtLeastPatch(final int major, final int minor, final int patch) {
        return TestUtils.isMinorVersionAndAtLeastPatch(version.getVersion(), major, minor, patch);
    }

    protected static boolean isLessThanVersion(final int major, final int minor, final int patch) {
        return TestUtils.isLessThanVersion(version.getVersion(), major, minor, patch);
    }

    protected static boolean isLessThanVersion(final int major, final int minor) {
        return isLessThanVersion(major, minor, 0);
    }

    boolean isStorageEngine(ArangoDBEngine.StorageEngineName name) {
        return name.equals(adb.getEngine().getName());
    }

    boolean isSingleServer() {
        return (adb.getRole() == ServerRole.SINGLE);
    }

    boolean isCluster() {
        return adb.getRole() == ServerRole.COORDINATOR;
    }

    boolean isEnterprise() {
        return version.getLicense() == License.ENTERPRISE;
    }

}
