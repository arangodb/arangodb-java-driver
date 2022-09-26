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

import com.arangodb.DbName;
import com.arangodb.async.ArangoDBAsync;
import com.arangodb.async.ArangoDatabaseAsync;
import com.arangodb.async.BaseTest;
import com.arangodb.entity.ViewEntity;
import com.arangodb.entity.ViewType;
import com.arangodb.entity.arangosearch.ArangoSearchPropertiesEntity;
import com.arangodb.entity.arangosearch.CollectionLink;
import com.arangodb.entity.arangosearch.FieldLink;
import com.arangodb.mapping.ArangoJack;
import com.arangodb.model.arangosearch.ArangoSearchCreateOptions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author Michele Rastelli
 * <p>
 * https://github.com/arangodb/arangodb-java-driver-async/issues/15
 */
class ConsolidationIntervalMsecTest extends BaseTest {

    @Test
    void consolidationIntervalMsec() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 4));

        ArangoDBAsync arango = new ArangoDBAsync.Builder()
                .serializer(new ArangoJack())
                .user("root")
                .password("test")
                .build();

        ArangoDatabaseAsync db = arango.db(DbName.of("database_of_things"));
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

        assertThat(result.getName()).isEqualTo("ThingsSearchView");
        assertThat(result.getType()).isEqualTo(ViewType.ARANGO_SEARCH);

        ArangoSearchPropertiesEntity props = db.arangoSearch("ThingsSearchView").getProperties().join();
        assertThat(props.getName()).isEqualTo("ThingsSearchView");
        assertThat(props.getConsolidationIntervalMsec()).isEqualTo(60000L);
        assertThat(props.getLinks().iterator().hasNext()).isTrue();
        assertThat(props.getLinks().iterator().next().getName()).isEqualTo("Thing");
    }

}
