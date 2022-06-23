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
import com.arangodb.DbName;
import com.arangodb.mapping.ArangoJack;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

/**
 * @author Mark Vollmary
 */
public class ExampleBase {

    private static final String DB_NAME = "json_example_db";
    protected static final String COLLECTION_NAME = "json_example_collection";

    private static ArangoDB arangoDB;
    protected static ArangoDatabase db;
    protected static ArangoCollection collection;

    @BeforeAll
    static void setUp() {
        arangoDB = new ArangoDB.Builder().serializer(new ArangoJack()).build();
        DbName dbName = DbName.of(DB_NAME);
        if (arangoDB.db(dbName).exists())
            arangoDB.db(dbName).drop();
        arangoDB.createDatabase(dbName);
        db = arangoDB.db(dbName);
        db.createCollection(COLLECTION_NAME);
        collection = db.collection(COLLECTION_NAME);
    }

    @AfterAll
    static void tearDown() {
        db.drop();
        arangoDB.shutdown();
    }

}
