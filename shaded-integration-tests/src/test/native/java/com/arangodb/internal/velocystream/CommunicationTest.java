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

package com.arangodb.internal.velocystream;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.ArangoDBVersion;
import com.arangodb.internal.config.FileConfigPropertiesProvider;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mark Vollmary
 */
class CommunicationTest {

    private static final String FAST = "fast";
    private static final String SLOW = "slow";

    @Test
    void chunkSizeSmall() {
        final ArangoDB arangoDB = new ArangoDB.Builder()
                .loadProperties(new FileConfigPropertiesProvider())
                .chunksize(20).build();
        final ArangoDBVersion version = arangoDB.getVersion();
        assertThat(version).isNotNull();
    }

    @Test
    void multiThread() throws Exception {
        final ArangoDB arangoDB = new ArangoDB.Builder()
                .loadProperties(new FileConfigPropertiesProvider())
                .build();
        arangoDB.getUsers(); // authentication and active-failover connection redirect to master

        final Collection<String> result = new ConcurrentLinkedQueue<>();
        final Thread fast = new Thread(() -> {
            arangoDB.db().query("return sleep(0.1)", null, null, null);
            result.add(FAST);
        });
        final Thread slow = new Thread(() -> {
            arangoDB.db().query("return sleep(0.5)", null, null, null);
            result.add(SLOW);
        });
        slow.start();
        fast.start();

        slow.join();
        fast.join();

        assertThat(result.size()).isEqualTo(2);
        final Iterator<String> iterator = result.iterator();
        assertThat(iterator.next()).isEqualTo(FAST);
        assertThat(iterator.next()).isEqualTo(SLOW);
    }

    @Test
    void multiThreadSameDatabases() throws Exception {
        final ArangoDB arangoDB = new ArangoDB.Builder()
                .loadProperties(new FileConfigPropertiesProvider())
                .build();
        arangoDB.getUsers(); // authentication and active-failover connection redirect to master

        final ArangoDatabase db = arangoDB.db();

        final Collection<String> result = new ConcurrentLinkedQueue<>();
        final Thread t1 = new Thread(() -> {
            db.query("return sleep(0.1)", null, null, null);
            result.add("1");
        });
        final Thread t2 = new Thread(() -> {
            db.query("return sleep(0.1)", null, null, null);
            result.add("1");
        });
        t2.start();
        t1.start();
        t2.join();
        t1.join();
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    void minOneConnection() {
        final ArangoDB arangoDB = new ArangoDB.Builder()
                .loadProperties(new FileConfigPropertiesProvider())
                .maxConnections(0).build();
        final ArangoDBVersion version = arangoDB.getVersion();
        assertThat(version).isNotNull();
    }

    @Test
    void defaultMaxConnection() {
        final ArangoDB arangoDB = new ArangoDB.Builder()
                .loadProperties(new FileConfigPropertiesProvider())
                .maxConnections(null).build();
        final ArangoDBVersion version = arangoDB.getVersion();
        assertThat(version).isNotNull();
    }
}
