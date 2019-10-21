package reactor;/*
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


import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Michele Rastelli
 */
public class ConcurrentTest {

    private final static String COLLECTION = "myColl";
    private final ArangoDB arangoDB = new ArangoDB.Builder().build();
    private final ArangoDatabase db = arangoDB.db("javaConcurrentTest");
    private final ArangoCollection collection = db.collection(COLLECTION);

    @Before
    public void init() {
        if (!db.exists()) {
            db.create();
        }
        if (!collection.exists()) {
            collection.create();
        }
    }

    @After
    public void shutDown() {
        if (db.exists()) {
            db.drop();
        }
    }

    @Test
    public void multiThreadTest() throws InterruptedException {
        List<Thread> threads = IntStream.range(1, 20)
                .mapToObj(it -> new Thread(this::saveAndRead))
                .peek(Thread::start)
                .collect(Collectors.toList());

        for (Thread t : threads) {
            t.join();
        }
    }

    private void saveAndRead() {
        for (int i = 0; i < 1000; i++) {
            String key = "test-" + UUID.randomUUID().toString();
            collection.insertDocument(new BaseDocument(key));
            BaseDocument res = collection.getDocument(key, BaseDocument.class);
            assertThat(res.getKey(), is(key));
            assertThat(res.getId(), is(COLLECTION + "/" + key));
        }
    }

}
