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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author Michele Rastelli
 */
public class CheckStateAfterRestartTest {

    private static ArangoDB arango1;
    private static ArangoDatabase db1;
    private static ArangoCollection collection1;

    @BeforeClass
    public static void setupClass() {
        arango1 = new ArangoDB.Builder()
                .useProtocol(Protocol.HTTP_JSON)
                .build();
        db1 = arango1.db("ForceBackupTest");
        collection1 = db1.collection("ForceBackupTest");
    }

    @AfterClass
    public static void shutdown() {
        arango1.shutdown();
    }

    /**
     * trigger manually after all tests and db restart (./docker/restart_cluster.sh)
     */
    @Test
    public void checkStateAfterRestart() {
        assertThat(db1.getStreamTransactions().size(), is(0));
        assertThat(collection1.count().getCount(), is(0L));
    }

}
