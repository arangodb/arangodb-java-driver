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

package com.arangodb.serde;


import com.arangodb.*;
import com.arangodb.model.DocumentCreateOptions;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_INTEGER_FOR_INTS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED;
import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Michele Rastelli
 */
class CustomSerdeTest {

    private static final String COLLECTION_NAME = "collection";
    private static final String PERSON_SERIALIZER_ADDED_PREFIX = "MyNameIs";
    private static final String PERSON_DESERIALIZER_ADDED_PREFIX = "Hello";

    private static ArangoDB arangoDB;
    private static ArangoDatabase db;
    private static ArangoCollection collection;

    static class PersonSerializer extends JsonSerializer<Person> {
        @Override
        public void serialize(Person value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeFieldName("name");
            gen.writeString(PERSON_SERIALIZER_ADDED_PREFIX + value.name);
            gen.writeEndObject();
        }
    }

    static class PersonDeserializer extends JsonDeserializer<Person> {
        @Override
        public Person deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
            Person person = new Person();
            JsonNode rootNode = parser.getCodec().readTree(parser);
            JsonNode nameNode = rootNode.get("name");
            if (nameNode != null && nameNode.isTextual()) {
                person.name = PERSON_DESERIALIZER_ADDED_PREFIX + nameNode.asText();
            }
            return person;
        }
    }

    @JsonSerialize(using = PersonSerializer.class)
    static class Person {
        String name;
    }

    @BeforeAll
    static void init() {
        JacksonSerde serde = JacksonSerde.of(DataType.VPACK);
        serde.configure((mapper) -> {
            mapper.configure(WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, true);
            mapper.configure(USE_BIG_INTEGER_FOR_INTS, true);
            SimpleModule module = new SimpleModule("PersonModule");
            module.addDeserializer(Person.class, new PersonDeserializer());
            mapper.registerModule(module);
        });
        arangoDB = new ArangoDB.Builder()
                .useProtocol(Protocol.VST)
                .serializer(serde).build();

        db = arangoDB.db(DbName.of("custom-serde-test"));
        if (!db.exists()) {
            db.create();
        }

        collection = db.collection(COLLECTION_NAME);
        if (!collection.exists()) {
            collection.create();
        }
    }

    @AfterAll
    static void shutdown() {
        if (db.exists())
            db.drop();
    }

    @Test
    void customPersonDeserializer() {
        Person person = new Person();
        person.name = "Joe";
        Person result = collection.insertDocument(
                person,
                new DocumentCreateOptions().returnNew(true)
        ).getNew();
        assertThat(result.name).isEqualTo(PERSON_DESERIALIZER_ADDED_PREFIX + PERSON_SERIALIZER_ADDED_PREFIX + person.name);
    }

    @Test
    void manualCustomPersonDeserializer() {
        Person person = new Person();
        person.name = "Joe";
        ArangoSerde serialization = arangoDB.getUserSerde();
        byte[] serialized = serialization.serialize(person);
        Person deserializedPerson = serialization.deserialize(serialized, Person.class);
        assertThat(deserializedPerson.name).isEqualTo(PERSON_DESERIALIZER_ADDED_PREFIX + PERSON_SERIALIZER_ADDED_PREFIX + person.name);
    }

    @Test
    void aqlSerialization() {
        String key = "test-" + UUID.randomUUID();

        Map<String, Object> doc = new HashMap<>();
        doc.put("_key", key);
        doc.put("arr", Collections.singletonList("hello"));
        doc.put("int", 10);

        HashMap<String, Object> params = new HashMap<>();
        params.put("doc", doc);
        params.put("@collection", COLLECTION_NAME);

        Map<String, Object> result = db.query(
                "INSERT @doc INTO @@collection RETURN NEW",
                params,
                Map.class
        ).next();

        assertThat(result.get("arr")).isInstanceOf(String.class);
        assertThat(result.get("arr")).isEqualTo("hello");
        assertThat(result.get("int")).isInstanceOf(BigInteger.class);
        assertThat(result.get("int")).isEqualTo(BigInteger.valueOf(10));
    }

    @Test
    void aqlDeserialization() {
        String key = "test-" + UUID.randomUUID();

        Map<String, Object> doc = new HashMap<>();
        doc.put("_key", key);
        doc.put("arr", Collections.singletonList("hello"));
        doc.put("int", 10);

        collection.insertDocument(doc, null);

        final Map<String, Object> result = db.query(
                "RETURN DOCUMENT(@docId)",
                Collections.singletonMap("docId", COLLECTION_NAME + "/" + key),
                Map.class
        ).next();

        assertThat(result.get("arr")).isInstanceOf(String.class);
        assertThat(result.get("arr")).isEqualTo("hello");
        assertThat(result.get("int")).isInstanceOf(BigInteger.class);
        assertThat(result.get("int")).isEqualTo(BigInteger.valueOf(10));
    }

    @Test
    void insertDocument() {
        String key = "test-" + UUID.randomUUID();

        Map<String, Object> doc = new HashMap<>();
        doc.put("_key", key);
        doc.put("arr", Collections.singletonList("hello"));
        doc.put("int", 10);

        Map<String, Object> result = collection.insertDocument(
                doc,
                new DocumentCreateOptions().returnNew(true)
        ).getNew();

        assertThat(result.get("arr")).isInstanceOf(String.class);
        assertThat(result.get("arr")).isEqualTo("hello");
        assertThat(result.get("int")).isInstanceOf(BigInteger.class);
        assertThat(result.get("int")).isEqualTo(BigInteger.valueOf(10));
    }

    @Test
    void getDocument() {
        String key = "test-" + UUID.randomUUID();

        Map<String, Object> doc = new HashMap<>();
        doc.put("_key", key);
        doc.put("arr", Collections.singletonList("hello"));
        doc.put("int", 10);

        collection.insertDocument(doc, null);

        final Map<String, Object> result = db.collection(COLLECTION_NAME).getDocument(
                key,
                Map.class,
                null);

        assertThat(result.get("arr")).isInstanceOf(String.class);
        assertThat(result.get("arr")).isEqualTo("hello");
        assertThat(result.get("int")).isInstanceOf(BigInteger.class);
        assertThat(result.get("int")).isEqualTo(BigInteger.valueOf(10));
    }

    @Test
    void parseNullString() {
        final String json = arangoDB.getUserSerde().deserialize(arangoDB.getUserSerde().serialize(null), String.class);
        assertThat(json).isNull();
    }

}
