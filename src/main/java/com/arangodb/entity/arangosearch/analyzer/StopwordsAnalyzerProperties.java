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


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Michele Rastelli
 */
public class StopwordsAnalyzerProperties {

    private static String stringToHex(String str) {
        final StringBuilder hex = new StringBuilder();
        for (final char temp : str.toCharArray()) {
            hex.append(Integer.toHexString(temp));
        }
        return hex.toString();
    }

    public StopwordsAnalyzerProperties() {
        stopwords = new ArrayList<>();
    }

    private List<String> stopwords;

    /**
     * @return array of hex-encoded strings that describe the tokens to be discarded.
     */
    public List<String> getStopwords() {
        return stopwords;
    }

    public StopwordsAnalyzerProperties addStopwordAsString(final String value) {
        stopwords.add(stringToHex(value));
        return this;
    }

    public StopwordsAnalyzerProperties addStopwordAsHex(final String value) {
        stopwords.add(value);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StopwordsAnalyzerProperties that = (StopwordsAnalyzerProperties) o;
        return Objects.equals(stopwords, that.stopwords);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stopwords);
    }
}
