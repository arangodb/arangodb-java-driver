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


package qa;


import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.arangodb.Protocol;
import com.arangodb.entity.BackupEntity;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.StreamTransactionEntity;
import com.arangodb.entity.StreamTransactionStatus;
import com.arangodb.model.CollectionCreateOptions;
import com.arangodb.model.StreamTransactionOptions;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * @author Michele Rastelli
 */
public class ForceBackupTest {

    private static ArangoDB arango;
    private static ArangoDatabase db;
    private static ArangoCollection collection;

    @BeforeClass
    public static void setup() {
        arango = new ArangoDB.Builder()
                .useProtocol(Protocol.HTTP_JSON)
                .build();
        db = arango.db("ForceBackupTest");
        if (db.exists())
            db.drop();
        db.create();
        db.createCollection("ForceBackupTest", new CollectionCreateOptions()
                .replicationFactor(2)
                .minReplicationFactor(2));
        collection = db.collection("ForceBackupTest");
    }

    @Test
    public void createAndRestoreBackup() {
        String key = "test-" + UUID.randomUUID().toString();
        BaseDocument initialDocument = new BaseDocument(key);
        collection.insertDocument(initialDocument);

        BackupEntity createdBackup = arango.createBackup(Collections.emptyMap());
        assertThat(createdBackup.getCode(), is(201));

        db.drop();
        arango.restoreBackup(Collections.singletonMap("id", createdBackup.getResult().get("id")));

        BaseDocument gotDocument = collection.getDocument(key, BaseDocument.class);
        assertThat(gotDocument, equalTo(initialDocument));
    }

    @Test
    public void forceBackupWhileStreamTransaction() {
        StreamTransactionEntity createdTx = db.beginStreamTransaction(new StreamTransactionOptions()
                .readCollections(collection.name())
                .writeCollections(collection.name()));

        BackupEntity createdBackup = arango.createBackup(Collections.singletonMap("force", true));
        assertThat(createdBackup.getCode(), is(201));

        StreamTransactionEntity gotTx = db.getStreamTransaction(createdTx.getId());
        assertThat(gotTx.getStatus(), is(StreamTransactionStatus.aborted));
    }

}
