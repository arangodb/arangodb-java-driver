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
import com.arangodb.internal.serde.InternalSerde;
import com.arangodb.internal.serde.SerdeContextImpl;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.serde.jackson.JacksonSerde;
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
import java.util.concurrent.ExecutionException;

import static com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_INTEGER_FOR_INTS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED;
import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Michele Rastelli
 */
class CustomSerdeAsyncTest {

    private static final String COLLECTION_NAME = "collection";
    private static final String PERSON_SERIALIZER_ADDED_PREFIX = "MyNameIs";
    private static final String PERSON_DESERIALIZER_ADDED_PREFIX = "Hello";

    private static ArangoDBAsync arangoDB;
    private static ArangoDatabaseAsync db;
    private static ArangoCollectionAsync collection;

    @BeforeAll
    static void init() throws ExecutionException, InterruptedException {
        JacksonSerde serde = JacksonSerde.of(ContentType.JSON)
                .configure((mapper) -> {
                    mapper.configure(WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, true);
                    mapper.configure(USE_BIG_INTEGER_FOR_INTS, true);
                    SimpleModule module = new SimpleModule("PersonModule");
                    module.addDeserializer(Person.class, new PersonDeserializer());
                    mapper.registerModule(module);
                });
        arangoDB = new ArangoDB.Builder()
                .loadProperties(ConfigUtils.loadConfig())
                .serde(serde)
                .protocol(Protocol.HTTP_JSON)
                .build()
                .async();

        db = arangoDB.db("custom-serde-test");
        if (!db.exists().get()) {
            db.create().get();
        }

        collection = db.collection(COLLECTION_NAME);
        if (!collection.exists().get()) {
            collection.create().get();
        }
    }

    @AfterAll
    static void shutdown() throws ExecutionException, InterruptedException {
        if (db.exists().get())
            db.drop().get();
    }

    @Test
    void customPersonDeserializer() throws ExecutionException, InterruptedException {
        Person person = new Person();
        person.name = "Joe";
        Person result = collection.insertDocument(
                person,
                new DocumentCreateOptions().returnNew(true)
        ).get().getNew();
        assertThat(result.name).isEqualTo(PERSON_DESERIALIZER_ADDED_PREFIX + PERSON_SERIALIZER_ADDED_PREFIX + person.name);
    }

    @Test
    void manualCustomPersonDeserializer() {
        Person person = new Person();
        person.name = "Joe";
        InternalSerde serialization = arangoDB.getSerde();
        byte[] serialized = serialization.serializeUserData(person);
        Person deserializedPerson = serialization.deserializeUserData(serialized, Person.class, SerdeContextImpl.EMPTY);
        assertThat(deserializedPerson.name).isEqualTo(PERSON_DESERIALIZER_ADDED_PREFIX + PERSON_SERIALIZER_ADDED_PREFIX + person.name);
    }

    @Test
    void aqlSerialization() throws ExecutionException, InterruptedException {
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
                Map.class,
                params
        ).get().getResult().get(0);

        assertThat(result.get("arr")).isInstanceOf(String.class);
        assertThat(result.get("arr")).isEqualTo("hello");
        assertThat(result.get("int")).isInstanceOf(BigInteger.class);
        assertThat(result.get("int")).isEqualTo(BigInteger.valueOf(10));
    }

    @Test
    void aqlDeserialization() throws ExecutionException, InterruptedException {
        String key = "test-" + UUID.randomUUID();

        Map<String, Object> doc = new HashMap<>();
        doc.put("_key", key);
        doc.put("arr", Collections.singletonList("hello"));
        doc.put("int", 10);

        collection.insertDocument(doc).get();

        final Map<String, Object> result = db.query(
                "RETURN DOCUMENT(@docId)",
                Map.class,
                Collections.singletonMap("docId", COLLECTION_NAME + "/" + key)
        ).get().getResult().get(0);

        assertThat(result.get("arr")).isInstanceOf(String.class);
        assertThat(result.get("arr")).isEqualTo("hello");
        assertThat(result.get("int")).isInstanceOf(BigInteger.class);
        assertThat(result.get("int")).isEqualTo(BigInteger.valueOf(10));
    }

    @Test
    void insertDocument() throws ExecutionException, InterruptedException {
        String key = "test-" + UUID.randomUUID();

        Map<String, Object> doc = new HashMap<>();
        doc.put("_key", key);
        doc.put("arr", Collections.singletonList("hello"));
        doc.put("int", 10);

        Map<String, Object> result = collection.insertDocument(
                doc,
                new DocumentCreateOptions().returnNew(true)
        ).get().getNew();

        assertThat(result.get("arr")).isInstanceOf(String.class);
        assertThat(result.get("arr")).isEqualTo("hello");
        assertThat(result.get("int")).isInstanceOf(BigInteger.class);
        assertThat(result.get("int")).isEqualTo(BigInteger.valueOf(10));
    }

    @Test
    void getDocument() throws ExecutionException, InterruptedException {
        String key = "test-" + UUID.randomUUID();

        Map<String, Object> doc = new HashMap<>();
        doc.put("_key", key);
        doc.put("arr", Collections.singletonList("hello"));
        doc.put("int", 10);

        collection.insertDocument(doc).get();

        final Map<String, Object> result = db.collection(COLLECTION_NAME).getDocument(
                key,
                Map.class,
                null).get();

        assertThat(result.get("arr")).isInstanceOf(String.class);
        assertThat(result.get("arr")).isEqualTo("hello");
        assertThat(result.get("int")).isInstanceOf(BigInteger.class);
        assertThat(result.get("int")).isEqualTo(BigInteger.valueOf(10));
    }

    @Test
    void parseNullString() {
        final String json = arangoDB.getSerde().deserializeUserData(arangoDB.getSerde().serializeUserData(null),
                String.class, SerdeContextImpl.EMPTY);
        assertThat(json).isNull();
    }

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

}
