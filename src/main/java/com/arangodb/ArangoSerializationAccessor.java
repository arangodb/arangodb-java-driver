/*
 * DISCLAIMER
 *
 * Copyright 2018 ArangoDB GmbH, Cologne, Germany
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

import com.arangodb.internal.util.ArangoSerializationFactory.Serializer;
import com.arangodb.util.ArangoSerialization;
import com.arangodb.velocypack.VPackSlice;

/**
 * @author Mark Vollmary
 */
public interface ArangoSerializationAccessor {

    /**
     * Returns driver internal serialization API for serializing and deserializing Java POJOs to/from {@link VPackSlice}
     *
     * @return ArangoSerialization
     */
    ArangoSerialization util();

    /**
     * Returns serialization API for serializing and deserializing Java POJOs to/from {@link VPackSlice} by the given
     * type
     *
     * @param serializer type of serializer
     * @return ArangoSerialization
     */
    ArangoSerialization util(Serializer serializer);

}
