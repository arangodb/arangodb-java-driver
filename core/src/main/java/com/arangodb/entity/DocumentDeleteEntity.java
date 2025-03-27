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

import com.arangodb.internal.serde.UserData;

import java.util.Objects;

/**
 * @author Mark Vollmary
 */
public final class DocumentDeleteEntity<T> extends DocumentEntity {

    private T oldDocument;

    public DocumentDeleteEntity() {
        super();
    }

    /**
     * @return If the query parameter returnOld is true, then the complete previous revision of the document is
     * returned.
     */
    public T getOld() {
        return oldDocument;
    }

    @UserData
    public void setOld(final T oldDocument) {
        this.oldDocument = oldDocument;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DocumentDeleteEntity)) return false;
        DocumentDeleteEntity<?> that = (DocumentDeleteEntity<?>) o;
        return Objects.equals(oldDocument, that.oldDocument);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(oldDocument);
    }
}
