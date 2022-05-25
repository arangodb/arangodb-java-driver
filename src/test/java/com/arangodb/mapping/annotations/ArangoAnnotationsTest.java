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

package com.arangodb.mapping.annotations;

import com.arangodb.mapping.ArangoJack;
import com.arangodb.velocypack.VPackSlice;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michele Rastelli
 */
class ArangoAnnotationsTest {

    private final ArangoJack mapper = new ArangoJack();

    @Test
    void documentField() {
        DocumentFieldEntity e = new DocumentFieldEntity();
        e.setId("Id");
        e.setKey("Key");
        e.setRev("Rev");
        e.setFrom("From");
        e.setTo("To");

        VPackSlice slice = mapper.serialize(e);
        System.out.println(slice);
        Map<String, String> deserialized = mapper.deserialize(slice, Object.class);
        assertThat(deserialized)
                .containsEntry("_id", e.getId())
                .containsEntry("_key", e.getKey())
                .containsEntry("_rev", e.getRev())
                .containsEntry("_from", e.getFrom())
                .containsEntry("_to", e.getTo())
                .hasSize(5);

        DocumentFieldEntity deserializedEntity = mapper.deserialize(slice, DocumentFieldEntity.class);
        assertThat(deserializedEntity).isEqualTo(e);
    }

    @Test
    void documentFieldAnnotations() {
        AnnotatedEntity e = new AnnotatedEntity();
        e.setId("Id");
        e.setKey("Key");
        e.setRev("Rev");
        e.setFrom("From");
        e.setTo("To");

        VPackSlice slice = mapper.serialize(e);
        System.out.println(slice);
        Map<String, String> deserialized = mapper.deserialize(slice, Object.class);
        assertThat(deserialized)
                .containsEntry("_id", e.getId())
                .containsEntry("_key", e.getKey())
                .containsEntry("_rev", e.getRev())
                .containsEntry("_from", e.getFrom())
                .containsEntry("_to", e.getTo())
                .hasSize(5);

        AnnotatedEntity deserializedEntity = mapper.deserialize(slice, AnnotatedEntity.class);
        assertThat(deserializedEntity).isEqualTo(e);
    }

    @Test
    void serializedName() {
        SerializedNameEntity e = new SerializedNameEntity();
        e.setA("A");
        e.setB("B");
        e.setC("C");

        VPackSlice slice = mapper.serialize(e);
        System.out.println(slice);
        Map<String, String> deserialized = mapper.deserialize(slice, Object.class);
        assertThat(deserialized)
                .containsEntry(SerializedNameEntity.SERIALIZED_NAME_A, e.getA())
                .containsEntry(SerializedNameEntity.SERIALIZED_NAME_B, e.getB())
                .containsEntry(SerializedNameEntity.SERIALIZED_NAME_C, e.getC())
                .hasSize(3);

        SerializedNameEntity deserializedEntity = mapper.deserialize(slice, SerializedNameEntity.class);
        assertThat(deserializedEntity).isEqualTo(e);
    }

    @Test
    void serializedNameParameter() {
        Map<String, String> e = new HashMap<>();
        e.put(SerializedNameParameterEntity.SERIALIZED_NAME_A, "A");
        e.put(SerializedNameParameterEntity.SERIALIZED_NAME_B, "B");
        e.put(SerializedNameParameterEntity.SERIALIZED_NAME_C, "C");

        VPackSlice slice = mapper.serialize(e);
        SerializedNameParameterEntity deserializedEntity = mapper
                .deserialize(slice, SerializedNameParameterEntity.class);
        assertThat(deserializedEntity).isEqualTo(new SerializedNameParameterEntity("A", "B", "C"));
    }

    @Test
    void expose() {
        ExposeEntity e = new ExposeEntity();
        e.setReadWrite("readWrite");
        e.setReadOnly("readOnly");
        e.setWriteOnly("writeOnly");
        e.setIgnored("ignored");

        VPackSlice serializedEntity = mapper.serialize(e);
        Map<String, String> deserializedEntity = mapper.deserialize(serializedEntity, Object.class);
        assertThat(deserializedEntity)
                .containsEntry("readWrite", "readWrite")
                .containsEntry("readOnly", "readOnly")
                .hasSize(2);

        Map<String, String> map = new HashMap<>();
        map.put("readWrite", "readWrite");
        map.put("readOnly", "readOnly");
        map.put("writeOnly", "writeOnly");
        map.put("ignored", "ignored");

        VPackSlice serializedMap = mapper.serialize(map);
        ExposeEntity deserializedMap = mapper.deserialize(serializedMap, ExposeEntity.class);
        assertThat(deserializedMap.getIgnored()).isNull();
        assertThat(deserializedMap.getReadOnly()).isNull();
        assertThat(deserializedMap.getWriteOnly()).isEqualTo("writeOnly");
        assertThat(deserializedMap.getReadWrite()).isEqualTo("readWrite");
    }

}
