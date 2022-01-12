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


import com.arangodb.velocypack.annotations.SerializedName;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Michele Rastelli
 */
public class TextAnalyzerProperties {

    public TextAnalyzerProperties() {
        stopwords = Collections.emptyList();
    }

    private String locale;

    private boolean accent;

    @SerializedName("case")
    private SearchAnalyzerCase analyzerCase;

    private boolean stemming;

    private EdgeNgram edgeNgram;

    private List<String> stopwords;

    private String stopwordsPath;

    /**
     * @return a locale in the format `language[_COUNTRY][.encoding][@variant]` (square brackets denote optional parts),
     * e.g. `de.utf-8` or `en_US.utf-8`. Only UTF-8 encoding is meaningful in ArangoDB.
     * @see <a href= "https://www.arangodb.com/docs/stable/arangosearch-analyzers.html#supported-languages">Supported Languages</a>
     */
    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * @return <code>true</code> to preserve accented characters (default)
     * <code>false</code> to convert accented characters to their base characters
     */
    public boolean isAccent() {
        return accent;
    }

    public void setAccent(boolean accent) {
        this.accent = accent;
    }

    public SearchAnalyzerCase getAnalyzerCase() {
        return analyzerCase;
    }

    /**
     * @param analyzerCase defaults to {@link SearchAnalyzerCase#lower}
     */
    public void setAnalyzerCase(SearchAnalyzerCase analyzerCase) {
        this.analyzerCase = analyzerCase;
    }

    /**
     * @return <code>true</code> to apply stemming on returned words (default)
     * <code>false</code> to leave the tokenized words as-is
     */
    public boolean isStemming() {
        return stemming;
    }

    public void setStemming(boolean stemming) {
        this.stemming = stemming;
    }

    /**
     * @return if present, then edge n-grams are generated for each token (word). That is, the start of the n-gram is
     * anchored to the beginning of the token, whereas the ngram Analyzer would produce all possible substrings from a
     * single input token (within the defined length restrictions). Edge n-grams can be used to cover word-based
     * auto-completion queries with an index, for which you should set the following other options:
     * - accent: false
     * - case: {@link SearchAnalyzerCase#lower}
     * - stemming: false
     */
    public EdgeNgram getEdgeNgram() {
        return edgeNgram;
    }

    public void setEdgeNgram(EdgeNgram edgeNgram) {
        this.edgeNgram = edgeNgram;
    }

    /**
     * @return an array of strings with words to omit from result. Default: load words from stopwordsPath. To disable
     * stop-word filtering provide an empty array []. If both stopwords and stopwordsPath are provided then both word
     * sources are combined.
     */
    public List<String> getStopwords() {
        return stopwords;
    }

    public void setStopwords(List<String> stopwords) {
        this.stopwords = stopwords;
    }

    /**
     * @return path with a language sub-directory (e.g. en for a locale en_US.utf-8) containing files with words to omit.
     * Each word has to be on a separate line. Everything after the first whitespace character on a line will be ignored
     * and can be used for comments. The files can be named arbitrarily and have any file extension (or none).
     * <p>
     * Default: if no path is provided then the value of the environment variable IRESEARCH_TEXT_STOPWORD_PATH is used
     * to determine the path, or if it is undefined then the current working directory is assumed. If the stopwords
     * attribute is provided then no stop-words are loaded from files, unless an explicit stopwordsPath is also provided.
     * <p>
     * Note that if the stopwordsPath can not be accessed, is missing language sub-directories or has no files for a
     * language required by an Analyzer, then the creation of a new Analyzer is refused. If such an issue is discovered
     * for an existing Analyzer during startup then the server will abort with a fatal error.
     */
    public String getStopwordsPath() {
        return stopwordsPath;
    }

    public void setStopwordsPath(String stopwordsPath) {
        this.stopwordsPath = stopwordsPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TextAnalyzerProperties that = (TextAnalyzerProperties) o;
        return accent == that.accent &&
                stemming == that.stemming &&
                Objects.equals(locale, that.locale) &&
                analyzerCase == that.analyzerCase &&
                Objects.equals(edgeNgram, that.edgeNgram) &&
                Objects.equals(stopwords, that.stopwords) &&
                Objects.equals(stopwordsPath, that.stopwordsPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locale, accent, analyzerCase, stemming, edgeNgram, stopwords, stopwordsPath);
    }
}
