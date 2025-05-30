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

import java.util.Objects;

/**
 * @author Michele Rastelli
 */
public final class ShardEntity {

    private String shardId;

    public ShardEntity() {
        super();
    }

    public String getShardId() {
        return shardId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ShardEntity)) return false;
        ShardEntity that = (ShardEntity) o;
        return Objects.equals(shardId, that.shardId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(shardId);
    }
}
