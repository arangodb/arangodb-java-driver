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

import java.util.Objects;

/**
 * @author Michele Rastelli
 */
public class NormAnalyzerProperties {

    /**
     * TODO: clarify: what are the supported locales? can this field be an enum?
     */
    private String locale;

    private boolean accent;

    @SerializedName("case")
    private SearchAnalyzerCase analyzerCase;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NormAnalyzerProperties that = (NormAnalyzerProperties) o;
        return accent == that.accent &&
                Objects.equals(locale, that.locale) &&
                analyzerCase == that.analyzerCase;
    }

    @Override
    public int hashCode() {
        return Objects.hash(locale, accent, analyzerCase);
    }
}
