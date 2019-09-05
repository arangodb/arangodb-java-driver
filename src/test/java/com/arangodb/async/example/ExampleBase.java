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

package com.arangodb.async.example;

import com.arangodb.async.ArangoCollectionAsync;
import com.arangodb.async.ArangoDBAsync;
import com.arangodb.async.ArangoDatabaseAsync;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.concurrent.ExecutionException;

/**
 * @author Mark Vollmary
 */
public class ExampleBase {

    protected static final String COLLECTION_NAME = "json_example_collection";
    private static final String DB_NAME = "json_example_db";
    protected static ArangoDatabaseAsync db;
    protected static ArangoCollectionAsync collection;
    private static ArangoDBAsync arangoDB;

    @BeforeClass
    public static void setUp() throws InterruptedException, ExecutionException {
        arangoDB = new ArangoDBAsync.Builder().build();
        if (arangoDB.db(DB_NAME).exists().get()) {
            arangoDB.db(DB_NAME).drop().get();
        }
        arangoDB.createDatabase(DB_NAME).get();
        db = arangoDB.db(DB_NAME);
        db.createCollection(COLLECTION_NAME).get();
        collection = db.collection(COLLECTION_NAME);
    }

    @AfterClass
    public static void tearDown() throws InterruptedException, ExecutionException {
        db.drop().get();
        arangoDB.shutdown();
    }

}
