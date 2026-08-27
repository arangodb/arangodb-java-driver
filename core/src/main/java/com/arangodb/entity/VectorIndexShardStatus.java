/*
 * DISCLAIMER
 *
 * Copyright 2026 ArangoDB GmbH, Cologne, Germany
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
 * Vector-index training status for one shard.
 *
 * @since ArangoDB 3.12.10
 */
public final class VectorIndexShardStatus {
    private VectorIndexTrainingState trainingState;
    private String error;
    private Integer resolvedNLists;

    public VectorIndexTrainingState getTrainingState() {
        return trainingState;
    }

    public String getError() {
        return error;
    }

    public Integer getResolvedNLists() {
        return resolvedNLists;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof VectorIndexShardStatus)) return false;
        VectorIndexShardStatus that = (VectorIndexShardStatus) o;
        return trainingState == that.trainingState && Objects.equals(error, that.error)
                && Objects.equals(resolvedNLists, that.resolvedNLists);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trainingState, error, resolvedNLists);
    }
}
