/*
 * Copyright (C) 2012,2013 tamtam180
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arangodb;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.entity.BooleanResultEntity;
import com.arangodb.entity.DatabaseEntity;
import com.arangodb.entity.StringsResultEntity;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * 
 */
public class ArangoDriverDatabaseTest extends BaseTest {

    public ArangoDriverDatabaseTest(ArangoConfigure configure, ArangoDriver driver) {
        super(configure, driver);
    }

    @Before
    public void before() {

    }

    @Test
    public void test_invalid_dbname1() throws ArangoException {
        try {
            driver.createDatabase(null);
            fail();
        } catch (ArangoException e) {
            assertThat(e.getMessage(), is("invalid format database:null"));
        }
    }

    @Test
    public void test_invalid_dbname2() throws ArangoException {
        try {
            driver.createDatabase("0");
            fail();
        } catch (ArangoException e) {
            assertThat(e.getMessage(), is("invalid format database:0"));
        }
    }

    @Test
    public void test_invalid_dbname3() throws ArangoException {
        try {
            driver.createDatabase("abcdefghi1abcdefghi2abcdefghi3abcdefghi4abcdefghi5abcdefghi612345"); // len=65
            fail();
        } catch (ArangoException e) {
            assertThat(e.getMessage(),
                is("invalid format database:abcdefghi1abcdefghi2abcdefghi3abcdefghi4abcdefghi5abcdefghi612345"));
        }
    }

    @Test
    public void test_invalid_dbname_for_delete() throws ArangoException {
        try {
            driver.deleteDatabase("abcdefghi1abcdefghi2abcdefghi3abcdefghi4abcdefghi5abcdefghi612345"); // len=65
            fail();
        } catch (ArangoException e) {
            assertThat(e.getMessage(),
                is("invalid format database:abcdefghi1abcdefghi2abcdefghi3abcdefghi4abcdefghi5abcdefghi612345"));
        }
    }

    @Test
    public void test_current_database() throws ArangoException {

        DatabaseEntity entity = driver.getCurrentDatabase();
        assertThat(entity.isError(), is(false));
        assertThat(entity.getCode(), is(200));
        assertThat(entity.getName(), is("_system"));
        assertThat(entity.getId(), is(notNullValue()));
        assertThat(entity.getPath(), is(notNullValue()));
        assertThat(entity.isSystem(), is(true));

    }

    @Test
    public void test_createDatabase() throws ArangoException {

        String database = "abcdefghi1abcdefghi2abcdefghi3abcdefghi4abcdefghi5abcdefghi61234";

        try {
            driver.deleteDatabase(database);
        } catch (ArangoException e) {
        }

        BooleanResultEntity entity = driver.createDatabase(database); // len=64
        assertThat(entity.getResult(), is(true));

    }

    @Test
    public void test_createDatabase_duplicate() throws ArangoException {

        String database = "abcdefghi1abcdefghi2abcdefghi3abcdefghi4abcdefghi5abcdefghi61234";

        try {
            driver.deleteDatabase(database);
        } catch (ArangoException e) {
        }

        BooleanResultEntity entity = driver.createDatabase(database); // len=64
        assertThat(entity.getResult(), is(true));

        try {
            driver.createDatabase(database);
            fail();
        } catch (ArangoException e) {
            assertThat(e.getCode(), is(409));
            assertThat(e.getErrorNumber(), is(1207));
        }

    }

    @Test
    public void test_delete() throws ArangoException {

        String database = "abcdefghi1abcdefghi2abcdefghi3abcdefghi4abcdefghi5abcdefghi61234";

        try {
            driver.deleteDatabase(database);
        } catch (ArangoException e) {
        }

        BooleanResultEntity entity = driver.createDatabase(database); // len=64
        assertThat(entity.getResult(), is(true));
        assertThat(entity.getCode(), is(201));
        assertThat(entity.isError(), is(false));

        entity = driver.deleteDatabase(database);
        assertThat(entity.getResult(), is(true));
        assertThat(entity.getCode(), is(200));
        assertThat(entity.isError(), is(false));

    }

    @Test
    public void test_delete_404() throws ArangoException {

        String database = "abcdefghi1abcdefghi2abcdefghi3abcdefghi4abcdefghi5abcdefghi61234";

        try {
            driver.deleteDatabase(database);
        } catch (ArangoException e) {
        }

        try {
            driver.deleteDatabase(database);
            fail();
        } catch (ArangoException e) {
            assertThat(e.getCode(), is(404));
            assertThat(e.getErrorNumber(), is(1228));
        }

    }

    @Test
    public void test_get_databases() throws ArangoException {

        String[] databases = new String[] { "db-1", "db_2", "db-_-3", "mydb", // other
                                                                              // testcase
                "mydb2", // other testcase
                "repl_scenario_test1", // other test case
                "unitTestDatabase", // other test case
        };

        for (String database : databases) {
            try {
                driver.deleteDatabase(database);
            } catch (ArangoException e) {
            }
            try {
                driver.createDatabase(database);
            } catch (ArangoException e) {
            }
        }

        StringsResultEntity entity = driver.getDatabases();
        assertThat(entity.isError(), is(false));
        assertThat(entity.getCode(), is(200));

        Collections.sort(entity.getResult());
        assertThat(entity.getResult(), is(Arrays.asList("_system", "db-1", "db-_-3", "db_2", "mydb", "mydb2",
            "repl_scenario_test1", "unitTestDatabase")));

    }

}
