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

package com.arangodb.velocypack.exception;

import com.arangodb.velocypack.ValueType;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class VPackValueTypeException extends IllegalStateException {

	private static final long serialVersionUID = 8128171173539033177L;

	public VPackValueTypeException(final ValueType... types) {
		super(createMessage(types));
	}

	private static String createMessage(final ValueType... types) {
		final StringBuilder sb = new StringBuilder();
		sb.append("Expecting type ");
		for (int i = 0; i < types.length; i++) {
			if (i > 0) {
				sb.append(" or ");
			}
			sb.append(types[i].name());
		}
		return sb.toString();
	}

}
