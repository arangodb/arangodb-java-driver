package qa;/*
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
import com.arangodb.Protocol;
import com.arangodb.entity.BaseDocument;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Michele Rastelli
 */
public class SmallQueuePerfTest {

    private static ArangoDB arangoDB;
    private static ArangoDatabase db;

    @BeforeClass
    public static void setup() {
        arangoDB = new ArangoDB.Builder()
                .useProtocol(Protocol.HTTP_JSON)
                .build();
        db = arangoDB.db("SmallQueuePerfTest");

        if (db.exists())
            db.drop();
        db.create();
    }

    @Test
    public void peftTest() throws InterruptedException {
        List<Thread> threads = Stream.of(
                "c1",
                "c2",
                "c3",
                "c4",
                "c5"
        )
                .peek(c -> db.createCollection(c))
                .map(this::perform1000CrudOps)
                .peek(Thread::start)
                .collect(Collectors.toList());

        for (Thread thread : threads) {
            thread.join();
        }
    }

    private Thread perform1000CrudOps(String collectionName) {
        return new Thread(() -> {
            ArangoCollection collection = new ArangoDB.Builder()
                    .useProtocol(Protocol.HTTP_JSON)
                    .build()
                    .db("SmallQueuePerfTest")
                    .collection(collectionName);

            for (int i = 0; i < 1000; i++) {
                String key = UUID.randomUUID().toString();
                BaseDocument doc = new BaseDocument(key);
                collection.insertDocument(doc);
                collection.getDocument(key, BaseDocument.class);
                doc.setProperties(Collections.singletonMap("k", "v"));
                collection.updateDocument(key, doc);
                collection.deleteDocument(key);
            }
        });
    }


}
