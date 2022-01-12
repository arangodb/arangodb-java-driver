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


import java.util.Objects;

/**
 * @author Michele Rastelli
 * @since ArangoDB 3.9
 */
public class CollationAnalyzerProperties {

    private String locale;

    /**
     * @return a locale in the format `language[_COUNTRY][.encoding][@variant]` (square brackets denote optional parts),
     * e.g. `de.utf-8` or `en_US.utf-8`. Only UTF-8 encoding is meaningful in ArangoDB.
     * The locale is forwarded to ICU without checks. An invalid locale does not prevent the creation of the Analyzer.
     * @see <a href= "https://www.arangodb.com/docs/stable/arangosearch-analyzers.html#supported-languages">Supported Languages</a>
     */
    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CollationAnalyzerProperties that = (CollationAnalyzerProperties) o;
        return Objects.equals(locale, that.locale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locale);
    }
}
