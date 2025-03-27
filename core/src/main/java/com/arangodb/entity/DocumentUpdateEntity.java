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
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * @author Mark Vollmary
 */
public final class DocumentUpdateEntity<T> extends DocumentEntity {

    @JsonProperty("_oldRev")
    private String oldRev;
    private T newDocument;
    private T oldDocument;

    public DocumentUpdateEntity() {
        super();
    }

    public String getOldRev() {
        return oldRev;
    }

    /**
     * @return If the query parameter returnNew is true, then the complete new document is returned.
     */
    public T getNew() {
        return newDocument;
    }

    @UserData
    public void setNew(final T newDocument) {
        this.newDocument = newDocument;
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
        if (!(o instanceof DocumentUpdateEntity)) return false;
        if (!super.equals(o)) return false;
        DocumentUpdateEntity<?> that = (DocumentUpdateEntity<?>) o;
        return Objects.equals(oldRev, that.oldRev) && Objects.equals(newDocument, that.newDocument) && Objects.equals(oldDocument, that.oldDocument);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), oldRev, newDocument, oldDocument);
    }
}
