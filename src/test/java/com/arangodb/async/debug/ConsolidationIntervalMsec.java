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

package com.arangodb.async.debug;

import com.arangodb.async.ArangoDBAsync;
import com.arangodb.async.ArangoDatabaseAsync;
import com.arangodb.async.BaseTest;
import com.arangodb.entity.ViewEntity;
import com.arangodb.entity.ViewType;
import com.arangodb.entity.arangosearch.ArangoSearchPropertiesEntity;
import com.arangodb.entity.arangosearch.CollectionLink;
import com.arangodb.entity.arangosearch.FieldLink;
import com.arangodb.model.arangosearch.ArangoSearchCreateOptions;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;

/**
 * @author Michele Rastelli
 * <p>
 * https://github.com/arangodb/arangodb-java-driver-async/issues/15
 */
public class ConsolidationIntervalMsec extends BaseTest {

    @Test
    public void consolidationIntervalMsec() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 4));

        ArangoDBAsync arango = new ArangoDBAsync.Builder()
                .user("root")
                .password("test")
                .build();

        ArangoDatabaseAsync db = arango.db("database_of_things");
        if (db.exists().join()) {
            db.drop().join();
        }

        db.create().join();
        db.collection("Thing").create().join();

        ViewEntity result = db.createArangoSearch("ThingsSearchView", new ArangoSearchCreateOptions()
                .consolidationIntervalMsec(60000L) //<== This line breaks it
                .link(CollectionLink.on("Thing")
                        .fields(FieldLink.on("name")
                                .analyzers("identity"))))
                .join();

        assertThat(result.getName(), is("ThingsSearchView"));
        assertThat(result.getType(), is(ViewType.ARANGO_SEARCH));

        ArangoSearchPropertiesEntity props = db.arangoSearch("ThingsSearchView").getProperties().join();
        assertThat(props.getName(), is("ThingsSearchView"));
        assertThat(props.getConsolidationIntervalMsec(), is(60000L));
        assertThat(props.getLinks().iterator().hasNext(), is(true));
        assertThat(props.getLinks().iterator().next().getName(), is("Thing"));
    }

}
