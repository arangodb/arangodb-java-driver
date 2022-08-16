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

package com.arangodb.internal.velocypack;

import com.arangodb.entity.arangosearch.ArangoSearchCompression;
import com.arangodb.entity.arangosearch.StoredValue;
import com.arangodb.model.arangosearch.ArangoSearchCreateOptions;
import com.arangodb.velocypack.VPack;
import com.arangodb.velocypack.VPackSlice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class VPackSerializersTest {

    private VPack vpack;

    @BeforeEach
    void init() {
        vpack = new VPack.Builder()
                .registerModule(new VPackDriverModule())
                .build();
    }

    @Test
    void serializeArangoSearchProperties() {
        final ArangoSearchCreateOptions opts = new ArangoSearchCreateOptions()
                .storedValues(new StoredValue(Collections.singletonList("dummy"), ArangoSearchCompression.lz4));

        final VPackSlice slice = vpack.serialize(opts);

        assertThat(slice.isObject()).isTrue();
        assertThat(slice.get("type").isString()).isTrue();
        assertThat(slice.get("type").getAsString()).isEqualTo("arangosearch");
        assertThat(slice.get("storedValues")).isNotNull();
        assertThat(slice.get("storedValues").isArray()).isTrue();
        assertThat(slice.get("storedValues").size()).isEqualTo(1);
        assertThat(slice.get("storedValues").get(0).isObject()).isTrue();
        assertThat(slice.get("storedValues").get(0).get("fields").isArray()).isTrue();
        assertThat(slice.get("storedValues").get(0).get("fields").size()).isEqualTo(1);
        assertThat(slice.get("storedValues").get(0).get("fields").get(0).isString()).isTrue();
        assertThat(slice.get("storedValues").get(0).get("fields").get(0).getAsString()).isEqualTo("dummy");
        assertThat(slice.get("storedValues").get(0).get("compression").isString()).isTrue();
        assertThat(slice.get("storedValues").get(0).get("compression").getAsString()).isEqualTo(ArangoSearchCompression.lz4.name());
    }

    @Test
    void serializeArangoSearchPropertiesWithDefaultCompression() {
        final ArangoSearchCreateOptions opts = new ArangoSearchCreateOptions()
                .storedValues(new StoredValue(Collections.singletonList("dummy")));

        final VPackSlice slice = vpack.serialize(opts);

        assertThat(slice.get("storedValues").get(0).get("compression").isNone()).isTrue();
    }
}
