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
public enum CollectionStatus {

    NEW_BORN_COLLECTION(1), UNLOADED(2), LOADED(3), IN_THE_PROCESS_OF_BEING_UNLOADED(4), DELETED(5);

    private final int status;

    CollectionStatus(final int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public static CollectionStatus fromStatus(final int status) {
        for (final CollectionStatus cStatus : CollectionStatus.values()) {
            if (cStatus.status == status) {
                return cStatus;
            }
        }
        return null;
    }

}
