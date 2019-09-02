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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.ArangoDBVersion;

/**
 * @author Mark Vollmary
 */
public class CommunicationTest {

    private static final String FAST = "fast";
    private static final String SLOW = "slow";

    @Test
    public void chunkSizeSmall() {
        final ArangoDB arangoDB = new ArangoDB.Builder().chunksize(20).build();
        final ArangoDBVersion version = arangoDB.getVersion();
        assertThat(version, is(notNullValue()));
    }

    @Test
    public void multiThread() throws Exception {
        final ArangoDB arangoDB = new ArangoDB.Builder().build();
        arangoDB.getVersion();// authentication

        final Collection<String> result = new ConcurrentLinkedQueue<>();
        final Thread fast = new Thread(() -> {
            arangoDB.db().query("return sleep(1)", null, null, null);
            result.add(FAST);
        });
        final Thread slow = new Thread(() -> {
            arangoDB.db().query("return sleep(4)", null, null, null);
            result.add(SLOW);
        });
        slow.start();
        Thread.sleep(1000);
        fast.start();

        slow.join();
        fast.join();

        assertThat(result.size(), is(2));
        final Iterator<String> iterator = result.iterator();
        assertThat(iterator.next(), is(FAST));
        assertThat(iterator.next(), is(SLOW));
    }

    @Test
    public void multiThreadSameDatabases() throws Exception {
        final ArangoDB arangoDB = new ArangoDB.Builder().build();
        arangoDB.getVersion();// authentication

        final ArangoDatabase db = arangoDB.db();

        final Collection<String> result = new ConcurrentLinkedQueue<>();
        final Thread t1 = new Thread(() -> {
            db.query("return sleep(1)", null, null, null);
            result.add("1");
        });
        final Thread t2 = new Thread(() -> {
            db.query("return sleep(1)", null, null, null);
            result.add("1");
        });
        t2.start();
        t1.start();
        t2.join();
        t1.join();
        assertThat(result.size(), is(2));
    }

    @Test
    public void multiThreadMultiDatabases() throws Exception {
        final ArangoDB arangoDB = new ArangoDB.Builder().build();
        arangoDB.getVersion();// authentication

        try {
            arangoDB.createDatabase("db1");
            arangoDB.createDatabase("db2");
            final ArangoDatabase db1 = arangoDB.db("db1");
            final ArangoDatabase db2 = arangoDB.db("db2");

            final Collection<String> result = new ConcurrentLinkedQueue<>();
            final Thread t1 = new Thread(() -> {
                    db1.query("return sleep(1)", null, null, null);
                    result.add("1");
            });
            final Thread t2 = new Thread(() -> {
                    db2.query("return sleep(1)", null, null, null);
                    result.add("1");
            });
            t2.start();
            t1.start();
            t2.join();
            t1.join();
            assertThat(result.size(), is(2));
        } finally {
            arangoDB.db("db1").drop();
            arangoDB.db("db2").drop();
        }
    }

    @Test
    public void minOneConnection() {
        final ArangoDB arangoDB = new ArangoDB.Builder().maxConnections(0).build();
        final ArangoDBVersion version = arangoDB.getVersion();
        assertThat(version, is(notNullValue()));
    }

    @Test
    public void defaultMaxConnection() {
        final ArangoDB arangoDB = new ArangoDB.Builder().maxConnections(null).build();
        final ArangoDBVersion version = arangoDB.getVersion();
        assertThat(version, is(notNullValue()));
    }
}
