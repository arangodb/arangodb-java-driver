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
import com.arangodb.entity.License;
import com.arangodb.entity.ServerRole;
import org.junit.AfterClass;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public abstract class BaseTest {

    @Parameters
    public static Collection<ArangoDB.Builder> builders() {
        return Arrays.asList(//
                new ArangoDB.Builder().useProtocol(Protocol.VST), //
                new ArangoDB.Builder().useProtocol(Protocol.HTTP_JSON), //
                new ArangoDB.Builder().useProtocol(Protocol.HTTP_VPACK) //
        );
    }

    static final String TEST_DB = "java_driver_test_db";
    static ArangoDB arangoDB;
    static ArangoDatabase db;

    BaseTest(final ArangoDB.Builder builder) {
        super();
        if (arangoDB != null) {
            shutdown();
        }
        arangoDB = builder.build();
        db = arangoDB.db(TEST_DB);

        if (!db.exists())
            db.create();
    }

    @AfterClass
    public static void shutdown() {
        arangoDB.shutdown();
        arangoDB = null;
    }

    boolean isAtLeastVersion(final int major, final int minor) {
        final String[] split = arangoDB.getVersion().getVersion().split("\\.");
        return Integer.parseInt(split[0]) >= major && Integer.parseInt(split[1]) >= minor;
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
