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

import java.util.Collection;
import java.util.Optional;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class AqlParseEntity {

	public static class AstNode {
		private String type;
		private Collection<AstNode> subNodes;
		private String name;
		private Long id;
		private Object value;

		public String getType() {
			return type;
		}

		public Optional<Collection<AstNode>> getSubNodes() {
			return Optional.ofNullable(subNodes);
		}

		public Optional<String> getName() {
			return Optional.ofNullable(name);
		}

		public Optional<Long> getId() {
			return Optional.ofNullable(id);
		}

		public Optional<Object> getValue() {
			return Optional.ofNullable(value);
		}

	}

	private Collection<String> collections;
	private Collection<String> bindVars;
	private Collection<AstNode> ast;

	public Collection<String> getCollections() {
		return collections;
	}

	public Collection<String> getBindVars() {
		return bindVars;
	}

	public Collection<AstNode> getAst() {
		return ast;
	}

}
