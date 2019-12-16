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
import com.arangodb.model.*;
import org.junit.*;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Michele Rastelli
 */
public class ForceBackupTest {

    private static ArangoDB arango1;
    private static ArangoDB arango2;
    private static ArangoDatabase db1;
    private static ArangoDatabase db2;
    private static ArangoCollection collection1;

    @BeforeClass
    public static void setupClass() {
        arango1 = new ArangoDB.Builder()
                .useProtocol(Protocol.HTTP_JSON)
                .build();
        db1 = arango1.db("ForceBackupTest");
        if (db1.exists())
            db1.drop();
        db1.create();
        db1.createCollection("ForceBackupTest", new CollectionCreateOptions()
                .replicationFactor(2)
                .minReplicationFactor(2));
        collection1 = db1.collection("ForceBackupTest");

        arango2 = new ArangoDB.Builder()
                .useProtocol(Protocol.HTTP_JSON)
                .build();
        db2 = arango2.db("ForceBackupTest");
    }

    @AfterClass
    public static void shutdown() {
        if (db1.exists())
            db1.drop();
        arango1.shutdown();
    }

    @Before
    public void setup() {
        collection1.truncate();
    }

    @Test
    public void createAndRestoreBackup() {
        String key = "test-" + UUID.randomUUID().toString();
        BaseDocument initialDocument = new BaseDocument(key);
        collection1.insertDocument(initialDocument);

        BackupEntity createdBackup = arango1.createBackup(Collections.emptyMap());
        assertThat(createdBackup.getCode(), is(201));

        db1.drop();
        arango1.restoreBackup(Collections.singletonMap("id", createdBackup.getResult().get("id")));

        BaseDocument gotDocument = collection1.getDocument(key, BaseDocument.class);
        assertThat(gotDocument, equalTo(initialDocument));
    }

    @Test
    public void writeStreamTransactionShouldBeAborted() {
        StreamTransactionEntity createdTx = db1.beginStreamTransaction(new StreamTransactionOptions()
                .writeCollections(collection1.name()));

        String key = "test-" + UUID.randomUUID().toString();
        BaseDocument initialDocument = new BaseDocument(key);
        collection1.insertDocument(initialDocument, new DocumentCreateOptions().streamTransactionId(createdTx.getId()));

        BackupEntity createdBackup = arango1.createBackup(Collections.singletonMap("force", true));
        assertThat(createdBackup.getCode(), is(201));

        StreamTransactionEntity gotTx = db1.getStreamTransaction(createdTx.getId());
        assertThat(gotTx.getStatus(), is(StreamTransactionStatus.aborted));

        db1.drop();
        arango1.restoreBackup(Collections.singletonMap("id", createdBackup.getResult().get("id")));

        assertThat(collection1.documentExists(key), is(false));

        StreamTransactionEntity gotTxAfterBkp = db1.getStreamTransaction(createdTx.getId());
        assertThat(gotTxAfterBkp.getStatus(), is(StreamTransactionStatus.aborted));

    }

    @Test
    public void readAndWriteStreamTransactionShouldBeAborted() {
        StreamTransactionEntity createdTx = db1.beginStreamTransaction(new StreamTransactionOptions()
                .readCollections(collection1.name())
                .writeCollections(collection1.name()));

        String key = "test-" + UUID.randomUUID().toString();
        BaseDocument initialDocument = new BaseDocument(key);
        collection1.insertDocument(initialDocument, new DocumentCreateOptions().streamTransactionId(createdTx.getId()));
        assertThat(collection1.documentExists(key, new DocumentExistsOptions().streamTransactionId(createdTx.getId())), is(true));

        BackupEntity createdBackup = arango1.createBackup(Collections.singletonMap("force", true));
        assertThat(createdBackup.getCode(), is(201));

        StreamTransactionEntity gotTx = db1.getStreamTransaction(createdTx.getId());
        assertThat(gotTx.getStatus(), is(StreamTransactionStatus.aborted));

        assertThat(collection1.documentExists(key), is(false));

        db1.drop();
        arango1.restoreBackup(Collections.singletonMap("id", createdBackup.getResult().get("id")));

        assertThat(collection1.documentExists(key), is(false));

        StreamTransactionEntity gotTxAfterBkp = db1.getStreamTransaction(createdTx.getId());
        assertThat(gotTxAfterBkp.getStatus(), is(StreamTransactionStatus.aborted));

    }

