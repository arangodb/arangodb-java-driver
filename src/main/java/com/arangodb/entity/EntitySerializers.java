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

package com.arangodb.entity;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class EntitySerializers {

	public static class BaseDocumentSerializer implements JsonSerializer<BaseDocument> {

		@Override
		public JsonElement serialize(
			final BaseDocument src,
			final Type typeOfSrc,
			final JsonSerializationContext context) {
			final JsonObject result = (JsonObject) context.serialize(src.getProperties());
			if (src.getDocumentKey() != null) {
				result.add("_key", context.serialize(src.getDocumentKey()));
			}
			if (src.getDocumentHandle() != null) {
				result.add("_id", context.serialize(src.getDocumentHandle()));
			}
			return result;
		}

	}

}
