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


import com.arangodb.*;
import com.arangodb.model.CollectionCreateOptions;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

/**
 * @author Michele Rastelli
 */
public class CreateCollection {

    private static ArangoDB arangoDB;
    private static ArangoDatabase db;

    @BeforeClass
    public static void setup() {
        arangoDB = new ArangoDB.Builder()
                .useProtocol(Protocol.HTTP_JSON)
                .build();
        db = arangoDB.db("CreateCollection");

        if (db.exists())
            db.drop();
        db.create();

    }

    @Test
    public void createCollectionShardKeysById() {
        String name = "coll-" + UUID.randomUUID().toString();
        try {
            db.createCollection(name, new CollectionCreateOptions().shardKeys("_id"));
            fail();
        } catch (ArangoDBException e) {
            assertThat(e.getResponseCode(), is(400));
            assertThat(e.getErrorNum(), is(10));
            assertThat(e.getErrorMessage(), is("_id or _rev cannot be used as shard keys"));
        }
    }

    @Test
    public void createCollectionShardKeysByRev() {
        String name = "coll-" + UUID.randomUUID().toString();
        try {
            db.createCollection(name, new CollectionCreateOptions().shardKeys("_rev"));
            fail();
        } catch (ArangoDBException e) {
            assertThat(e.getResponseCode(), is(400));
            assertThat(e.getErrorNum(), is(10));
            assertThat(e.getErrorMessage(), is("_id or _rev cannot be used as shard keys"));
        }
    }

    @Test
    public void createCollectionWith128CharsName() {
        String name = RandomStringUtils.random(128, true, false);
        System.out.println(name);
        assertThat(name.length(), is(128));

        ArangoCollection c = db.collection(name);
        c.create();
        c.drop();
    }

    @Test
    public void createCollectionWith256CharsName() {
        String name = RandomStringUtils.random(256, true, false);
        System.out.println(name);
        assertThat(name.length(), is(256));

        ArangoCollection c = db.collection(name);
        c.create();
        c.drop();
    }

    @Test
    public void createCollectionWith255CharsName() {
        String name = RandomStringUtils.random(255, true, false);
        System.out.println(name);
        assertThat(name.length(), is(255));

        ArangoCollection c = db.collection(name);
        c.create();
        c.drop();
    }

    @Test
    public void createCollectionWithMoreThan256CharsName() {
        String name = RandomStringUtils.random(257, true, false);
        System.out.println(name);
        assertThat(name.length(), is(257));

        ArangoCollection c = db.collection(name);
        try {
            c.create();
        } catch (ArangoDBException e) {
            assertThat(e.getResponseCode(), is(400));
            assertThat(e.getErrorNum(), is(1208));
            assertThat(e.getErrorMessage(), is("illegal name"));
        }
    }

}
