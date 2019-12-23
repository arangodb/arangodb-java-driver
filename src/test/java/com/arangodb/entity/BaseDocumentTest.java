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

package com.arangodb.entity;

import com.arangodb.internal.velocypack.VPackDriverModule;
import com.arangodb.velocypack.VPack;
import com.arangodb.velocypack.VPack.Builder;
import com.arangodb.velocypack.VPackBuilder;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.ValueType;
import com.arangodb.velocypack.exception.VPackException;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Mark Vollmary
 */
public class BaseDocumentTest {

    @Test
    public void serialize() throws VPackException {
        final BaseDocument entity = new BaseDocument();
        entity.setKey("test");
        entity.setRevision("test");
        entity.addAttribute("a", "a");

        final Builder builder = new VPack.Builder();
        builder.registerModule(new VPackDriverModule());
        final VPack vpacker = builder.build();

        final VPackSlice vpack = vpacker.serialize(entity);
        assertThat(vpack, is(notNullValue()));
        assertThat(vpack.isObject(), is(true));
        assertThat(vpack.size(), is(3));

        final VPackSlice key = vpack.get("_key");
        assertThat(key.isString(), is(true));
        assertThat(key.getAsString(), is("test"));

        final VPackSlice rev = vpack.get("_rev");
        assertThat(rev.isString(), is(true));
        assertThat(rev.getAsString(), is("test"));

        final VPackSlice a = vpack.get("a");
        assertThat(a.isString(), is(true));
        assertThat(a.getAsString(), is("a"));
    }

    @Test
    public void deserialize() throws VPackException {
        final VPackBuilder builder = new VPackBuilder();
        builder.add(ValueType.OBJECT);
        builder.add("_id", "test/test");
        builder.add("_key", "test");
        builder.add("_rev", "test");
        builder.add("a", "a");
        builder.close();

        final VPack.Builder vbuilder = new VPack.Builder();
        vbuilder.registerModule(new VPackDriverModule());
        final VPack vpacker = vbuilder.build();

        final BaseDocument entity = vpacker.deserialize(builder.slice(), BaseDocument.class);
        assertThat(entity.getId(), is(notNullValue()));
        assertThat(entity.getId(), is("test/test"));
        assertThat(entity.getKey(), is(notNullValue()));
        assertThat(entity.getKey(), is("test"));
        assertThat(entity.getRevision(), is(notNullValue()));
        assertThat(entity.getRevision(), is("test"));
        assertThat(entity.getProperties().size(), is(1));
        assertThat(String.valueOf(entity.getAttribute("a")), is("a"));
    }

}
