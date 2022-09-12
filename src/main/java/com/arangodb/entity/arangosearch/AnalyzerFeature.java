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

package com.arangodb.entity.arangosearch;

/**
 * @author Michele Rastelli
 * @see <a href= "https://www.arangodb.com/docs/stable/arangosearch-analyzers.html#analyzer-features">API Documentation</a>
 */
public enum AnalyzerFeature {

    /**
     * how often a term is seen, required for PHRASE()
     */
    frequency,

    /**
     * the field normalization factor
     */
    norm,

    /**
     * sequentially increasing term position, required for PHRASE(). If present then the frequency feature is also required.
     */
    position,

    /**
     * enable search highlighting capabilities (Enterprise Edition only). If present, then the `position` and `frequency` features are also required.
     * @since ArangoDB 3.10
     */
    offset

}
