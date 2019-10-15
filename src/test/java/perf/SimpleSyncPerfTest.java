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

package perf;

import com.arangodb.ArangoDB;
import com.arangodb.Protocol;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Michele Rastelli
 */
@Ignore
@RunWith(Parameterized.class)
public class SimpleSyncPerfTest {
    private static final int REPETITIONS = 50_000;
    private final ArangoDB arangoDB;

    @Parameterized.Parameters
    public static Collection<ArangoDB> builders() {
        return Arrays.asList(
                new ArangoDB.Builder().useProtocol(Protocol.HTTP_VPACK).build(),
                new ArangoDB.Builder().useProtocol(Protocol.HTTP_JSON).build(),
                new ArangoDB.Builder().useProtocol(Protocol.VST).build()
        );
    }

    public SimpleSyncPerfTest(final ArangoDB arangoDB) {
        this.arangoDB = arangoDB;
    }

    @Before
    public void warmup() {
        getVersion();
    }

    @Test
    public void getVersion() {
        for (int i = 0; i < REPETITIONS; i++) {
            arangoDB.getVersion();
        }
    }
}
