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
import com.arangodb.serde.DataType;
import com.arangodb.serde.InternalSerde;
import com.arangodb.util.ArangoDeserializer;
import com.arangodb.util.ArangoSerialization;
import com.arangodb.util.ArangoSerializer;
import com.arangodb.velocypack.VPackParser;
import com.arangodb.velocypack.VPackSlice;

import java.lang.reflect.Type;

/**
 * @author Mark Vollmary
 */
public class DefaultArangoSerialization implements ArangoSerialization {

    private final ArangoSerializer serializer;
    private final ArangoDeserializer deserializer;
    private final InternalSerde serde;

    public DefaultArangoSerialization(final ArangoSerializer serializer, final ArangoDeserializer deserializer, final InternalSerde serde) {
        super();
        this.serializer = serializer;
        this.deserializer = deserializer;
        this.serde = serde;
    }

    @Override
    public VPackSlice serialize(final Object entity) throws ArangoDBException {
        DataType dataType = serde.getDataType();
        switch (dataType) {
            case JSON:
                String json = new String(serde.serialize(entity));
                VPackParser parser = new VPackParser.Builder().build();
                return parser.fromJson(json, true);
            case VPACK:
                return new VPackSlice(serde.serialize(entity));
            default:
                throw new IllegalStateException("Unexpected value: " + dataType);
        }
    }

    @Override
    public VPackSlice serialize(final Object entity, final Options options) throws ArangoDBException {
        return serializer.serialize(entity, options);
    }

    @Override
    public <T> T deserialize(final VPackSlice vpack, final Type type) throws ArangoDBException {
        return deserializer.deserialize(vpack, type);
    }

}
