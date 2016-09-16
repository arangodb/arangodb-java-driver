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
public class VPackBuilderUnexpectedValueException extends VPackBuilderException {

	private static final long serialVersionUID = -7365305871886897353L;

	public VPackBuilderUnexpectedValueException(final ValueType type, final Class<?>... classes) {
		super(createMessage(type, null, classes));
	}

	public VPackBuilderUnexpectedValueException(final ValueType type, final String specify, final Class<?>... classes) {
		super(createMessage(type, specify, classes));
	}

	private static String createMessage(final ValueType type, final String specify, final Class<?>... classes) {
		final StringBuilder sb = new StringBuilder();
		sb.append("Must give ");
		if (specify != null) {
			sb.append(specify);
			sb.append(" ");
		}
		for (int i = 0; i < classes.length; i++) {
			if (i > 0) {
				sb.append(" or ");
			}
			sb.append(classes[i].getSimpleName());
		}
		sb.append(" for ");
		sb.append(type.getClass().getSimpleName());
		sb.append(".");
		sb.append(type.name());
		return sb.toString();
	}

}
