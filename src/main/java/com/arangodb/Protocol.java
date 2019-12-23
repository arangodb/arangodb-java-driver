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

package com.arangodb;

/**
 * @author Mark Vollmary
 */
public enum Protocol {
    /**
     * VelocyStream
     *
     * @see <a href="https://github.com/arangodb/velocystream">VelocyStream specification</a>
     */
    VST,
    /**
     * HTTP with JSON body
     */
    HTTP_JSON,
    /**
     * HTTP with VelocyPack body
     *
     * @see <a href="https://github.com/arangodb/velocypack">VelocyPack specification</a>
     */
    HTTP_VPACK
}
