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

package com.arangodb.internal.velocypack;

import com.arangodb.internal.CollectionCache;
import com.arangodb.velocypack.VPackDeserializationContext;
import com.arangodb.velocypack.VPackDeserializer;
import com.arangodb.velocypack.VPackJsonDeserializer;
import com.arangodb.velocypack.VPackModule;
import com.arangodb.velocypack.VPackParser;
import com.arangodb.velocypack.VPackParserModule;
import com.arangodb.velocypack.VPackParserSetupContext;
import com.arangodb.velocypack.VPackSetupContext;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.ValueType;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocypack.internal.util.NumberUtil;

/**
 * @author Mark Vollmary
 *
 */
public class VPackDocumentModule implements VPackModule, VPackParserModule {

	private static final String ID = "_id";
	private final CollectionCache collectionCache;

	public VPackDocumentModule(final CollectionCache collectionCache) {
		super();
		this.collectionCache = collectionCache;
	}

	@Override
	public <C extends VPackSetupContext<C>> void setup(final C context) {
		context.registerDeserializer(ID, String.class, new VPackDeserializer<String>() {
			@Override
			public String deserialize(
				final VPackSlice parent,
				final VPackSlice vpack,
				final VPackDeserializationContext context) throws VPackException {
				final String id;
				if (vpack.isCustom()) {
					final long idLong = NumberUtil.toLong(vpack.getBuffer(), vpack.getStart() + 1,
						vpack.getByteSize() - 1);
					final String collectionName = collectionCache.getCollectionName(idLong);
					if (collectionName != null) {
						final VPackSlice key = parent.get("_key");
						id = String.format("%s/%s", collectionName, key.getAsString());
					} else {
						id = null;
					}
				} else {
					id = vpack.getAsString();
				}
				return id;
			}
		});

	}

	@Override
	public <C extends VPackParserSetupContext<C>> void setup(final C context) {
		context.registerDeserializer(ID, ValueType.CUSTOM, new VPackJsonDeserializer() {
			@Override
			public void deserialize(
				final VPackSlice parent,
				final String attribute,
				final VPackSlice vpack,
				final StringBuilder json) throws VPackException {
				final String id;
				final long idLong = NumberUtil.toLong(vpack.getBuffer(), vpack.getStart() + 1, vpack.getByteSize() - 1);
				final String collectionName = collectionCache.getCollectionName(idLong);
				if (collectionName != null) {
					final VPackSlice key = parent.get("_key");
					id = String.format("%s/%s", collectionName, key.getAsString());
				} else {
					id = null;
				}
				json.append(VPackParser.toJSONString(id));
			}
		});
	}

}
