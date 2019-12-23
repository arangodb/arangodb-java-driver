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

/**
 * @author Mark Vollmary
 */
public class KeyOptions {

    private Boolean allowUserKeys;
    private KeyType type;
    private Integer increment;
    private Integer offset;

    public KeyOptions() {
        super();
    }

    public KeyOptions(final Boolean allowUserKeys, final KeyType type, final Integer increment, final Integer offset) {
        super();
        this.allowUserKeys = allowUserKeys;
        this.type = type;
        this.increment = increment;
        this.offset = offset;
    }

    public Boolean getAllowUserKeys() {
        return allowUserKeys;
    }

    public void setAllowUserKeys(final Boolean allowUserKeys) {
        this.allowUserKeys = allowUserKeys;
    }

    public KeyType getType() {
        return type;
    }

    public void setType(final KeyType type) {
        this.type = type;
    }

    public Integer getIncrement() {
        return increment;
    }

    public void setIncrement(final Integer increment) {
        this.increment = increment;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(final Integer offset) {
        this.offset = offset;
    }

}
