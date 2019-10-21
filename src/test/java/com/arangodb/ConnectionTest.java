package com.arangodb;/*
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


import com.arangodb.entity.ArangoDBVersion;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Michele Rastelli
 */
@RunWith(Parameterized.class)
public class ConnectionTest {

    private final ArangoDB.Builder builder;

    @Parameterized.Parameters
    public static Collection<ArangoDB.Builder> builders() {
        return Arrays.asList(
                new ArangoDB.Builder().useProtocol(Protocol.VST),
                new ArangoDB.Builder().useProtocol(Protocol.HTTP_JSON)
        );
    }

    public ConnectionTest(ArangoDB.Builder builder) {
        this.builder = builder;
    }

    @Test
    public void timeoutTest() {
        ArangoDB arangoDB = builder.timeout(500).build();
        try {
            arangoDB.db().query("return sleep(1)", null, null, null);
            fail();
        } catch (ArangoDBException e) {
            assertThat(e.getCause(), instanceOf(TimeoutException.class));
        }
    }

    /**
     * FIXME: hosts are merged to the ones coming from arangodb.properties
     */
    @Test
    @Ignore
    public void notConnectedTest() {
        ArangoDB arangoDB = builder.host("nonExistingHost", 9999).build();
        try {
            ArangoDBVersion version = arangoDB.getVersion();
            System.out.println(version.getVersion());
            fail();
        } catch (ArangoDBException e) {
            assertThat(e.getMessage(), is("Cannot contact any host!"));
        }
    }


}
