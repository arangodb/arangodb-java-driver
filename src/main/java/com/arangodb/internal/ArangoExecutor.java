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

package com.arangodb.internal;

import com.arangodb.entity.Entity;
import com.arangodb.internal.util.ArangoSerializationFactory;
import com.arangodb.internal.util.ArangoSerializationFactory.Serializer;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocystream.Response;

import java.lang.reflect.Type;

/**
 * @author Mark Vollmary
 */
public abstract class ArangoExecutor {

    @SuppressWarnings("unchecked")
    protected <T> T createResult(final Type type, final Response response) {
        if (type != Void.class && response.getBody() != null) {
            if (type instanceof Class && Entity.class.isAssignableFrom((Class) type)) {
                return (T) util.get(Serializer.INTERNAL).deserialize(response.getBody(), type);
            } else {
                return (T) util.get(Serializer.CUSTOM).deserialize(response.getBody(), type);
            }
        } else {
            return null;
        }
    }

    private final DocumentCache documentCache;
    private final ArangoSerializationFactory util;

    protected ArangoExecutor(final ArangoSerializationFactory util, final DocumentCache documentCache) {
        super();
        this.documentCache = documentCache;
        this.util = util;
    }

    public DocumentCache documentCache() {
        return documentCache;
    }

    public interface ResponseDeserializer<T> {
        T deserialize(Response response) throws VPackException;
    }

}
