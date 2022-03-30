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


import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.arangodb.DbName;
import com.arangodb.entity.BaseDocument;
import com.arangodb.mapping.ArangoJack;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.util.ArangoSerialization;
import com.arangodb.velocypack.VPackBuilder;
import com.arangodb.velocypack.VPackSlice;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import static com.arangodb.internal.util.ArangoSerializationFactory.Serializer.CUSTOM;
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
        ArangoJack arangoJack = new ArangoJack();
        arangoJack.configure((mapper) -> {
            mapper.configure(WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, true);
            mapper.configure(USE_BIG_INTEGER_FOR_INTS, true);
            SimpleModule module = new SimpleModule("PersonModule");
            module.addDeserializer(Person.class, new PersonDeserializer());
            mapper.registerModule(module);
        });
        arangoDB = new ArangoDB.Builder().serializer(arangoJack).build();

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
        ArangoSerialization serialization = arangoDB.util(CUSTOM);
        VPackSlice serializedPerson = serialization.serialize(person);
        Person deserializedPerson = serialization.deserialize(serializedPerson, Person.class);
        assertThat(deserializedPerson.name).isEqualTo(PERSON_DESERIALIZER_ADDED_PREFIX + PERSON_SERIALIZER_ADDED_PREFIX + person.name);
    }

    @Test
    void aqlSerialization() {
        String key = "test-" + UUID.randomUUID();

        BaseDocument doc = new BaseDocument(key);
        doc.addAttribute("arr", Collections.singletonList("hello"));
        doc.addAttribute("int", 10);

        HashMap<String, Object> params = new HashMap<>();
        params.put("doc", doc);
        params.put("@collection", COLLECTION_NAME);

        BaseDocument result = db.query(
                "INSERT @doc INTO @@collection RETURN NEW",
                params,
                BaseDocument.class
        ).next();

        assertThat(result.getAttribute("arr")).isInstanceOf(String.class);
        assertThat(result.getAttribute("arr")).isEqualTo("hello");
        assertThat(result.getAttribute("int")).isInstanceOf(BigInteger.class);
        assertThat(result.getAttribute("int")).isEqualTo(BigInteger.valueOf(10));
    }

    @Test
    void aqlDeserialization() {
        String key = "test-" + UUID.randomUUID();

        BaseDocument doc = new BaseDocument(key);
        doc.addAttribute("arr", Collections.singletonList("hello"));
        doc.addAttribute("int", 10);

        collection.insertDocument(doc, null);

        final BaseDocument result = db.query(
                "RETURN DOCUMENT(@docId)",
                Collections.singletonMap("docId", COLLECTION_NAME + "/" + key),
                BaseDocument.class
        ).next();

        assertThat(result.getAttribute("arr")).isInstanceOf(String.class);
        assertThat(result.getAttribute("arr")).isEqualTo("hello");
        assertThat(result.getAttribute("int")).isInstanceOf(BigInteger.class);
        assertThat(result.getAttribute("int")).isEqualTo(BigInteger.valueOf(10));
    }

    @Test
    void insertDocument() {
        String key = "test-" + UUID.randomUUID();

        BaseDocument doc = new BaseDocument(key);
        doc.addAttribute("arr", Collections.singletonList("hello"));
        doc.addAttribute("int", 10);

        BaseDocument result = collection.insertDocument(
                doc,
                new DocumentCreateOptions().returnNew(true)
        ).getNew();

        assertThat(result.getAttribute("arr")).isInstanceOf(String.class);
        assertThat(result.getAttribute("arr")).isEqualTo("hello");
        assertThat(result.getAttribute("int")).isInstanceOf(BigInteger.class);
        assertThat(result.getAttribute("int")).isEqualTo(BigInteger.valueOf(10));
    }

    @Test
    void getDocument() {
        String key = "test-" + UUID.randomUUID();

        BaseDocument doc = new BaseDocument(key);
        doc.addAttribute("arr", Collections.singletonList("hello"));
        doc.addAttribute("int", 10);

        collection.insertDocument(doc, null);

        final BaseDocument result = db.collection(COLLECTION_NAME).getDocument(
                key,
                BaseDocument.class,
                null);

        assertThat(result.getAttribute("arr")).isInstanceOf(String.class);
        assertThat(result.getAttribute("arr")).isEqualTo("hello");
        assertThat(result.getAttribute("int")).isInstanceOf(BigInteger.class);
        assertThat(result.getAttribute("int")).isEqualTo(BigInteger.valueOf(10));
    }

    @Test
    void parseNullString() {
        final String json = arangoDB.util(CUSTOM).deserialize(new VPackBuilder().add((String) null).slice(), String.class);
        assertThat(json).isNull();
    }

}
