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

import com.arangodb.velocypack.annotations.Expose;
import com.arangodb.velocypack.annotations.SerializedName;

/**
 * @param <T>
 * @author Mark Vollmary
 * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#update-document">API
 * Documentation</a>
 */
public class DocumentUpdateEntity<T> extends DocumentEntity {

    @SerializedName("_oldRev")
    private String oldRev;
    @Expose(deserialize = false)
    private T newDocument;
    @Expose(deserialize = false)
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

    public void setOld(final T oldDocument) {
        this.oldDocument = oldDocument;
    }

}
