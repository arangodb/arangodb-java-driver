package com.arangodb.entity;

import java.util.Collection;
import java.util.Optional;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class AqlParseResult {

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
