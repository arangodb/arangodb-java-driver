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

import com.arangodb.serde.ArangoSerde;
import com.arangodb.serde.DataType;
import com.arangodb.serde.JacksonSerde;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michele Rastelli
 */
class ArangoAnnotationsTest {

    @ParameterizedTest
    @EnumSource(DataType.class)
    void documentFieldAnnotations(DataType dataType) {
        ArangoSerde mapper = JacksonSerde.of(dataType);

        AnnotatedEntity e = new AnnotatedEntity();
        e.setId("Id");
        e.setKey("Key");
        e.setRev("Rev");
        e.setFrom("From");
        e.setTo("To");

        byte[] serialized = mapper.serialize(e);
        Map<String, String> deserialized = mapper.deserialize(serialized, Map.class);
        assertThat(deserialized)
                .containsEntry("_id", e.getId())
                .containsEntry("_key", e.getKey())
                .containsEntry("_rev", e.getRev())
                .containsEntry("_from", e.getFrom())
                .containsEntry("_to", e.getTo())
                .hasSize(5);

        AnnotatedEntity deserializedEntity = mapper.deserialize(serialized, AnnotatedEntity.class);
        assertThat(deserializedEntity).isEqualTo(e);
    }

}
