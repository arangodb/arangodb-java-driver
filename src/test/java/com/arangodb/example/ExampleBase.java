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

package com.arangodb.example;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * @author Mark Vollmary
 */
public class ExampleBase {

    private static final String DB_NAME = "json_example_db";
    protected static final String COLLECTION_NAME = "json_example_collection";

    private static ArangoDB arangoDB;
    protected static ArangoDatabase db;
    protected static ArangoCollection collection;

    @BeforeClass
    public static void setUp() {
        arangoDB = new ArangoDB.Builder().build();
        if (arangoDB.db(DB_NAME).exists())
            arangoDB.db(DB_NAME).drop();
        arangoDB.createDatabase(DB_NAME);
        db = arangoDB.db(DB_NAME);
        db.createCollection(COLLECTION_NAME);
        collection = db.collection(COLLECTION_NAME);
    }

    @AfterClass
    public static void tearDown() {
        db.drop();
        arangoDB.shutdown();
    }

}
