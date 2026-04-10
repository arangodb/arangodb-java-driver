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
import com.arangodb.config.ConfigUtils;
import com.arangodb.internal.RequestContextHolder;
import com.arangodb.internal.serde.InternalSerde;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.serde.jackson.JacksonSerde;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.*;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * NB: excluded from shaded tests
 */
class CustomSerdeTest {

    private static final String COLLECTION_NAME = "collection";
    private static final String PERSON_SERIALIZER_ADDED_PREFIX = "MyNameIs";
    private static final String PERSON_DESERIALIZER_ADDED_PREFIX = "Hello";

    private static ArangoDB arangoDB;
    private static ArangoDatabase db;
    private static ArangoCollection collection;

    @BeforeAll
    static void init() {
        JacksonSerde serde = JacksonSerde.create(JsonMapper.builder()
                .configure(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, true)
                .configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, true)
                .addModule(new SimpleModule("PersonModule").addDeserializer(Person.class, new PersonDeserializer()))
                .build());

        arangoDB = new ArangoDB.Builder()
                .loadProperties(ConfigUtils.loadConfig())
                .protocol(Protocol.HTTP_1_1)
                .serde(serde).build();

        db = arangoDB.db("custom-serde-test");
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
        InternalSerde serialization = arangoDB.getSerde();
        byte[] serialized = serialization.serializeUserData(person);
        Person deserializedPerson = RequestContextHolder.INSTANCE.runWithCtx(RequestContext.EMPTY, () ->
                serialization.deserializeUserData(serialized, Person.class));
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

        @SuppressWarnings("unchecked")
        Map<String, Object> result = db.query(
                "INSERT @doc INTO @@collection RETURN NEW",
                Map.class,
                params
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

        collection.insertDocument(doc);

        @SuppressWarnings("unchecked") final Map<String, Object> result = db.query(
                "RETURN DOCUMENT(@docId)",
                Map.class,
                Collections.singletonMap("docId", COLLECTION_NAME + "/" + key)
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

        collection.insertDocument(doc);

        @SuppressWarnings("unchecked") final Map<String, Object> result = db.collection(COLLECTION_NAME).getDocument(
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
        final String json = RequestContextHolder.INSTANCE.runWithCtx(RequestContext.EMPTY, () ->
                arangoDB.getSerde().deserializeUserData(arangoDB.getSerde().serializeUserData(null), String.class));
        assertThat(json).isNull();
    }

    static class PersonSerializer extends ValueSerializer<Person> {
        @Override
        public void serialize(Person value, JsonGenerator gen, SerializationContext ctxt) {
            gen.writeStartObject();
            gen.writeName("name");
            gen.writeString(PERSON_SERIALIZER_ADDED_PREFIX + value.name);
            gen.writeEndObject();
        }
    }

    static class PersonDeserializer extends ValueDeserializer<Person> {
        @Override
        public Person deserialize(JsonParser parser, DeserializationContext ctxt) {
            Person person = new Person();
            JsonNode rootNode = ctxt.readTree(parser);
            JsonNode nameNode = rootNode.get("name");
            if (nameNode != null && nameNode.isString()) {
                person.name = PERSON_DESERIALIZER_ADDED_PREFIX + nameNode.asString();
            }
            return person;
        }
    }

    @JsonSerialize(using = PersonSerializer.class)
    static class Person {
        String name;
    }

}
