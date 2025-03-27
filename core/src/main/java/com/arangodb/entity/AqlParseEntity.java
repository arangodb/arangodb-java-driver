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
import java.util.Objects;

/**
 * @author Mark Vollmary
 */
public final class AqlParseEntity {

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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AqlParseEntity)) return false;
        AqlParseEntity that = (AqlParseEntity) o;
        return Objects.equals(collections, that.collections) && Objects.equals(bindVars, that.bindVars) && Objects.equals(ast, that.ast);
    }

    @Override
    public int hashCode() {
        return Objects.hash(collections, bindVars, ast);
    }

    public static final class AstNode {
        private String type;
        private Collection<AstNode> subNodes;
        private String name;
        private Long id;
        private Object value;

        public String getType() {
            return type;
        }

        public Collection<AstNode> getSubNodes() {
            return subNodes;
        }

        public String getName() {
            return name;
        }

        public Long getId() {
            return id;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof AstNode)) return false;
            AstNode astNode = (AstNode) o;
            return Objects.equals(type, astNode.type) && Objects.equals(subNodes, astNode.subNodes) && Objects.equals(name, astNode.name) && Objects.equals(id, astNode.id) && Objects.equals(value, astNode.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, subNodes, name, id, value);
        }
    }

}
