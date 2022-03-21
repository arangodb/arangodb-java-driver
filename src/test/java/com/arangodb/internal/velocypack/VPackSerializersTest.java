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

import com.arangodb.entity.ViewType;
import com.arangodb.entity.arangosearch.ArangoSearchCompression;
import com.arangodb.entity.arangosearch.ArangoSearchProperties;
import com.arangodb.entity.arangosearch.StoredValue;
import com.arangodb.model.arangosearch.ArangoSearchCreateOptions;
import com.arangodb.velocypack.VPack;
import com.arangodb.velocypack.VPackSlice;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class VPackSerializersTest {

    private VPack vpack;

    @Before
    public void init() {
        vpack = new VPack.Builder()
                .registerSerializer(ArangoSearchProperties.class, VPackSerializers.ARANGO_SEARCH_PROPERTIES)
                .build();
    }

    @Test
    public void serializeArangoSearchProperties() {
        final ArangoSearchCreateOptions opts = new ArangoSearchCreateOptions()
                .storedValues(new StoredValue(Collections.singletonList("dummy"), ArangoSearchCompression.lz4));

        final VPackSlice slice = vpack.serialize(opts);

        assertThat(slice.isObject(), is(true));
        assertThat(slice.get("type").isString(), is(true));
        assertThat(slice.get("type").getAsString(), is(ViewType.ARANGO_SEARCH.name()));
        assertThat(slice.get("storedValues"), notNullValue());
        assertThat(slice.get("storedValues").isArray(), is(true));
        assertThat(slice.get("storedValues").size(), is(1));
        assertThat(slice.get("storedValues").get(0).isObject(), is(true));
        assertThat(slice.get("storedValues").get(0).get("fields").isArray(), is(true));
        assertThat(slice.get("storedValues").get(0).get("fields").size(), is(1));
        assertThat(slice.get("storedValues").get(0).get("fields").get(0).isString(), is(true));
        assertThat(slice.get("storedValues").get(0).get("fields").get(0).getAsString(), is("dummy"));
        assertThat(slice.get("storedValues").get(0).get("compression").isString(), is(true));
        assertThat(slice.get("storedValues").get(0).get("compression").getAsString(), is(ArangoSearchCompression.lz4.name()));
    }

    @Test
    public void serializeArangoSearchPropertiesWithDefaultCompression() {
        final ArangoSearchCreateOptions opts = new ArangoSearchCreateOptions()
                .storedValues(new StoredValue(Collections.singletonList("dummy")));

        final VPackSlice slice = vpack.serialize(opts);

        assertThat(slice.get("storedValues").get(0).get("compression").isNone(), is(true));
    }
}