    @Test
    public void updatingWriteStreamTransactionShouldBeAborted() throws InterruptedException {
        StreamTransactionEntity createdTx = db1.beginStreamTransaction(new StreamTransactionOptions()
                .writeCollections(collection1.name()));

        new Thread(() -> {
            while (true) {
                String key = "test-" + UUID.randomUUID().toString();
                BaseDocument initialDocument = new BaseDocument(key);
                collection1.insertDocument(initialDocument, new DocumentCreateOptions().streamTransactionId(createdTx.getId()));
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        Thread.sleep(1000);

        System.out.println("performing bkp");
        BackupEntity createdBackup = arango1.createBackup(Collections.singletonMap("force", true));
        System.out.println("bkp done");
        assertThat(createdBackup.getCode(), is(201));

        StreamTransactionEntity gotTx = db1.getStreamTransaction(createdTx.getId());
        assertThat(gotTx.getStatus(), is(StreamTransactionStatus.aborted));

        db1.drop();
        arango1.restoreBackup(Collections.singletonMap("id", createdBackup.getResult().get("id")));

        assertThat(collection1.count().getCount(), is(0L));

        StreamTransactionEntity gotTxAfterBkp = db1.getStreamTransaction(createdTx.getId());
        assertThat(gotTxAfterBkp.getStatus(), is(StreamTransactionStatus.aborted));

    }

    private List<String> performLongRunningJsTransaction(double duration, TransactionOptions options) {
        String key1 = "test-" + UUID.randomUUID().toString();
        String key2 = "test-" + UUID.randomUUID().toString();
        new Thread(() -> {
            String jsTx = "function (params) { "
                    + "var db = require('internal').db;"
                    + "db." + collection1.name() + ".save({\"_key\": \"" + key1 + "\"});"
                    + "require('internal').sleep(" + duration + ");"
                    + "db." + collection1.name() + ".save({\"_key\": \"" + key2 + "\"});"
                    + "}";
            db2.transaction(jsTx, Void.class, options);
        }).start();
        return Arrays.asList(key1, key2);
    }

    @Test
    public void jsTransaction() throws InterruptedException {
        List<String> jsTxKeys = performLongRunningJsTransaction(1, new TransactionOptions()
                .writeCollections(collection1.name()));
        Thread.sleep(1500);
        assertThat(collection1.documentExists(jsTxKeys.get(0)), equalTo(true));
        assertThat(collection1.documentExists(jsTxKeys.get(1)), equalTo(true));
    }

    private void waitUntil(long until) throws InterruptedException {
        long msToWait = until - new Date().getTime();
        if (msToWait > 0) {
            Thread.sleep(msToWait);
        }
    }

    @Test
    @Ignore
    public void writeJsTransaction() throws InterruptedException {
        List<String> jsTxKeys = performLongRunningJsTransaction(130, new TransactionOptions()
                .writeCollections(collection1.name()));
        Thread.sleep(1000);

        long b4Backup = new Date().getTime();
        long waitUntil = b4Backup + 130_000;

        BackupEntity createdBackup = arango1.createBackup(Collections.singletonMap("force", true));
        long afterBackup = new Date().getTime();
        assertThat(afterBackup - b4Backup, is(lessThan(125_000L)));
        assertThat(createdBackup.getCode(), is(201));

        waitUntil(waitUntil);
        assertThat(collection1.documentExists(jsTxKeys.get(0)), equalTo(false));
        assertThat(collection1.documentExists(jsTxKeys.get(1)), equalTo(false));

        db1.drop();
        arango1.restoreBackup(Collections.singletonMap("id", createdBackup.getResult().get("id")));

        assertThat(collection1.documentExists(jsTxKeys.get(0)), equalTo(false));
        assertThat(collection1.documentExists(jsTxKeys.get(1)), equalTo(false));
    }

}
