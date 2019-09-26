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

package com.arangodb.async;

import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Mark Vollmary
 */
public class CommunicationTest {

    @Test
    @Ignore
    public void disconnect() {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().build();
        final CompletableFuture<ArangoCursorAsync<Object>> result = arangoDB.db().query("return sleep(1)", null, null,
                null);
        arangoDB.shutdown();
        assertThat(result.isCompletedExceptionally(), is(true));
    }

}
