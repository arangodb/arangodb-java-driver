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

package com.arangodb.entity.arangosearch.analyzer;


import java.util.*;

/**
 * @author Michele Rastelli
 * @since ArangoDB 3.12
 */
public final class MultiDelimiterAnalyzerProperties {

    private Collection<String> delimiters = Collections.emptyList();

    /**
     * @return a list of strings of which each is considered as one delimiter that can be one or multiple characters
     * long. The delimiters must not overlap, which means that a delimiter cannot be a prefix of another delimiter.
     */
    public Collection<String> getDelimiters() {
        return delimiters;
    }

    public void setDelimiters(String... delimiters) {
        this.delimiters = Arrays.asList(delimiters);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultiDelimiterAnalyzerProperties that = (MultiDelimiterAnalyzerProperties) o;
        return Objects.equals(delimiters, that.delimiters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(delimiters);
    }
}
