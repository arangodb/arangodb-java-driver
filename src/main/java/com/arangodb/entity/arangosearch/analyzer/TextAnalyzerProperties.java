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

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public boolean isAccent() {
        return accent;
    }

    public void setAccent(boolean accent) {
        this.accent = accent;
    }

    public SearchAnalyzerCase getAnalyzerCase() {
        return analyzerCase;
    }

    public void setAnalyzerCase(SearchAnalyzerCase analyzerCase) {
        this.analyzerCase = analyzerCase;
    }

    public boolean isStemming() {
        return stemming;
    }

    public void setStemming(boolean stemming) {
        this.stemming = stemming;
    }

    public EdgeNgram getEdgeNgram() {
        return edgeNgram;
    }

    public void setEdgeNgram(EdgeNgram edgeNgram) {
        this.edgeNgram = edgeNgram;
    }

    public List<String> getStopwords() {
        return stopwords;
    }

    public void setStopwords(List<String> stopwords) {
        this.stopwords = stopwords;
    }

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
