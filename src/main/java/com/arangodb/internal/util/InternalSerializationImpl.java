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

package com.arangodb.internal.util;

import com.arangodb.ArangoDBException;
import com.arangodb.serde.InternalSerde;
import com.arangodb.util.InternalSerialization;
import com.fasterxml.jackson.databind.JsonNode;

import java.lang.reflect.Type;

/**
 * @author Mark Vollmary
 */
public class InternalSerializationImpl implements InternalSerialization {

    private final InternalSerde serde;

    public InternalSerializationImpl(final InternalSerde serde) {
        super();
        this.serde = serde;
    }

    @Override
    public byte[] serialize(final Object entity) throws ArangoDBException {
        return serde.serialize(entity);
    }

    @Override
    public String toJsonString(byte[] content) {
        return serde.toJsonString(content);
    }

    @Override
    public JsonNode parse(byte[] content) {
        return serde.parse(content);
    }

    @Override
    public <T> T deserialize(byte[] content, Type type) {
        return serde.deserialize(content, type);
    }

    @Override
    public <T> T deserialize(JsonNode node, Type type) {
        return serde.deserialize(node, type);
    }

    @Override
    public JsonNode parse(byte[] content, String jsonPointer) {
        return serde.parse(content, jsonPointer);
    }

    @Override
    public <T> T deserialize(byte[] content, String jsonPointer, Type type) {
        return serde.deserialize(content, jsonPointer, type);
    }

    @Override
    public byte[] extract(byte[] content, String jsonPointer) {
        return serde.extract(content, jsonPointer);
    }

}
