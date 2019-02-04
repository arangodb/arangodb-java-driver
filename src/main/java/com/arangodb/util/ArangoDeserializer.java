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

package com.arangodb.util;

import java.lang.reflect.Type;

import com.arangodb.ArangoDBException;
import com.arangodb.velocypack.VPackSlice;

/**
 * @author Mark Vollmary
 *
 */
public interface ArangoDeserializer {

	/**
	 * Deserialize a given VelocyPack to an instance of a given type
	 * 
	 * @param vpack
	 *            The VelocyPack to deserialize
	 * @param type
	 *            The target type to deserialize to. Use String for raw JSON.
	 * @return The deserialized VelocyPack
	 * @throws ArangoDBException
	 */
	<T> T deserialize(final VPackSlice vpack, final Type type) throws ArangoDBException;

}
