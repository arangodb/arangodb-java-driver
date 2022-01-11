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
import java.util.stream.Collectors;

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

    private static String hexToString(String hex) {
        final StringBuilder result = new StringBuilder();
        for (int i = 0; i < hex.length() - 1; i += 2) {
            String tempInHex = hex.substring(i, (i + 2));
            int decimal = Integer.parseInt(tempInHex, 16);
            result.append((char) decimal);
        }
        return result.toString();
    }

    public StopwordsAnalyzerProperties() {
        stopwords = new ArrayList<>();
        hex = true;
    }

    private final List<String> stopwords;
    private final boolean hex;

    /**
     * @return list of hex-encoded strings that describe the tokens to be discarded.
     * @deprecated use {@link #getStopwordsAsHexList()} instead
     */
    @Deprecated
    public List<String> getStopwords() {
        return getStopwordsAsHexList();
    }

    /**
     * @return list of verbatim strings that describe the tokens to be discarded.
     */
    public List<String> getStopwordsAsStringList() {
        if (hex) {
            return stopwords.stream()
                    .map(StopwordsAnalyzerProperties::hexToString)
                    .collect(Collectors.toList());
        } else {
            return stopwords;
        }
    }

    /**
     * @return list of hex-encoded strings that describe the tokens to be discarded.
     */
    public List<String> getStopwordsAsHexList() {
        if (hex) {
            return stopwords;
        } else {
            return stopwords.stream()
                    .map(StopwordsAnalyzerProperties::stringToHex)
                    .collect(Collectors.toList());
        }
    }

    /**
     * @return if false each string in {@link #stopwords} is used as verbatim, if true as hex-encoded.
     */
    public boolean getHex() {
        return hex;
    }

    /**
     * @param value stopword as verbatim string
     * @return this
     */
    public StopwordsAnalyzerProperties addStopwordAsString(final String value) {
        if (hex) {
            stopwords.add(stringToHex(value));
        } else {
            stopwords.add(value);
        }
        return this;
    }

    /**
     * @param value stopword as hex string
     * @return this
     */
    public StopwordsAnalyzerProperties addStopwordAsHex(final String value) {
        if (hex) {
            stopwords.add(value);
        } else {
            stopwords.add(hexToString(value));
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StopwordsAnalyzerProperties that = (StopwordsAnalyzerProperties) o;
        return hex == that.hex && Objects.equals(stopwords, that.stopwords);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stopwords, hex);
    }
}
