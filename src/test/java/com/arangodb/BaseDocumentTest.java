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

package com.arangodb;

import com.arangodb.entity.BaseDocument;
import com.arangodb.internal.velocypack.VPackDriverModule;
import com.arangodb.velocypack.VPack;
import com.arangodb.velocypack.VPack.Builder;
import com.arangodb.velocypack.VPackBuilder;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.ValueType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
class BaseDocumentTest {

    @Test
    void serialize() {
        BaseDocument entity = new BaseDocument();
        entity.setKey("test");
        entity.setRevision("test");
        entity.addAttribute("a", "a");

        Builder builder = new VPack.Builder();
        builder.registerModule(new VPackDriverModule());
        VPack vpacker = builder.build();

        VPackSlice vpack = vpacker.serialize(entity);
        assertThat(vpack).isNotNull();
        assertThat(vpack.isObject()).isTrue();
        assertThat(vpack.size()).isEqualTo(3);

        VPackSlice key = vpack.get("_key");
        assertThat(key.isString()).isTrue();
        assertThat(key.getAsString()).isEqualTo("test");

        VPackSlice rev = vpack.get("_rev");
        assertThat(rev.isString()).isTrue();
        assertThat(rev.getAsString()).isEqualTo("test");

        VPackSlice a = vpack.get("a");
        assertThat(a.isString()).isTrue();
        assertThat(a.getAsString()).isEqualTo("a");
    }

    @Test
    void deserialize() {
        VPackBuilder builder = new VPackBuilder();
        builder.add(ValueType.OBJECT);
        builder.add("_id", "test/test");
        builder.add("_key", "test");
        builder.add("_rev", "test");
        builder.add("a", "a");
        builder.close();

        VPack.Builder vbuilder = new VPack.Builder();
        vbuilder.registerModule(new VPackDriverModule());
        VPack vpacker = vbuilder.build();

        BaseDocument entity = vpacker.deserialize(builder.slice(), BaseDocument.class);
        assertThat(entity.getId()).isNotNull();
        assertThat(entity.getId()).isEqualTo("test/test");
        assertThat(entity.getKey()).isNotNull();
        assertThat(entity.getKey()).isEqualTo("test");
        assertThat(entity.getRevision()).isNotNull();
        assertThat(entity.getRevision()).isEqualTo("test");
        assertThat(entity.getProperties()).hasSize(1);
        assertThat(String.valueOf(entity.getAttribute("a"))).isEqualTo("a");
    }

}
