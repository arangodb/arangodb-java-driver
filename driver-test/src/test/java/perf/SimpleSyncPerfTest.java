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
import com.arangodb.BaseJunit5;
import com.arangodb.Protocol;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Date;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author Michele Rastelli
 */
@Disabled
class SimpleSyncPerfTest {
    private static final int REPETITIONS = 50_000;

    private void doGetVersion(ArangoDB arangoDB) {
        for (int i = 0; i < REPETITIONS; i++) {
            arangoDB.getVersion();
        }
    }

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void getVersion(Protocol protocol) throws InterruptedException {
        assumeTrue(!protocol.equals(Protocol.VST) || BaseJunit5.isLessThanVersion(3, 12));

        ArangoDB arangoDB = new ArangoDB.Builder()
                .host("172.28.0.1", 8529)
                .password("test")
                .protocol(protocol)
                .build();
        // warmup
        doGetVersion(arangoDB);

        long start = new Date().getTime();
        doGetVersion(arangoDB);
        long end = new Date().getTime();
        System.out.println("elapsed ms: " + (end - start));
        Thread.sleep(5000);
    }
}
